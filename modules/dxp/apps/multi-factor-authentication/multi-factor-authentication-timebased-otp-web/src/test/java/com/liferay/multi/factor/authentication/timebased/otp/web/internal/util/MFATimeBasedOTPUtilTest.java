/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.timebased.otp.web.internal.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.List;

import jodd.util.Base32;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
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
	public void testGenerateSharedSecret() {
		byte[] sharedSecret = Base32.decode(
			MFATimeBasedOTPUtil.generateSharedSecret(10));

		Assert.assertEquals(
			Arrays.toString(sharedSecret), 10, sharedSecret.length);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			sharedSecret = Base32.decode(
				MFATimeBasedOTPUtil.generateSharedSecret(10));

			Assert.assertEquals(
				Arrays.toString(sharedSecret),
				MFATimeBasedOTPUtil.
					MFA_TIMEBASED_OTP_FIPS_ALGORITHM_KEY_SIZE_MIN,
				sharedSecret.length);

			sharedSecret = Base32.decode(
				MFATimeBasedOTPUtil.generateSharedSecret(20));

			Assert.assertEquals(
				Arrays.toString(sharedSecret), 20, sharedSecret.length);
		}
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

	@Test
	public void testVerifyTimeBasedOTPWithAShortSharedSecret() {
		String sharedSecret = Base32.encode(
			"1234567890".getBytes(StandardCharsets.UTF_8));

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				MFATimeBasedOTPUtil.class.getName(), LoggerTestUtil.WARN);
			MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertFalse(
				MFATimeBasedOTPUtil.verifyTimeBasedOTP(
					0, sharedSecret, "000000"));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to verify the time-based one-time password",
				logEntry.getMessage());

			mockedStatic.verify(() -> FIPSAuditUtil.write(Mockito.any()));
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