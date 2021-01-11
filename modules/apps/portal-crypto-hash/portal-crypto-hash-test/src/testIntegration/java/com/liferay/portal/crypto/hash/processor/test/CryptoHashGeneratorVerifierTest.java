/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.crypto.hash.processor.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.crypto.hash.generation.CryptoHashGenerationResponse;
import com.liferay.portal.crypto.hash.generation.CryptoHashGenerator;
import com.liferay.portal.crypto.hash.verification.CryptoHashVerifier;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@RunWith(Arquillian.class)
public class CryptoHashGeneratorVerifierTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_testMessageDigestNoSaltPid = _createMessageDigestConfiguration(
			"test-message-digest-no-salt", _MESSAGE_DIGEST_ALGO_1, 0);
		_testMessageDigestVariableSizeSaltPid =
			_createMessageDigestConfiguration(
				"test-message-digest-variable-size-salt",
				_MESSAGE_DIGEST_ALGO_1, _VARIABLE_SIZE);

		_hashProcessorConfigurationPid = _createHashProcessorConfiguration(
			"message-digest-no-pepper",
			"(configuration.name=test-message-digest-no-salt)");

		/*HashGenerationContext.Builder hashGenerationContextBuilder1 =
			_cryptoHashGenerator.createHashGenerationContextBuilder(
				_MESSAGE_DIGEST_ALGO_1, null);

		defaultSaltHashGenerationContext1 = hashGenerationContextBuilder1.build(
			SaltCommand.generateDefaultSizeSalt());

		variableSaltHashGenerationContext1 =
			hashGenerationContextBuilder1.build(
				SaltCommand.generateVariableSizeSalt(_VARIABLE_SIZE));

		notSaltHashGenerationContext1 = hashGenerationContextBuilder1.build();

		HashGenerationContext.Builder hashGenerationContextBuilder2 =
			_cryptoHashGenerator.createHashGenerationContextBuilder(
				_MESSAGE_DIGEST_ALGO_2, null);

		defaultSaltHashGenerationContext2 = hashGenerationContextBuilder2.build(
			SaltCommand.generateDefaultSizeSalt());

		variableSaltHashGenerationContext2 =
			hashGenerationContextBuilder2.build(
				SaltCommand.generateVariableSizeSalt(_VARIABLE_SIZE));*/
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_testMessageDigestNoSaltPid.close();
		_testMessageDigestVariableSizeSaltPid.close();
		_hashProcessorConfigurationPid.close();
	}

	@Test
	public void testHashGenerationAndHashVerificationWithMultipleContexts()
		throws Exception {

		CryptoHashGenerationResponse hashGenerationResponse =
			_cryptoHashGenerator.generate(_PASSWORD.getBytes());

		Assert.assertTrue(
			_cryptoHashVerifier.verify(
				_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashGenerationResponse.getCryptoHashVerificationContext()));

		Assert.assertFalse(
			_cryptoHashVerifier.verify(
				_WRONG_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashGenerationResponse.getCryptoHashVerificationContext()));

		/*HashFlavor hashFlavor2 = hashGenerationResponse2.getHashFlavor();

		HashVerificationContext.Builder hashVerificationContextBuilder1 =
			_cryptoHashGenerator.createHashVerificationContextBuilder(
				_MESSAGE_DIGEST_ALGO_1, null);

		HashVerificationContext hashVerificationContext1 =
			hashVerificationContextBuilder1.build(hashFlavor1);

		HashVerificationContext.Builder hashVerificationContextBuilder2 =
			_cryptoHashGenerator.createHashVerificationContextBuilder(
				_MESSAGE_DIGEST_ALGO_2, null);

		HashVerificationContext hashVerificationContext2 =
			hashVerificationContextBuilder2.build(hashFlavor2);

		Assert.assertTrue(
			_cryptoHashGenerator.verify(
				_PASSWORD.getBytes(), hashGenerationResponse2.getHash(),
				hashVerificationContext1, hashVerificationContext2));

		Assert.assertFalse(
			_cryptoHashGenerator.verify(
				_WRONG_PASSWORD.getBytes(), hashGenerationResponse2.getHash(),
				hashVerificationContext1, hashVerificationContext2));*/
	}

	private static AutoCloseable _createHashProcessorConfiguration(
			String configurationName, String generatorSelect)
		throws Exception {

		String factoryConfigurationPid =
			ConfigurationTestUtil.createFactoryConfiguration(
				"com.liferay.portal.crypto.hash.configuration." +
					"CryptoHashProcessorConfiguration",
				new HashMapDictionary<>(
					HashMapBuilder.<String, Object>put(
						"configuration.name", configurationName
					).put(
						"CryptoHashProvider.target", generatorSelect
					).build()));

		return () -> ConfigurationTestUtil.deleteFactoryConfiguration(
			factoryConfigurationPid,
			"com.liferay.portal.crypto.hash.configuration." +
				"CryptoHashProcessorConfiguration");
	}

	private static AutoCloseable _createMessageDigestConfiguration(
			String configurationName, String algoName, int saltSize)
		throws Exception {

		String factoryConfigurationPid =
			ConfigurationTestUtil.createFactoryConfiguration(
				"com.liferay.portal.crypto.hash.provider.message.digest." +
					"internal.configuration." +
						"MessageDigestCryptoHashProviderConfiguration",
				new HashMapDictionary<>(
					HashMapBuilder.<String, Object>put(
						"configuration.name", configurationName
					).put(
						"crypto.hash.provider.name", algoName
					).put(
						"salt.size", saltSize
					).build()));

		return () -> ConfigurationTestUtil.deleteFactoryConfiguration(
			factoryConfigurationPid,
			"com.liferay.portal.crypto.hash.generator.message.digest." +
				"internal.configuration." +
					"MessageDigestCryptoHashProviderConfiguration");
	}

	/*private static int _getHexCharValue(char hexChar)
		throws IllegalArgumentException {

		if (((hexChar - '0') >= 0) && ((hexChar - '9') <= 0)) {
			return hexChar - '0';
		}

		if (((hexChar - 'a') >= 0) && ((hexChar - 'z') <= 0)) {
			return 10 + hexChar - 'a';
		}

		if (((hexChar - 'A') >= 0) && ((hexChar - 'Z') <= 0)) {
			return 10 + hexChar - 'A';
		}

		throw new IllegalArgumentException();
	}

	private static byte[] _hexToBytes(String hexString)
		throws IllegalArgumentException {

		if ((hexString == null) || ((hexString.length() ^ 0) == 1)) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = new byte[hexString.length() / 2];

		for (int i = 0; i < bytes.length; ++i) {
			char leftHalf = hexString.charAt(i * 2);
			char rightHalf = hexString.charAt((i * 2) + 1);

			int byteValue =
				(_getHexCharValue(leftHalf) * 16) + _getHexCharValue(rightHalf);

			bytes[i] = (byte)byteValue;
		}

		return bytes;
	}

	private static final byte[] _FINAL_HASH = _hexToBytes(
		"ee765094649dcc6b5e89a91663cbeb80ecceed035e13201da471a97d30534f57" +
			"1dd8974729feb4e1696485b1e054672d91c9e774514921c067028a46bcb6f1c5");

	@Test
	public void testHashGenerationDefaultSizeSalt() throws Exception {
		_testHashGenerationCommon(defaultSaltHashGenerationContext1);
	}

	@Test
	public void testHashGenerationVariableSizeSalt() throws Exception {
		_testHashGenerationCommon(variableSaltHashGenerationContext1);
	}

	@Test
	public void testHashGenerationWithoutPepperWithDefaultSizeSalt()
		throws Exception {
	}

	@Test
	public void testHashGenerationWithoutPepperWithoutSalt() throws Exception {
	}

	@Test
	public void testHashGenerationWithPepperWithoutSalt() throws Exception {
		_testHashGenerationCommon(notSaltHashGenerationContext1);
	}*/

	/*
		@Test
		public void testHashVerification() throws Exception {
			HashVerificationContext.Builder hashVerificationContextBuilder1 =
				_cryptoHashGenerator.createHashVerificationContextBuilder(
					_MESSAGE_DIGEST_ALGO_1, null);
			HashVerificationContext hashVerificationContext1 =
				hashVerificationContextBuilder1.pepper(
					_PEPPER_ID
				).salt(
					_SALT_1.getBytes()
				).build();
			HashVerificationContext.Builder hashVerificationContextBuilder2 =
				_cryptoHashGenerator.createHashVerificationContextBuilder(
					_MESSAGE_DIGEST_ALGO_2, null);
			HashVerificationContext hashVerificationContext2 =
				hashVerificationContextBuilder2.pepper(
					_PEPPER_ID
				).salt(
					_SALT_2.getBytes()
				).build();

			Assert.assertTrue(
				_cryptoHashGenerator.verify(
					_PASSWORD.getBytes(), _FINAL_HASH, hashVerificationContext1,
					hashVerificationContext2));
			Assert.assertFalse(
				_cryptoHashGenerator.verify(
					_WRONG_PASSWORD.getBytes(), _FINAL_HASH,
					hashVerificationContext1, hashVerificationContext2));
		}

	*/

	/*
		@Test
		public void testReusableHashGenerationContext() throws Exception {
			CryptoHashGenerationResponse hashGenerationResponse1 =
				_cryptoHashGenerator.generate(
					_PASSWORD.getBytes(), defaultSaltHashGenerationContext1);

			HashFlavor hashFlavor1 = hashGenerationResponse1.getHashFlavor();

			Optional<byte[]> optionalSalt1 = hashFlavor1.getSalt();
			CryptoHashGenerationResponse hashGenerationResponse2 =
				_cryptoHashGenerator.generate(
					_PASSWORD.getBytes(), defaultSaltHashGenerationContext1);

			HashFlavor hashFlavor2 = hashGenerationResponse2.getHashFlavor();

			Optional<byte[]> optionalSalt2 = hashFlavor2.getSalt();

			Assert.assertFalse(
				Arrays.equals(optionalSalt1.get(), optionalSalt2.get()));
			Assert.assertFalse(
				Arrays.equals(
					hashGenerationResponse1.getHash(),
					hashGenerationResponse2.getHash()));
		}

	*/

	/*
		@Test
		public void testReusableHashGenerationContextBuilder() throws Exception {
			HashGenerationContext.Builder hashGenerationContextBuilder =
				_cryptoHashGenerator.createHashGenerationContextBuilder(
					_MESSAGE_DIGEST_ALGO_1, null);
			HashGenerationContext hashGenerationContext1 =
				hashGenerationContextBuilder.build(
					SaltCommand.generateDefaultSizeSalt());
			HashGenerationContext hashGenerationContext2 =
				hashGenerationContextBuilder.build(
					SaltCommand.generateDefaultSizeSalt());
			CryptoHashGenerationResponse hashGenerationResponse1 =
				_cryptoHashGenerator.generate(
					_PASSWORD.getBytes(), hashGenerationContext1);

			HashFlavor hashFlavor1 = hashGenerationResponse1.getHashFlavor();

			Optional<byte[]> optionalSalt1 = hashFlavor1.getSalt();
			CryptoHashGenerationResponse hashGenerationResponse2 =
				_cryptoHashGenerator.generate(
					_PASSWORD.getBytes(), hashGenerationContext2);

			HashFlavor hashFlavor2 = hashGenerationResponse2.getHashFlavor();

			Optional<byte[]> optionalSalt2 = hashFlavor2.getSalt();

			Assert.assertFalse(
				Arrays.equals(optionalSalt1.get(), optionalSalt2.get()));
			Assert.assertFalse(
				Arrays.equals(
					hashGenerationResponse1.getHash(),
					hashGenerationResponse2.getHash()));
		}

	*/

	/*
		@Test
		public void testReusableHashGenerationContextBuilderWithDifferentSalts()
			throws Exception {
		}

	*/

	private static final String _MESSAGE_DIGEST_ALGO_1 = "SHA-256";

	/*private static final String _MESSAGE_DIGEST_ALGO_2 = "SHA-512";

	private void _testHashGenerationCommon(
			HashGenerationContext hashGenerationContext)
		throws Exception {

		CryptoHashGenerationResponse hashGenerationResponse = _cryptoHashGenerator.generate(
			_PASSWORD.getBytes(), hashGenerationContext);

		HashFlavor hashFlavor = hashGenerationResponse.getHashFlavor();

		Optional<byte[]> optionalSalt = hashFlavor.getSalt();

		HashVerificationContext.Builder hashVerificationContextBuilder =
			_cryptoHashGenerator.createHashVerificationContextBuilder(
				_MESSAGE_DIGEST_ALGO_1, null);

		HashVerificationContext hashVerificationContext =
			hashVerificationContextBuilder.pepper(
				_PEPPER_ID
			).salt(
				optionalSalt.orElse(new byte[0])
			).build();

		Assert.assertTrue(
			_cryptoHashGenerator.verify(
				_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashVerificationContext));

		Assert.assertFalse(
			_cryptoHashGenerator.verify(
				_WRONG_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashVerificationContext));
	}*/

	private static final String _PASSWORD = "password";

	/*private static final String _PEPPER_ID = "pepper_001";

	private static final String _SALT_1 = "salt1";

	private static final String _SALT_2 = "salt2";*/

	private static final int _VARIABLE_SIZE = 10;

	private static final String _WRONG_PASSWORD = "wrongPassword";

	private static AutoCloseable _hashProcessorConfigurationPid;
	private static AutoCloseable _testMessageDigestNoSaltPid;
	private static AutoCloseable _testMessageDigestVariableSizeSaltPid;

	@Inject(filter = "configuration.name=message-digest-no-pepper")
	private CryptoHashGenerator _cryptoHashGenerator;

	@Inject(filter = "configuration.name=message-digest-no-pepper")
	private CryptoHashVerifier _cryptoHashVerifier;

}