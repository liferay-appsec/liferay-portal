/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

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
public class FIPSModeUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsNotAllowedAlgorithm() {
		try (MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic = _mockFIPS(
				false)) {

			for (String algorithm : _notAllowedAlgorithms) {
				Assert.assertFalse(
					FIPSModeUtil.isNotAllowedAlgorithm(algorithm));
			}
		}

		try (MockedStatic<FIPSModeUtil> fipsModeUtilMockedStatic = _mockFIPS(
				true)) {

			Assert.assertTrue(FIPSModeUtil.isNotAllowedAlgorithm(null));

			for (String algorithm : _allowedAlgorithms) {
				Assert.assertFalse(
					FIPSModeUtil.isNotAllowedAlgorithm(algorithm));
			}

			for (String algorithm : _notAllowedAlgorithms) {
				Assert.assertTrue(
					FIPSModeUtil.isNotAllowedAlgorithm(algorithm));
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

	private static final Set<String> _allowedAlgorithms = Set.of(
		"PBKDF2WithHmacSHA1/160/1300000", "SHA-256", "SHA-384");
	private static final Set<String> _notAllowedAlgorithms = Set.of(
		"BCRYPT", "MD2", "MD5", "NONE", "SHA", "SSHA", "UFC-CRYPT");

}