/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.bootstrap.fips;

import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsValues;

import java.nio.charset.StandardCharsets;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Caio Farias
 */
public class FIPSCryptoChecker {

	public static void run() {
		Provider provider = Security.getProvider(
			PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_NAME);

		if (provider == null) {
			throw new SecurityException(
				"FIPS provider not found: " +
					PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_NAME);
		}

		_runKnownAnswerTest("_checkAESCBC", () -> _checkAESCBC(provider));
		_runKnownAnswerTest("_checkAESGCM", () -> _checkAESGCM(provider));
		_runRejectionCheck(
			"DES/CBC/PKCS5Padding", () -> _checkDESRejected(provider));
		_runRejectionCheck(
			"DESede/CBC/PKCS5Padding",
			() -> _checkDESEdeRejected(provider));
		_runKnownAnswerTest("_checkECDSA", () -> _checkECDSA(provider));
		_runRejectionCheck(
			"EC secp192r1", () -> _checkECWeakKeyRejected(provider));
		_runKnownAnswerTest(
			"_checkHmacSHA256", () -> _checkHmacSHA256(provider));
		_runRejectionCheck("MD5", () -> _checkMD5Rejected(provider));
		_runKnownAnswerTest("_checkPBKDF2", () -> _checkPBKDF2(provider));
		_runKnownAnswerTest("_checkRSA", () -> _checkRSA(provider));
		_runRejectionCheck(
			"RSA 1024-bit", () -> _checkRSAWeakKeyRejected(provider));
		_runRejectionCheck(
			"SHA1withRSA", () -> _checkSHA1withRSARejected(provider));
		_runKnownAnswerTest("_checkSHA256", () -> _checkSHA256(provider));
		_runKnownAnswerTest(
			"_checkSecureRandom", () -> _checkSecureRandom(provider));

		if (_log.isInfoEnabled()) {
			_log.info("FIPS known-answer tests passed");
		}
	}

	private static void _checkAESCBC(Provider provider) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(_AES_CBC_KEY, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", provider);

		cipher.init(
			Cipher.ENCRYPT_MODE, secretKeySpec,
			new IvParameterSpec(_AES_CBC_IV));

		byte[] ciphertext = cipher.doFinal(_AES_CBC_PLAINTEXT);

		if (!Arrays.equals(_AES_CBC_EXPECTED_CIPHERTEXT, ciphertext)) {
			throw new SecurityException(
				"AES/CBC output did not match the NIST SP 800-38A test vector");
		}
	}

	private static void _checkAESGCM(Provider provider) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(_AES_GCM_KEY, "AES");

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", provider);

		cipher.init(
			Cipher.ENCRYPT_MODE, secretKeySpec,
			new GCMParameterSpec(128, _AES_GCM_IV));

		byte[] ciphertext = cipher.doFinal(_AES_GCM_PLAINTEXT);

		if (!Arrays.equals(_AES_GCM_EXPECTED_CIPHERTEXT, ciphertext)) {
			throw new SecurityException(
				"AES/GCM output did not match the NIST SP 800-38D test vector");
		}
	}

	private static void _checkDESEdeRejected(Provider provider)
		throws Exception {

		Cipher.getInstance("DESede/CBC/PKCS5Padding", provider);
	}

	private static void _checkDESRejected(Provider provider) throws Exception {
		Cipher.getInstance("DES/CBC/PKCS5Padding", provider);
	}

	private static void _checkECDSA(Provider provider) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			"EC", provider);

		keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		Signature signature = Signature.getInstance(
			"SHA256withECDSA", provider);

		signature.initSign(keyPair.getPrivate());

		signature.update(_ECDSA_SIGN_DATA);

		byte[] signed = signature.sign();

		signature.initVerify(keyPair.getPublic());

		signature.update(_ECDSA_SIGN_DATA);

		if (!signature.verify(signed)) {
			throw new SecurityException(
				"ECDSA SHA256withECDSA sign/verify self-test failed");
		}
	}

	private static void _checkECWeakKeyRejected(Provider provider)
		throws Exception {

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			"EC", provider);

		keyPairGenerator.initialize(new ECGenParameterSpec("secp192r1"));

		keyPairGenerator.generateKeyPair();
	}

	private static void _checkHmacSHA256(Provider provider) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256", provider);

		mac.init(new SecretKeySpec(_HMAC_SHA_256_KEY, "HmacSHA256"));

		byte[] actual = mac.doFinal(_HMAC_SHA_256_MESSAGE);

		if (!Arrays.equals(_HMAC_SHA_256_EXPECTED_MAC, actual)) {
			throw new SecurityException(
				"HmacSHA256 output did not match the FIPS 198-1 test vector");
		}
	}

	private static void _checkMD5Rejected(Provider provider) throws Exception {
		MessageDigest.getInstance("MD5", provider);
	}

	private static void _checkPBKDF2(Provider provider) throws Exception {
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(
			"PBKDF2WithHmacSHA256", provider);

		PBEKeySpec pbeKeySpec = new PBEKeySpec(
			_PBKDF2_PASSWORD, _PBKDF2_SALT, 1, 256);

		try {
			SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

			byte[] actual = secretKey.getEncoded();

			if (!Arrays.equals(_PBKDF2_EXPECTED_DK, actual)) {
				throw new SecurityException(
					"PBKDF2WithHmacSHA256 output did not match expected test " +
						"vector");
			}
		}
		finally {
			pbeKeySpec.clearPassword();
		}
	}

	private static void _checkRSA(Provider provider) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			"RSA", provider);

		keyPairGenerator.initialize(2048);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		Signature signature = Signature.getInstance("SHA256withRSA", provider);

		signature.initSign(keyPair.getPrivate());

		signature.update(_RSA_SIGN_DATA);

		byte[] signed = signature.sign();

		signature.initVerify(keyPair.getPublic());

		signature.update(_RSA_SIGN_DATA);

		if (!signature.verify(signed)) {
			throw new SecurityException(
				"RSA SHA256withRSA sign/verify self-test failed");
		}
	}

	private static void _checkRSAWeakKeyRejected(Provider provider)
		throws Exception {

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			"RSA", provider);

		keyPairGenerator.initialize(1024);

		keyPairGenerator.generateKeyPair();
	}

	private static void _checkSHA1withRSARejected(Provider provider)
		throws Exception {

		Signature.getInstance("SHA1withRSA", provider);
	}

	private static void _checkSHA256(Provider provider) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance(
			"SHA-256", provider);

		byte[] actual = messageDigest.digest(new byte[0]);

		if (!Arrays.equals(_SHA_256_EMPTY_INPUT_EXPECTED_DIGEST, actual)) {
			throw new SecurityException(
				"SHA-256 of empty input did not match FIPS 180-4 canonical " +
					"value");
		}
	}

	private static void _checkSecureRandom(Provider provider) throws Exception {
		Set<Provider.Service> providerServices = provider.getServices();

		Stream<Provider.Service> providerServicesStream =
			providerServices.stream();

		Optional<Provider.Service> optional = providerServicesStream.filter(
			service -> "SecureRandom".equals(service.getType())
		).findFirst();

		if (!optional.isPresent()) {
			throw new SecurityException(
				"FIPS provider has no SecureRandom service");
		}

		Provider.Service providerService = optional.get();

		SecureRandom secureRandom = SecureRandom.getInstance(
			providerService.getAlgorithm(), provider);

		byte[] bytes = new byte[32];

		secureRandom.nextBytes(bytes);

		for (byte b : bytes) {
			if (b != 0) {
				return;
			}
		}

		throw new SecurityException("FIPS DRBG produced all-zero output");
	}

	private static void _runKnownAnswerTest(
		String operation, UnsafeRunnable<Exception> kat) {

		try {
			kat.run();

			if (_log.isInfoEnabled()) {
				_log.info("FIPS KAT " + operation + " passed");
			}
		}
		catch (Exception exception) {
			String message = StringBundler.concat(
				"FIPS KAT ", operation, " failed. ", exception.getMessage());

			throw new SecurityException(message, exception);
		}
	}

	private static void _runRejectionCheck(
		String algorithm, UnsafeRunnable<Exception> check) {

		boolean rejected = false;

		try {
			check.run();
		}
		catch (Exception exception) {
			rejected = true;

			if (_log.isInfoEnabled()) {
				_log.info(
					"FIPS rejection check " + algorithm + " passed");
			}
		}

		if (!rejected) {
			throw new SecurityException(
				"FIPS provider accepted non-FIPS algorithm: " + algorithm);
		}
	}

	// NIST SP 800-38A, F.2.1 — AES-128-CBC, single block

	private static final byte[] _AES_CBC_EXPECTED_CIPHERTEXT = {
		(byte)0x76, (byte)0x49, (byte)0xAB, (byte)0xAC, (byte)0x81, (byte)0x19,
		(byte)0xB2, (byte)0x46, (byte)0xCE, (byte)0xE9, (byte)0x8E, (byte)0x9B,
		(byte)0x12, (byte)0xE9, (byte)0x19, (byte)0x7D
	};

	private static final byte[] _AES_CBC_IV = {
		0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,
		0x0C, 0x0D, 0x0E, 0x0F
	};

	private static final byte[] _AES_CBC_KEY = {
		(byte)0x2B, (byte)0x7E, (byte)0x15, (byte)0x16, (byte)0x28, (byte)0xAE,
		(byte)0xD2, (byte)0xA6, (byte)0xAB, (byte)0xF7, (byte)0x15, (byte)0x88,
		(byte)0x09, (byte)0xCF, (byte)0x4F, (byte)0x3C
	};

	private static final byte[] _AES_CBC_PLAINTEXT = {
		(byte)0x6B, (byte)0xC1, (byte)0xBE, (byte)0xE2, (byte)0x2E, (byte)0x40,
		(byte)0x9F, (byte)0x96, (byte)0xE9, (byte)0x3D, (byte)0x7E, (byte)0x11,
		(byte)0x73, (byte)0x93, (byte)0x17, (byte)0x2A
	};

	// NIST SP 800-38D, Test Case 2 — K=00*16, IV=00*12, P=00*16, A=empty
	// C=0388dace60b6a392f328c2b971b2fe78, T=ab6e47d42cec13bdf53a67b21257bddf

	private static final byte[] _AES_GCM_EXPECTED_CIPHERTEXT = {
		(byte)0x03, (byte)0x88, (byte)0xDA, (byte)0xCE, (byte)0x60, (byte)0xB6,
		(byte)0xA3, (byte)0x92, (byte)0xF3, (byte)0x28, (byte)0xC2, (byte)0xB9,
		(byte)0x71, (byte)0xB2, (byte)0xFE, (byte)0x78, (byte)0xAB, (byte)0x6E,
		(byte)0x47, (byte)0xD4, (byte)0x2C, (byte)0xEC, (byte)0x13, (byte)0xBD,
		(byte)0xF5, (byte)0x3A, (byte)0x67, (byte)0xB2, (byte)0x12, (byte)0x57,
		(byte)0xBD, (byte)0xDF
	};

	private static final byte[] _AES_GCM_IV = new byte[12];

	private static final byte[] _AES_GCM_KEY = new byte[16];

	private static final byte[] _AES_GCM_PLAINTEXT = new byte[16];

	private static final byte[] _ECDSA_SIGN_DATA =
		"FIPS-KAT-ECDSA-DATA".getBytes(StandardCharsets.UTF_8);

	private static final byte[] _HMAC_SHA_256_EXPECTED_MAC = {
		(byte)0x0E, (byte)0x71, (byte)0x9C, (byte)0x66, (byte)0xC8, (byte)0x35,
		(byte)0x3F, (byte)0x1C, (byte)0xB9, (byte)0x22, (byte)0x6B, (byte)0xD3,
		(byte)0xF1, (byte)0x35, (byte)0xFD, (byte)0x48, (byte)0xB8, (byte)0x88,
		(byte)0x35, (byte)0xDC, (byte)0x3D, (byte)0x91, (byte)0x51, (byte)0xE6,
		(byte)0x58, (byte)0x8B, (byte)0xB7, (byte)0xEE, (byte)0x76, (byte)0x24,
		(byte)0xF3, (byte)0xEB
	};

	private static final byte[] _HMAC_SHA_256_KEY =
		"FIPS-KAT-HMAC-KEY".getBytes(StandardCharsets.UTF_8);

	private static final byte[] _HMAC_SHA_256_MESSAGE =
		"FIPS-KAT-HMAC-MESSAGE".getBytes(StandardCharsets.UTF_8);

	// RFC 7914 — PBKDF2-HMAC-SHA256("password", "salt", 1, 32)

	private static final byte[] _PBKDF2_EXPECTED_DK = {
		(byte)0x12, (byte)0x0F, (byte)0xB6, (byte)0xCF, (byte)0xFC, (byte)0xCD,
		(byte)0x20, (byte)0x21, (byte)0x59, (byte)0x08, (byte)0x6B, (byte)0x6B,
		(byte)0xFC, (byte)0x52, (byte)0x70, (byte)0x82, (byte)0xB5, (byte)0x0E,
		(byte)0x1A, (byte)0x4E, (byte)0x0B, (byte)0xB8, (byte)0x5D, (byte)0x0B,
		(byte)0x6C, (byte)0x6A, (byte)0x2C, (byte)0xED, (byte)0xD9, (byte)0xC9,
		(byte)0xD3, (byte)0x9B
	};

	private static final char[] _PBKDF2_PASSWORD = "password".toCharArray();

	private static final byte[] _PBKDF2_SALT =
		"salt".getBytes(StandardCharsets.UTF_8);

	private static final byte[] _RSA_SIGN_DATA = "FIPS-KAT-RSA-DATA".getBytes(
		StandardCharsets.UTF_8);

	private static final byte[] _SHA_256_EMPTY_INPUT_EXPECTED_DIGEST = {
		(byte)0xE3, (byte)0xB0, (byte)0xC4, (byte)0x42, (byte)0x98, (byte)0xFC,
		(byte)0x1C, (byte)0x14, (byte)0x9A, (byte)0xFB, (byte)0xF4, (byte)0xC8,
		(byte)0x99, (byte)0x6F, (byte)0xB9, (byte)0x24, (byte)0x27, (byte)0xAE,
		(byte)0x41, (byte)0xE4, (byte)0x64, (byte)0x9B, (byte)0x93, (byte)0x4C,
		(byte)0xA4, (byte)0x95, (byte)0x99, (byte)0x1B, (byte)0x78, (byte)0x52,
		(byte)0xB8, (byte)0x55
	};

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSCryptoChecker.class);

}
