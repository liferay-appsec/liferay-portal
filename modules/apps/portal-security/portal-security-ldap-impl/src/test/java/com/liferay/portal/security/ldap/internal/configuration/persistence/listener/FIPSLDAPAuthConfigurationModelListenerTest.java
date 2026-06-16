/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.portal.security.ldap.FIPSModeUtil;
import com.liferay.portal.security.ldap.LocalizedLDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.authenticator.configuration.LDAPAuthConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 */
public class FIPSLDAPAuthConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testBind() throws Exception {
		try (MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic = _mockFIPS(
				true)) {

			for (String algorithm : _allowedAlgorithms) {
				_listener.onBeforeSave(_PID, _properties("bind", algorithm));
			}

			for (String algorithm : _notAllowedAlgorithms) {
				_listener.onBeforeSave(_PID, _properties("bind", algorithm));
			}
		}
	}

	@Test
	public void testPasswordCompare() throws Exception {
		try (MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic = _mockFIPS(
				false)) {

			for (String algorithm : _allowedAlgorithms) {
				_listener.onBeforeSave(
					_PID, _properties("password-compare", algorithm));
			}

			for (String algorithm : _notAllowedAlgorithms) {
				_listener.onBeforeSave(
					_PID, _properties("password-compare", algorithm));
			}
		}

		try (MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic = _mockFIPS(
				true)) {

			for (String algorithm : _allowedAlgorithms) {
				_listener.onBeforeSave(
					_PID, _properties("password-compare", algorithm));
			}

			for (String algorithm : _notAllowedAlgorithms) {
				try {
					_listener.onBeforeSave(
						_PID, _properties("password-compare", algorithm));

					Assert.fail();
				}
				catch (LocalizedLDAPConfigurationModelListenerException
							localizedLDAPConfigurationModelListenerException) {

					Assert.assertEquals(
						"fips-mode-does-not-permit-ldap-password-encryption-" +
							"algorithm-x",
						localizedLDAPConfigurationModelListenerException.
							getMessageKey());
					Assert.assertArrayEquals(
						new Object[] {algorithm},
						localizedLDAPConfigurationModelListenerException.
							getMessageArguments());
				}
			}
		}
	}

	private MockedStatic<FIPSModeUtil> _mockFIPS(boolean enabled) {
		MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic =
			Mockito.mockStatic(FIPSModeUtil.class, Mockito.CALLS_REAL_METHODS);

		fipsModeUtilMockedStatic.when(
			FIPSModeUtil::isEnabled
		).thenReturn(
			enabled
		);

		return fipsModeUtilMockedStatic;
	}

	private Dictionary<String, Object> _properties(
		String method, String algorithm) {

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("method", method);
		properties.put("passwordEncryptionAlgorithm", algorithm);

		return properties;
	}

	private static final String _PID = LDAPAuthConfiguration.class.getName();

	private static final Set<String> _allowedAlgorithms = Set.of(
		"SHA-256", "SHA-384", "SHA-512");
	private static final Set<String> _notAllowedAlgorithms = Set.of(
		"", "BCRYPT", "MD5", "NONE", "SHA", "SSHA");

	private final FIPSLDAPAuthConfigurationModelListener _listener =
		new FIPSLDAPAuthConfigurationModelListener();

}