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
import com.liferay.portal.security.key.crypto.CryptoException;
import com.liferay.portal.security.key.crypto.CryptoKey;
import com.liferay.portal.security.key.crypto.CryptoManager;
import com.liferay.portal.security.key.crypto.CryptoServiceResult;
import com.liferay.portal.security.key.spi.ProviderStatus;
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
		throws CryptoException {

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
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to decrypt cipherText", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public void deleteKey(long companyId, KeyReference keyReference)
		throws CryptoException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			cryptoProvider.deleteKey(companyId, keyReference.getIdentifier());
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoServiceResult<byte[]> encrypt(
			long companyId, KeyReference keyReference, byte[] plaintext)
		throws CryptoException {

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
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to encrypt plaintext", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoServiceResult<Key> exportKey(
			long companyId, KeyReference keyReference)
		throws CryptoException {

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
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to export key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoServiceResult<KeyReference> generateAsymmetricKeyPair(
			String algorithm, long companyId, KeyReference keyReference)
		throws CryptoException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			return _generate(
				algorithm, companyId, CryptoProvider::generateAsymmetricKeyPair,
				keyReference);
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to generate asymmetric key pair", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoServiceResult<KeyReference> generateSecretKey(
			String algorithm, long companyId, KeyReference keyReference)
		throws CryptoException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			return _generate(
				algorithm, companyId, CryptoProvider::generateSecretKey,
				keyReference);
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to generate secret key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoKey getCryptoKey(long companyId, KeyReference keyReference)
		throws CryptoException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			return cryptoProvider.getCryptoKey(
				companyId, keyReference.getIdentifier());
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get crypto key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public List<KeyReference> getKeyReferences(
			long companyId, String providerId)
		throws CryptoException {

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
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to list key identifiers", cryptoException);
			}

			throw cryptoException;
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
			String algorithm, long companyId, KeyReference keyReference,
			byte[] rawKeyMaterial)
		throws CryptoException {

		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm is null");
		}

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		if (rawKeyMaterial == null) {
			throw new IllegalArgumentException("Raw key material is null");
		}

		try {
			String resolvedKeyProviderId = _getCryptoProviderId(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedKeyProviderId);

			CryptoServiceResult<String> cryptoServiceResult =
				cryptoProvider.importSecretKey(
					algorithm, companyId, keyReference.getIdentifier(),
					rawKeyMaterial);

			_auditServiceIndicator(
				companyId, "importSecretKey",
				cryptoServiceResult.getServiceIndicator());

			return new CryptoServiceResult<>(
				cryptoServiceResult.getServiceIndicator(),
				new KeyReference(
					cryptoServiceResult.getValue(), resolvedKeyProviderId,
					KeyReference.Type.CRYPTO));
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to import secret key", cryptoException);
			}

			throw cryptoException;
		}
		finally {
			if (rawKeyMaterial != null) {
				Arrays.fill(rawKeyMaterial, (byte)0);
			}
		}
	}

	@Override
	public CryptoServiceResult<KeyReference> unwrap(
			long companyId, KeyReference keyReference,
			KeyReference masterKeyReference, String wrappedKeyAlgorithm,
			byte[] wrappedKeyBytes, int wrappedKeyCipherType)
		throws CryptoException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
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
			String resolvedKeyProviderId = _getCryptoProviderId(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			String resolvedMasterKeyProviderId = _getCryptoProviderId(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			if (!Objects.equals(
					resolvedKeyProviderId, resolvedMasterKeyProviderId)) {

				throw new CryptoException(
					StringBundler.concat(
						"Key provider ", resolvedKeyProviderId,
						" does not match master key provider ",
						resolvedMasterKeyProviderId));
			}

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedKeyProviderId);

			CryptoServiceResult<String> cryptoServiceResult =
				cryptoProvider.unwrap(
					companyId, keyReference.getIdentifier(),
					masterKeyReference.getIdentifier(), wrappedKeyAlgorithm,
					wrappedKeyBytes, wrappedKeyCipherType);

			_auditServiceIndicator(
				companyId, "unwrap", cryptoServiceResult.getServiceIndicator());

			return new CryptoServiceResult<>(
				cryptoServiceResult.getServiceIndicator(),
				new KeyReference(
					cryptoServiceResult.getValue(), resolvedKeyProviderId,
					KeyReference.Type.CRYPTO));
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to unwrap key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Override
	public CryptoServiceResult<byte[]> wrap(
			long companyId, KeyReference keyReference,
			KeyReference masterKeyReference)
		throws CryptoException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		if (masterKeyReference == null) {
			throw new IllegalArgumentException("Master key reference is null");
		}

		try {
			String resolvedKeyProviderId = _getCryptoProviderId(
				companyId, keyReference.getProviderId(), ProviderRole.DEK);

			String resolvedMasterKeyProviderId = _getCryptoProviderId(
				companyId, masterKeyReference.getProviderId(),
				ProviderRole.KEK);

			if (!Objects.equals(
					resolvedKeyProviderId, resolvedMasterKeyProviderId)) {

				throw new CryptoException(
					StringBundler.concat(
						"Key provider ", resolvedKeyProviderId,
						" does not match master key provider ",
						resolvedMasterKeyProviderId));
			}

			CryptoProvider cryptoProvider = _getCryptoProvider(
				companyId, resolvedKeyProviderId);

			CryptoServiceResult<byte[]> cryptoServiceResult =
				cryptoProvider.wrap(
					companyId, keyReference.getIdentifier(),
					masterKeyReference.getIdentifier());

			_auditServiceIndicator(
				companyId, "wrap", cryptoServiceResult.getServiceIndicator());

			return cryptoServiceResult;
		}
		catch (CryptoException cryptoException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to wrap key", cryptoException);
			}

			throw cryptoException;
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, CryptoProvider.class, "(crypto.provider.id=*)",
			new PropertyServiceReferenceMapper<>("crypto.provider.id"));
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
			String algorithm, long companyId, KeyGenerator keyGenerator,
			KeyReference keyReference)
		throws CryptoException {

		String resolvedKeyProviderId = _getCryptoProviderId(
			companyId, keyReference.getProviderId(), ProviderRole.DEK);

		CryptoServiceResult<String> cryptoServiceResult = keyGenerator.generate(
			_getCryptoProvider(companyId, resolvedKeyProviderId), algorithm,
			companyId, keyReference.getIdentifier());

		_auditServiceIndicator(
			companyId, "generate", cryptoServiceResult.getServiceIndicator());

		return new CryptoServiceResult<>(
			cryptoServiceResult.getServiceIndicator(),
			new KeyReference(
				cryptoServiceResult.getValue(), resolvedKeyProviderId,
				KeyReference.Type.CRYPTO));
	}

	private CryptoProvider _getCryptoProvider(
			long companyId, String resolvedProviderId)
		throws CryptoException {

		if (resolvedProviderId == null) {
			throw new CryptoException("Resolved provider ID is null");
		}

		List<CryptoProvider> cryptoProviders = _serviceTrackerMap.getService(
			resolvedProviderId);

		if (cryptoProviders != null) {
			for (CryptoProvider cryptoProvider : cryptoProviders) {
				if (!cryptoProvider.isAllowedCompany(companyId)) {
					continue;
				}

				if (cryptoProvider.getStatus() == ProviderStatus.ERROR) {
					throw new CryptoException(
						StringBundler.concat(
							"Crypto provider ", resolvedProviderId,
							" is in an error state for company ID ",
							companyId));
				}

				return cryptoProvider;
			}
		}

		throw new CryptoException(
			StringBundler.concat(
				"No crypto provider found for ID ", resolvedProviderId,
				" and company ID ", companyId));
	}

	private CryptoProvider _getCryptoProvider(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoException {

		return _getCryptoProvider(
			companyId,
			_getCryptoProviderId(companyId, providerId, providerRole));
	}

	private String _getCryptoProviderId(
			long companyId, String providerId, ProviderRole providerRole)
		throws CryptoException {

		if (!Objects.equals(providerId, StringPool.STAR)) {
			return providerId;
		}

		KeyManagerProfile activeProfile =
			_keyManagerProfileRegistry.getActiveKeyManagerProfile();

		if (activeProfile == null) {
			throw new CryptoException(
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
			throw new CryptoException(
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
			throws CryptoException;

	}

	private enum ProviderRole {

		DEK, KEK

	}

}