/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.timebased.otp.web.internal.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.nio.charset.StandardCharsets;

import jodd.util.Base32;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class MFATimeBasedOTPUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void test() throws Exception {
		String sharedSecret = MFATimeBasedOTPUtil.generateSharedSecret(20);

		FIPSModeTestUtil.assertAlgorithmSwitch(
			"HmacSHA1", CryptoUtil.class, "HmacSHA256",
			algorithm -> CryptoUtil.createMac(
				Mockito.eq(algorithm), Mockito.any()),
			() -> _generateTimeBasedOTP(
				Base32.decode(sharedSecret),
				System.currentTimeMillis() /
					MFATimeBasedOTPUtil.MFA_TIMEBASED_OTP_COUNTER));
	}

	@Test
	public void testGenerateTimeBasedOTP() {
		Assert.assertEquals("287082", _generateTimeBasedOTP(_SHARED_SECRET, 1));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertEquals(
				"247374", _generateTimeBasedOTP(_SHARED_SECRET, 1));
		}
	}

	private String _generateTimeBasedOTP(byte[] sharedSecret, long time) {
		String timeCountHex = ReflectionTestUtil.invoke(
			MFATimeBasedOTPUtil.class, "_getTimeCountHex",
			new Class<?>[] {long.class}, time);

		return ReflectionTestUtil.invoke(
			MFATimeBasedOTPUtil.class, "_generateTimeBasedOTP",
			new Class<?>[] {byte[].class, String.class}, sharedSecret,
			timeCountHex);
	}

	private static final byte[] _SHARED_SECRET =
		"12345678901234567890".getBytes(StandardCharsets.UTF_8);

}