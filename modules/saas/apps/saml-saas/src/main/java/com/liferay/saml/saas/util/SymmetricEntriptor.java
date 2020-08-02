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

package com.liferay.saml.saas.util;

import com.liferay.portal.kernel.util.Base64;

import java.nio.ByteBuffer;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Marta Medio
 */
public class SymmetricEntriptor {

	public static String decryptData(String preSharedKey, String data)
		throws Exception {

		byte[] decode = Base64.decode(data);

		ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

		byte[] salt = new byte[_PBKDF2_SALT_LENGTH];

		byteBuffer.get(salt);

		byte[] nonce = new byte[_GCM_NONCE_LENGTH];

		byteBuffer.get(nonce);

		byte[] cipherBytes = new byte[byteBuffer.remaining()];

		byteBuffer.get(cipherBytes);

		SecretKeyFactory factory = SecretKeyFactory.getInstance(_ALGORITHM);

		KeySpec keySpec = new PBEKeySpec(
			preSharedKey.toCharArray(), salt, _PBKDF2_ITERATION_COUNT,
			_AES_KEY_LENGTH);

		SecretKey secretKey = factory.generateSecret(keySpec);

		SecretKey key = new SecretKeySpec(secretKey.getEncoded(), "AES");

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(
			_GCM_TAG_LENGTH, nonce);

		cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

		byte[] decryptedBytes = cipher.doFinal(cipherBytes);

		return new String(decryptedBytes);
	}

	public static String encryptData(String preSharedKey, String data)
		throws Exception {

		SecretKeyFactory factory = SecretKeyFactory.getInstance(_ALGORITHM);

		byte[] salt = new byte[16];

		KeySpec keySpec = new PBEKeySpec(
			preSharedKey.toCharArray(), salt, _PBKDF2_ITERATION_COUNT,
			_AES_KEY_LENGTH);

		SecretKey secretKey = factory.generateSecret(keySpec);

		byte[] secret = secretKey.getEncoded();

		SecretKey key = new SecretKeySpec(secret, "AES");

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		byte[] nonce = new byte[12];

		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);

		cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

		byte[] encrypted = cipher.doFinal(data.getBytes());

		ByteBuffer byteBuffer = ByteBuffer.allocate(
			salt.length + nonce.length + encrypted.length);

		byteBuffer.put(salt);
		byteBuffer.put(nonce);
		byteBuffer.put(encrypted);

		return Base64.encode(byteBuffer.array());
	}

	private static final int _AES_KEY_LENGTH = 256;

	private static final String _ALGORITHM = "PBKDF2WithHmacSHA512";

	private static final int _GCM_NONCE_LENGTH = 12;

	private static final int _GCM_TAG_LENGTH = 128;

	private static final int _PBKDF2_ITERATION_COUNT = 10000;

	private static final int _PBKDF2_SALT_LENGTH = 16;

}