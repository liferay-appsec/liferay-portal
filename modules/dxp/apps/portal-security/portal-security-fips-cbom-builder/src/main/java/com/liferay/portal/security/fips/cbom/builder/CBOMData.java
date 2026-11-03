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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMData {

	public void addCryptoUse(CryptoUse cryptoUse, CryptoUse keySizeCryptoUse) {
		_scannedCryptoUses.addAll(_toCryptoUses(cryptoUse, keySizeCryptoUse));
	}

	public Set<String> getBundleSymbolicNames() {
		return new HashSet<>(_portalBundleSymbolicNames);
	}

	public List<CryptoUse> getCryptoUses() {
		_scopeCryptoUses();

		return _cryptoUses;
	}

	public Set<String> getMessages() {
		_scopeCryptoUses();

		return _messages;
	}

	public List<CryptoUse> getScannedCryptoUses() {
		return _scannedCryptoUses;
	}

	private CryptoUse _resolve(CryptoUse cryptoUse) {
		CryptoUse resolvedCryptoUse = new CryptoUse();

		resolvedCryptoUse.addAll(cryptoUse);

		resolvedCryptoUse.clearReferences();

		for (String reference : cryptoUse.getReferences()) {
			resolvedCryptoUse.addUnresolvedReason(
				CBOMUtil.getUnresolvedReason(reference));
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

			if (!_portalBundleSymbolicNames.contains(
					cryptoUse.getBundleSymbolicName())) {

				continue;
			}

			String standardAlgorithmName = CBOMUtil.getStandardAlgorithmName(
				cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName());

			cryptoUse.setAlgorithm(standardAlgorithmName);

			cryptoUse.setFeatureName(
				StringBundler.concat(
					cryptoUse.getAlgorithm(), " in ",
					cryptoUse.getClassName()));

			_cryptoUses.add(cryptoUse);
		}
	}

	private List<CryptoUse> _toCryptoUses(
		CryptoUse cryptoUse, CryptoUse keySizeCryptoUse) {

		CryptoUse resolvedAlgorithmCryptoUse = _resolve(cryptoUse);
		CryptoUse resolvedKeySizeCryptoUse = _resolve(keySizeCryptoUse);

		List<CryptoUse> cryptoUses = new ArrayList<>();

		SortedSet<String> keySizes = resolvedKeySizeCryptoUse.getValues();
		SortedSet<String> values = resolvedAlgorithmCryptoUse.getValues();

		for (String value : values) {
			CryptoUse valueCryptoUse = cryptoUse.createPart();

			valueCryptoUse.setAlgorithm(value);

			if ((values.size() == 1) && resolvedKeySizeCryptoUse.isConstant() &&
				(keySizes.size() == 1)) {

				valueCryptoUse.setKeySize(
					GetterUtil.getInteger(keySizes.first()));
			}

			cryptoUses.add(valueCryptoUse);
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

	private static final Set<String> _portalBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.portal.impl", "com.liferay.portal.kernel");

	private List<CryptoUse> _cryptoUses;
	private final Set<String> _messages = new TreeSet<>();
	private final List<CryptoUse> _scannedCryptoUses = new ArrayList<>();

}