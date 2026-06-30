/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.password.encryptor.internal;

import com.liferay.petra.io.BigEndianCodec;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PwdEncryptorException;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptor;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import java.security.GeneralSecurityException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jodd.util.StringUtil;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Tomas Polesovsky
 */
@Component(
	property = "type=" + PasswordEncryptor.TYPE_PBKDF2,
	service = PasswordEncryptor.class
)
public class PBKDF2PasswordEncryptor implements PasswordEncryptor {

	@Override
	public String encrypt(
			String algorithm, String plainTextPassword,
			String encryptedPassword, boolean upgradeHashSecurity)
		throws PwdEncryptorException {

		if (upgradeHashSecurity) {
			encryptedPassword = null;
		}

		PBKDF2EncryptionConfiguration pbkdf2EncryptionConfiguration =
			new PBKDF2EncryptionConfiguration();

		pbkdf2EncryptionConfiguration.configure(algorithm, encryptedPassword);

		byte[] saltBytes = pbkdf2EncryptionConfiguration.getSaltBytes();

		byte[] secretKeyBytes = _generateDerivedKey(
			pbkdf2EncryptionConfiguration.getMacAlgorithm(),
			plainTextPassword.getBytes(), saltBytes,
			pbkdf2EncryptionConfiguration.getRounds(),
			pbkdf2EncryptionConfiguration.getKeySize());

		ByteBuffer byteBuffer = ByteBuffer.allocate(
			(2 * 4) + saltBytes.length + secretKeyBytes.length);

		byteBuffer.putInt(pbkdf2EncryptionConfiguration.getKeySize());
		byteBuffer.putInt(pbkdf2EncryptionConfiguration.getRounds());
		byteBuffer.put(saltBytes);
		byteBuffer.put(secretKeyBytes);

		return Base64.encode(byteBuffer.array());
	}

	@Override
	public String getEncryptedPasswordAlgorithmSettings(
		String encryptedPassword) {

		try {
			int index = encryptedPassword.indexOf(CharPool.CLOSE_CURLY_BRACE);

			if (index < 0) {
				return null;
			}

			PBKDF2EncryptionConfiguration pbkdf2EncryptionConfiguration =
				new PBKDF2EncryptionConfiguration();

			pbkdf2EncryptionConfiguration.configure(
				StringPool.BLANK, encryptedPassword.substring(index + 1));

			return StringBundler.concat(
				encryptedPassword.substring(1, index), StringPool.FORWARD_SLASH,
				pbkdf2EncryptionConfiguration.getKeySize(),
				StringPool.FORWARD_SLASH,
				pbkdf2EncryptionConfiguration.getRounds());
		}
		catch (PwdEncryptorException pwdEncryptorException) {
			return ReflectionUtil.throwException(pwdEncryptorException);
		}
	}

	private byte[] _generateDerivedKey(
			String macAlgorithm, byte[] passwordBytes, byte[] saltBytes,
			int rounds, int keySize)
		throws PwdEncryptorException {

		try {
			Mac mac = Mac.getInstance(macAlgorithm);

			mac.init(new SecretKeySpec(passwordBytes, macAlgorithm));

			int macLength = mac.getMacLength();

			int derivedKeyLength = keySize / 8;

			byte[] derivedKeyBytes = new byte[derivedKeyLength];

			byte[] blockBytes = new byte[saltBytes.length + 4];

			System.arraycopy(saltBytes, 0, blockBytes, 0, saltBytes.length);

			int blockCount = (int)Math.ceil(
				(double)derivedKeyLength / macLength);

			int offset = 0;

			for (int i = 1; i <= blockCount; i++) {
				blockBytes[saltBytes.length] = (byte)(i >>> 24);
				blockBytes[saltBytes.length + 1] = (byte)(i >>> 16);
				blockBytes[saltBytes.length + 2] = (byte)(i >>> 8);
				blockBytes[saltBytes.length + 3] = (byte)i;

				byte[] uBytes = mac.doFinal(blockBytes);

				byte[] tBytes = uBytes.clone();

				for (int j = 1; j < rounds; j++) {
					uBytes = mac.doFinal(uBytes);

					for (int k = 0; k < tBytes.length; k++) {
						tBytes[k] ^= uBytes[k];
					}
				}

				int length = Math.min(macLength, derivedKeyLength - offset);

				System.arraycopy(tBytes, 0, derivedKeyBytes, offset, length);

				offset += length;
			}

			return derivedKeyBytes;
		}
		catch (GeneralSecurityException generalSecurityException) {
			throw new PwdEncryptorException.InvalidAlgorithm(
				"Unable to derive key using " + macAlgorithm,
				generalSecurityException);
		}
	}

	private static final int _KEY_SIZE = 160;

	private static final int _ROUNDS = 1300000;

	private static final int _SALT_BYTES_LENGTH = 16;

	private static final Pattern _pattern = Pattern.compile(
		"^.*/?([0-9]+)?/([0-9]+)$");

	private static class PBKDF2EncryptionConfiguration {

		public void configure(String algorithm, String encryptedPassword)
			throws PwdEncryptorException {

			if (Validator.isNull(encryptedPassword)) {
				_saltBytes = new byte[_SALT_BYTES_LENGTH];

				Matcher matcher = _pattern.matcher(algorithm);

				if (matcher.matches()) {
					_keySize = GetterUtil.getInteger(
						matcher.group(1), _KEY_SIZE);

					_rounds = GetterUtil.getInteger(matcher.group(2), _ROUNDS);
				}

				BigEndianCodec.putLong(
					_saltBytes, 0, SecureRandomUtil.nextLong());
			}
			else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(
					Base64.decode(encryptedPassword));

				try {
					int length = byteBuffer.remaining();

					_keySize = byteBuffer.getInt();
					_rounds = byteBuffer.getInt();

					_saltBytes = new byte
						[length - (2 * 4) -
							(int)Math.ceil((double)_keySize / 8)];

					byteBuffer.get(_saltBytes);
				}
				catch (BufferUnderflowException bufferUnderflowException) {
					throw new PwdEncryptorException.InvalidEncryptedPwd(
						"Unable to extract salt from encrypted password",
						bufferUnderflowException);
				}
			}

			int index = StringUtil.indexOfIgnoreCase(algorithm, "SHA");

			if (index < 0) {
				return;
			}

			String[] parts = StringUtil.split(
				algorithm.substring(index), StringPool.FORWARD_SLASH);

			_macAlgorithm = "Hmac" + StringUtil.remove(parts[0], CharPool.DASH);
		}

		public int getKeySize() {
			return _keySize;
		}

		public String getMacAlgorithm() {
			return _macAlgorithm;
		}

		public int getRounds() {
			return _rounds;
		}

		public byte[] getSaltBytes() {
			return _saltBytes;
		}

		private int _keySize = _KEY_SIZE;
		private String _macAlgorithm = "HmacSHA1";
		private int _rounds = _ROUNDS;
		private byte[] _saltBytes;

	}

}