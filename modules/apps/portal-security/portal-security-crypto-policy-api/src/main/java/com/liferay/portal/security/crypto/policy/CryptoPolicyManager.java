/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy;

import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;

import java.util.List;

/**
 * Exposes the set of FIPS-approved cryptographic algorithms and key sizes from
 * a curated, NIST-cited catalog. Features consult it to build
 * algorithm-selection UIs, validate configuration, and - when FIPS mode is
 * enabled - refuse non-approved algorithms at runtime.
 *
 * @author Manuele Castro
 */
public interface CryptoPolicyManager {

	/**
	 * In non-FIPS mode, does nothing. In FIPS mode, does nothing when the
	 * algorithm is approved for the service type; otherwise throws {@link
	 * CryptoPolicyException}.
	 */
	public void checkAlgorithm(String algorithm, String cryptoServiceType)
		throws CryptoPolicyException;

	/**
	 * In non-FIPS mode, does nothing. In FIPS mode, does nothing when the
	 * algorithm is approved and the key size is approved (or the algorithm
	 * has no key-size constraint); otherwise throws {@link
	 * CryptoPolicyException}.
	 */
	public void checkAlgorithm(
			String algorithm, String cryptoServiceType, int keySize)
		throws CryptoPolicyException;

	/**
	 * In non-FIPS mode returns the algorithm unchanged, regardless of
	 * approval. In FIPS mode returns the algorithm when it is approved for
	 * the service type; otherwise returns {@code fallbackAlgorithm} after
	 * confirming the fallback itself is approved, or throws {@link
	 * CryptoPolicyException} when the fallback is not approved either.
	 */
	public String getApprovedAlgorithm(
			String algorithm, String cryptoServiceType,
			String fallbackAlgorithm)
		throws CryptoPolicyException;

	/**
	 * Filters the caller's candidate algorithms to those usable in the current
	 * environment. In non-FIPS mode returns the candidates unchanged. In FIPS
	 * mode returns only the candidates approved for the service type, preserving
	 * the caller's order. The result is always a subset of the candidates -
	 * never a superset - so an approved algorithm the caller did not offer is
	 * never added.
	 */
	public List<String> getApprovedAlgorithms(
		List<String> algorithms, String cryptoServiceType);

	/**
	 * Filters the caller's candidate key sizes to those approved for the
	 * algorithm. In non-FIPS mode returns the candidates unchanged. In FIPS mode
	 * returns only the approved candidate key sizes, preserving order; when the
	 * algorithm has no key-size constraint the candidates are returned unchanged
	 * (an empty catalog set means "unconstrained," not "nothing allowed"). Pass
	 * the base algorithm name (for example {@code AES}, {@code RSA}, {@code
	 * EC}); no service type is supplied, so Cipher transformation names are not
	 * normalized.
	 */
	public List<Integer> getApprovedKeySizes(
		String algorithm, List<Integer> keySizes);

}