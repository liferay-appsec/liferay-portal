/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.crypto;

import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.ServiceIndicator;
import com.liferay.portal.security.key.crypto.CryptoKey;
import com.liferay.portal.security.key.crypto.CryptoManager;
import com.liferay.portal.security.key.crypto.CryptoManagerException;
import com.liferay.portal.security.key.crypto.CryptoServiceResult;
import com.liferay.portal.security.key.spi.ModuleStatus;
import com.liferay.portal.security.key.spi.crypto.CryptoProvider;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileRegistry;

import java.security.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	public CryptoServiceResult<byte[]> decrypt(
			byte[] ciphertext, long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (ciphertext == null) {
			throw new IllegalArgumentException("Ciphertext is null");
		}

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			CryptoServiceResult<byte[]> cryptoServiceResult =
				cryptoProvider.decrypt(
					ciphertext, companyId, keyReference.getIdentifier());

			_auditServiceIndicator(
				companyId, "decrypt",
				cryptoServiceResult.getServiceIndicator());

			return cryptoServiceResult;
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to decrypt cipherText", cryptoManagerException);
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
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			cryptoProvider.deleteKey(companyId, keyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoServiceResult<byte[]> encrypt(
			long companyId, KeyReference keyReference, byte[] plaintext)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		if (plaintext == null) {
			throw new IllegalArgumentException("Plaintext is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			CryptoServiceResult<byte[]> cryptoServiceResult =
				cryptoProvider.encrypt(
					companyId, keyReference.getIdentifier(), plaintext);

			_auditServiceIndicator(
				companyId, "encrypt",
				cryptoServiceResult.getServiceIndicator());

			return cryptoServiceResult;
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to encrypt plaintext", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoServiceResult<Key> exportKey(
			long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			CryptoServiceResult<Key> cryptoServiceResult =
				cryptoProvider.exportKey(
					companyId, keyReference.getIdentifier());

			_auditServiceIndicator(
				companyId, "exportKey",
				cryptoServiceResult.getServiceIndicator());

			return cryptoServiceResult;
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to export key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoServiceResult<KeyReference> generateAsymmetricKeyPair(
			String algorithm, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (identifier == null) {
			throw new IllegalArgumentException("Identifier is null");
		}

		if (providerId == null) {
			throw new IllegalArgumentException("Provider ID is null");
		}

		try {
			return _generate(
				algorithm, companyId, identifier,
				CryptoProvider::generateAsymmetricKeyPair, providerId);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to generate asymmetric key pair",
					cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoServiceResult<KeyReference> generateSecretKey(
			String algorithm, long companyId, String identifier,
			String providerId)
		throws CryptoManagerException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (identifier == null) {
			throw new IllegalArgumentException("Identifier is null");
		}

		if (providerId == null) {
			throw new IllegalArgumentException("Provider ID is null");
		}

		try {
			return _generate(
				algorithm, companyId, identifier,
				CryptoProvider::generateSecretKey, providerId);
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to generate secret key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoKey getCryptoKey(long companyId, KeyReference keyReference)
		throws CryptoManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			return cryptoProvider.getCryptoKey(
				companyId, keyReference.getIdentifier());
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get crypto key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public List<KeyReference> getKeyReferences(
			long companyId, String providerId)
		throws CryptoManagerException {

		if (providerId == null) {
			throw new IllegalArgumentException("Provider ID is null");
		}

		try {
			String resolvedProviderId = _getCryptoProviderId(
				companyId, providerId, ProviderRole.DEK);

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedProviderId);

			List<String> identifiers = cryptoProvider.getKeyIdentifiers(
				companyId);

			if (identifiers == null) {
				return Collections.emptyList();
			}

			return TransformUtil.transform(
				identifiers,
				identifier -> new KeyReference(
					identifier, resolvedProviderId, KeyReference.Type.CRYPTO));
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list key identifiers", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public List<String> getProviderIds(long companyId) {
		List<String> providerIds = new ArrayList<>();

		ServiceTrackerMap<String, List<CryptoProvider>> serviceTrackerMap =
			_serviceTrackerMap;

		if (serviceTrackerMap == null) {
			return providerIds;
		}

		for (String providerId : serviceTrackerMap.keySet()) {
			List<CryptoProvider> cryptoProviders = serviceTrackerMap.getService(
				providerId);

			if (cryptoProviders == null) {
				continue;
			}

			for (CryptoProvider cryptoProvider : cryptoProviders) {
				if (cryptoProvider.isAllowedCompany(companyId)) {
					providerIds.add(providerId);

					break;
				}
			}
		}

		return providerIds;
	}

	@Override
	public CryptoServiceResult<KeyReference> importSecretKey(
			String algorithm, long companyId, String identifier,
			String providerId, byte[] rawKeyMaterial)
		throws CryptoManagerException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (identifier == null) {
			throw new IllegalArgumentException("Identifier is null");
		}

		if (providerId == null) {
			throw new IllegalArgumentException("Provider ID is null");
		}

		if (rawKeyMaterial == null) {
			throw new IllegalArgumentException("Raw key material is null");
		}

		try {
			providerId = _getCryptoProviderId(
				companyId, providerId, ProviderRole.DEK);

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, providerId);

			CryptoServiceResult<String> cryptoServiceResult =
				cryptoProvider.importSecretKey(
					algorithm, companyId, identifier, rawKeyMaterial);

			_auditServiceIndicator(
				companyId, "importSecretKey",
				cryptoServiceResult.getServiceIndicator());

			return new CryptoServiceResult<>(
				cryptoServiceResult.getServiceIndicator(),
				new KeyReference(
					cryptoServiceResult.getValue(), providerId,
					KeyReference.Type.CRYPTO));
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to import secret key", cryptoManagerException);
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
	public CryptoServiceResult<KeyReference> unwrap(
			long companyId, String identifier, KeyReference masterKeyReference,
			String wrappedKeyAlgorithm, byte[] wrappedKeyBytes,
			int wrappedKeyCipherType)
		throws CryptoManagerException {

		if (identifier == null) {
			throw new IllegalArgumentException("Identifier is null");
		}

		if (masterKeyReference == null) {
			throw new IllegalArgumentException("Master key reference is null");
		}

		if (wrappedKeyAlgorithm == null) {
			throw new IllegalArgumentException("Wrapped key algorithm is null");
		}

		if (wrappedKeyBytes == null) {
			throw new IllegalArgumentException("Wrapped key bytes is null");
		}

		try {
			String resolvedProviderId = _getCryptoProviderId(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedProviderId);

			CryptoServiceResult<String> cryptoServiceResult =
				cryptoProvider.unwrap(
					companyId, identifier, masterKeyReference.getIdentifier(),
					wrappedKeyAlgorithm, wrappedKeyBytes, wrappedKeyCipherType);

			_auditServiceIndicator(
				companyId, "unwrap", cryptoServiceResult.getServiceIndicator());

			return new CryptoServiceResult<>(
				cryptoServiceResult.getServiceIndicator(),
				new KeyReference(
					cryptoServiceResult.getValue(), resolvedProviderId,
					KeyReference.Type.CRYPTO));
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to unwrap key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Override
	public CryptoServiceResult<byte[]> wrap(
			long companyId, KeyReference keyToWrapReference,
			KeyReference masterKeyReference)
		throws CryptoManagerException {

		if (keyToWrapReference == null) {
			throw new IllegalArgumentException("Key to wrap reference is null");
		}

		if (masterKeyReference == null) {
			throw new IllegalArgumentException("Master key reference is null");
		}

		try {
			String resolvedKeyToWrapProviderId = _getCryptoProviderId(
				companyId, keyToWrapReference.getProviderId(),
				ProviderRole.DEK);

			String resolvedMasterKeyProviderId = _getCryptoProviderId(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			if (!Objects.equals(
					resolvedKeyToWrapProviderId, resolvedMasterKeyProviderId)) {

				throw new CryptoManagerException(
					StringBundler.concat(
						"Key to wrap provider ", resolvedKeyToWrapProviderId,
						" does not match master key provider ",
						resolvedMasterKeyProviderId));
			}

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedMasterKeyProviderId);

			CryptoServiceResult<byte[]> cryptoServiceResult =
				cryptoProvider.wrap(
					companyId, keyToWrapReference.getIdentifier(),
					masterKeyReference.getIdentifier());

			_auditServiceIndicator(
				companyId, "wrap", cryptoServiceResult.getServiceIndicator());

			return cryptoServiceResult;
		}
		catch (CryptoManagerException cryptoManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to wrap key", cryptoManagerException);
			}

			throw cryptoManagerException;
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, CryptoProvider.class, "(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));
	}

	@Deactivate
	protected void deactivate() {
		if (_serviceTrackerMap != null) {
			_serviceTrackerMap.close();

			_serviceTrackerMap = null;
		}
	}

	private void _auditServiceIndicator(
		long companyId, String operation, ServiceIndicator serviceIndicator) {

		if (serviceIndicator == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Operation ", operation, " for company ID ", companyId,
						" returned a null service indicator"));
			}

			return;
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"Operation ", operation, " for company ID ", companyId,
					" used ",
					serviceIndicator.isApproved() ? "an approved " :
						"a nonapproved ",
					"security function ",
					serviceIndicator.getSecurityFunctionName()));
		}
	}

	private CryptoServiceResult<KeyReference> _generate(
			String algorithm, long companyId, String identifier,
			KeyGenerator keyGenerator, String providerId)
		throws CryptoManagerException {

		providerId = _getCryptoProviderId(
			companyId, providerId, ProviderRole.DEK);

		CryptoServiceResult<String> cryptoServiceResult = keyGenerator.generate(
			_getCryptoProvider(companyId, providerId), algorithm, companyId,
			identifier);

		_auditServiceIndicator(
			companyId, "generate", cryptoServiceResult.getServiceIndicator());

		return new CryptoServiceResult<>(
			cryptoServiceResult.getServiceIndicator(),
			new KeyReference(
				cryptoServiceResult.getValue(), providerId,
				KeyReference.Type.CRYPTO));
	}

	private CryptoProvider _getCryptoProvider(
			long companyId, String resolvedProviderId)
		throws CryptoManagerException {

		if (resolvedProviderId == null) {
			throw new CryptoManagerException("Resolved provider ID is null");
		}

		List<CryptoProvider> cryptoProviders = _serviceTrackerMap.getService(
			resolvedProviderId);

		if (cryptoProviders != null) {
			for (CryptoProvider cryptoProvider : cryptoProviders) {
				if (!cryptoProvider.isAllowedCompany(companyId)) {
					continue;
				}

				if (cryptoProvider.getModuleStatus() == ModuleStatus.ERROR) {
					throw new CryptoManagerException(
						StringBundler.concat(
							"Crypto provider ", resolvedProviderId,
							" is in an error state for company ID ",
							companyId));
				}

				return cryptoProvider;
			}
		}

		throw new CryptoManagerException(
			StringBundler.concat(
				"No crypto provider found for ID ", resolvedProviderId,
				" and company ID ", companyId));
	}

	private CryptoProvider _getCryptoProvider(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoManagerException {

		return _getCryptoProvider(
			companyId,
			_getCryptoProviderId(companyId, providerId, providerRole));
	}

	private String _getCryptoProviderId(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoManagerException {

		if (!Objects.equals(providerId, StringPool.STAR)) {
			return providerId;
		}

		KeyManagerProfile activeProfile =
			_keyManagerProfileRegistry.getActiveKeyManagerProfile();

		if (activeProfile == null) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"No active KeyManagerProfile found to resolve the ",
					"provider wildcard for company ID ", companyId));
		}

		if (companyId == CompanyConstants.SYSTEM) {
			if (providerRole == ProviderRole.DEK) {
				providerId = activeProfile.getSystemDEKProviderId();
			}
			else {
				providerId = activeProfile.getSystemKEKProviderId();
			}
		}
		else if (providerRole == ProviderRole.DEK) {
			providerId = activeProfile.getCompanyDEKProviderId();
		}
		else {
			providerId = activeProfile.getCompanyKEKProviderId();
		}

		if (providerId == null) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Active KeyManagerProfile resolved a null provider ID for ",
					"role ", providerRole, " and company ID ", companyId));
		}

		return providerId;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CryptoManagerImpl.class);

	@Reference
	private KeyManagerProfileRegistry _keyManagerProfileRegistry;

	private ServiceTrackerMap<String, List<CryptoProvider>> _serviceTrackerMap;

	@FunctionalInterface
	private interface KeyGenerator {

		public CryptoServiceResult<String> generate(
				CryptoProvider cryptoProvider, String algorithm, long companyId,
				String identifier)
			throws CryptoManagerException;

	}

	private enum ProviderRole {

		DEK, KEK

	}

}