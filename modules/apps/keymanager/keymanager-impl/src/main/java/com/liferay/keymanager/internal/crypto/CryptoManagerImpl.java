/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.crypto;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.crypto.CryptoKey;
import com.liferay.keymanager.crypto.CryptoManager;
import com.liferay.keymanager.crypto.CryptoManagerException;
import com.liferay.keymanager.spi.crypto.CryptoVaultProvider;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.security.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
@Component(service = CryptoManager.class)
public class CryptoManagerImpl implements CryptoManager {

	@Override
	public byte[] decrypt(
			byte[] ciphertext, long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (ciphertext == null) {
			throw new IllegalArgumentException("Ciphertext is null");
		}

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			return cryptoVaultProvider.decrypt(
				ciphertext, companyId, keyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to decrypt cipherText: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public void deleteKey(long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			cryptoVaultProvider.deleteKey(
				companyId, keyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to delete key: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public byte[] encrypt(
			long companyId, KeyReference keyReference, byte[] plaintext)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		if (plaintext == null) {
			throw new IllegalArgumentException("Plaintext is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			return cryptoVaultProvider.encrypt(
				companyId, keyReference.getIdentifier(), plaintext);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to encrypt plaintext: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public KeyReference generateAsymmetricKeyPair(
			String algorithmSpec, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException {

		try {
			return _generate(
				algorithmSpec, companyId, identifier,
				CryptoVaultProvider::generateAsymmetricKeyPair, providerId);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to generate asymmetric key pair: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public KeyReference generateSecretKey(
			String algorithmSpec, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException {

		try {
			return _generate(
				algorithmSpec, companyId, identifier,
				CryptoVaultProvider::generateSecretKey, providerId);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to generate secret key: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public List<KeyReference> getKeyIdentifiers(
			long companyId, String providerId)
		throws CryptoManagerException {

		try {
			String resolvedProviderId = _getCryptoVaultProviderId(
				companyId, providerId, ProviderRole.DEK);

			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, resolvedProviderId, ProviderRole.DEK);

			List<String> identifiers = cryptoVaultProvider.getKeyIdentifiers(
				companyId);

			List<KeyReference> keyReferences = new ArrayList<>(
				identifiers.size());

			for (String identifier : identifiers) {
				keyReferences.add(
					new KeyReference(
						identifier, resolvedProviderId,
						KeyReference.Type.CRYPTO));
			}

			return keyReferences;
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list key identifiers: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoKey getKeyMetadata(long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			return cryptoVaultProvider.getKeyMetadata(
				companyId, keyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get key metadata: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public List<String> getProviders(long companyId) {
		List<String> providerIds = new ArrayList<>();

		for (String providerId : _getProviderIds()) {
			List<CryptoVaultProvider> cryptoVaultProviders =
				_serviceTrackerMap.getService(providerId);

			if (cryptoVaultProviders == null) {
				continue;
			}

			for (CryptoVaultProvider cryptoVaultProvider :
					cryptoVaultProviders) {

				if (cryptoVaultProvider.isAllowedCompany(companyId)) {
					providerIds.add(providerId);

					break;
				}
			}
		}

		return providerIds;
	}

	@Override
	public KeyReference importSecretKey(
			String algorithmSpec, long companyId, String identifier,
			String providerId, byte[] rawKeyMaterial)
		throws CryptoManagerException {

		try {
			String resolvedProviderId = _getCryptoVaultProviderId(
				companyId, providerId, ProviderRole.DEK);

			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, resolvedProviderId, ProviderRole.DEK);

			String resultIdentifier = cryptoVaultProvider.importSecretKey(
				algorithmSpec, companyId, identifier, rawKeyMaterial);

			return new KeyReference(
				resultIdentifier, resolvedProviderId, KeyReference.Type.CRYPTO);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to import secret key: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
		finally {
			if (rawKeyMaterial != null) {
				Arrays.fill(rawKeyMaterial, (byte)0);
			}
		}
	}

	@Override
	public Key unwrap(
			long companyId, KeyReference masterKeyReference,
			String wrappedKeyAlgorithm, byte[] wrappedKeyBytes,
			int wrappedKeyCipherType)
		throws CryptoManagerException {

		if (masterKeyReference == null) {
			throw new IllegalArgumentException("Master key reference is null");
		}

		if (wrappedKeyBytes == null) {
			throw new IllegalArgumentException("Wrapped key bytes is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			return cryptoVaultProvider.unwrap(
				companyId, masterKeyReference.getIdentifier(),
				wrappedKeyAlgorithm, wrappedKeyBytes, wrappedKeyCipherType);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to unwrap key: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public byte[] wrap(
			long companyId, Key keyToWrap, KeyReference masterKeyReference)
		throws CryptoManagerException {

		if (keyToWrap == null) {
			throw new IllegalArgumentException("Key to wrap is null");
		}

		if (masterKeyReference == null) {
			throw new IllegalArgumentException("Master key reference is null");
		}

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			return cryptoVaultProvider.wrap(
				companyId, keyToWrap, masterKeyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to wrap key: " +
						cryptoManagerException.getMessage(),
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, CryptoVaultProvider.class,
			"(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private KeyReference _generate(
			String algorithmSpec, long companyId, String identifier,
			KeyGenerator keyGenerator, String providerId)
		throws CryptoManagerException {

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, ProviderRole.DEK);

		String resultIdentifier = keyGenerator.generate(
			_getCryptoVaultProvider(
				companyId, resolvedProviderId, ProviderRole.DEK),
			algorithmSpec, companyId, identifier);

		return new KeyReference(
			resultIdentifier, resolvedProviderId, KeyReference.Type.CRYPTO);
	}

	private CryptoVaultProvider _getCryptoVaultProvider(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoManagerException {

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, providerRole);

		List<CryptoVaultProvider> cryptoVaultProviders =
			_serviceTrackerMap.getService(resolvedProviderId);

		if (cryptoVaultProviders != null) {
			for (CryptoVaultProvider cryptoVaultProvider :
					cryptoVaultProviders) {

				if (cryptoVaultProvider.isAllowedCompany(companyId)) {
					return cryptoVaultProvider;
				}
			}
		}

		throw new CryptoManagerException(
			StringBundler.concat(
				"No crypto vault provider found for ID: ", resolvedProviderId,
				" and company ID: ", companyId));
	}

	private String _getCryptoVaultProviderId(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoManagerException {

		if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
			KeyManagerProfile activeProfile =
				_profileOrchestrator.getActiveProfile();

			if (activeProfile == null) {
				throw new CryptoManagerException(
					StringBundler.concat(
						"No active KeyManagerProfile found to resolve ANY ",
						"provider wildcard for company ID: ", companyId));
			}

			if (companyId == 0L) {
				if (providerRole == ProviderRole.KEK) {
					return activeProfile.getSystemKekProviderId();
				}

				return activeProfile.getSystemDekProviderId();
			}

			if (providerRole == ProviderRole.KEK) {
				return activeProfile.getCompanyKekProviderId();
			}

			return activeProfile.getCompanyDekProviderId();
		}

		return providerId;
	}

	private Collection<String> _getProviderIds() {
		return _serviceTrackerMap.keySet();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CryptoManagerImpl.class);

	@Reference
	private ProfileOrchestrator _profileOrchestrator;

	private ServiceTrackerMap<String, List<CryptoVaultProvider>>
		_serviceTrackerMap;

	@FunctionalInterface
	private interface KeyGenerator {

		public String generate(
				CryptoVaultProvider cryptoVaultProvider, String algorithmSpec,
				long companyId, String identifier)
			throws CryptoManagerException;

	}

	private enum ProviderRole {

		DEK, KEK

	}

}