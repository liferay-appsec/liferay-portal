/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.crypto;

import com.liferay.keymanager.KeyReference;

import java.security.Key;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
@ProviderType
public interface CryptoManager {

	public byte[] decrypt(
			byte[] ciphertext, long companyId, KeyReference keyReference)
		throws CryptoManagerException;

	public void deleteKey(long companyId, KeyReference keyReference)
		throws CryptoManagerException;

	public byte[] encrypt(
			long companyId, KeyReference keyReference, byte[] plaintext)
		throws CryptoManagerException;

	public KeyReference generateAsymmetricKeyPair(
			String algorithmSpec, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException;

	public KeyReference generateSecretKey(
			String algorithmSpec, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException;

	public List<KeyReference> getKeyIdentifiers(
			long companyId, String providerId)
		throws CryptoManagerException;

	public CryptoKey getKeyMetadata(long companyId, KeyReference keyReference)
		throws CryptoManagerException;

	public List<String> getProviderIds(long companyId)
		throws CryptoManagerException;

	public KeyReference importSecretKey(
			String algorithmSpec, long companyId, String identifier,
			String providerId, byte[] rawKeyMaterial)
		throws CryptoManagerException;

	public Key unwrap(
			long companyId, KeyReference masterKeyReference,
			String wrappedKeyAlgorithm, byte[] wrappedKeyBytes,
			int wrappedKeyCipherType)
		throws CryptoManagerException;

	public byte[] wrap(
			long companyId, Key keyToWrap, KeyReference masterKeyReference)
		throws CryptoManagerException;

}