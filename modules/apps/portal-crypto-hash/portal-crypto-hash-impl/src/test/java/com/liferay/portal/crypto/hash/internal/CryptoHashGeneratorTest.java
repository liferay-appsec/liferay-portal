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

package com.liferay.portal.crypto.hash.internal;

import com.liferay.portal.crypto.hash.CryptoHashGenerationContext;
import com.liferay.portal.crypto.hash.CryptoHashGenerator;
import com.liferay.portal.crypto.hash.CryptoHashResponse;
import com.liferay.portal.crypto.hash.CryptoHashVerificationContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.UnicodeFormatter;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
public class CryptoHashGeneratorTest {

	@Before
	public void setUp() throws Exception {
		_cryptoHashGenerator = new CryptoHashGeneratorImpl();
	}

	@Test
	public void testGenerate() throws Exception {
		CryptoHashGenerationContext cryptoHashGenerationContext =
			new CryptoHashGenerationContext(
				_CRYPTO_HASH_PROVIDER_1, new HashMap<>(), -1);

		CryptoHashResponse cryptoHashResponse = _cryptoHashGenerator.generate(
			_INPUT, cryptoHashGenerationContext);

		CryptoHashVerificationContext cryptoHashVerificationContext =
			new CryptoHashVerificationContext(
				new byte[0], cryptoHashResponse.getSalt(),
				_CRYPTO_HASH_PROVIDER_1, new HashMap<>());

		Assert.assertFalse(
			_cryptoHashGenerator.verify(
				_randomBytes(), cryptoHashResponse.getHash(),
				cryptoHashVerificationContext));
		Assert.assertTrue(
			_cryptoHashGenerator.verify(
				_INPUT, cryptoHashResponse.getHash(),
				cryptoHashVerificationContext));
	}

	/**
	 * Example to show how to verify an INPUT against a HASH when the
	 * HASH was previous generated from multiple contexts.
	 */
	@Test
	public void testVerify() throws Exception {
		CryptoHashVerificationContext[] cryptoHashVerificationContexts =
			new CryptoHashVerificationContext[2];

		cryptoHashVerificationContexts[0] = new CryptoHashVerificationContext(
			_PEPPER, _SALT_1, _CRYPTO_HASH_PROVIDER_1, new HashMap<>());

		cryptoHashVerificationContexts[1] = new CryptoHashVerificationContext(
			_PEPPER, _SALT_2, _CRYPTO_HASH_PROVIDER_2, new HashMap<>());

		Assert.assertFalse(
			_cryptoHashGenerator.verify(
				_randomBytes(), _HASH, cryptoHashVerificationContexts));

		Assert.assertTrue(
			_cryptoHashGenerator.verify(
				_INPUT, _HASH, cryptoHashVerificationContexts));
	}

	private byte[] _randomBytes() {
		String string = RandomTestUtil.randomString();

		return string.getBytes();
	}

	private static final String _CRYPTO_HASH_PROVIDER_1 = "SHA-256";

	private static final String _CRYPTO_HASH_PROVIDER_2 = "SHA-512";

	private static final byte[] _HASH = UnicodeFormatter.hexToBytes(
		"ee765094649dcc6b5e89a91663cbeb80ecceed035e13201da471a97d30534f57" +
			"1dd8974729feb4e1696485b1e054672d91c9e774514921c067028a46bcb6f1c5");

	private static final byte[] _INPUT = "password".getBytes();

	// Stub pepper, real pepper implementation comes in next iteration

	private static final byte[] _PEPPER = new byte[0];

	private static final byte[] _SALT_1 = "salt1".getBytes();

	private static final byte[] _SALT_2 = "salt2".getBytes();

	private CryptoHashGenerator _cryptoHashGenerator;

}