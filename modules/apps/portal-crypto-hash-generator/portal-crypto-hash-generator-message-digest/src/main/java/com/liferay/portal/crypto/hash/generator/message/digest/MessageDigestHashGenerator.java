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

package com.liferay.portal.crypto.hash.generator.message.digest;

import com.liferay.portal.crypto.hash.generator.spi.BaseHashGenerator;
import com.liferay.portal.crypto.hash.generator.spi.salt.VariableSizeSaltGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Arthur Chan
 */
public class MessageDigestHashGenerator
	extends BaseHashGenerator implements VariableSizeSaltGenerator {

	public MessageDigestHashGenerator() throws NoSuchAlgorithmException {
		this("SHA-512");
	}

	public MessageDigestHashGenerator(String generatorName)
		throws NoSuchAlgorithmException {

		_messageDigest = MessageDigest.getInstance(generatorName);
	}

	@Override
	public byte[] generate(byte[] input) {
		byte[] bytes = new byte[pepper.length + salt.length + input.length];

		System.arraycopy(pepper, 0, bytes, 0, pepper.length);
		System.arraycopy(salt, 0, bytes, pepper.length, salt.length);
		System.arraycopy(
			input, 0, bytes, pepper.length + salt.length, input.length);

		return _messageDigest.digest(bytes);
	}

	private final MessageDigest _messageDigest;

}