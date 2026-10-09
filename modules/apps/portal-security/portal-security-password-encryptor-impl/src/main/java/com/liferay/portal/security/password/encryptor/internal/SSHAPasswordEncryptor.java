/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.password.encryptor.internal;

import com.liferay.petra.io.BigEndianCodec;
import com.liferay.portal.kernel.exception.PwdEncryptorException;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptor;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Tomas Polesovsky
 */
@Component(
	property = "type=" + PasswordEncryptor.TYPE_SSHA,
	service = PasswordEncryptor.class
)
public class SSHAPasswordEncryptor implements PasswordEncryptor {

	@Override
	public String encrypt(
			String algorithm, String plaintextPassword,
			String encryptedPassword, boolean upgradeHashSecurity)
		throws PwdEncryptorException {

		byte[] plaintextPasswordBytes = null;
		byte[] saltedPlaintextPasswordBytes = null;

		if (upgradeHashSecurity) {
			encryptedPassword = null;
		}

		CryptoFunctionApprovalType approvalType =
			CryptoFunctionApprovalType.LEGACY;

		if (Validator.isNull(encryptedPassword)) {
			approvalType = CryptoFunctionApprovalType.APPROVED;
		}

		try {
			byte[] encryptedPasswordBytes = _getEncryptedPasswordBytes(
				encryptedPassword);

			MessageDigest messageDigest = CryptoUtil.createMessageDigest(
				_getAlgorithm(encryptedPasswordBytes), approvalType);

			plaintextPasswordBytes = plaintextPassword.getBytes(
				DigesterUtil.ENCODING);

			byte[] saltBytes = getSaltBytes(encryptedPasswordBytes);

			saltedPlaintextPasswordBytes = ArrayUtil.append(
				plaintextPasswordBytes, saltBytes);

			byte[] messageDigestBytes = messageDigest.digest(
				saltedPlaintextPasswordBytes);

			return Base64.encode(
				ArrayUtil.append(messageDigestBytes, saltBytes));
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			throw new PwdEncryptorException.InvalidAlgorithm(
				noSuchAlgorithmException.getMessage(),
				noSuchAlgorithmException);
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new PwdEncryptorException.UnsupportedEncoding(
				unsupportedEncodingException.getMessage(),
				unsupportedEncodingException);
		}
		finally {
			if (plaintextPasswordBytes != null) {
				Arrays.fill(plaintextPasswordBytes, (byte)0);
			}

			if (saltedPlaintextPasswordBytes != null) {
				Arrays.fill(saltedPlaintextPasswordBytes, (byte)0);
			}
		}
	}

	protected byte[] getSaltBytes(byte[] encryptedPasswordBytes) {
		byte[] saltBytes = new byte[_SALT_SIZE];

		if (encryptedPasswordBytes == null) {
			BigEndianCodec.putLong(saltBytes, 0, SecureRandomUtil.nextLong());
		}
		else {
			System.arraycopy(
				encryptedPasswordBytes,
				encryptedPasswordBytes.length - _SALT_SIZE, saltBytes, 0,
				saltBytes.length);
		}

		return saltBytes;
	}

	private String _getAlgorithm(byte[] encryptedPasswordBytes)
		throws PwdEncryptorException {

		if (encryptedPasswordBytes == null) {
			if (PropsValues.FIPS_ENABLED) {
				return DigesterUtil.SHA_256;
			}

			return DigesterUtil.SHA_1;
		}

		int digestSize = encryptedPasswordBytes.length - _SALT_SIZE;

		if (digestSize == 20) {
			return DigesterUtil.SHA_1;
		}
		else if (digestSize == 32) {
			return DigesterUtil.SHA_256;
		}
		else if (digestSize == 48) {
			return "SHA-384";
		}
		else if (digestSize == 64) {
			return "SHA-512";
		}

		throw new PwdEncryptorException.InvalidEncryptedPwd(
			"Unable to determine the digest of an encrypted password of " +
				encryptedPasswordBytes.length + " bytes",
			null);
	}

	private byte[] _getEncryptedPasswordBytes(String encryptedPassword)
		throws PwdEncryptorException {

		if (Validator.isNull(encryptedPassword)) {
			return null;
		}

		try {
			byte[] encryptedPasswordBytes = Base64.decode(encryptedPassword);

			if (encryptedPasswordBytes.length <= _SALT_SIZE) {
				throw new IllegalArgumentException(
					"The encrypted password has no salt");
			}

			return encryptedPasswordBytes;
		}
		catch (Exception exception) {
			throw new PwdEncryptorException.InvalidEncryptedPwd(
				"Unable to extract salt from encrypted password " +
					exception.getMessage(),
				exception);
		}
	}

	private static final int _SALT_SIZE = 8;

}