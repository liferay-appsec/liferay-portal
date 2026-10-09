/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.util;

import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Objects;

/**
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 * @author Connor McKay
 */
public class DigesterUtil {

	public static final String ENCODING = StringPool.UTF8;

	public static final String MD5 = "MD5";

	public static final String SHA = "SHA";

	public static final String SHA_1 = "SHA-1";

	public static final String SHA_256 = "SHA-256";

	public static String digest(ByteBuffer byteBuffer) {
		return _encode(digestRaw(_getAlgorithm(), byteBuffer));
	}

	public static String digest(
		CryptoFunctionApprovalType approvalType, String algorithm,
		String... text) {

		return _encode(digestRaw(approvalType, algorithm, text));
	}

	public static String digest(InputStream inputStream) {
		return _encode(digestRaw(_getAlgorithm(), inputStream));
	}

	public static String digest(String text) {
		return digest(_getAlgorithm(), text);
	}

	public static String digest(String algorithm, ByteBuffer byteBuffer) {
		if (_BASE_64) {
			return digestBase64(algorithm, byteBuffer);
		}

		return digestHex(algorithm, byteBuffer);
	}

	public static String digest(String algorithm, InputStream inputStream) {
		if (_BASE_64) {
			return digestBase64(algorithm, inputStream);
		}

		return digestHex(algorithm, inputStream);
	}

	public static String digest(String algorithm, String... text) {
		if (_BASE_64) {
			return digestBase64(algorithm, text);
		}

		return digestHex(algorithm, text);
	}

	public static String digestBase64(ByteBuffer byteBuffer) {
		return Base64.encode(digestRaw(_getAlgorithm(), byteBuffer));
	}

	public static String digestBase64(
		CryptoFunctionApprovalType approvalType, String algorithm,
		InputStream inputStream) {

		byte[] bytes = _digestRaw(approvalType, algorithm, inputStream);

		return Base64.encode(bytes);
	}

	public static String digestBase64(InputStream inputStream) {
		return digestBase64(_getAlgorithm(), inputStream);
	}

	public static String digestBase64(String text) {
		return Base64.encode(digestRaw(_getAlgorithm(), text));
	}

	public static String digestBase64(String algorithm, ByteBuffer byteBuffer) {
		byte[] bytes = digestRaw(algorithm, byteBuffer);

		return Base64.encode(bytes);
	}

	public static String digestBase64(
		String algorithm, InputStream inputStream) {

		byte[] bytes = digestRaw(algorithm, inputStream);

		return Base64.encode(bytes);
	}

	public static String digestBase64(String algorithm, String... text) {
		byte[] bytes = digestRaw(algorithm, text);

		return Base64.encode(bytes);
	}

	public static String digestHex(ByteBuffer byteBuffer) {
		return StringUtil.bytesToHexString(
			digestRaw(_getAlgorithm(), byteBuffer));
	}

	public static String digestHex(
		CryptoFunctionApprovalType approvalType, String algorithm,
		InputStream inputStream) {

		byte[] bytes = _digestRaw(approvalType, algorithm, inputStream);

		return StringUtil.bytesToHexString(bytes);
	}

	public static String digestHex(
		CryptoFunctionApprovalType approvalType, String algorithm,
		String... text) {

		return StringUtil.bytesToHexString(
			digestRaw(approvalType, algorithm, text));
	}

	public static String digestHex(InputStream inputStream) {
		return digestHex(_getAlgorithm(), inputStream);
	}

	public static String digestHex(String text) {
		return digestHex(_getAlgorithm(), text);
	}

	public static String digestHex(String algorithm, ByteBuffer byteBuffer) {
		byte[] bytes = digestRaw(algorithm, byteBuffer);

		return StringUtil.bytesToHexString(bytes);
	}

	public static String digestHex(String algorithm, InputStream inputStream) {
		byte[] bytes = digestRaw(algorithm, inputStream);

		return StringUtil.bytesToHexString(bytes);
	}

	public static String digestHex(String algorithm, String... text) {
		byte[] bytes = digestRaw(algorithm, text);

		return StringUtil.bytesToHexString(bytes);
	}

	public static byte[] digestRaw(ByteBuffer byteBuffer) {
		return digestRaw(_getAlgorithm(), byteBuffer);
	}

	public static byte[] digestRaw(
		CryptoFunctionApprovalType approvalType, String algorithm,
		ByteBuffer byteBuffer) {

		return _digestRaw(
			_newMessageDigest(approvalType, algorithm), byteBuffer);
	}

	public static byte[] digestRaw(
		CryptoFunctionApprovalType approvalType, String algorithm,
		String... text) {

		return _digestRaw(_newMessageDigest(approvalType, algorithm), text);
	}

	public static byte[] digestRaw(String text) {
		return digestRaw(_getAlgorithm(), text);
	}

	public static byte[] digestRaw(String algorithm, ByteBuffer byteBuffer) {
		return _digestRaw(
			_newMessageDigest(CryptoFunctionApprovalType.APPROVED, algorithm),
			byteBuffer);
	}

	public static byte[] digestRaw(String algorithm, InputStream inputStream1) {
		try (InputStream inputStream2 = inputStream1) {
			return _digestRaw(
				_newMessageDigest(
					CryptoFunctionApprovalType.APPROVED, algorithm),
				inputStream2);
		}
		catch (IOException ioException) {
			throw new SystemException(ioException);
		}
	}

	public static byte[] digestRaw(String algorithm, String... text) {
		return _digestRaw(
			_newMessageDigest(CryptoFunctionApprovalType.APPROVED, algorithm),
			text);
	}

	private static byte[] _digestRaw(
		CryptoFunctionApprovalType approvalType, String algorithm,
		InputStream inputStream1) {

		try (InputStream inputStream2 = inputStream1) {
			return _digestRaw(
				_newMessageDigest(approvalType, algorithm), inputStream2);
		}
		catch (IOException ioException) {
			throw new SystemException(ioException);
		}
	}

	private static byte[] _digestRaw(
		MessageDigest messageDigest, ByteBuffer byteBuffer) {

		messageDigest.update(byteBuffer);

		return messageDigest.digest();
	}

	private static byte[] _digestRaw(
			MessageDigest messageDigest, InputStream inputStream)
		throws IOException {

		byte[] buffer = new byte[StreamUtil.BUFFER_SIZE];

		int read = 0;

		while ((read = inputStream.read(buffer)) != -1) {
			if (read > 0) {
				messageDigest.update(buffer, 0, read);
			}
		}

		return messageDigest.digest();
	}

	private static byte[] _digestRaw(
		MessageDigest messageDigest, String... text) {

		StringBundler sb = new StringBundler((text.length * 2) - 1);

		for (String t : text) {
			if (sb.length() > 0) {
				sb.append(StringPool.COLON);
			}

			sb.append(t);
		}

		String s = sb.toString();

		try {
			messageDigest.update(s.getBytes(ENCODING));
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new SystemException(unsupportedEncodingException);
		}

		return messageDigest.digest();
	}

	private static String _encode(byte[] bytes) {
		if (_BASE_64) {
			return Base64.encode(bytes);
		}

		return StringUtil.bytesToHexString(bytes);
	}

	private static String _getAlgorithm() {
		return CryptoUtil.getAlgorithm(SHA, SHA_256);
	}

	private static MessageDigest _newMessageDigest(
		CryptoFunctionApprovalType approvalType, String algorithm) {

		try {
			return CryptoUtil.createMessageDigest(algorithm, approvalType);
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			throw new SystemException(noSuchAlgorithmException);
		}
	}

	private static final boolean _BASE_64 = Objects.equals(
		PropsUtil.get(PropsKeys.PASSWORDS_DIGEST_ENCODING), "base64");

}