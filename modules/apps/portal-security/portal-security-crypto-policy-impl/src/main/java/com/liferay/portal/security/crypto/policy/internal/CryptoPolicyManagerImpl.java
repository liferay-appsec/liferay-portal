/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.internal;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.crypto.policy.CryptoPolicyManager;
import com.liferay.portal.security.crypto.policy.constants.CryptoServiceTypes;
import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Manuele Castro
 */
@Component(service = CryptoPolicyManager.class)
public class CryptoPolicyManagerImpl implements CryptoPolicyManager {

	@Override
	public void checkAlgorithm(String algorithm, String cryptoServiceType) {
		if (!PropsValues.FIPS_ENABLED ||
			_isValidAlgorithm(algorithm, cryptoServiceType)) {

			return;
		}

		throw new CryptoPolicyException(
			StringBundler.concat(
				"Algorithm ", algorithm, " is not approved in FIPS mode"));
	}

	@Override
	public void checkAlgorithm(
			String algorithm, String cryptoServiceType, int keySize)
		throws CryptoPolicyException {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		checkAlgorithm(algorithm, cryptoServiceType);

		Set<Integer> approvedKeySizes = _getApprovedKeySizes(
			_getBaseAlgorithm(algorithm, cryptoServiceType));

		if (!approvedKeySizes.isEmpty() &&
			!approvedKeySizes.contains(keySize)) {

			throw new CryptoPolicyException(
				StringBundler.concat(
					"Key size ", keySize, " for algorithm ", algorithm,
					" is not approved in FIPS mode"));
		}
	}

	@Override
	public String getApprovedAlgorithm(
			String algorithm, String cryptoServiceType,
			String fallbackAlgorithm)
		throws CryptoPolicyException {

		if (!PropsValues.FIPS_ENABLED ||
			_isValidAlgorithm(algorithm, cryptoServiceType)) {

			return algorithm;
		}

		checkAlgorithm(fallbackAlgorithm, cryptoServiceType);

		return fallbackAlgorithm;
	}

	@Override
	public List<String> getApprovedAlgorithms(
		List<String> algorithms, String cryptoServiceType) {

		if (!PropsValues.FIPS_ENABLED || ListUtil.isEmpty(algorithms)) {
			return algorithms;
		}

		Set<String> approvedAlgorithmsSet = _getApprovedAlgorithms(
			cryptoServiceType);

		List<String> approvedAlgorithmsList = new ArrayList<>();

		for (String algorithm : algorithms) {
			if (approvedAlgorithmsSet.contains(
					_getBaseAlgorithm(algorithm, cryptoServiceType))) {

				approvedAlgorithmsList.add(algorithm);
			}
		}

		return approvedAlgorithmsList;
	}

	@Override
	public List<Integer> getApprovedKeySizes(
		String algorithm, List<Integer> keySizes) {

		if (!PropsValues.FIPS_ENABLED || ListUtil.isEmpty(keySizes)) {
			return keySizes;
		}

		Set<Integer> approvedKeySizesSet = _getApprovedKeySizes(algorithm);

		if (approvedKeySizesSet.isEmpty()) {
			return Collections.emptyList();
		}

		List<Integer> approvedKeySizesList = new ArrayList<>();

		for (Integer keySize : keySizes) {
			if (approvedKeySizesSet.contains(keySize)) {
				approvedKeySizesList.add(keySize);
			}
		}

		return approvedKeySizesList;
	}

	private Set<String> _getApprovedAlgorithms(String serviceType) {
		return _approvedAlgorithms.getOrDefault(
			serviceType, Collections.emptySet());
	}

	private Set<Integer> _getApprovedKeySizes(String algorithm) {
		if (Validator.isNull(algorithm)) {
			return Collections.emptySet();
		}

		return _approvedKeySizes.getOrDefault(
			algorithm.trim(), Collections.emptySet());
	}

	private String _getBaseAlgorithm(String algorithm, String serviceType) {
		if (Validator.isNull(algorithm)) {
			return "";
		}

		algorithm = algorithm.trim();

		if (serviceType.equals(CryptoServiceTypes.CIPHER) ||
			serviceType.equals(CryptoServiceTypes.SECRET_KEY_FACTORY)) {

			int index = algorithm.indexOf('/');

			if (index >= 0) {
				return algorithm.substring(0, index);
			}
		}

		return algorithm;
	}

	private boolean _isValidAlgorithm(String algorithm, String serviceType) {
		if (Validator.isNull(algorithm) || Validator.isNull(serviceType)) {
			return false;
		}

		Set<String> approvedAlgorithms = _getApprovedAlgorithms(serviceType);

		return approvedAlgorithms.contains(
			_getBaseAlgorithm(algorithm, serviceType));
	}

	private static final Map<String, Set<String>> _approvedAlgorithms =
		HashMapBuilder.<String, Set<String>>put(
			CryptoServiceTypes.CIPHER, SetUtil.fromArray("AES")
		).put(
			CryptoServiceTypes.KEY_GENERATOR,
			SetUtil.fromArray(
				"AES", "HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384",
				"HmacSHA512")
		).put(
			CryptoServiceTypes.KEY_PAIR_GENERATOR,
			SetUtil.fromArray("EC", "Ed25519", "Ed448", "RSA")
		).put(
			CryptoServiceTypes.MAC,
			SetUtil.fromArray(
				"AESCMAC", "HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384",
				"HmacSHA512", "HmacSHA512/224", "HmacSHA512/256", "KMAC128",
				"KMAC256")
		).put(
			CryptoServiceTypes.MESSAGE_DIGEST,
			SetUtil.fromArray(
				"SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224",
				"SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512")
		).put(
			CryptoServiceTypes.SECRET_KEY_FACTORY,
			SetUtil.fromArray(
				"PBKDF2WithHmacSHA1", "PBKDF2WithHmacSHA256",
				"PBKDF2WithHmacSHA384", "PBKDF2WithHmacSHA512")
		).put(
			CryptoServiceTypes.SIGNATURE,
			SetUtil.fromArray(
				"Ed25519", "Ed448", "RSASSA-PSS", "SHA256withECDSA",
				"SHA256withRSA", "SHA384withECDSA", "SHA384withRSA",
				"SHA512withECDSA", "SHA512withRSA")
		).build();
	private static final Map<String, Set<Integer>> _approvedKeySizes =
		HashMapBuilder.<String, Set<Integer>>put(
			"AES", SetUtil.fromArray(128, 192, 256)
		).put(
			"EC", SetUtil.fromArray(256, 384, 521)
		).put(
			"RSA", SetUtil.fromArray(2048, 3072, 4096)
		).build();

}