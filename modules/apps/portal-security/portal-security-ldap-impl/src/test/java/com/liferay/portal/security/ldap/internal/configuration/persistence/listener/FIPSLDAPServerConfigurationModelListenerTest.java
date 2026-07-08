/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jorge García Jiménez
 */
public class FIPSLDAPServerConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testOnBeforeSave() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"baseProviderURL", "ldap://" + RandomTestUtil.randomString()
				).build());
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"baseProviderURL",
					"ldaps://" + RandomTestUtil.randomString()
				).build());

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"baseProviderURL", ""
				).build());

			String baseProviderURL = "ldap://" + RandomTestUtil.randomString();

			LDAPConfigurationModelListenerException
				ldapConfigurationModelListenerException = Assert.assertThrows(
					LDAPConfigurationModelListenerException.class,
					() -> _modelListener.onBeforeSave(
						_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							"baseProviderURL", baseProviderURL
						).build()));

			Assert.assertEquals(
				"fips-mode-requires-the-ldaps-scheme-for-the-base-provider-" +
					"url-x",
				ldapConfigurationModelListenerException.getMessageKey());
			Assert.assertArrayEquals(
				new Object[] {baseProviderURL},
				ldapConfigurationModelListenerException.getMessageArguments());
		}
	}

	private static final String _PID = LDAPServerConfiguration.class.getName();

	private final FIPSLDAPServerConfigurationModelListener _modelListener =
		new FIPSLDAPServerConfigurationModelListener();

}