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
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

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
			_validateAlgorithm(transformation, Cipher.class);

			if ((mode == Cipher.ENCRYPT_MODE) &&
				_isAuthenticatedEncryption(transformation)) {

				_writeAlgorithmRejected(
					transformation, true, Cipher.class, null,
					"caller-supplied-parameters");

				throw new InvalidAlgorithmParameterException(
					"Caller supplied parameters are not allowed in FIPS mode " +
						"for authenticated encryption");
			}

			_validateKey(key.getAlgorithm(), Cipher.class, _getKeySize(key));
		}

		Cipher cipher = _getCipher(transformation);

		cipher.init(mode, key, algorithmParameterSpec);

		_recordProvider(transformation, Cipher.class, cipher.getProvider());

		return cipher;
	}

	public static Cipher createCipher(Key key, int mode, String transformation)
		throws InvalidKeyException, NoSuchAlgorithmException,
			   NoSuchPaddingException {

		if (PropsValues.FIPS_ENABLED) {
			_validateAlgorithm(transformation, Cipher.class);
			_validateKey(key.getAlgorithm(), Cipher.class, _getKeySize(key));
		}

		Cipher cipher = _getCipher(transformation);

		cipher.init(mode, key);

		_recordProvider(transformation, Cipher.class, cipher.getProvider());

		return cipher;
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

	private static Provider _fetchPinnedProvider() {
		if (!PropsValues.FIPS_ENABLED) {
			return null;
		}

		return FIPSModeValidator.fetchApprovedModeProvider();
	}

	private static Cipher _getCipher(String transformation)
		throws NoSuchAlgorithmException, NoSuchPaddingException {

		Provider provider = _fetchPinnedProvider();

		if (provider == null) {
			return Cipher.getInstance(transformation);
		}

		try {
			return Cipher.getInstance(transformation, provider);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException exception) {
			_writeProviderException(
				transformation, exception, Cipher.class, provider);

			throw exception;
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

	private static boolean _isAuthenticatedEncryption(String transformation) {
		String[] segments = StringUtil.split(transformation, CharPool.SLASH);

		if ((segments.length == 3) &&
			StringUtil.equalsIgnoreCase(segments[1], "GCM")) {

			return true;
		}

		return false;
	}

	private static boolean _isNewRecord(
		String algorithm, Class<?> factoryClass, String providerName,
		String reason) {

		String memoKey = StringBundler.concat(
			algorithm, StringPool.POUND, factoryClass.getName(),
			StringPool.POUND, providerName, StringPool.POUND, reason);

		if (_memoKeys.size() < _MEMO_KEYS_MAX_SIZE) {
			return _memoKeys.add(memoKey);
		}

		if (_memoKeys.contains(memoKey) ||
			!_memoSaturated.compareAndSet(false, true)) {

			return false;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createAlgorithmRejected(
				false, factoryClass.getName(), StringPool.BLANK,
				StringPool.BLANK, "memo-saturated", null));

		return false;
	}

	private static void _recordProvider(
		String algorithm, Class<?> factoryClass, Provider provider) {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		Provider approvedModeProvider =
			FIPSModeValidator.fetchApprovedModeProvider();

		if ((approvedModeProvider != null) &&
			(approvedModeProvider != provider)) {

			_writeAlgorithmRejected(
				algorithm, false, factoryClass, provider,
				"served-outside-module");
		}
	}

	private static void _validateAlgorithm(
			String algorithm, Class<?> factoryClass)
		throws NoSuchAlgorithmException {

		if (FIPSModeValidator.isAllowedAlgorithm(factoryClass, algorithm)) {
			return;
		}

		_writeAlgorithmRejected(
			algorithm, true, factoryClass, null, "algorithm-not-allowed");

		throw new NoSuchAlgorithmException(
			StringBundler.concat(
				"Algorithm \"", algorithm, "\" is not allowed in FIPS mode"));
	}

	private static void _validateKey(
			String keyAlgorithm, Class<?> factoryClass, int keySize)
		throws InvalidKeyException {

		if (!FIPSModeValidator.isAllowedKeyAlgorithm(
				factoryClass, keyAlgorithm)) {

			_writeAlgorithmRejected(
				keyAlgorithm, true, factoryClass, null,
				"key-algorithm-not-allowed");

			throw new InvalidKeyException(
				StringBundler.concat(
					"Key algorithm \"", keyAlgorithm,
					"\" is not allowed in FIPS mode"));
		}

		if ((keySize > 0) &&
			!FIPSModeValidator.isAllowedKeySize(keyAlgorithm, keySize)) {

			_writeAlgorithmRejected(
				keyAlgorithm, true, factoryClass, null, "key-size-not-allowed");

			throw new InvalidKeyException(
				StringBundler.concat(
					"The key size is not allowed in FIPS mode for key ",
					"algorithm \"", keyAlgorithm, "\""));
		}
	}

	private static void _writeAlgorithmRejected(
		String algorithm, boolean enforced, Class<?> factoryClass,
		Provider provider, String rejectionReason) {

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
				algorithm, factoryClass, providerName, rejectionReason)) {

			return;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createAlgorithmRejected(
				enforced, factoryClass.getName(), providerName, providerVersion,
				rejectionReason, algorithm));
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
		String algorithm, Exception exception, Class<?> factoryClass,
		Provider provider) {

		Throwable throwable = _fetchErrorThrowable(exception);

		if ((throwable == null) &&
			FIPSModeValidator.isHealthyProvider(provider)) {

			_writeAlgorithmRejected(
				algorithm, true, factoryClass, provider, "provider-refused");

			return;
		}

		if (!_isNewRecord(
				algorithm, factoryClass, provider.getName(),
				"provider-failure")) {

			return;
		}

		if (throwable == null) {
			throwable = exception;
		}

		_writeFIPSAuditEvent(
			FIPSAuditEventFactory.createProviderFailure(
				factoryClass.getName(), String.valueOf(throwable),
				provider.getName(), provider.getVersionStr(), algorithm));
	}

	private static final int _MEMO_KEYS_MAX_SIZE = 1024;

	private static final int _REQUESTED_ALGORITHM_MAX_LENGTH = 64;

	private static final Log _log = LogFactoryUtil.getLog(CryptoUtil.class);

	private static final Set<String> _memoKeys = ConcurrentHashMap.newKeySet();
	private static final AtomicBoolean _memoSaturated = new AtomicBoolean();

}