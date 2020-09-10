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

package com.liferay.saml.saas.internal.util;

import com.liferay.portal.kernel.security.SecureRandom;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.DigesterUtil;

import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Marta Medio
 */
public class SymmetricEncryptor {

	public static String decryptData(String preSharedKey, String data)
		throws Exception {

		ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.decode(data));

		byte[] gmcParameterSpecSrc = new byte[_GCM_NONCE_LENGTH];

		byteBuffer.get(gmcParameterSpecSrc);

		byte[] cipherInput = new byte[byteBuffer.remaining()];

		byteBuffer.get(cipherInput);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		cipher.init(
			Cipher.DECRYPT_MODE,
			new SecretKeySpec(
				DigesterUtil.digestRaw("SHA-256", preSharedKey), "AES"),
			new GCMParameterSpec(_GCM_TAG_LENGTH, gmcParameterSpecSrc));

		return new String(cipher.doFinal(cipherInput));
	}

	public static String encryptData(String preSharedKey, String data)
		throws Exception {

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		SecureRandom secureRandom = new SecureRandom();

		byte[] gmcParameterSpecSrc = new byte[_GCM_NONCE_LENGTH];

		secureRandom.nextBytes(gmcParameterSpecSrc);

		cipher.init(
			Cipher.ENCRYPT_MODE,
			new SecretKeySpec(
				DigesterUtil.digestRaw("SHA-256", preSharedKey), "AES"),
			new GCMParameterSpec(128, gmcParameterSpecSrc));

		byte[] cipherOutput = cipher.doFinal(data.getBytes());

		ByteBuffer byteBuffer = ByteBuffer.allocate(
			gmcParameterSpecSrc.length + cipherOutput.length);

		byteBuffer.put(gmcParameterSpecSrc);

		byteBuffer.put(cipherOutput);

		return Base64.encode(byteBuffer.array());
	}

	private static final int _GCM_NONCE_LENGTH = 12;

	private static final int _GCM_TAG_LENGTH = 128;

}