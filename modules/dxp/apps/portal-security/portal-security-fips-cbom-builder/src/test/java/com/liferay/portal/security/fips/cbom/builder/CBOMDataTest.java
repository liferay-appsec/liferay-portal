/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Caio Farias
 */
public class CBOMDataTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetCryptoUses() throws Exception {
		CBOMData cbomData = new CBOMData();

		CryptoUse cryptoUse1 = _createMessageDigestCryptoUse(
			"com.liferay.portal.kernel", "com.liferay.test.Class1");

		cryptoUse1.addValue("SHA");

		cbomData.addCryptoUse(cryptoUse1, new CryptoUse());

		CryptoUse cryptoUse4 = _createMessageDigestCryptoUse(
			"com.liferay.portal.encryptor", RandomTestUtil.randomString());

		cryptoUse4.addValue("MD5");

		cbomData.addCryptoUse(cryptoUse4, new CryptoUse());

		Assert.assertEquals(
			Arrays.asList("SHA-1 in com.liferay.test.Class1 SHA-1"),
			TransformUtil.transform(
				cbomData.getCryptoUses(),
				cryptoUse -> StringBundler.concat(
					cryptoUse.getFeatureName(), StringPool.SPACE,
					cryptoUse.getAlgorithm())));
	}

	private CryptoUse _createMessageDigestCryptoUse(
		String bundleSymbolicName, String className) {

		return new CryptoUse(
			bundleSymbolicName, className, RandomTestUtil.randomString(),
			"MessageDigest");
	}

}