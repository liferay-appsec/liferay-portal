/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.SetUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMData {

	public void addCryptoUse(CryptoUse cryptoUse) {
		_scannedCryptoUses.addAll(_toCryptoUses(cryptoUse));
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

			cryptoUse.setFeatureName(
				StringBundler.concat(
					cryptoUse.getAlgorithm(), " in ",
					cryptoUse.getClassName()));

			_cryptoUses.add(cryptoUse);
		}
	}

	private List<CryptoUse> _toCryptoUses(CryptoUse cryptoUse) {
		List<CryptoUse> cryptoUses = new ArrayList<>();

		for (String value : cryptoUse.getValues()) {
			CryptoUse valueCryptoUse = cryptoUse.createPart();

			valueCryptoUse.setAlgorithm(value);

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