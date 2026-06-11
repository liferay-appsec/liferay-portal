/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.math.BigInteger;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.util.Base64;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Pedro Victor Silvestre
 */
public class OAuth2JWKValidatorUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_fipsEnabled = ReflectionTestUtil.getAndSetFieldValue(
			PropsValues.class, "FIPS_ENABLED", Boolean.TRUE);
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "FIPS_ENABLED", _fipsEnabled);
	}

	@Test
	public void testValidateJWK() throws Exception {
		OAuth2JWKValidatorUtil.validateJWK(_generateRsaJWK(2048, "RS256"));

		Assert.assertThrows(
			SecurityException.class,
			() -> OAuth2JWKValidatorUtil.validateJWK(
				_generateRsaJWK(1024, "RS256")));
		Assert.assertThrows(
			SecurityException.class,
			() -> OAuth2JWKValidatorUtil.validateJWK(
				_generateRsaJWK(2048, "RS1")));
	}

	@Test
	public void testValidateJWKS() throws Exception {
		OAuth2JWKValidatorUtil.validateJWKS(
			"{\"keys\":[" + _generateRsaJWK(2048, "RS256") + "]}");

		Assert.assertThrows(
			SecurityException.class,
			() -> OAuth2JWKValidatorUtil.validateJWKS(
				StringBundler.concat(
					"{\"keys\":[", _generateRsaJWK(2048, "RS256"), ",",
					_generateRsaJWK(1024, "RS256"), "]}")));
	}

	@Test
	public void testValidateJWSAlgorithm() {
		for (String algorithm :
				new String[] {
					"ES256", "ES384", "ES512", "HS256", "HS384", "HS512",
					"PS256", "PS384", "PS512", "RS256", "RS384", "RS512"
				}) {

			OAuth2JWKValidatorUtil.validateJWSAlgorithm(algorithm);
		}

		for (String algorithm :
				new String[] {
					null, "", "none", "HS1", "RS1", "ES256K", "EdDSA", "RSA1_5",
					"garbage", "rs256"
				}) {

			Assert.assertThrows(
				SecurityException.class,
				() -> OAuth2JWKValidatorUtil.validateJWSAlgorithm(algorithm));
		}
	}

	private String _generateRsaJWK(int bits, String algorithm)
		throws Exception {

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

		keyPairGenerator.initialize(bits);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		Base64.Encoder encoder = Base64.getUrlEncoder();

		encoder = encoder.withoutPadding();

		RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();

		BigInteger modulus = rsaPublicKey.getModulus();
		BigInteger publicExponent = rsaPublicKey.getPublicExponent();
		BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();

		return String.format(
			"{\"kty\":\"RSA\",\"alg\":\"%s\",\"kid\":\"test\",\"n\":\"%s\"," +
				"\"e\":\"%s\",\"d\":\"%s\"}",
			algorithm, encoder.encodeToString(modulus.toByteArray()),
			encoder.encodeToString(publicExponent.toByteArray()),
			encoder.encodeToString(privateExponent.toByteArray()));
	}

	private boolean _fipsEnabled;

}