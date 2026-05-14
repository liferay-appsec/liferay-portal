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
			throw new NullPointerException("No ciphertext provided!");
		}

		if (keyReference == null) {
			throw new NullPointerException("No KeyReference provided!");
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
			throw new NullPointerException("No KeyReference provided!");
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
			throw new NullPointerException("No KeyReference provided!");
		}

		if (plaintext == null) {
			throw new NullPointerException("No plaintext provided!");
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

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, ProviderRole.DEK);

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, resolvedProviderId, ProviderRole.DEK);

			String resultIdentifier =
				cryptoVaultProvider.generateAsymmetricKeyPair(
					algorithmSpec, companyId, identifier);

			return new KeyReference(
				resultIdentifier, resolvedProviderId, KeyReference.Type.CRYPTO);
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

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, ProviderRole.DEK);

		try {
			CryptoVaultProvider cryptoVaultProvider = _getCryptoVaultProvider(
				companyId, resolvedProviderId, ProviderRole.DEK);

			String resultIdentifier = cryptoVaultProvider.generateSecretKey(
				algorithmSpec, companyId, identifier);

			return new KeyReference(
				resultIdentifier, resolvedProviderId, KeyReference.Type.CRYPTO);
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
				companyId, providerId, ProviderRole.DEK);

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
			throw new NullPointerException("No KeyReference provided!");
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
			List<CryptoVaultProvider> providers = _serviceTrackerMap.getService(
				providerId);

			if (providers == null) {
				continue;
			}

			for (CryptoVaultProvider provider : providers) {
				if (provider.isAllowedCompany(companyId)) {
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

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, ProviderRole.DEK);

		try {
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
			throw new NullPointerException("No master KeyReference provided!");
		}

		if (wrappedKeyBytes == null) {
			throw new NullPointerException("No wrappedKeyBytes provided!");
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
			throw new NullPointerException("No keyToWrap provided!");
		}

		if (masterKeyReference == null) {
			throw new NullPointerException("No master KeyReference provided!");
		}

		byte[] encodedKey = keyToWrap.getEncoded();

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
		finally {
			if (encodedKey != null) {
				Arrays.fill(encodedKey, (byte)0);
			}
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

	private CryptoVaultProvider _getCryptoVaultProvider(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoManagerException {

		String resolvedProviderId = _getCryptoVaultProviderId(
			companyId, providerId, providerRole);

		List<CryptoVaultProvider> providers = _serviceTrackerMap.getService(
			resolvedProviderId);

		if (providers != null) {
			for (CryptoVaultProvider provider : providers) {
				if (provider.isAllowedCompany(companyId)) {
					return provider;
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

	private enum ProviderRole {

		DEK, KEK

	}

}