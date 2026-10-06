/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.json.web.service.client.internal;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.NoSuchAlgorithmException;
import java.security.Security;

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
	public void testConstructor() {
		Assert.assertNotNull(
			ReflectionTestUtil.getFieldValue(
				new X509TrustManagerImpl(), "_defaultX509TrustManager"));
		Assert.assertNotNull(
			ReflectionTestUtil.getFieldValue(
				new X509TrustManagerImpl(null, true),
				"_defaultX509TrustManager"));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertNotNull(
				ReflectionTestUtil.getFieldValue(
					new X509TrustManagerImpl(null, false),
					"_defaultX509TrustManager"));

			SecurityException securityException1 = Assert.assertThrows(
				SecurityException.class, () -> new X509TrustManagerImpl());

			Assert.assertEquals(
				"Self signed certificates are not allowed in FIPS mode",
				securityException1.getMessage());

			SecurityException securityException2 = Assert.assertThrows(
				SecurityException.class,
				() -> new X509TrustManagerImpl(null, true));

			Assert.assertEquals(
				"Self signed certificates are not allowed in FIPS mode",
				securityException2.getMessage());

			String algorithm = Security.getProperty(
				"ssl.TrustManagerFactory.algorithm");

			Security.setProperty(
				"ssl.TrustManagerFactory.algorithm", "SunX509");

			try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
					FIPSAuditUtil.class)) {

				RuntimeException runtimeException = Assert.assertThrows(
					RuntimeException.class,
					() -> new X509TrustManagerImpl(null, false));

				Assert.assertTrue(
					String.valueOf(runtimeException.getCause()),
					runtimeException.getCause() instanceof
						NoSuchAlgorithmException);

				mockedStatic.verify(() -> FIPSAuditUtil.write(Mockito.any()));
			}
			finally {
				Security.setProperty(
					"ssl.TrustManagerFactory.algorithm", algorithm);
			}
		}
	}

}