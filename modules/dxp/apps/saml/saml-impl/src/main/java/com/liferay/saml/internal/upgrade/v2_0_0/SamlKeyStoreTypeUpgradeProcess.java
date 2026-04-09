/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.internal.upgrade.v2_0_0;

import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.saml.runtime.certificate.CertificateEntityId;
import com.liferay.saml.runtime.certificate.CertificateTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Migrates SAML keystores from JKS format to PKCS12 format for FIPS 140-3
 * compliance. Handles both Document Library-based and filesystem-based
 * keystores. Updates the OSGi configuration to set the keystore type to
 * PKCS12. When FIPS mode is enabled, also regenerates certificates that use
 * non-compliant algorithms or key sizes.
 *
 * @author Rafael Praxedes
 */
public class SamlKeyStoreTypeUpgradeProcess extends UpgradeProcess {

	public SamlKeyStoreTypeUpgradeProcess(
		CertificateTool certificateTool,
		CompanyLocalService companyLocalService,
		ConfigurationAdmin configurationAdmin, Store store) {

		_certificateTool = certificateTool;
		_companyLocalService = companyLocalService;
		_configurationAdmin = configurationAdmin;
		_store = store;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			_upgradeConfiguration();
			_upgradeDLKeystores();
			_upgradeFileSystemKeystore();
		}
	}

	private KeyStore _convertJKSToPKCS12(
			InputStream inputStream, char[] password)
		throws Exception {

		KeyStore jksKeyStore = KeyStore.getInstance("JKS");

		jksKeyStore.load(inputStream, password);

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		pkcs12KeyStore.load(null, null);

		Enumeration<String> aliases = jksKeyStore.aliases();

		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();

			if (jksKeyStore.isKeyEntry(alias)) {
				KeyStore.Entry entry = jksKeyStore.getEntry(
					alias, new KeyStore.PasswordProtection(password));

				pkcs12KeyStore.setEntry(
					alias, entry, new KeyStore.PasswordProtection(password));
			}
			else if (jksKeyStore.isCertificateEntry(alias)) {
				pkcs12KeyStore.setCertificateEntry(
					alias, jksKeyStore.getCertificate(alias));
			}
		}

		if (PropsValues.PORTAL_SECURITY_FIPS_MODE_ENABLED) {
			_upgradeCertificates(pkcs12KeyStore, password);
		}

		return pkcs12KeyStore;
	}

	private String _getKeystorePassword() {
		try {
			Configuration configuration =
				_configurationAdmin.getConfiguration(
					_SAML_CONFIGURATION_PID, StringPool.QUESTION);

			Dictionary<String, Object> properties =
				configuration.getProperties();

			if (properties != null) {
				Object password = properties.get("saml.keystore.password");

				if (password != null) {
					return password.toString();
				}
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to read keystore password from configuration",
					exception);
			}
		}

		return "liferay";
	}

	private boolean _isFIPSCompliant(X509Certificate x509Certificate) {
		java.security.PublicKey publicKey = x509Certificate.getPublicKey();

		if (publicKey instanceof DSAKey) {
			return false;
		}

		if (publicKey instanceof RSAKey) {
			return ((RSAKey)publicKey).getModulus().bitLength() >=
				_MINIMUM_RSA_KEY_SIZE;
		}

		return true;
	}

	private KeyStore _loadKeyStore(InputStream inputStream, char[] password)
		throws Exception {

		KeyStore keyStore = KeyStore.getInstance("PKCS12");

		keyStore.load(inputStream, password);

		return keyStore;
	}

	private void _saveDLKeyStore(
			long companyId, String path, KeyStore keyStore, char[] password)
		throws Exception {

		File tempFile = File.createTempFile("saml-ks", ".p12");

		try {
			try (FileOutputStream fileOutputStream =
					new FileOutputStream(tempFile)) {

				keyStore.store(fileOutputStream, password);
			}

			if (_store.hasFile(
					companyId, CompanyConstants.SYSTEM, path,
					Store.VERSION_DEFAULT)) {

				_store.deleteDirectory(
					companyId, CompanyConstants.SYSTEM, path);
			}

			try (FileInputStream fileInputStream =
					new FileInputStream(tempFile)) {

				_store.addFile(
					companyId, CompanyConstants.SYSTEM, path,
					Store.VERSION_DEFAULT, fileInputStream);
			}
		}
		finally {
			tempFile.delete();
		}
	}

	private CertificateEntityId _toCertificateEntityId(
		X500Principal principal) {

		String name = principal.getName(X500Principal.RFC2253);

		String cn = _x500Attribute(name, "CN");
		String o = _x500Attribute(name, "O");
		String ou = _x500Attribute(name, "OU");
		String l = _x500Attribute(name, "L");
		String st = _x500Attribute(name, "ST");
		String c = _x500Attribute(name, "C");

		return new CertificateEntityId(cn, o, ou, l, st, c);
	}

	private boolean _upgradeCertificates(KeyStore keyStore, char[] password) {
		boolean modified = false;

		try {
			Enumeration<String> aliases = keyStore.aliases();

			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();

				if (!keyStore.isKeyEntry(alias)) {
					continue;
				}

				X509Certificate certificate =
					(X509Certificate)keyStore.getCertificate(alias);

				if ((certificate == null) || _isFIPSCompliant(certificate)) {
					continue;
				}

				String algorithm =
					certificate.getPublicKey().getAlgorithm();
				int keySize = -1;

				if (certificate.getPublicKey() instanceof RSAKey) {
					keySize =
						((RSAKey)certificate.getPublicKey()
						).getModulus().bitLength();
				}

				_log.warn(
					"SAML certificate for alias \"" + alias +
						"\" uses non-FIPS-compliant " + algorithm +
							(keySize > 0 ? " " + keySize + "-bit" : "") +
								" key. Regenerating with RSA " +
									_DEFAULT_RSA_KEY_SIZE + "-bit key");

				KeyPair keyPair = _certificateTool.generateKeyPair(
					"RSA", _DEFAULT_RSA_KEY_SIZE);

				CertificateEntityId subjectEntityId =
					_toCertificateEntityId(
						certificate.getSubjectX500Principal());

				Calendar startDate = Calendar.getInstance();

				Calendar endDate = (Calendar)startDate.clone();

				endDate.add(Calendar.DAY_OF_YEAR, _DEFAULT_VALIDITY_DAYS);

				X509Certificate newCertificate =
					_certificateTool.generateCertificate(
						keyPair, subjectEntityId, subjectEntityId,
						startDate.getTime(), endDate.getTime(),
						"SHA256withRSA");

				keyStore.setKeyEntry(
					alias, keyPair.getPrivate(), password,
					new X509Certificate[] {newCertificate});

				_log.warn(
					"Regenerated SAML certificate for alias \"" + alias +
						"\" with RSA " + _DEFAULT_RSA_KEY_SIZE +
							"-bit key. SAML metadata must be " +
								"re-exchanged with federation partners");

				modified = true;
			}
		}
		catch (Exception exception) {
			_log.error(
				"Failed to upgrade non-compliant SAML certificates: " +
					exception.getMessage(),
				exception);
		}

		return modified;
	}

	private void _upgradeConfiguration() throws Exception {
		Configuration configuration = _configurationAdmin.getConfiguration(
			_SAML_CONFIGURATION_PID, StringPool.QUESTION);

		Dictionary<String, Object> properties = configuration.getProperties();

		if (properties == null) {
			properties = new HashMapDictionary<>();
		}

		Object keystoreType = properties.get("saml.keystore.type");

		if ((keystoreType != null) &&
			"jks".equalsIgnoreCase(keystoreType.toString())) {

			properties.put("saml.keystore.type", "PKCS12");

			Object keystorePath = properties.get("saml.keystore.path");

			if ((keystorePath != null) &&
				keystorePath.toString().endsWith(".jks")) {

				String newPath = keystorePath.toString().replaceAll(
					"\\.jks$", ".p12");

				properties.put("saml.keystore.path", newPath);
			}

			configuration.update(properties);

			if (_log.isInfoEnabled()) {
				_log.info(
					"Updated SAML configuration: keystore type changed " +
						"from JKS to PKCS12");
			}
		}
	}

	private void _upgradeDLKeystores() {
		String password = _getKeystorePassword();

		_companyLocalService.forEachCompanyId(
			companyId -> {
				char[] passwordChars = password.toCharArray();

				try {
					boolean hasOldKeystore = _store.hasFile(
						companyId, CompanyConstants.SYSTEM,
						_OLD_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT);

					boolean hasNewKeystore = _store.hasFile(
						companyId, CompanyConstants.SYSTEM,
						_NEW_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT);

					if (hasOldKeystore && !hasNewKeystore) {
						try (InputStream inputStream =
								_store.getFileAsStream(
									companyId, CompanyConstants.SYSTEM,
									_OLD_DL_KEYSTORE_PATH,
									Store.VERSION_DEFAULT)) {

							KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
								inputStream, passwordChars);

							_saveDLKeyStore(
								companyId, _NEW_DL_KEYSTORE_PATH,
								pkcs12KeyStore, passwordChars);
						}

						_store.deleteDirectory(
							companyId, CompanyConstants.SYSTEM,
							_OLD_DL_KEYSTORE_PATH);

						if (_log.isInfoEnabled()) {
							_log.info(
								"Migrated DL SAML keystore from JKS to " +
									"PKCS12 for company " + companyId);
						}
					}
					else if (hasNewKeystore &&
							 PropsValues.PORTAL_SECURITY_FIPS_MODE_ENABLED) {

						try (InputStream inputStream =
								_store.getFileAsStream(
									companyId, CompanyConstants.SYSTEM,
									_NEW_DL_KEYSTORE_PATH,
									Store.VERSION_DEFAULT)) {

							KeyStore keyStore = _loadKeyStore(
								inputStream, passwordChars);

							if (_upgradeCertificates(
									keyStore, passwordChars)) {

								_saveDLKeyStore(
									companyId, _NEW_DL_KEYSTORE_PATH,
									keyStore, passwordChars);
							}
						}
					}
				}
				catch (NoSuchFileException noSuchFileException) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"No JKS keystore found in Document Library " +
								"for company " + companyId,
							noSuchFileException);
					}
				}
				catch (Exception exception) {
					_log.error(
						"Failed to migrate DL SAML keystore for company " +
							companyId + ": " + exception.getMessage(),
						exception);
				}
				finally {
					Arrays.fill(passwordChars, '\0');
				}
			});
	}

	private void _upgradeFileSystemKeystore() {
		String liferayHome = PropsUtil.get(PropsKeys.LIFERAY_HOME);

		String oldPath = liferayHome + "/data/keystore.jks";
		String newPath = liferayHome + "/data/keystore.p12";

		File oldFile = new File(oldPath);
		File newFile = new File(newPath);

		String password = _getKeystorePassword();

		char[] passwordChars = password.toCharArray();

		try {
			if (oldFile.exists() && !newFile.exists()) {
				try (FileInputStream fileInputStream =
						new FileInputStream(oldFile)) {

					KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
						fileInputStream, passwordChars);

					File parentDir = newFile.getParentFile();

					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}

					try (FileOutputStream fileOutputStream =
							new FileOutputStream(newFile)) {

						pkcs12KeyStore.store(
							fileOutputStream, passwordChars);
					}

					if (_log.isInfoEnabled()) {
						_log.info(
							"Migrated filesystem SAML keystore from " +
								oldPath + " (JKS) to " + newPath +
									" (PKCS12)");
					}
				}
			}
			else if (newFile.exists() &&
					 PropsValues.PORTAL_SECURITY_FIPS_MODE_ENABLED) {

				try (FileInputStream fileInputStream =
						new FileInputStream(newFile)) {

					KeyStore keyStore = _loadKeyStore(
						fileInputStream, passwordChars);

					if (_upgradeCertificates(keyStore, passwordChars)) {
						try (FileOutputStream fileOutputStream =
								new FileOutputStream(newFile)) {

							keyStore.store(fileOutputStream, passwordChars);
						}
					}
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Failed to migrate filesystem SAML keystore: " +
					exception.getMessage(),
				exception);
		}
		finally {
			Arrays.fill(passwordChars, '\0');
		}
	}

	private String _x500Attribute(String dn, String attribute) {
		String prefix = attribute + "=";

		for (String part : dn.split(",")) {
			String trimmed = part.trim();

			if (trimmed.startsWith(prefix)) {
				return trimmed.substring(prefix.length());
			}
		}

		return null;
	}

	private static final int _DEFAULT_RSA_KEY_SIZE = 2048;

	private static final int _DEFAULT_VALIDITY_DAYS = 356;

	private static final int _MINIMUM_RSA_KEY_SIZE = 2048;

	private static final String _NEW_DL_KEYSTORE_PATH = "saml/keystore.p12";

	private static final String _OLD_DL_KEYSTORE_PATH = "saml/keystore.jks";

	private static final String _SAML_CONFIGURATION_PID =
		"com.liferay.saml.runtime.configuration.SamlConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		SamlKeyStoreTypeUpgradeProcess.class);

	private final CertificateTool _certificateTool;
	private final CompanyLocalService _companyLocalService;
	private final ConfigurationAdmin _configurationAdmin;
	private final Store _store;

}
