/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.ldap.exportimport.configuration.LDAPImportConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 */
public class FIPSLDAPImportConfigurationModelListenerTest {

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
					"importUserPasswordEnabled", true
				).build());
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<PropsUtil> propsUtilMockedStatic = Mockito.mockStatic(
				PropsUtil.class)) {

			propsUtilMockedStatic.when(
				() -> PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM)
			).thenReturn(
				"PBKDF2WithHmacSHA256/160/1300000"
			);

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"importUserPasswordEnabled", true
				).build());

			propsUtilMockedStatic.when(
				() -> PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM)
			).thenReturn(
				"PBKDF2WithHmacSHA1/160/1300000"
			);

			_modelListener.onBeforeSave(
				_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"importUserPasswordEnabled", false
				).build());

			Assert.assertThrows(
				ConfigurationModelListenerException.class,
				() -> _modelListener.onBeforeSave(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"importUserPasswordEnabled", true
					).build()));
		}
	}

	private static final String _PID = LDAPImportConfiguration.class.getName();

	private final FIPSLDAPImportConfigurationModelListener _modelListener =
		new FIPSLDAPImportConfigurationModelListener();

}