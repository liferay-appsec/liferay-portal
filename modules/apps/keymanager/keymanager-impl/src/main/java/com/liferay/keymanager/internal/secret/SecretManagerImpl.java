/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.secret;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.secret.SecretManager;
import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.keymanager.spi.secret.SecretVaultReader;
import com.liferay.keymanager.spi.secret.SecretVaultWriter;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.ArrayList;
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
@Component(service = SecretManager.class)
public class SecretManagerImpl implements SecretManager {

	@Override
	public void deleteSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		if (keyReference == null) {
			throw new NullPointerException("No KeyReference provided!");
		}

		String providerId = keyReference.getProviderId();

		try {
			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			SecretVaultWriter secretVaultWriter = _getSecretVaultWriter(
				companyId, providerId);

			secretVaultWriter.deleteSecret(
				companyId, keyReference.getIdentifier());
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to delete secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public List<String> getProviders(long companyId)
		throws SecretManagerException {

		List<String> providerIds = new ArrayList<>();

		for (String providerId : _readerServiceTrackerMap.keySet()) {
			List<SecretVaultReader> readers =
				_readerServiceTrackerMap.getService(providerId);

			if (readers == null) {
				continue;
			}

			for (SecretVaultReader reader : readers) {
				if (reader.isAllowedCompany(companyId)) {
					providerIds.add(providerId);

					break;
				}
			}
		}

		for (String providerId : _writerServiceTrackerMap.keySet()) {
			if (providerIds.contains(providerId)) {
				continue;
			}

			List<SecretVaultWriter> writers =
				_writerServiceTrackerMap.getService(providerId);

			if (writers == null) {
				continue;
			}

			for (SecretVaultWriter writer : writers) {
				if (writer.isAllowedCompany(companyId)) {
					providerIds.add(providerId);

					break;
				}
			}
		}

		return providerIds;
	}

	@Override
	public SecureSecret getSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		if (keyReference == null) {
			throw new NullPointerException("No KeyReference provided!");
		}

		try {
			for (SecretVaultReader reader :
					_getSecretVaultReaders(
						companyId, keyReference.getProviderId())) {

				SecureSecret secureSecret = reader.getSecret(
					companyId, keyReference.getIdentifier());

				if (secureSecret != null) {
					return secureSecret;
				}
			}
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}

		return null;
	}

	@Override
	public List<KeyReference> getSecretIdentifiers(
			long companyId, String providerId)
		throws SecretManagerException {

		try {
			String resolvedProviderId = providerId;

			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				resolvedProviderId = _getSecretVaultProviderId(companyId);
			}

			List<SecretVaultReader> readers =
				_readerServiceTrackerMap.getService(resolvedProviderId);

			SecretVaultReader reader = null;

			if (readers != null) {
				for (SecretVaultReader candidate : readers) {
					if (candidate.isAllowedCompany(companyId)) {
						reader = candidate;

						break;
					}
				}
			}

			if (reader == null) {
				throw new SecretManagerException(
					StringBundler.concat(
						"No secret vault reader found for ID: ",
						resolvedProviderId, " and company ID: ", companyId));
			}

			List<String> identifiers = reader.getSecretIdentifiers(companyId);

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
					"Unable to list secret identifiers: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public KeyReference putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException {

		if (secureSecret == null) {
			throw new NullPointerException("No secureSecret provided!");
		}

		KeyReference keyReference = secureSecret.getKeyReference();

		String providerId = keyReference.getProviderId();

		try {
			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
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
				_log.warn(
					"Unable to put secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
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
			_profileOrchestrator.getActiveProfile();

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

	private List<String> _getSecretVaultReaderProviderIds(
			long companyId, String providerId)
		throws SecretManagerException {

		if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
			return Collections.singletonList(
				_getSecretVaultProviderId(companyId));
		}

		return Collections.singletonList(providerId);
	}

	private List<SecretVaultReader> _getSecretVaultReaders(
			long companyId, String providerId)
		throws SecretManagerException {

		List<SecretVaultReader> readers = new ArrayList<>();

		for (String id :
				_getSecretVaultReaderProviderIds(companyId, providerId)) {

			List<SecretVaultReader> candidates =
				_readerServiceTrackerMap.getService(id);

			SecretVaultReader matched = null;

			if (candidates != null) {
				for (SecretVaultReader candidate : candidates) {
					if (candidate.isAllowedCompany(companyId)) {
						matched = candidate;

						break;
					}
				}
			}

			if (matched != null) {
				readers.add(matched);
			}
			else if (!Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				throw new SecretManagerException(
					StringBundler.concat(
						"No secret vault reader found for ID: ", id,
						" and company ID: ", companyId));
			}
		}

		if (readers.isEmpty() &&
			Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {

			throw new SecretManagerException(
				StringBundler.concat(
					"No secret vault reader found for ANY provider and ",
					"company ID: ", companyId));
		}

		return readers;
	}

	private SecretVaultWriter _getSecretVaultWriter(
			long companyId, String providerId)
		throws SecretManagerException {

		List<SecretVaultWriter> writers = _writerServiceTrackerMap.getService(
			providerId);

		if (writers != null) {
			for (SecretVaultWriter writer : writers) {
				if (writer.isAllowedCompany(companyId)) {
					return writer;
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
	private ProfileOrchestrator _profileOrchestrator;

	private ServiceTrackerMap<String, List<SecretVaultReader>>
		_readerServiceTrackerMap;
	private ServiceTrackerMap<String, List<SecretVaultWriter>>
		_writerServiceTrackerMap;

}