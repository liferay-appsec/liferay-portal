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

package com.liferay.portal.crypto.hash.provider.message.digest;

import com.liferay.portal.crypto.hash.provider.message.digest.internal.MessageDigestCryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.message.digest.internal.factory.MessageDigestCryptoHashProviderFactory;
import com.liferay.portal.crypto.hash.provider.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.spi.salt.VariableSizeSaltProvider;
import com.liferay.portal.kernel.util.UnicodeFormatter;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.MockitoAnnotations;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Arthur Chan
 */
@PrepareForTest(MessageDigestCryptoHashProvider.class)
@RunWith(PowerMockRunner.class)
public class MessageDigestCryptoHashProviderTest extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		MessageDigestCryptoHashProviderFactory cryptoHashProviderFactory =
			new MessageDigestCryptoHashProviderFactory();

		_cryptoHashProvider = cryptoHashProviderFactory.create(
			_CRYPTO_HASH_PROVIDER_NAME, new HashMap<>());
	}

	@Test
	public void testGenerateDefaultSalt() throws Exception {
		byte[] salt = _cryptoHashProvider.generateSalt();

		Assert.assertEquals(salt.length, Long.BYTES);
	}

	@Test
	public void testGenerateVariableSizeSalt() throws Exception {
		VariableSizeSaltProvider variableSizeSaltGenerator =
			(VariableSizeSaltProvider)_cryptoHashProvider;

		byte[] salt = variableSizeSaltGenerator.generateSalt(_VARIABLE_SIZE);

		Assert.assertEquals(salt.length, _VARIABLE_SIZE);
	}

	@Test
	public void testHash() throws Exception {
		byte[] hash = _cryptoHashProvider.hash(_INPUT);

		Assert.assertArrayEquals(_HASH, hash);
	}

	@Test
	public void testHashWithPepper() throws Exception {
		_cryptoHashProvider.setPepper(_PEPPER);

		byte[] hash = _cryptoHashProvider.hash(_INPUT);

		Assert.assertArrayEquals(_HASH_WITH_PEPPER, hash);

		_cryptoHashProvider.setPepper(_EMPTY_BYTE_ARRAY);
	}

	@Test
	public void testHashWithSalt() throws Exception {
		_cryptoHashProvider.setSalt(_SALT);

		byte[] hash = _cryptoHashProvider.hash(_INPUT);

		Assert.assertArrayEquals(_HASH_WITH_SALT, hash);

		_cryptoHashProvider.setSalt(_EMPTY_BYTE_ARRAY);
	}

	private static final String _CRYPTO_HASH_PROVIDER_NAME = "SHA-256";

	private static final byte[] _EMPTY_BYTE_ARRAY = new byte[0];

	private static final byte[] _HASH = UnicodeFormatter.hexToBytes(
		"5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");

	private static final byte[] _HASH_WITH_PEPPER = UnicodeFormatter.hexToBytes(
		"4b65d30b048d9eab292a2ea50fd60423d3d5d581a6ed85169b8a0c4f7dd10c00");

	private static final byte[] _HASH_WITH_SALT = UnicodeFormatter.hexToBytes(
		"13601bda4ea78e55a07b98866d2be6be0744e3866f13c00c811cab608a28f322");

	private static final byte[] _INPUT = "password".getBytes();

	private static final byte[] _PEPPER = "pepper".getBytes();

	private static final byte[] _SALT = "salt".getBytes();

	private static final int _VARIABLE_SIZE = 10;

	private CryptoHashProvider _cryptoHashProvider;

}