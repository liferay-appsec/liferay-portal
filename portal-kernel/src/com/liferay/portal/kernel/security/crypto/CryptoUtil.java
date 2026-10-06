/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.crypto;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditEventFactory;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Rafael Praxedes
 */
public class CryptoUtil {

	public static Cipher createCipher(
			AlgorithmParameterSpec algorithmParameterSpec, Key key, int mode,
			String transformation)
		throws InvalidAlgorithmParameterException, InvalidKeyException,
			   NoSuchAlgorithmException, NoSuchPaddingException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(
				transformation, CryptoFunctionApprovalType.APPROVED,
				Cipher.class);

			if ((mode == Cipher.ENCRYPT_MODE) &&
				_isAuthenticatedEncryption(transformation)) {

				_writeAlgorithmRejected(
					transformation, CryptoFunctionApprovalType.APPROVED, true,
					Cipher.class, null, "caller-supplied-parameters");

				throw new InvalidAlgorithmParameterException(
					"Caller supplied parameters are not allowed in FIPS mode " +
						"for authenticated encryption");
			}

			_validateKey(
				key.getAlgorithm(), CryptoFunctionApprovalType.APPROVED,
				Cipher.class, _getKeySize(key));
		}

		Cipher cipher = _getCipher(
			CryptoFunctionApprovalType.APPROVED, transformation);

		cipher.init(mode, key, algorithmParameterSpec);

		_recordProvider(
			transformation, CryptoFunctionApprovalType.APPROVED, Cipher.class,
			cipher.getProvider());

		return cipher;
	}

	public static Cipher createCipher(Key key, int mode, String transformation)
		throws InvalidKeyException, NoSuchAlgorithmException,
			   NoSuchPaddingException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(
				transformation, CryptoFunctionApprovalType.APPROVED,
				Cipher.class);
			_validateKey(
				key.getAlgorithm(), CryptoFunctionApprovalType.APPROVED,
				Cipher.class, _getKeySize(key));
		}

		Cipher cipher = _getCipher(
			CryptoFunctionApprovalType.APPROVED, transformation);

		cipher.init(mode, key);

		_recordProvider(
			transformation, CryptoFunctionApprovalType.APPROVED, Cipher.class,
			cipher.getProvider());

		return cipher;
	}

	public static KeyGenerator createKeyGenerator(String algorithm, int keySize)
		throws NoSuchAlgorithmException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(
				algorithm, CryptoFunctionApprovalType.APPROVED,
				KeyGenerator.class);

			keySize = _getGenerationKeySize(algorithm);
		}

		KeyGenerator keyGenerator = _getKeyGenerator(algorithm);

		keyGenerator.init(keySize);

		_recordProvider(
			algorithm, CryptoFunctionApprovalType.APPROVED, KeyGenerator.class,
			keyGenerator.getProvider());

		return keyGenerator;
	}

	public static KeyStore createKeyStore(
			CryptoFunctionApprovalType approvalType, String type)
		throws KeyStoreException {

		if (PropsValues.FIPS_ENABLED &&
			!FIPSModeValidator.isAllowedAlgorithm(
				approvalType, KeyStore.class, type)) {

			_writeAlgorithmRejected(
				type, approvalType, true, KeyStore.class, null,
				"type-not-allowed");

			throw new KeyStoreException(
				StringBundler.concat(
					"Keystore type \"", type,
					"\" is not allowed in FIPS mode"));
		}

		KeyStore keyStore = KeyStore.getInstance(type);

		_recordServed(
			type, approvalType, KeyStore.class, keyStore.getProvider());

		return keyStore;
	}

	public static KeyStore createKeyStore(String type)
		throws KeyStoreException {

		return createKeyStore(CryptoFunctionApprovalType.APPROVED, type);
	}

	public static Mac createMac(String algorithm, Key key)
		throws InvalidKeyException, NoSuchAlgorithmException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(
				algorithm, CryptoFunctionApprovalType.APPROVED, Mac.class);
			_validateKey(
				key.getAlgorithm(), CryptoFunctionApprovalType.APPROVED,
				Mac.class, _getKeySize(key));
		}

		Mac mac = _getMac(algorithm, CryptoFunctionApprovalType.APPROVED);

		mac.init(key);

		_recordProvider(
			algorithm, CryptoFunctionApprovalType.APPROVED, Mac.class,
			mac.getProvider());

		return mac;
	}

	public static MessageDigest createMessageDigest(String algorithm)
		throws NoSuchAlgorithmException {

		return createMessageDigest(
			algorithm, CryptoFunctionApprovalType.APPROVED);
	}

	public static MessageDigest createMessageDigest(
			String algorithm, CryptoFunctionApprovalType approvalType)
		throws NoSuchAlgorithmException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(algorithm, approvalType, MessageDigest.class);
		}

		MessageDigest messageDigest = _getMessageDigest(
			algorithm, approvalType);

		_recordProvider(
			algorithm, approvalType, MessageDigest.class,
			messageDigest.getProvider());

		return messageDigest;
	}

	public static SecretKey createSecretKey(String algorithm, byte[] keyBytes)
		throws InvalidKeyException {

		if (PropsValues.FIPS_ENABLED) {
			_validateKey(
				algorithm, CryptoFunctionApprovalType.APPROVED,
				SecretKeySpec.class, keyBytes.length * 8);
		}

		return new SecretKeySpec(keyBytes, algorithm);
	}

	public static String getAlgorithm(String algorithm, String fipsAlgorithm) {
		if (PropsValues.FIPS_ENABLED) {
			return fipsAlgorithm;
		}

		return algorithm;
	}

	public static boolean isAllowedAlgorithm(
		String algorithm, Class<?> factoryClass) {

		if (!PropsValues.FIPS_ENABLED) {
			return true;
		}

		return FIPSModeValidator.isAllowedAlgorithm(
			CryptoFunctionApprovalType.APPROVED, factoryClass, algorithm);
	}

	private static Throwable _fetchErrorThrowable(Throwable throwable) {
		while (throwable != null) {
			if (throwable instanceof Error) {
				return throwable;
			}

			throwable = throwable.getCause();
		}

		return null;
	}

	private static Provider _fetchPinnedProvider(
		CryptoFunctionApprovalType approvalType) {

		if (!PropsValues.FIPS_ENABLED ||
			(approvalType == CryptoFunctionApprovalType.ALLOWED)) {

			return null;
		}

		return FIPSModeValidator.fetchApprovedModeProvider();
	}

	private static Cipher _getCipher(
			CryptoFunctionApprovalType approvalType, String transformation)
		throws NoSuchAlgorithmException, NoSuchPaddingException {

		Provider provider = _fetchPinnedProvider(approvalType);

		if (provider == null) {
			return Cipher.getInstance(transformation);
		}

		try {
			return Cipher.getInstance(transformation, provider);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException exception) {
			_writeProviderException(
				transformation, approvalType, exception, Cipher.class,
				provider);

			throw exception;
		}
	}

	private static int _getGenerationKeySize(String algorithm) {
		if (StringUtil.equalsIgnoreCase(algorithm, "AES")) {
			return 256;
		}

		return 0;
	}

	private static KeyGenerator _getKeyGenerator(String algorithm)
		throws NoSuchAlgorithmException {

		Provider provider = _fetchPinnedProvider(
			CryptoFunctionApprovalType.APPROVED);

		if (provider == null) {
			return KeyGenerator.getInstance(algorithm);
		}

		try {
			return KeyGenerator.getInstance(algorithm, provider);
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			_writeProviderException(
				algorithm, CryptoFunctionApprovalType.APPROVED,
				noSuchAlgorithmException, KeyGenerator.class, provider);

			throw noSuchAlgorithmException;
		}
	}

	private static int _getKeySize(Key key) {
		byte[] encodedBytes = key.getEncoded();

		if (encodedBytes == null) {
			return 0;
		}

		Arrays.fill(encodedBytes, (byte)0);

		return encodedBytes.length * 8;
	}

	private static Mac _getMac(
			String algorithm, CryptoFunctionApprovalType approvalType)
		throws NoSuchAlgorithmException {

		Provider provider = _fetchPinnedProvider(approvalType);

		if (provider == null) {
			return Mac.getInstance(algorithm);
		}

		try {
			return Mac.getInstance(algorithm, provider);
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			_writeProviderException(
				algorithm, approvalType, noSuchAlgorithmException, Mac.class,
				provider);

			throw noSuchAlgorithmException;
		}
	}

	private static MessageDigest _getMessageDigest(
			String algorithm, CryptoFunctionApprovalType approvalType)
		throws NoSuchAlgorithmException {

		Provider provider = _fetchPinnedProvider(approvalType);

		if (provider == null) {
			return MessageDigest.getInstance(algorithm);
		}

		try {
			return MessageDigest.getInstance(algorithm, provider);
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			_writeProviderException(
				algorithm, approvalType, noSuchAlgorithmException,
				MessageDigest.class, provider);

			throw noSuchAlgorithmException;
		}
	}

	private static boolean _isAuthenticatedEncryption(String transformation) {
		String[] segments = StringUtil.split(transformation, CharPool.SLASH);

		if ((segments.length == 3) &&
			StringUtil.equalsIgnoreCase(segments[1], "GCM")) {

			return true;
		}

		return false;
	}

	private static boolean _isNewRecord(
		String algorithm, CryptoFunctionApprovalType approvalType,
		Class<?> factoryClass, String providerName, String reason) {

		String memoKey = StringBundler.concat(
			algorithm, StringPool.POUND, approvalType.name(), StringPool.POUND,
			factoryClass.getName(), StringPool.POUND, providerName,
			StringPool.POUND, reason);

		if (_memoKeys.size() < _MEMO_KEYS_MAX_SIZE) {
			return _memoKeys.add(memoKey);
		}

		if (_memoKeys.contains(memoKey) ||
			!_memoSaturated.compareAndSet(false, true)) {

			return false;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createAlgorithmRejected(
				approvalType.name(), false, factoryClass.getName(),
				StringPool.BLANK, StringPool.BLANK, "memo-saturated", null));

		return false;
	}

	private static void _recordProvider(
		String algorithm, CryptoFunctionApprovalType approvalType,
		Class<?> factoryClass, Provider provider) {

		if (approvalType == CryptoFunctionApprovalType.ALLOWED) {
			_recordServed(algorithm, approvalType, factoryClass, provider);

			return;
		}

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		Provider approvedModeProvider =
			FIPSModeValidator.fetchApprovedModeProvider();

		if ((approvedModeProvider != null) &&
			(approvedModeProvider != provider)) {

			_writeAlgorithmRejected(
				algorithm, approvalType, false, factoryClass, provider,
				"served-outside-module");
		}
	}

	private static void _recordServed(
		String algorithm, CryptoFunctionApprovalType approvalType,
		Class<?> factoryClass, Provider provider) {

		if (!PropsValues.FIPS_ENABLED ||
			(FIPSModeValidator.fetchApprovedModeProvider() == null) ||
			!_isNewRecord(
				algorithm, approvalType, factoryClass, provider.getName(),
				"algorithm-served")) {

			return;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createAlgorithmServed(
				approvalType.name(), factoryClass.getName(), provider.getName(),
				provider.getVersionStr(), algorithm));
	}

	private static void _validateAlgorithm(
			String algorithm, CryptoFunctionApprovalType approvalType,
			Class<?> factoryClass)
		throws NoSuchAlgorithmException {

		if (FIPSModeValidator.isAllowedAlgorithm(
				approvalType, factoryClass, algorithm)) {

			return;
		}

		_writeAlgorithmRejected(
			algorithm, approvalType, true, factoryClass, null,
			"algorithm-not-allowed");

		throw new NoSuchAlgorithmException(
			StringBundler.concat(
				"Algorithm \"", algorithm, "\" is not allowed in FIPS mode"));
	}

	private static void _validateKey(
			String keyAlgorithm, CryptoFunctionApprovalType approvalType,
			Class<?> factoryClass, int keySize)
		throws InvalidKeyException {

		if (!FIPSModeValidator.isAllowedKeyAlgorithm(
				approvalType, factoryClass, keyAlgorithm)) {

			_writeAlgorithmRejected(
				keyAlgorithm, approvalType, true, factoryClass, null,
				"key-algorithm-not-allowed");

			throw new InvalidKeyException(
				StringBundler.concat(
					"Key algorithm \"", keyAlgorithm,
					"\" is not allowed in FIPS mode"));
		}

		if ((keySize > 0) &&
			!FIPSModeValidator.isAllowedKeySize(keyAlgorithm, keySize)) {

			_writeAlgorithmRejected(
				keyAlgorithm, approvalType, true, factoryClass, null,
				"key-size-not-allowed");

			throw new InvalidKeyException(
				StringBundler.concat(
					"The key size is not allowed in FIPS mode for key ",
					"algorithm \"", keyAlgorithm, "\""));
		}
	}

	private static void _writeAlgorithmRejected(
		String algorithm, CryptoFunctionApprovalType approvalType,
		boolean enforced, Class<?> factoryClass, Provider provider,
		String rejectionReason) {

		String providerName = StringPool.BLANK;
		String providerVersion = StringPool.BLANK;

		if (provider != null) {
			providerName = provider.getName();
			providerVersion = provider.getVersionStr();
		}

		if ((algorithm != null) &&
			(algorithm.length() > _REQUESTED_ALGORITHM_MAX_LENGTH)) {

			algorithm = algorithm.substring(0, _REQUESTED_ALGORITHM_MAX_LENGTH);
		}

		if (!_isNewRecord(
				algorithm, approvalType, factoryClass, providerName,
				rejectionReason)) {

			return;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createAlgorithmRejected(
				approvalType.name(), enforced, factoryClass.getName(),
				providerName, providerVersion, rejectionReason, algorithm));
	}

	private static void _writeFIPSAuditEvent(FIPSAuditEvent fipsAuditEvent) {
		try {
			FIPSAuditUtil.write(fipsAuditEvent);
		}
		catch (Exception exception) {
			_log.error(
				StringBundler.concat(
					"Unable to write the FIPS audit event \"",
					fipsAuditEvent.getEventType(), "\""),
				exception);
		}
	}

	private static void _writeProviderException(
		String algorithm, CryptoFunctionApprovalType approvalType,
		Exception exception, Class<?> factoryClass, Provider provider) {

		Throwable throwable = _fetchErrorThrowable(exception);

		if ((throwable == null) &&
			FIPSModeValidator.isHealthyProvider(provider)) {

			_writeAlgorithmRejected(
				algorithm, approvalType, true, factoryClass, provider,
				"provider-refused");

			return;
		}

		if (!_isNewRecord(
				algorithm, approvalType, factoryClass, provider.getName(),
				"provider-failure")) {

			return;
		}

		if (throwable == null) {
			throwable = exception;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createProviderFailure(
				approvalType.name(), factoryClass.getName(),
				String.valueOf(throwable), provider.getName(),
				provider.getVersionStr(), algorithm));
	}

	private static final int _MEMO_KEYS_MAX_SIZE = 1024;

	private static final int _REQUESTED_ALGORITHM_MAX_LENGTH = 64;

	private static final Log _log = LogFactoryUtil.getLog(CryptoUtil.class);

	private static final Set<String> _memoKeys = ConcurrentHashMap.newKeySet();
	private static final AtomicBoolean _memoSaturated = new AtomicBoolean();

}