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
import com.liferay.portal.crypto.hash.CryptoHashResponse;
import com.liferay.portal.crypto.hash.CryptoHashVerificationContext;
import com.liferay.portal.crypto.hash.provider.bcrypt.internal.BCryptCryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.message.digest.internal.MessageDigestCryptoHashProvider;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.util.ArrayList;
import java.util.List;

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
		_cryptoHashGeneratorImpl = new CryptoHashGeneratorImpl();

		_cryptoHashVerifierImpl = new CryptoHashVerifierImpl();

		BCryptCryptoHashProvider bCryptCryptoHashProvider =
			new BCryptCryptoHashProvider();

		CryptoHashProviderRegistry.register(bCryptCryptoHashProvider);

		MessageDigestCryptoHashProvider messageDigestCryptoHashProvider =
			new MessageDigestCryptoHashProvider();

		CryptoHashProviderRegistry.register(messageDigestCryptoHashProvider);
	}

	@Test
	public void testGenerate() throws Exception {
		List<CryptoHashGenerationContext> cryptoHashGenerationContexts =
			new ArrayList<>();

		cryptoHashGenerationContexts.add(
			new CryptoHashGenerationContext("BCrypt"));

		cryptoHashGenerationContexts.add(
			new CryptoHashGenerationContext("MessageDigest", 16));

		for (CryptoHashGenerationContext cryptoHashGenerationContext :
				cryptoHashGenerationContexts) {

			CryptoHashResponse cryptoHashResponse =
				_cryptoHashGeneratorImpl.generate(
					_INPUT, cryptoHashGenerationContext);

			Assert.assertFalse(
				_cryptoHashVerifierImpl.verify(
					_randomBytes(), cryptoHashResponse.getHash(),
					cryptoHashResponse.getCryptoHashVerificationContext()));
			Assert.assertTrue(
				_cryptoHashVerifierImpl.verify(
					_INPUT, cryptoHashResponse.getHash(),
					cryptoHashResponse.getCryptoHashVerificationContext()));
		}

		List<CryptoHashVerificationContext> cryptoHashVerificationContexts =
			new ArrayList<>();
		byte[] hash = _INPUT;

		for (CryptoHashGenerationContext cryptoHashGenerationContext :
				cryptoHashGenerationContexts) {

			CryptoHashResponse cryptoHashResponse =
				_cryptoHashGeneratorImpl.generate(
					hash, cryptoHashGenerationContext);

			cryptoHashVerificationContexts.add(
				cryptoHashResponse.getCryptoHashVerificationContext());

			hash = cryptoHashResponse.getHash();
		}

		Assert.assertFalse(
			_cryptoHashVerifierImpl.verify(
				_randomBytes(), hash, cryptoHashVerificationContexts));
		Assert.assertTrue(
			_cryptoHashVerifierImpl.verify(
				_INPUT, hash, cryptoHashVerificationContexts));
	}

	private static byte[] _randomBytes() {
		String string = RandomTestUtil.randomString();

		return string.getBytes();
	}

	private static final byte[] _INPUT = _randomBytes();

	private CryptoHashGeneratorImpl _cryptoHashGeneratorImpl;
	private CryptoHashVerifierImpl _cryptoHashVerifierImpl;

}