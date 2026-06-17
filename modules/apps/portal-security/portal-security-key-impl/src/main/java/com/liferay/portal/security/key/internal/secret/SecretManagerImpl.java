/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.secret;

import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.secret.SecretManagerException;
import com.liferay.portal.security.key.secret.SecureSecret;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileOrchestrator;
import com.liferay.portal.security.key.spi.secret.SecretVaultReader;
import com.liferay.portal.security.key.spi.secret.SecretVaultWriter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
@Component(service = SecretManager.class)
public class SecretManagerImpl implements SecretManager {

	@Override
	public void deleteSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			String providerId = keyReference.getProviderId();

			if (Objects.equals(providerId, StringPool.STAR)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			SecretVaultWriter secretVaultWriter = _getSecretVaultWriter(
				companyId, providerId);

			secretVaultWriter.deleteSecret(
				companyId, keyReference.getIdentifier());
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete secret", secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public List<String> getProviderIds(long companyId)
		throws SecretManagerException {

		Set<String> providerIds = new LinkedHashSet<>();

		ServiceTrackerMap<String, List<SecretVaultReader>>
			readerServiceTrackerMap = _readerServiceTrackerMap;

		if (readerServiceTrackerMap != null) {
			for (String providerId : readerServiceTrackerMap.keySet()) {
				List<SecretVaultReader> secretVaultReaders =
					readerServiceTrackerMap.getService(providerId);

				if (secretVaultReaders == null) {
					continue;
				}

				for (SecretVaultReader secretVaultReader : secretVaultReaders) {
					if (secretVaultReader.isAllowedCompany(companyId)) {
						providerIds.add(providerId);

						break;
					}
				}
			}
		}

		ServiceTrackerMap<String, List<SecretVaultWriter>>
			writerServiceTrackerMap = _writerServiceTrackerMap;

		if (writerServiceTrackerMap != null) {
			for (String providerId : writerServiceTrackerMap.keySet()) {
				List<SecretVaultWriter> secretVaultWriters =
					writerServiceTrackerMap.getService(providerId);

				if (secretVaultWriters == null) {
					continue;
				}

				for (SecretVaultWriter secretVaultWriter : secretVaultWriters) {
					if (secretVaultWriter.isAllowedCompany(companyId)) {
						providerIds.add(providerId);

						break;
					}
				}
			}
		}

		return new ArrayList<>(providerIds);
	}

	@Override
	public SecureSecret getSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		if (keyReference == null) {
			throw new IllegalArgumentException("Key reference is null");
		}

		try {
			SecretVaultReader secretVaultReader = _getSecretVaultReader(
				companyId, keyReference.getProviderId());

			return secretVaultReader.getSecret(
				companyId, keyReference.getIdentifier());
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get secret", secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public List<KeyReference> getSecretIdentifiers(
			long companyId, String providerId)
		throws SecretManagerException {

		try {
			String resolvedProviderId = providerId;

			if (Objects.equals(providerId, StringPool.STAR)) {
				resolvedProviderId = _getSecretVaultProviderId(companyId);
			}

			SecretVaultReader secretVaultReader = _getSecretVaultReader(
				companyId, resolvedProviderId);

			List<String> identifiers = secretVaultReader.getSecretIdentifiers(
				companyId);

			if (identifiers == null) {
				return new ArrayList<>();
			}

			List<KeyReference> keyReferences = new ArrayList<>(
				identifiers.size());

			for (String identifier : identifiers) {
				keyReferences.add(
					new KeyReference(
						identifier, resolvedProviderId,
						KeyReference.Type.SECRET));
			}

			return keyReferences;
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list secret identifiers",
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public KeyReference putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException {

		if (secureSecret == null) {
			throw new IllegalArgumentException("Secure secret is null");
		}

		try {
			KeyReference keyReference = secureSecret.getKeyReference();

			String providerId = keyReference.getProviderId();

			if (Objects.equals(providerId, StringPool.STAR)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			SecretVaultWriter secretVaultWriter = _getSecretVaultWriter(
				companyId, providerId);

			secretVaultWriter.putSecret(companyId, secureSecret);

			return new KeyReference(
				keyReference.getIdentifier(), providerId,
				KeyReference.Type.SECRET);
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to put secret", secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_readerServiceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, SecretVaultReader.class,
			"(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));

		_writerServiceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, SecretVaultWriter.class,
			"(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));
	}

	@Deactivate
	protected void deactivate() {
		_readerServiceTrackerMap.close();
		_writerServiceTrackerMap.close();
	}

	private String _getSecretVaultProviderId(long companyId)
		throws SecretManagerException {

		KeyManagerProfile activeProfile =
			_keyManagerProfileOrchestrator.getActiveKeyManagerProfile();

		if (activeProfile == null) {
			throw new SecretManagerException(
				StringBundler.concat(
					"No active KeyManagerProfile found to resolve ANY ",
					"provider wildcard for company ID: ", companyId));
		}

		if (companyId == 0L) {
			return activeProfile.getSystemSecretProviderId();
		}

		return activeProfile.getCompanySecretProviderId();
	}

	private SecretVaultReader _getSecretVaultReader(
			long companyId, String providerId)
		throws SecretManagerException {

		ServiceTrackerMap<String, List<SecretVaultReader>>
			readerServiceTrackerMap = _readerServiceTrackerMap;

		if (readerServiceTrackerMap == null) {
			throw new SecretManagerException(
				"The secret manager is not active");
		}

		String secretVaultReaderProviderId = _getSecretVaultReaderProviderId(
			companyId, providerId);

		List<SecretVaultReader> candidateSecretVaultReaders =
			readerServiceTrackerMap.getService(secretVaultReaderProviderId);

		if (candidateSecretVaultReaders != null) {
			for (SecretVaultReader secretVaultReader :
					candidateSecretVaultReaders) {

				if (secretVaultReader.isAllowedCompany(companyId)) {
					return secretVaultReader;
				}
			}
		}

		if (Objects.equals(providerId, StringPool.STAR)) {
			throw new SecretManagerException(
				StringBundler.concat(
					"No secret vault reader found for ANY provider and ",
					"company ID: ", companyId));
		}

		throw new SecretManagerException(
			StringBundler.concat(
				"No secret vault reader found for ID: ",
				secretVaultReaderProviderId, " and company ID: ", companyId));
	}

	private String _getSecretVaultReaderProviderId(
			long companyId, String providerId)
		throws SecretManagerException {

		if (Objects.equals(providerId, StringPool.STAR)) {
			return _getSecretVaultProviderId(companyId);
		}

		return providerId;
	}

	private SecretVaultWriter _getSecretVaultWriter(
			long companyId, String providerId)
		throws SecretManagerException {

		ServiceTrackerMap<String, List<SecretVaultWriter>>
			writerServiceTrackerMap = _writerServiceTrackerMap;

		if (writerServiceTrackerMap == null) {
			throw new SecretManagerException(
				"The secret manager is not active");
		}

		List<SecretVaultWriter> secretVaultWriters =
			writerServiceTrackerMap.getService(providerId);

		if (secretVaultWriters != null) {
			for (SecretVaultWriter secretVaultWriter : secretVaultWriters) {
				if (secretVaultWriter.isAllowedCompany(companyId)) {
					return secretVaultWriter;
				}
			}
		}

		throw new SecretManagerException(
			StringBundler.concat(
				"No secret vault writer found for ID: ", providerId,
				" and company ID: ", companyId));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SecretManagerImpl.class);

	@Reference
	private KeyManagerProfileOrchestrator _keyManagerProfileOrchestrator;

	private volatile ServiceTrackerMap<String, List<SecretVaultReader>>
		_readerServiceTrackerMap;
	private volatile ServiceTrackerMap<String, List<SecretVaultWriter>>
		_writerServiceTrackerMap;

}