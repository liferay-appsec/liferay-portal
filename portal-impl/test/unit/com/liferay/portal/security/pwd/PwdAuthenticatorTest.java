/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.pwd;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.MessageDigest;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class PwdAuthenticatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAuthenticate() throws Exception {
		String login = RandomTestUtil.randomString();
		String sharedKey = RandomTestUtil.randomString();

		MessageDigest messageDigest = MessageDigest.getInstance("MD5");

		messageDigest.update(login.getBytes(StringPool.UTF8));

		String clearTextPassword = Base64.encode(
			messageDigest.digest(sharedKey.getBytes(StringPool.UTF8)));

		try (MockedStatic<PasswordEncryptorUtil> mockedStatic =
				Mockito.mockStatic(PasswordEncryptorUtil.class);
			SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"AUTH_MAC_ALGORITHM", "MD5", false);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"AUTH_MAC_ALLOW", "true", false);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"AUTH_MAC_SHARED_KEY", sharedKey, false)) {

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(Mockito.anyString())
			).thenReturn(
				RandomTestUtil.randomString()
			);

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(
					Mockito.anyString(), Mockito.anyString())
			).thenReturn(
				RandomTestUtil.randomString()
			);

			Assert.assertTrue(
				PwdAuthenticator.authenticate(
					login, clearTextPassword, RandomTestUtil.randomString()));

			try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
					Mockito.mockStatic(FIPSAuditUtil.class);
				SafeCloseable safeCloseable4 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"FIPS_ENABLED", true)) {

				Assert.assertFalse(
					PwdAuthenticator.authenticate(
						login, clearTextPassword,
						RandomTestUtil.randomString()));

				ArgumentCaptor<FIPSAuditEvent> argumentCaptor =
					ArgumentCaptor.forClass(FIPSAuditEvent.class);

				fipsAuditUtilMockedStatic.verify(
					() -> FIPSAuditUtil.write(argumentCaptor.capture()));

				FIPSAuditEvent fipsAuditEvent = argumentCaptor.getValue();

				Map<String, Object> fields = fipsAuditEvent.getFields();

				Assert.assertEquals(
					"APPROVED", fields.get("crypto-function-approval-type"));
				Assert.assertEquals("MD5", fields.get("requested-algorithm"));
			}
		}
	}

}