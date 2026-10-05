/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.encryptor;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brian Wing Shun Chan
 * @author Shuyang Zhou
 * @author Mika Koivisto
 */
@Component(service = Encryptor.class)
public class EncryptorImpl implements Encryptor {

	public static final String ENCODING = DigesterUtil.ENCODING;

	public static final String KEY_ALGORITHM = StringUtil.toUpperCase(
		GetterUtil.getString(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM)));

	public static final int KEY_SIZE = GetterUtil.getInteger(
		PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_KEY_SIZE));

	@Override
	public String decrypt(Key key, String encryptedString)
		throws EncryptorException {

		byte[] encryptedBytes = Base64.decode(encryptedString);

		return _decryptUnencodedAsString(key, encryptedBytes);
	}

	@Override
	public String decryptAuthenticated(Key key, String encryptedString)
		throws EncryptorException {

		try {
			byte[] decryptedBytes = _decryptGCM(
				Base64.decode(encryptedString), key);

			return new String(decryptedBytes, ENCODING);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	@Override
	public byte[] decryptUnencodedAsBytes(Key key, byte[] encryptedBytes)
		throws EncryptorException {

		byte[] encodedBytes = key.getEncoded();

		FIPSModeValidator.validateKey(
			key.getAlgorithm(),
			(encodedBytes == null) ? 0 : encodedBytes.length * 8);

		if (PropsValues.FIPS_ENABLED) {
			return _decryptGCM(encryptedBytes, key);
		}

		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());

			cipher.init(Cipher.DECRYPT_MODE, key);

			return cipher.doFinal(encryptedBytes);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	@Override
	public Key deserializeKey(String base64String) {
		try {
			return CryptoUtil.createSecretKey(
				KEY_ALGORITHM, Base64.decode(base64String));
		}
		catch (InvalidKeyException invalidKeyException) {
			throw new SystemException(invalidKeyException);
		}
	}

	@Override
	public String encrypt(Key key, String plaintext) throws EncryptorException {
		if (key == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Skip encrypting based on a null key");
			}

			return plaintext;
		}

		byte[] encryptedBytes = encryptUnencoded(key, plaintext);

		return Base64.encode(encryptedBytes);
	}

	@Override
	public String encryptAuthenticated(Key key, String plaintext)
		throws EncryptorException {

		try {
			byte[] encryptedBytes = _encryptGCM(
				key, plaintext.getBytes(ENCODING));

			return Base64.encode(encryptedBytes);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	@Override
	public byte[] encryptUnencoded(Key key, byte[] plainBytes)
		throws EncryptorException {

		byte[] encodedBytes = key.getEncoded();

		FIPSModeValidator.validateKey(
			key.getAlgorithm(),
			(encodedBytes == null) ? 0 : encodedBytes.length * 8);

		if (PropsValues.FIPS_ENABLED) {
			return _encryptGCM(key, plainBytes);
		}

		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());

			cipher.init(Cipher.ENCRYPT_MODE, key);

			return cipher.doFinal(plainBytes);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	@Override
	public byte[] encryptUnencoded(Key key, String plaintext)
		throws EncryptorException {

		try {
			byte[] decryptedBytes = plaintext.getBytes(ENCODING);

			return encryptUnencoded(key, decryptedBytes);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	@Override
	public Key generateKey() throws EncryptorException {
		return _generateKey(KEY_ALGORITHM);
	}

	@Override
	public String serializeKey(Key key) {
		return Base64.encode(key.getEncoded());
	}

	private byte[] _decryptGCM(byte[] encryptedBytes, Key key)
		throws EncryptorException {

		try {
			byte[] cipherBytes = Arrays.copyOfRange(
				encryptedBytes, _GCM_INITIALIZATION_VECTOR_LENGTH,
				encryptedBytes.length);

			byte[] initializationVector = Arrays.copyOfRange(
				encryptedBytes, 0, _GCM_INITIALIZATION_VECTOR_LENGTH);

			Cipher cipher = CryptoUtil.createCipher(
				new GCMParameterSpec(
					_GCM_TAG_LENGTH_BITS, initializationVector),
				key, Cipher.DECRYPT_MODE, _GCM_TRANSFORMATION);

			return cipher.doFinal(cipherBytes);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	private String _decryptUnencodedAsString(Key key, byte[] encryptedBytes)
		throws EncryptorException {

		try {
			byte[] decryptedBytes = decryptUnencodedAsBytes(
				key, encryptedBytes);

			return new String(decryptedBytes, ENCODING);
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	private byte[] _encryptGCM(Key key, byte[] plainBytes)
		throws EncryptorException {

		try {
			Cipher cipher = CryptoUtil.createCipher(
				key, Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION);

			byte[] initializationVector = cipher.getIV();

			AlgorithmParameters algorithmParameters = cipher.getParameters();

			GCMParameterSpec gcmParameterSpec =
				algorithmParameters.getParameterSpec(GCMParameterSpec.class);

			if ((initializationVector.length !=
					_GCM_INITIALIZATION_VECTOR_LENGTH) ||
				(gcmParameterSpec.getTLen() != _GCM_TAG_LENGTH_BITS)) {

				throw new InvalidAlgorithmParameterException(
					StringBundler.concat(
						"Initialization vector length ",
						initializationVector.length, " and tag length ",
						gcmParameterSpec.getTLen(), " are not supported"));
			}

			byte[] cipherBytes = cipher.doFinal(plainBytes);

			byte[] encryptedBytes =
				new byte[initializationVector.length + cipherBytes.length];

			System.arraycopy(
				initializationVector, 0, encryptedBytes, 0,
				initializationVector.length);
			System.arraycopy(
				cipherBytes, 0, encryptedBytes, initializationVector.length,
				cipherBytes.length);

			return encryptedBytes;
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	private Key _generateKey(String algorithm) throws EncryptorException {
		try {
			KeyGenerator keyGenerator = CryptoUtil.createKeyGenerator(
				algorithm, KEY_SIZE);

			return keyGenerator.generateKey();
		}
		catch (Exception exception) {
			throw new EncryptorException(exception);
		}
	}

	private static final int _GCM_INITIALIZATION_VECTOR_LENGTH = 12;

	private static final int _GCM_TAG_LENGTH_BITS = 128;

	private static final String _GCM_TRANSFORMATION = "AES/GCM/NoPadding";

	private static final Log _log = LogFactoryUtil.getLog(EncryptorImpl.class);

}