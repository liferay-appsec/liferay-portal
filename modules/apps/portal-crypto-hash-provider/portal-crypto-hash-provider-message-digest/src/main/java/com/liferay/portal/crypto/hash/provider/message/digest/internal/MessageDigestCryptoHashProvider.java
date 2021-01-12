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

package com.liferay.portal.crypto.hash.provider.message.digest.internal;

import com.liferay.portal.crypto.hash.provider.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.spi.salt.VariableSizeSaltProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Arthur Chan
 */
public class MessageDigestCryptoHashProvider
	implements CryptoHashProvider, VariableSizeSaltProvider {

	public MessageDigestCryptoHashProvider(String cryptoHashProviderName)
		throws NoSuchAlgorithmException {

		_messageDigest = MessageDigest.getInstance(cryptoHashProviderName);
	}

	@Override
	public byte[] hash(byte[] input) {
		byte[] bytes = new byte[_pepper.length + _salt.length + input.length];

		System.arraycopy(_pepper, 0, bytes, 0, _pepper.length);
		System.arraycopy(_salt, 0, bytes, _pepper.length, _salt.length);
		System.arraycopy(
			input, 0, bytes, _pepper.length + _salt.length, input.length);

		return _messageDigest.digest(bytes);
	}

	@Override
	public void setPepper(byte[] pepper) {
		if (pepper == null) {
			return;
		}

		_pepper = pepper;
	}

	@Override
	public void setSalt(byte[] salt) {
		if (salt == null) {
			return;
		}

		_salt = salt;
	}

	private final MessageDigest _messageDigest;
	private byte[] _pepper = new byte[0];
	private byte[] _salt = new byte[0];

}