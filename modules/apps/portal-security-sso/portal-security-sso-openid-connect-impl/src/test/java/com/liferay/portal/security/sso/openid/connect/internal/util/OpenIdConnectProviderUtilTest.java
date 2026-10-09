/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Lucas Miranda
 */
public class OpenIdConnectProviderUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGenerateLocalWellKnownURI() throws Exception {
		FIPSModeTestUtil.assertAlgorithmSwitch(
			DigesterUtil.MD5, CryptoUtil.class, DigesterUtil.SHA_256,
			CryptoUtil::createMessageDigest,
			() -> OpenIdConnectProviderUtil.generateLocalWellKnownURI(
				"https://" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString()));

		_assertGenerateLocalWellKnownURI("5aivIuWg02SUCX2lnFUEhA**");

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_assertGenerateLocalWellKnownURI(
				"G4Jf788EdKANQdhQPFj2Gv5hJKT_3VQMH8Mr3mLwWtU*");
		}
	}

	private void _assertGenerateLocalWellKnownURI(String hash)
		throws Exception {

		Assert.assertEquals(
			StringBundler.concat(
				"https://liferay.com/.well-known/openid-configuration/", hash,
				"/local"),
			OpenIdConnectProviderUtil.generateLocalWellKnownURI(
				"https://liferay.com", "https://liferay.com/token"));
	}

}