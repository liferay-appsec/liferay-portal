/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Provider;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testFetchJCAClassName() {
		Assert.assertNull(CBOMUtil.fetchJCAClassName("Cipher"));
		Assert.assertNull(
			CBOMUtil.fetchJCAClassName("com/liferay/test/Cipher"));
		Assert.assertEquals(
			"MessageDigest",
			CBOMUtil.fetchJCAClassName("java/security/MessageDigest"));
		Assert.assertEquals(
			"Cipher", CBOMUtil.fetchJCAClassName("javax/crypto/Cipher"));
		Assert.assertNull(CBOMUtil.fetchJCAClassName("javax/crypto/SecretKey"));
		Assert.assertEquals(
			"SSLContext",
			CBOMUtil.fetchJCAClassName("javax/net/ssl/SSLContext"));
	}

	@Test
	public void testFetchProvider() {
		_testFetchProvider("AES", "SunJCE", "KeyGenerator");
		_testFetchProvider("AES/GCM/NoPadding", "SunJCE", "Cipher");
		_testFetchProvider("EC", "SunEC", "KeyPairGenerator");
		_testFetchProvider("ECDH", "SunEC", "KeyAgreement");
		_testFetchProvider("HmacSHA256", "SunJCE", "Mac");
		_testFetchProvider(
			"PBKDF2WithHmacSHA256", "SunJCE", "SecretKeyFactory");
		_testFetchProvider("PKCS12", "SUN", "KeyStore");
		_testFetchProvider("PKIX", "SunJSSE", "KeyManagerFactory");
		_testFetchProvider("PKIX", "SunJSSE", "TrustManagerFactory");
		_testFetchProvider("RSA", "SunRsaSign", "KeyFactory");
		_testFetchProvider("SHA-256", "SUN", "MessageDigest");
		_testFetchProvider("SHA1PRNG", "SUN", "SecureRandom");
		_testFetchProvider("SHA256withRSA", "SunRsaSign", "Signature");
		_testFetchProvider("TLSv1.3", "SunJSSE", "SSLContext");

		Assert.assertNull(CBOMUtil.fetchProvider("AES", "SecretKeySpec"));
		Assert.assertNull(
			CBOMUtil.fetchProvider(RandomTestUtil.randomString(), "BCrypt"));
		Assert.assertNull(
			CBOMUtil.fetchProvider(
				RandomTestUtil.randomString(), "MessageDigest"));
	}

	private void _testFetchProvider(
		String algorithm, String expectedProviderName, String jcaClassName) {

		Provider provider = CBOMUtil.fetchProvider(algorithm, jcaClassName);

		Assert.assertEquals(expectedProviderName, provider.getName());
	}

}