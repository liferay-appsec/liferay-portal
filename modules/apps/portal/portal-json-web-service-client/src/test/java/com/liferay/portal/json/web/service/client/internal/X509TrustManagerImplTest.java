/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.json.web.service.client.internal;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class X509TrustManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testX509TrustManagerImpl() {
		try (MockedStatic<FIPSModeValidator> fipsModeValidatorMockedStatic =
				Mockito.mockStatic(FIPSModeValidator.class)) {

			new X509TrustManagerImpl();

			fipsModeValidatorMockedStatic.verify(
				() -> FIPSModeValidator.validateServerCertificateVerification(
					false));

			new X509TrustManagerImpl(null, false);

			fipsModeValidatorMockedStatic.verify(
				() -> FIPSModeValidator.validateServerCertificateVerification(
					true));
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			new X509TrustManagerImpl(null, false);

			SecurityException securityException1 = Assert.assertThrows(
				SecurityException.class, () -> new X509TrustManagerImpl());

			Assert.assertEquals(
				"Server certificates must be verified in FIPS mode",
				securityException1.getMessage());

			SecurityException securityException2 = Assert.assertThrows(
				SecurityException.class,
				() -> new X509TrustManagerImpl(null, true));

			Assert.assertEquals(
				"Server certificates must be verified in FIPS mode",
				securityException2.getMessage());
		}
	}

}