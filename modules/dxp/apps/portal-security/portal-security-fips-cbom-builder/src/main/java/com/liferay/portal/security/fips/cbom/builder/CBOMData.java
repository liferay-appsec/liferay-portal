/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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

	public List<CryptoUse> getScannedCryptoUses() {
		return _scannedCryptoUses;
	}

	private void _scopeCryptoUses() {
		if (_cryptoUses != null) {
			return;
		}

		_cryptoUses = new ArrayList<>();

		for (CryptoUse cryptoUse : getScannedCryptoUses()) {
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

		List<CryptoUse> cryptoUses = new ArrayList<>();

		SortedSet<String> keySizes = keySizeCryptoUse.getValues();
		SortedSet<String> values = cryptoUse.getValues();

		for (String value : values) {
			CryptoUse valueCryptoUse = cryptoUse.createPart();

			valueCryptoUse.setAlgorithm(value);

			if ((values.size() == 1) && (keySizes.size() == 1)) {
				valueCryptoUse.setKeySize(
					GetterUtil.getInteger(keySizes.first()));
			}

			cryptoUses.add(valueCryptoUse);
		}

		return cryptoUses;
	}

	private static final Set<String> _portalBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.portal.impl", "com.liferay.portal.kernel");

	private List<CryptoUse> _cryptoUses;
	private final List<CryptoUse> _scannedCryptoUses = new ArrayList<>();

}