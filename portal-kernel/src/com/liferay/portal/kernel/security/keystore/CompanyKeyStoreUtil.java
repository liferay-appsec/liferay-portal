/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.keystore;

import com.liferay.petra.concurrent.DCLSingleton;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.security.Key;
import java.security.KeyStore;

import java.util.Arrays;

/**
 * @author Pedro Victor Silvestre
 */
public class CompanyKeyStoreUtil {

	public static Key getKey(String alias) {
		char[] password = null;

		try {
			KeyStore keyStore = _getKeyStore();

			password = _getKeyStorePassword();

			return keyStore.getKey(alias, password);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to retrieve key from company KeyStore for alias: " +
					alias,
				exception);

			return null;
		}
		finally {
			if (password != null) {
				Arrays.fill(password, '\0');
			}
		}
	}

	public static boolean isKeyStoreAlias(String value) {
		return value.startsWith(_ALIAS_PREFIX);
	}

	public static void removeKey(String alias) {
		try {
			KeyStore keyStore = _getKeyStore();

			if (keyStore.containsAlias(alias)) {
				keyStore.deleteEntry(alias);

				_saveKeyStore(keyStore);

				if (_log.isInfoEnabled()) {
					_log.info(
						"Removed key from company KeyStore with alias: " +
							alias);
				}
			}
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Unable to remove key from company KeyStore for alias: " +
					alias,
				exception);
		}
	}

	public static String saveKey(long companyId, Key key) {
		String alias = _ALIAS_PREFIX + companyId;

		char[] password = null;

		try {
			KeyStore keyStore = _getKeyStore();

			password = _getKeyStorePassword();

			keyStore.setKeyEntry(alias, key, password, null);

			_saveKeyStore(keyStore);

			if (_log.isInfoEnabled()) {
				_log.info(
					"Stored company encryption key in KeyStore with alias: " +
						alias);
			}

			return alias;
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Unable to store key in company KeyStore for alias: " + alias,
				exception);
		}
		finally {
			if (password != null) {
				Arrays.fill(password, '\0');
			}
		}
	}

	private static KeyStore _getKeyStore() {
		return _keyStoreDCLSingleton.getSingleton(
			() -> {
				try {
					return _loadKeyStore();
				}
				catch (Exception exception) {
					return ReflectionUtil.throwException(exception);
				}
			});
	}

	private static File _getKeyStoreFile() {
		String path = GetterUtil.getString(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_KEY_KEYSTORE_PATH));

		String liferayHome = PropsUtil.get(PropsKeys.LIFERAY_HOME);

		if (Validator.isNull(path)) {
			return new File(liferayHome.concat("/data/company-keystore.p12"));
		}

		return new File(
			StringUtil.replace(path, "${liferay.home}", liferayHome));
	}

	private static char[] _getKeyStorePassword() {
		String password = GetterUtil.getString(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_KEY_KEYSTORE_PASSWORD));

		if (Validator.isNull(password) ||
			password.equals(_DEFAULT_KEYSTORE_PASSWORD)) {

			return _DEFAULT_KEYSTORE_PASSWORD.toCharArray();
		}

		return password.toCharArray();
	}

	private static KeyStore _loadKeyStore() throws Exception {
		String keyStoreType = GetterUtil.getString(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_KEY_KEYSTORE_TYPE),
			KeyStore.getDefaultType());

		KeyStore keyStore = KeyStore.getInstance(keyStoreType);

		File keyStoreFile = _getKeyStoreFile();

		if (keyStoreFile.exists()) {
			char[] password = _getKeyStorePassword();

			try (FileInputStream fileInputStream = new FileInputStream(
					keyStoreFile)) {

				keyStore.load(fileInputStream, password);
			}
			finally {
				Arrays.fill(password, '\0');
			}
		}
		else {
			keyStore.load(null, null);

			_saveKeyStore(keyStore);

			if (_log.isInfoEnabled()) {
				_log.info(
					"Created new company KeyStore at: " +
						keyStoreFile.getAbsolutePath());
			}
		}

		return keyStore;
	}

	private static void _saveKeyStore(KeyStore keyStore) throws Exception {
		File keyStoreFile = _getKeyStoreFile();

		File parentDir = keyStoreFile.getParentFile();

		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		char[] password = _getKeyStorePassword();

		try (FileOutputStream fileOutputStream = new FileOutputStream(
				keyStoreFile)) {

			keyStore.store(fileOutputStream, password);
		}
		finally {
			Arrays.fill(password, '\0');
		}

		keyStoreFile.setReadable(false, false);
		keyStoreFile.setReadable(true, true);
		keyStoreFile.setWritable(false, false);
		keyStoreFile.setWritable(true, true);
	}

	private static final String _ALIAS_PREFIX = "company-key-";

	private static final String _DEFAULT_KEYSTORE_PASSWORD = "liferay";

	private static final Log _log = LogFactoryUtil.getLog(
		CompanyKeyStoreUtil.class);

	private static final DCLSingleton<KeyStore> _keyStoreDCLSingleton =
		new DCLSingleton<>();

}