/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.solr8.internal.http;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Caio Farias
 */
public class SSLSocketFactoryBuilderImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testBuild() throws Exception {
		SSLSocketFactoryBuilderImpl sslSocketFactoryBuilderImpl =
			new SSLSocketFactoryBuilderImpl();

		_assertVerificationRequired(
			sslSocketFactoryBuilderImpl, "_verifyServerCertificate",
			"Servers certificates must be verified in FIPS mode");

		_assertVerificationRequired(
			sslSocketFactoryBuilderImpl, "_verifyServerHostname",
			"Servers hostname must be verified in FIPS mode");

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertThrows(
				NullPointerException.class, sslSocketFactoryBuilderImpl::build);
		}
	}

	private void _assertVerificationRequired(
			SSLSocketFactoryBuilderImpl sslSocketFactoryBuilderImpl,
			String fieldName, String expectedMessage)
		throws Exception {

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					sslSocketFactoryBuilderImpl, fieldName, false)) {

			try (SafeCloseable safeCloseable =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"FIPS_ENABLED", false)) {

				Assert.assertThrows(
					NullPointerException.class,
					sslSocketFactoryBuilderImpl::build);
			}

			try (SafeCloseable safeCloseable =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"FIPS_ENABLED", true)) {

				SecurityException securityException = Assert.assertThrows(
					SecurityException.class,
					sslSocketFactoryBuilderImpl::build);

				Assert.assertEquals(
					expectedMessage, securityException.getMessage());
			}
		}
	}

}