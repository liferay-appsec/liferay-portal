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

import java.util.ArrayList;
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

		CryptoUse cryptoUse2 = _createMessageDigestCryptoUse(
			"com.liferay.portal.impl", "com.liferay.test.Class2");

		cryptoUse2.addUnresolvedReason(
			"the field com.liferay.test.Class2._algorithm");

		cbomData.addCryptoUse(cryptoUse2, new CryptoUse());

		CryptoUse cryptoUse4 = _createMessageDigestCryptoUse(
			"com.liferay.portal.encryptor", RandomTestUtil.randomString());

		cryptoUse4.addValue("MD5");

		cbomData.addCryptoUse(cryptoUse4, new CryptoUse());

		CryptoUse cryptoUse5 = _createMessageDigestCryptoUse(
			"com.liferay.portal.encryptor", "com.liferay.test.Class5");

		cryptoUse5.addUnresolvedReason(
			"parameter 1 of com.liferay.test.Class5.digest");

		cbomData.addCryptoUse(cryptoUse5, new CryptoUse());

		Assert.assertEquals(
			Arrays.asList(
				"SHA-1 in com.liferay.test.Class1 SHA-1",
				"[the field com.liferay.test.Class2._algorithm]",
				"[parameter 1 of com.liferay.test.Class5.digest]"),
			TransformUtil.transform(
				cbomData.getCryptoUses(),
				cryptoUse -> {
					if (cryptoUse.getAlgorithm() != null) {
						return StringBundler.concat(
							cryptoUse.getFeatureName(), StringPool.SPACE,
							cryptoUse.getAlgorithm());
					}

					return String.valueOf(cryptoUse.getUnresolvedReasons());
				}));
		Assert.assertEquals(
			Arrays.asList(
				"The scan cannot read the algorithm of the MessageDigest " +
					"call in com.liferay.test.Class2, which comes from the " +
						"field com.liferay.test.Class2._algorithm",
				"The scan cannot read the algorithm of the MessageDigest " +
					"call in com.liferay.test.Class5, which comes from " +
						"parameter 1 of com.liferay.test.Class5.digest"),
			new ArrayList<>(cbomData.getMessages()));
	}

	private CryptoUse _createMessageDigestCryptoUse(
		String bundleSymbolicName, String className) {

		return new CryptoUse(
			bundleSymbolicName, className, RandomTestUtil.randomString(),
			"MessageDigest");
	}

}