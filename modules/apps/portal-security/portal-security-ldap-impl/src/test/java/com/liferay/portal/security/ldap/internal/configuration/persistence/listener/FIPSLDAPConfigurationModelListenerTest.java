/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.authenticator.configuration.LDAPAuthConfiguration;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;
import com.liferay.portal.security.ldap.exportimport.configuration.LDAPImportConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 */
public class FIPSLDAPConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testOnBeforeSave() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			_serverListener.onBeforeSave(
				_SERVER_PID, _serverProperties("ldap://dc.example:389"));

			for (String algorithm : _NOT_ALLOWED_ALGORITHMS) {
				_authListener.onBeforeSave(
					_AUTH_PID, _authProperties("password-compare", algorithm));
			}

			_importListener.onBeforeSave(_IMPORT_PID, _importProperties(true));
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_serverListener.onBeforeSave(
				_SERVER_PID, _serverProperties("ldaps://dc.example:636"));

			_serverListener.onBeforeSave(_SERVER_PID, new Hashtable<>());

			try {
				_serverListener.onBeforeSave(
					_SERVER_PID, _serverProperties("ldap://dc.example:389"));

				Assert.fail();
			}
			catch (LDAPConfigurationModelListenerException
						ldapConfigurationModelListenerException) {
			}

			_authListener.onBeforeSave(
				_AUTH_PID, _authProperties("bind", "MD5"));
			_authListener.onBeforeSave(_AUTH_PID, _authProperties(null, "MD5"));

			_authListener.onBeforeSave(
				_AUTH_PID, _authProperties("password-compare", "SHA-256"));
			_authListener.onBeforeSave(
				_AUTH_PID, _authProperties("password-compare", "SHA-384"));

			for (String algorithm : _NOT_ALLOWED_ALGORITHMS) {
				try {
					_authListener.onBeforeSave(
						_AUTH_PID,
						_authProperties("password-compare", algorithm));

					Assert.fail();
				}
				catch (LDAPConfigurationModelListenerException
							ldapConfigurationModelListenerException) {

					Assert.assertEquals(
						"the-algorithm-x-is-not-allowed-in-fips-mode",
						ldapConfigurationModelListenerException.
							getMessageKey());
					Assert.assertArrayEquals(
						new Object[] {algorithm},
						ldapConfigurationModelListenerException.
							getMessageArguments());
				}
			}

			try (MockedStatic<PropsUtil> propsUtilMockedStatic =
					Mockito.mockStatic(PropsUtil.class)) {

				propsUtilMockedStatic.when(
					() -> PropsUtil.get(
						PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM)
				).thenReturn(
					"PBKDF2WithHmacSHA256/160/1300000"
				);

				_importListener.onBeforeSave(
					_IMPORT_PID, _importProperties(true));

				propsUtilMockedStatic.when(
					() -> PropsUtil.get(
						PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM)
				).thenReturn(
					"PBKDF2WithHmacSHA1/160/1300000"
				);

				_importListener.onBeforeSave(
					_IMPORT_PID, _importProperties(false));

				try {
					_importListener.onBeforeSave(
						_IMPORT_PID, _importProperties(true));

					Assert.fail();
				}
				catch (ConfigurationModelListenerException
							configurationModelListenerException) {
				}
			}
		}
	}

	private Dictionary<String, Object> _authProperties(
		String method, String algorithm) {

		Dictionary<String, Object> properties = new Hashtable<>();

		if (method != null) {
			properties.put("method", method);
		}

		properties.put("passwordEncryptionAlgorithm", algorithm);

		return properties;
	}

	private Dictionary<String, Object> _importProperties(
		boolean importPasswordEnabled) {

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("importUserPasswordEnabled", importPasswordEnabled);

		return properties;
	}

	private Dictionary<String, Object> _serverProperties(
		String baseProviderURL) {

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("baseProviderURL", baseProviderURL);

		return properties;
	}

	private static final String _AUTH_PID =
		LDAPAuthConfiguration.class.getName();

	private static final String _IMPORT_PID =
		LDAPImportConfiguration.class.getName();

	private static final String[] _NOT_ALLOWED_ALGORITHMS = {
		"", "BCRYPT", "MD5", "NONE", "SHA", "SSHA"
	};

	private static final String _SERVER_PID =
		LDAPServerConfiguration.class.getName();

	private final FIPSLDAPAuthConfigurationModelListener _authListener =
		new FIPSLDAPAuthConfigurationModelListener();
	private final FIPSLDAPImportConfigurationModelListener _importListener =
		new FIPSLDAPImportConfigurationModelListener();
	private final FIPSLDAPServerConfigurationModelListener _serverListener =
		new FIPSLDAPServerConfigurationModelListener();

}