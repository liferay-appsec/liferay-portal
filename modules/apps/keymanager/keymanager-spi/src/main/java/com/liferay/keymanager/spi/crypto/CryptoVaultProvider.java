/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.crypto;

import com.liferay.keymanager.crypto.CryptoKey;
import com.liferay.keymanager.crypto.CryptoManagerException;

import java.security.Key;

import java.util.List;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
public interface CryptoVaultProvider {

	public byte[] decrypt(byte[] ciphertext, long companyId, String identifier)
		throws CryptoManagerException;

	public void deleteKey(long companyId, String identifier)
		throws CryptoManagerException;

	public byte[] encrypt(long companyId, String identifier, byte[] plaintext)
		throws CryptoManagerException;

	public String generateAsymmetricKeyPair(
			String algorithmSpec, long companyId, String identifier)
		throws CryptoManagerException;

	public String generateSecretKey(
			String algorithmSpec, long companyId, String identifier)
		throws CryptoManagerException;

	public List<String> getKeyIdentifiers(long companyId)
		throws CryptoManagerException;

	public CryptoKey getKeyMetadata(long companyId, String identifier)
		throws CryptoManagerException;

	public String importSecretKey(
			String algorithmSpec, long companyId, String identifier,
			byte[] rawKeyMaterial)
		throws CryptoManagerException;

	public boolean isAllowedCompany(long companyId);

	public Key unwrap(
			long companyId, String identifier, String wrappedKeyAlgorithm,
			byte[] wrappedKeyBytes, int wrappedKeyCipherType)
		throws CryptoManagerException;

	public byte[] wrap(long companyId, Key keyToWrap, String identifier)
		throws CryptoManagerException;

}