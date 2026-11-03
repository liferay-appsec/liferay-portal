/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMData {

	public String addComputedReference(CryptoUse cryptoUse) {
		String reference =
			_COMPUTED_REFERENCE_PREFIX + _computedReferenceCount++;

		_referenceCryptoUses.put(reference, cryptoUse);

		return reference;
	}

	public void addCryptoUse(
		CryptoUse cryptoUse, CryptoUse keySizeCryptoUse,
		CryptoUse methodCryptoUse) {

		_cryptoUsesSuppliers.add(
			() -> _toCryptoUses(cryptoUse, keySizeCryptoUse, methodCryptoUse));
	}

	public void addReferenceCryptoUse(CryptoUse cryptoUse, String reference) {
		CryptoUse referenceCryptoUse = _referenceCryptoUses.get(reference);

		if (referenceCryptoUse == null) {
			_referenceCryptoUses.put(reference, cryptoUse);
		}
		else {
			referenceCryptoUse.addAll(cryptoUse);
		}
	}

	public Set<String> getBundleSymbolicNames() {
		Set<String> bundleSymbolicNames = new HashSet<>(
			_portalBundleSymbolicNames);

		bundleSymbolicNames.addAll(_settingBundleSymbolicNames);

		return bundleSymbolicNames;
	}

	public List<CryptoUse> getCryptoUses() {
		_scopeCryptoUses();

		return _cryptoUses;
	}

	public Set<String> getMessages() {
		_scopeCryptoUses();

		Set<String> messages = new TreeSet<>(_messages);

		for (String propertyKey : _unmatchedPropertyKeys) {
			messages.add(
				"The scan found no crypto call for \"" + propertyKey + "\"");
		}

		return messages;
	}

	public List<CryptoUse> getScannedCryptoUses() {
		List<CryptoUse> cryptoUses = new ArrayList<>();

		for (Supplier<List<CryptoUse>> cryptoUsesSupplier :
				_cryptoUsesSuppliers) {

			cryptoUses.addAll(cryptoUsesSupplier.get());
		}

		return cryptoUses;
	}

	private CryptoUse _resolve(
		CryptoUse cryptoUse, Set<String> visitedReferences) {

		CryptoUse resolvedCryptoUse = new CryptoUse();

		resolvedCryptoUse.addAll(cryptoUse);

		resolvedCryptoUse.clearReferences();

		for (String reference : cryptoUse.getReferences()) {
			if (!visitedReferences.add(reference)) {
				continue;
			}

			CryptoUse referenceCryptoUse = _referenceCryptoUses.get(reference);

			if (referenceCryptoUse == null) {
				resolvedCryptoUse.addUnresolvedReason(
					CBOMUtil.getUnresolvedReason(reference));

				continue;
			}

			CryptoUse resolvedReferenceCryptoUse = _resolve(
				referenceCryptoUse, visitedReferences);

			if (reference.startsWith(_COMPUTED_REFERENCE_PREFIX)) {
				resolvedReferenceCryptoUse = CBOMUtil.toComputedCryptoUse(
					resolvedReferenceCryptoUse);
			}

			resolvedCryptoUse.addAll(resolvedReferenceCryptoUse);
		}

		return resolvedCryptoUse;
	}

	private void _scopeCryptoUses() {
		if (_cryptoUses != null) {
			return;
		}

		_cryptoUses = new ArrayList<>();

		for (CryptoUse cryptoUse : getScannedCryptoUses()) {
			if (!cryptoUse.isResolved()) {
				_cryptoUses.add(cryptoUse);

				_messages.add(
					StringBundler.concat(
						"The scan cannot read the algorithm of the ",
						cryptoUse.getJCAClassName(), " call in ",
						cryptoUse.getClassName(), ", which comes from ",
						StringUtil.merge(
							cryptoUse.getUnresolvedReasons(), ", ")));

				continue;
			}

			if (cryptoUse.isConfigurable()) {
				List<String> configuredBy = cryptoUse.getConfiguredBy();

				String propertyKey = configuredBy.get(0);

				if (!_portalBundleSymbolicNames.contains(
						cryptoUse.getBundleSymbolicName()) &&
					!_settingPropertyKeys.contains(propertyKey)) {

					continue;
				}

				cryptoUse.setFeatureName(propertyKey);

				_cryptoUses.add(cryptoUse);

				_unmatchedPropertyKeys.remove(propertyKey);

				continue;
			}

			String standardAlgorithmName = CBOMUtil.getStandardAlgorithmName(
				cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName());

			cryptoUse.setAlgorithm(standardAlgorithmName);

			if (_portalBundleSymbolicNames.contains(
					cryptoUse.getBundleSymbolicName())) {

				cryptoUse.setFeatureName(
					StringBundler.concat(
						cryptoUse.getAlgorithm(), " in ",
						cryptoUse.getClassName()));

				_cryptoUses.add(cryptoUse);

				continue;
			}

			for (String propertyKey : cryptoUse.getSelectedBy()) {
				if (!_settingPropertyKeys.contains(propertyKey)) {
					continue;
				}

				CryptoUse featureCryptoUse = cryptoUse.createPart();

				featureCryptoUse.setAlgorithm(cryptoUse.getAlgorithm());
				featureCryptoUse.setFeatureName(propertyKey);
				featureCryptoUse.setKeySize(cryptoUse.getKeySize());

				_cryptoUses.add(featureCryptoUse);

				_unmatchedPropertyKeys.remove(propertyKey);
			}
		}
	}

	private List<CryptoUse> _toCryptoUses(
		CryptoUse cryptoUse, CryptoUse keySizeCryptoUse,
		CryptoUse methodCryptoUse) {

		CryptoUse resolvedAlgorithmCryptoUse = _resolve(
			cryptoUse, new HashSet<>());
		CryptoUse resolvedKeySizeCryptoUse = _resolve(
			keySizeCryptoUse, new HashSet<>());

		List<CryptoUse> cryptoUses = new ArrayList<>();

		SortedSet<String> keySizes = resolvedKeySizeCryptoUse.getValues();
		SortedSet<String> values = resolvedAlgorithmCryptoUse.getValues();

		Set<String> selectedBy = new TreeSet<>();

		if (!values.isEmpty()) {
			selectedBy.addAll(resolvedAlgorithmCryptoUse.getPropertyKeys());

			CryptoUse resolvedMethodCryptoUse = _resolve(
				methodCryptoUse, new HashSet<>());

			selectedBy.addAll(resolvedMethodCryptoUse.getPropertyKeys());
		}

		for (String value : values) {
			CryptoUse valueCryptoUse = cryptoUse.createPart();

			valueCryptoUse.setAlgorithm(value);
			valueCryptoUse.setSelectedBy(new ArrayList<>(selectedBy));

			if ((values.size() == 1) && resolvedKeySizeCryptoUse.isConstant() &&
				(keySizes.size() == 1)) {

				valueCryptoUse.setKeySize(
					GetterUtil.getInteger(keySizes.first()));
			}

			cryptoUses.add(valueCryptoUse);
		}

		for (String propertyKey :
				resolvedAlgorithmCryptoUse.getPropertyKeys()) {

			CryptoUse configuredCryptoUse = cryptoUse.createPart();

			List<String> configuredBy = new ArrayList<>();

			configuredBy.add(propertyKey);

			for (String keySizePropertyKey :
					resolvedKeySizeCryptoUse.getPropertyKeys()) {

				if (configuredBy.contains(keySizePropertyKey)) {
					continue;
				}

				configuredBy.add(keySizePropertyKey);
			}

			configuredCryptoUse.setConfiguredBy(configuredBy);

			cryptoUses.add(configuredCryptoUse);
		}

		Set<String> unresolvedReasons =
			resolvedAlgorithmCryptoUse.getUnresolvedReasons();

		if (!unresolvedReasons.isEmpty() || cryptoUses.isEmpty()) {
			CryptoUse unresolvedCryptoUse = cryptoUse.createPart();

			if (unresolvedReasons.isEmpty()) {
				unresolvedCryptoUse.setUnresolvedReasons(
					Collections.singletonList("a value the code never sets"));
			}
			else {
				unresolvedCryptoUse.setUnresolvedReasons(
					new ArrayList<>(unresolvedReasons));
			}

			cryptoUses.add(unresolvedCryptoUse);
		}

		return cryptoUses;
	}

	private static final String _COMPUTED_REFERENCE_PREFIX = "computed:";

	private static final Set<String> _portalBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.portal.impl", "com.liferay.portal.kernel");
	private static final Set<String> _settingBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.object.service", "com.liferay.portal.encryptor",
			"com.liferay.portal.security.key.impl",
			"com.liferay.portal.security.password.encryptor.impl");
	private static final Set<String> _settingPropertyKeys = SetUtil.fromArray(
		"auth.mac.algorithm", "company.encryption.algorithm",
		"object.encryption.algorithm", "passwords.encryption.algorithm",
		"passwords.encryption.algorithm.legacy",
		"tunneling.servlet.encryption.algorithm");

	private int _computedReferenceCount;
	private List<CryptoUse> _cryptoUses;
	private final List<Supplier<List<CryptoUse>>> _cryptoUsesSuppliers =
		new ArrayList<>();
	private final Set<String> _messages = new TreeSet<>();
	private final Map<String, CryptoUse> _referenceCryptoUses = new HashMap<>();
	private final Set<String> _unmatchedPropertyKeys = new TreeSet<>(
		_settingPropertyKeys);

}