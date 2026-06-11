/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.internal.upgrade.v2_0_0;

import com.liferay.configuration.admin.util.ConfigurationFilterStringUtil;
import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 * @author Rafael Praxedes
 */
public class SamlConfigurationUpgradeProcess extends UpgradeProcess {

	public SamlConfigurationUpgradeProcess(
		CompanyLocalService companyLocalService,
		ConfigurationAdmin configurationAdmin, Store store) {

		_companyLocalService = companyLocalService;
		_configurationAdmin = configurationAdmin;
		_store = store;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeConfiguration();
		_upgradeDLKeyStores();
		_upgradeFileSystemKeyStore();
	}

	private KeyStore _convertJKSToPKCS12(
			InputStream inputStream, char[] keyStorePassword)
		throws Exception {

		KeyStore jksKeyStore = KeyStore.getInstance("JKS");

		jksKeyStore.load(inputStream, keyStorePassword);

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		pkcs12KeyStore.load(null, null);

		Enumeration<String> aliasesEnumeration = jksKeyStore.aliases();

		while (aliasesEnumeration.hasMoreElements()) {
			String alias = aliasesEnumeration.nextElement();

			if (jksKeyStore.isKeyEntry(alias)) {
				char[] keyEntryPassword = _getKeyEntryPassword(alias);

				try {
					KeyStore.Entry entry = jksKeyStore.getEntry(
						alias,
						new KeyStore.PasswordProtection(keyEntryPassword));

					pkcs12KeyStore.setEntry(
						alias, entry,
						new KeyStore.PasswordProtection(keyEntryPassword));
				}
				catch (Exception exception) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"Skipping inactive key: " + alias, exception);
					}
				}
				finally {
					Arrays.fill(keyEntryPassword, '\0');
				}
			}
			else if (jksKeyStore.isCertificateEntry(alias)) {
				pkcs12KeyStore.setCertificateEntry(
					alias, jksKeyStore.getCertificate(alias));
			}
		}

		return pkcs12KeyStore;
	}

	private char[] _getKeyEntryPassword(String entityId) throws Exception {
		String passwordProperty = "saml.keystore.credential.password";

		if (entityId.endsWith("-encryption")) {
			entityId = entityId.substring(
				0, entityId.lastIndexOf("-encryption"));

			passwordProperty = "saml.keystore.encryption.credential.password";
		}

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			ConfigurationFilterStringUtil.getSystemScopedFilterString(
				SamlProviderConfiguration.class.getName()));

		if (configurations == null) {
			throw new Exception(
				"There is no SAML configuration associated with key: " +
					entityId);
		}

		for (Configuration configuration : configurations) {
			Dictionary<String, Object> properties =
				configuration.getProperties();

			if (!StringUtil.equalsIgnoreCase(
					entityId,
					GetterUtil.getString(properties.get("saml.entity.id")))) {

				continue;
			}

			String password = GetterUtil.getString(
				properties.get(passwordProperty));

			if (Validator.isNull(password)) {
				break;
			}

			return password.toCharArray();
		}

		throw new Exception("No password match was found for key: " + entityId);
	}

	private char[] _getKeyStorePassword() {
		try {
			Configuration configuration = _configurationAdmin.getConfiguration(
				SamlConfiguration.class.getName(), StringPool.QUESTION);

			Dictionary<String, Object> properties =
				configuration.getProperties();

			if (properties != null) {
				String password = GetterUtil.getString(
					properties.get("saml.keystore.password"));

				if (Validator.isNotNull(password)) {
					return password.toCharArray();
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

		return "liferay".toCharArray();
	}

	private void _saveDLKeyStore(
			long companyId, KeyStore keyStore, char[] password, String path)
		throws Exception {

		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		keyStore.store(byteArrayOutputStream, password);

		if (_store.hasFile(
				companyId, CompanyConstants.SYSTEM, path,
				Store.VERSION_DEFAULT)) {

			_store.deleteDirectory(companyId, CompanyConstants.SYSTEM, path);
		}

		_store.addFile(
			companyId, CompanyConstants.SYSTEM, path, Store.VERSION_DEFAULT,
			new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
	}

	private void _upgradeConfiguration() throws Exception {
		Configuration configuration = _configurationAdmin.getConfiguration(
			SamlConfiguration.class.getName(), StringPool.QUESTION);

		Dictionary<String, Object> properties = configuration.getProperties();

		String liferayHome = PropsUtil.get(PropsKeys.LIFERAY_HOME);

		if (properties == null) {
			_jksFileSystemKeyStorePath = liferayHome + "/data/keystore.jks";
			_pkcs12FileSystemKeyStorePath = StringUtil.replace(
				SamlConfiguration.KEYSTORE_PATH_DEFAULT, "${liferay.home}",
				liferayHome);

			return;
		}

		String keyStoreType = GetterUtil.getString(
			properties.get("saml.keystore.type"));

		if (StringUtil.equalsIgnoreCase(keyStoreType, "jks")) {
			properties.put("saml.keystore.type", "PKCS12");

			String keyStorePath = GetterUtil.getString(
				properties.get("saml.keystore.path"));

			if (keyStorePath.endsWith(".jks")) {
				_jksFileSystemKeyStorePath = StringUtil.replace(
					keyStorePath, "${liferay.home}", liferayHome);

				properties.put(
					"saml.keystore.path",
					keyStorePath.replaceAll("\\.jks$", ".p12"));

				_pkcs12FileSystemKeyStorePath = StringUtil.replace(
					GetterUtil.getString(properties.get("saml.keystore.path")),
					"${liferay.home}", liferayHome);
			}

			configuration.update(properties);

			if (_log.isInfoEnabled()) {
				_log.info(
					"Updated SAML configuration: keystore type changed from " +
						"JKS to PKCS12");
			}
		}
	}

	private void _upgradeDLKeyStores() {
		_companyLocalService.forEachCompanyId(
			companyId -> {
				char[] password = _getKeyStorePassword();

				try {
					boolean hasJKSKeyStore = _store.hasFile(
						companyId, CompanyConstants.SYSTEM,
						_JKS_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT);

					boolean hasPKCS12KeyStore = _store.hasFile(
						companyId, CompanyConstants.SYSTEM,
						_PKCS12_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT);

					if (hasJKSKeyStore && !hasPKCS12KeyStore) {
						try (InputStream inputStream = _store.getFileAsStream(
								companyId, CompanyConstants.SYSTEM,
								_JKS_DL_KEYSTORE_PATH, Store.VERSION_DEFAULT)) {

							KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
								inputStream, password);

							_saveDLKeyStore(
								companyId, pkcs12KeyStore, password,
								_PKCS12_DL_KEYSTORE_PATH);
						}

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
							"No JKS keystore found in Document Library for " +
								"company " + companyId,
							noSuchFileException);
					}
				}
				catch (Exception exception) {
					_log.error(
						"Unable to migrate DL SAML keystore for company " +
							companyId,
						exception);
				}
				finally {
					Arrays.fill(password, '\0');
				}
			});
	}

	private void _upgradeFileSystemKeyStore() {
		if ((_jksFileSystemKeyStorePath == null) ||
			(_pkcs12FileSystemKeyStorePath == null)) {

			return;
		}

		File oldFile = new File(_jksFileSystemKeyStorePath);
		File newFile = new File(_pkcs12FileSystemKeyStorePath);

		char[] password = _getKeyStorePassword();

		try {
			if (oldFile.exists() && !newFile.exists()) {
				try (FileInputStream fileInputStream = new FileInputStream(
						oldFile)) {

					KeyStore pkcs12KeyStore = _convertJKSToPKCS12(
						fileInputStream, password);

					File parentDir = newFile.getParentFile();

					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}

					try (FileOutputStream fileOutputStream =
							new FileOutputStream(newFile)) {

						pkcs12KeyStore.store(fileOutputStream, password);
					}

					if (_log.isInfoEnabled()) {
						_log.info(
							StringBundler.concat(
								"Migrated filesystem SAML keystore from ",
								_jksFileSystemKeyStorePath, " (JKS) to ",
								_pkcs12FileSystemKeyStorePath, " (PKCS12)"));
					}
				}
			}
		}
		catch (Exception exception) {
			_log.error("Unable to migrate filesystem SAML keystore", exception);
		}
		finally {
			Arrays.fill(password, '\0');
		}
	}

	private static final String _JKS_DL_KEYSTORE_PATH = "saml/keystore.jks";

	private static final String _PKCS12_DL_KEYSTORE_PATH = "saml/keystore.p12";

	private static final Log _log = LogFactoryUtil.getLog(
		SamlConfigurationUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final ConfigurationAdmin _configurationAdmin;
	private String _jksFileSystemKeyStorePath;
	private String _pkcs12FileSystemKeyStorePath;
	private final Store _store;

}