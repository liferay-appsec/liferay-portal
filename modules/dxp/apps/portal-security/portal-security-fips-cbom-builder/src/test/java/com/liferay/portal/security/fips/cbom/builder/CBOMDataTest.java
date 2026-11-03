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

		cbomData.addCryptoUse(cryptoUse1, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse2 = _createMessageDigestCryptoUse(
			"com.liferay.portal.impl", "com.liferay.test.Class2");

		cryptoUse2.addUnresolvedReason(
			"the field com.liferay.test.Class2._algorithm");

		cbomData.addCryptoUse(cryptoUse2, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse3 = _createMessageDigestCryptoUse(
			"com.liferay.portal.impl", RandomTestUtil.randomString());

		cryptoUse3.addPropertyKey("test.algorithm");

		cbomData.addCryptoUse(cryptoUse3, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse4 = _createMessageDigestCryptoUse(
			"com.liferay.portal.encryptor", RandomTestUtil.randomString());

		cryptoUse4.addValue("MD5");

		cbomData.addCryptoUse(cryptoUse4, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse5 = _createMessageDigestCryptoUse(
			"com.liferay.portal.encryptor", "com.liferay.test.Class5");

		cryptoUse5.addUnresolvedReason(
			"parameter 1 of com.liferay.test.Class5.digest");

		cbomData.addCryptoUse(cryptoUse5, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse6 = _createMessageDigestCryptoUse(
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		cryptoUse6.addPropertyKey("company.encryption.algorithm");

		CryptoUse keySizeCryptoUse6 = new CryptoUse();

		keySizeCryptoUse6.addPropertyKey("company.encryption.key.size");

		cbomData.addCryptoUse(cryptoUse6, keySizeCryptoUse6, new CryptoUse());

		CryptoUse cryptoUse7 = _createMessageDigestCryptoUse(
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		cryptoUse7.addPropertyKey(RandomTestUtil.randomString());

		cbomData.addCryptoUse(cryptoUse7, new CryptoUse(), new CryptoUse());

		CryptoUse cryptoUse8 = _createMessageDigestCryptoUse(
			"com.liferay.portal.security.password.encryptor.impl",
			RandomTestUtil.randomString());

		cryptoUse8.addValue("SHA-1");

		CryptoUse methodCryptoUse8 = new CryptoUse();

		methodCryptoUse8.addPropertyKey("passwords.encryption.algorithm");
		methodCryptoUse8.addPropertyKey(
			"passwords.encryption.algorithm.legacy");
		methodCryptoUse8.addPropertyKey(RandomTestUtil.randomString());

		cbomData.addCryptoUse(cryptoUse8, new CryptoUse(), methodCryptoUse8);

		Assert.assertEquals(
			Arrays.asList(
				"SHA-1 in com.liferay.test.Class1 SHA-1",
				"[the field com.liferay.test.Class2._algorithm]",
				"test.algorithm [test.algorithm]",
				"[parameter 1 of com.liferay.test.Class5.digest]",
				"company.encryption.algorithm [company.encryption.algorithm, " +
					"company.encryption.key.size]",
				"passwords.encryption.algorithm SHA-1",
				"passwords.encryption.algorithm.legacy SHA-1"),
			TransformUtil.transform(
				cbomData.getCryptoUses(),
				cryptoUse -> {
					if (cryptoUse.getAlgorithm() != null) {
						return StringBundler.concat(
							cryptoUse.getFeatureName(), StringPool.SPACE,
							cryptoUse.getAlgorithm());
					}

					if (cryptoUse.isConfigurable()) {
						return StringBundler.concat(
							cryptoUse.getFeatureName(), StringPool.SPACE,
							cryptoUse.getConfiguredBy());
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
						"parameter 1 of com.liferay.test.Class5.digest",
				"The scan found no crypto call for \"auth.mac.algorithm\"",
				"The scan found no crypto call for " +
					"\"object.encryption.algorithm\"",
				"The scan found no crypto call for " +
					"\"tunneling.servlet.encryption.algorithm\""),
			new ArrayList<>(cbomData.getMessages()));
	}

	private CryptoUse _createMessageDigestCryptoUse(
		String bundleSymbolicName, String className) {

		return new CryptoUse(
			bundleSymbolicName, className, RandomTestUtil.randomString(),
			"MessageDigest");
	}

}