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
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.security.KeyStore;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Migrates SAML keystores from JKS format to PKCS12 format for FIPS 140-3
 * compliance. Handles both Document Library-based and filesystem-based
 * keystores. Updates the OSGi configuration to set the keystore type to
 * PKCS12.
 *
 * @author Rafael Praxedes
 */
public class SamlKeyStoreTypeUpgradeProcess extends UpgradeProcess {

	public SamlKeyStoreTypeUpgradeProcess(
		CompanyLocalService companyLocalService,
		ConfigurationAdmin configurationAdmin, Store store) {

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

		return pkcs12KeyStore;
	}

	private String _getKeystorePassword() {
		Configuration configuration = null;

		try {
			configuration = _configurationAdmin.getConfiguration(
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
					if (!_store.hasFile(
							companyId, CompanyConstants.SYSTEM,
							_OLD_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT)) {

						return;
					}

					if (_store.hasFile(
							companyId, CompanyConstants.SYSTEM,
							_NEW_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT)) {

						if (_log.isInfoEnabled()) {
							_log.info(
								"PKCS12 keystore already exists for " +
									"company " + companyId +
										", skipping DL migration");
						}

						return;
					}

					try (InputStream inputStream = _store.getFileAsStream(
							companyId, CompanyConstants.SYSTEM,
							_OLD_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT)) {

						KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
							inputStream, passwordChars);

						File tempFile = File.createTempFile("saml-ks", ".p12");

						try {
							try (FileOutputStream fileOutputStream =
									new FileOutputStream(tempFile)) {

								pkcs12KeyStore.store(
									fileOutputStream, passwordChars);
							}

							try (FileInputStream fileInputStream =
									new FileInputStream(tempFile)) {

								_store.addFile(
									companyId, CompanyConstants.SYSTEM,
									_NEW_DL_KEYSTORE_PATH,
									Store.VERSION_DEFAULT, fileInputStream);
							}
						}
						finally {
							tempFile.delete();
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

		File oldFile = new File(oldPath);

		if (!oldFile.exists()) {
			return;
		}

		String newPath = liferayHome + "/data/keystore.p12";

		File newFile = new File(newPath);

		if (newFile.exists()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"PKCS12 keystore already exists at " + newPath +
						", skipping filesystem migration");
			}

			return;
		}

		String password = _getKeystorePassword();

		char[] passwordChars = password.toCharArray();

		try (FileInputStream fileInputStream = new FileInputStream(oldFile)) {
			KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
				fileInputStream, passwordChars);

			File parentDir = newFile.getParentFile();

			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			try (FileOutputStream fileOutputStream =
					new FileOutputStream(newFile)) {

				pkcs12KeyStore.store(fileOutputStream, passwordChars);
			}

			if (_log.isInfoEnabled()) {
				_log.info(
					"Migrated filesystem SAML keystore from " + oldPath +
						" (JKS) to " + newPath + " (PKCS12)");
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

	private static final String _NEW_DL_KEYSTORE_PATH = "saml/keystore.p12";

	private static final String _OLD_DL_KEYSTORE_PATH = "saml/keystore.jks";

	private static final String _SAML_CONFIGURATION_PID =
		"com.liferay.saml.runtime.configuration.SamlConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		SamlKeyStoreTypeUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final ConfigurationAdmin _configurationAdmin;
	private final Store _store;

}
