/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CryptoUse {

	public CryptoUse() {
		this(null, null, null, null);
	}

	public CryptoUse(
		String bundleSymbolicName, String className, String featureName,
		String jcaClassName) {

		_bundleSymbolicName = bundleSymbolicName;
		_className = className;
		_featureName = featureName;
		_jcaClassName = jcaClassName;
	}

	public void addAll(CryptoUse cryptoUse) {
		_values.addAll(cryptoUse._values);
	}

	public void addValue(String value) {
		_values.add(value);
	}

	public CryptoUse createPart() {
		CryptoUse cryptoUse = new CryptoUse(
			_bundleSymbolicName, _className, _featureName, _jcaClassName);

		cryptoUse.setCryptoFunctions(_cryptoFunctions);
		cryptoUse.setRelatedCryptoMaterialTypes(_relatedCryptoMaterialTypes);

		return cryptoUse;
	}

	public String getAlgorithm() {
		return _algorithm;
	}

	public String getBundleSymbolicName() {
		return _bundleSymbolicName;
	}

	public String getClassName() {
		return _className;
	}

	public List<CryptoFunction> getCryptoFunctions() {
		return _cryptoFunctions;
	}

	public String getFeatureName() {
		return _featureName;
	}

	public String getJCAClassName() {
		return _jcaClassName;
	}

	public int getKeySize() {
		return _keySize;
	}

	public List<RelatedCryptoMaterialType> getRelatedCryptoMaterialTypes() {
		return _relatedCryptoMaterialTypes;
	}

	public SortedSet<String> getValues() {
		return _values;
	}

	public void setAlgorithm(String algorithm) {
		_algorithm = algorithm;
	}

	public void setCryptoFunctions(List<CryptoFunction> cryptoFunctions) {
		_cryptoFunctions = cryptoFunctions;
	}

	public void setFeatureName(String featureName) {
		_featureName = featureName;
	}

	public void setKeySize(int keySize) {
		_keySize = keySize;
	}

	public void setRelatedCryptoMaterialTypes(
		List<RelatedCryptoMaterialType> relatedCryptoMaterialTypes) {

		_relatedCryptoMaterialTypes = relatedCryptoMaterialTypes;
	}

	private String _algorithm;
	private final String _bundleSymbolicName;
	private final String _className;
	private List<CryptoFunction> _cryptoFunctions = Collections.emptyList();
	private String _featureName;
	private final String _jcaClassName;
	private int _keySize;
	private List<RelatedCryptoMaterialType> _relatedCryptoMaterialTypes =
		Collections.emptyList();
	private final SortedSet<String> _values = new TreeSet<>();

}