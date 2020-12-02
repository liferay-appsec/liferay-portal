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
import com.liferay.portal.crypto.hash.context.builder.HashContextBuilder;
import com.liferay.portal.crypto.hash.flavor.HashFlavor;
import com.liferay.portal.crypto.hash.generation.context.HashGenerationContext;
import com.liferay.portal.crypto.hash.generation.context.salt.SaltCommand;
import com.liferay.portal.crypto.hash.generation.response.HashGenerationResponse;
import com.liferay.portal.crypto.hash.processor.HashProcessor;
import com.liferay.portal.crypto.hash.verification.context.HashVerificationContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@RunWith(Arquillian.class)
public class HashProcessorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		HashContextBuilder hashContextBuilder1 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_1);

		HashGenerationContext.Builder hashGenerationContextBuilder1 =
			hashContextBuilder1.createHashGenerationContextBuilder();

		defaultSaltHashGenerationContext1 = hashGenerationContextBuilder1.build(
			SaltCommand.generateDefaultSizeSalt());

		variableSaltHashGenerationContext1 =
			hashGenerationContextBuilder1.build(
				SaltCommand.generateVariableSizeSalt(_VARIABLE_SIZE));

		notSaltHashGenerationContext1 = hashGenerationContextBuilder1.build();

		HashContextBuilder hashContextBuilder2 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_2);

		HashGenerationContext.Builder hashGenerationContextBuilder2 =
			hashContextBuilder2.createHashGenerationContextBuilder();

		defaultSaltHashGenerationContext2 = hashGenerationContextBuilder2.build(
			SaltCommand.generateDefaultSizeSalt());

		variableSaltHashGenerationContext2 =
			hashGenerationContextBuilder2.build(
				SaltCommand.generateVariableSizeSalt(_VARIABLE_SIZE));
	}

	@Test
	public void testHashGenerationAndHashVerificationWithMultipleContexts()
		throws Exception {

		HashGenerationResponse hashGenerationResponse1 =
			_hashProcessor.generate(
				_PASSWORD.getBytes(), defaultSaltHashGenerationContext1);

		HashFlavor hashFlavor1 = hashGenerationResponse1.getHashFlavor();

		Optional<String> optionalPepperId1 = hashFlavor1.getPepperId();
		Optional<byte[]> optionalSalt1 = hashFlavor1.getSalt();

		HashGenerationResponse hashGenerationResponse2 =
			_hashProcessor.generate(
				hashGenerationResponse1.getHash(),
				variableSaltHashGenerationContext2);

		HashFlavor hashFlavor2 = hashGenerationResponse2.getHashFlavor();

		Optional<String> optionalPepperId2 = hashFlavor2.getPepperId();
		Optional<byte[]> optionalSalt2 = hashFlavor2.getSalt();

		HashContextBuilder hashContextBuilder1 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_1);

		HashVerificationContext.Builder hashVerificationContextBuilder1 =
			hashContextBuilder1.createHashVerificationContextBuilder();

		HashVerificationContext hashVerificationContext1 =
			hashVerificationContextBuilder1.pepper(
				optionalPepperId1.orElse("")
			).salt(
				optionalSalt1.orElse(new byte[0])
			).build();

		HashContextBuilder hashContextBuilder2 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_2);

		HashVerificationContext.Builder hashVerificationContextBuilder2 =
			hashContextBuilder2.createHashVerificationContextBuilder();

		HashVerificationContext hashVerificationContext2 =
			hashVerificationContextBuilder2.pepper(
				optionalPepperId2.orElse("")
			).salt(
				optionalSalt2.orElse(new byte[0])
			).build();

		Assert.assertTrue(
			_hashProcessor.verify(
				_PASSWORD.getBytes(), hashGenerationResponse2.getHash(),
				hashVerificationContext1, hashVerificationContext2));

		Assert.assertFalse(
			_hashProcessor.verify(
				_WRONG_PASSWORD.getBytes(), hashGenerationResponse2.getHash(),
				hashVerificationContext1, hashVerificationContext2));
	}

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
	}

	@Test
	public void testHashVerification() throws Exception {
		HashContextBuilder hashContextBuilder1 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_1);

		HashVerificationContext.Builder hashVerificationContextBuilder1 =
			hashContextBuilder1.createHashVerificationContextBuilder();

		HashVerificationContext hashVerificationContext1 =
			hashVerificationContextBuilder1.pepper(
				_PEPPER_ID
			).salt(
				_SALT_1.getBytes()
			).build();

		HashContextBuilder hashContextBuilder2 =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_2);

		HashVerificationContext.Builder hashVerificationContextBuilder2 =
			hashContextBuilder2.createHashVerificationContextBuilder();

		HashVerificationContext hashVerificationContext2 =
			hashVerificationContextBuilder2.pepper(
				_PEPPER_ID
			).salt(
				_SALT_2.getBytes()
			).build();

		Assert.assertTrue(
			_hashProcessor.verify(
				_PASSWORD.getBytes(), _FINAL_HASH, hashVerificationContext1,
				hashVerificationContext2));

		Assert.assertFalse(
			_hashProcessor.verify(
				_WRONG_PASSWORD.getBytes(), _FINAL_HASH,
				hashVerificationContext1, hashVerificationContext2));
	}

	@Test
	public void testReusableHashGenerationContext() throws Exception {
		HashGenerationResponse hashGenerationResponse1 =
			_hashProcessor.generate(
				_PASSWORD.getBytes(), defaultSaltHashGenerationContext1);

		HashFlavor hashFlavor1 = hashGenerationResponse1.getHashFlavor();

		Optional<byte[]> optionalSalt1 = hashFlavor1.getSalt();

		HashGenerationResponse hashGenerationResponse2 =
			_hashProcessor.generate(
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

	@Test
	public void testReusableHashGenerationContextBuilder() throws Exception {
		HashContextBuilder hashContextBuilder =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_1);

		HashGenerationContext.Builder hashGenerationContextBuilder =
			hashContextBuilder.createHashGenerationContextBuilder();

		HashGenerationContext hashGenerationContext1 =
			hashGenerationContextBuilder.build(
				SaltCommand.generateDefaultSizeSalt());

		HashGenerationContext hashGenerationContext2 =
			hashGenerationContextBuilder.build(
				SaltCommand.generateDefaultSizeSalt());

		HashGenerationResponse hashGenerationResponse1 =
			_hashProcessor.generate(
				_PASSWORD.getBytes(), hashGenerationContext1);

		HashFlavor hashFlavor1 = hashGenerationResponse1.getHashFlavor();

		Optional<byte[]> optionalSalt1 = hashFlavor1.getSalt();

		HashGenerationResponse hashGenerationResponse2 =
			_hashProcessor.generate(
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

	@Test
	public void testReusableHashGenerationContextBuilderWithDifferentSalts()
		throws Exception {
	}

	protected HashGenerationContext defaultSaltHashGenerationContext1;
	protected HashGenerationContext defaultSaltHashGenerationContext2;
	protected HashGenerationContext notSaltHashGenerationContext1;
	protected HashGenerationContext variableSaltHashGenerationContext1;
	protected HashGenerationContext variableSaltHashGenerationContext2;

	private static int _getHexCharValue(char hexChar)
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

	private void _testHashGenerationCommon(
			HashGenerationContext hashGenerationContext)
		throws Exception {

		HashGenerationResponse hashGenerationResponse = _hashProcessor.generate(
			_PASSWORD.getBytes(), hashGenerationContext);

		HashFlavor hashFlavor = hashGenerationResponse.getHashFlavor();

		Optional<byte[]> optionalSalt = hashFlavor.getSalt();

		HashContextBuilder hashContextBuilder =
			_hashProcessor.createHashContextBuilder(_MESSAGE_DIGEST_ALGO_1);

		HashVerificationContext.Builder hashVerificationContextBuilder =
			hashContextBuilder.createHashVerificationContextBuilder();

		HashVerificationContext hashVerificationContext =
			hashVerificationContextBuilder.pepper(
				_PEPPER_ID
			).salt(
				optionalSalt.orElse(new byte[0])
			).build();

		Assert.assertTrue(
			_hashProcessor.verify(
				_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashVerificationContext));

		Assert.assertFalse(
			_hashProcessor.verify(
				_WRONG_PASSWORD.getBytes(), hashGenerationResponse.getHash(),
				hashVerificationContext));
	}

	private static final byte[] _FINAL_HASH = _hexToBytes(
		"5d4f56d5d78b4d7854e2943db62c33b83b12a540c7d2c6769332ea6cd6d8b786" +
			"817660a273f32cf7d82ad592081941fa096a83c60dec5feed6065abf4b98ab86");

	private static final String _MESSAGE_DIGEST_ALGO_1 = "SHA-256";

	private static final String _MESSAGE_DIGEST_ALGO_2 = "SHA-512";

	private static final String _PASSWORD = "password";

	private static final String _PEPPER_ID = "pepper_001";

	private static final String _SALT_1 = "salt1";

	private static final String _SALT_2 = "salt2";

	private static final int _VARIABLE_SIZE = 10;

	private static final String _WRONG_PASSWORD = "wrongPassword";

	@Inject
	private HashProcessor _hashProcessor;

}