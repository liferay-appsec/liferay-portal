/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.authenticator.configuration.LDAPAuthConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jorge García Jiménez
 */
public class FIPSLDAPAuthConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testOnBeforeSave() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			for (String algorithm : _NOT_ALLOWED_ALGORITHMS) {
				_modelListener.onBeforeSave(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"method", "password-compare"
					).put(
						"passwordEncryptionAlgorithm", algorithm
					).build());
			}
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"method", "bind"
				).put(
					"passwordEncryptionAlgorithm", "MD5"
				).build());

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"passwordEncryptionAlgorithm", "MD5"
				).build());

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"method", "password-compare"
				).put(
					"passwordEncryptionAlgorithm", "SHA-256"
				).build());
			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"method", "password-compare"
				).put(
					"passwordEncryptionAlgorithm", "SHA-384"
				).build());

			for (String algorithm : _NOT_ALLOWED_ALGORITHMS) {
				LDAPConfigurationModelListenerException
					ldapConfigurationModelListenerException =
						Assert.assertThrows(
							LDAPConfigurationModelListenerException.class,
							() -> _modelListener.onBeforeSave(
								_PID,
								HashMapDictionaryBuilder.<String, Object>put(
									"method", "password-compare"
								).put(
									"passwordEncryptionAlgorithm", algorithm
								).build()));

				Assert.assertEquals(
					"the-algorithm-x-is-not-allowed-in-fips-mode",
					ldapConfigurationModelListenerException.getMessageKey());
				Assert.assertArrayEquals(
					new Object[] {algorithm},
					ldapConfigurationModelListenerException.
						getMessageArguments());
			}
		}
	}

	private static final String[] _NOT_ALLOWED_ALGORITHMS = {
		"", "BCRYPT", "MD5", "NONE", "SHA", "SSHA"
	};

	private static final String _PID = LDAPAuthConfiguration.class.getName();

	private final FIPSLDAPAuthConfigurationModelListener _modelListener =
		new FIPSLDAPAuthConfigurationModelListener();

}