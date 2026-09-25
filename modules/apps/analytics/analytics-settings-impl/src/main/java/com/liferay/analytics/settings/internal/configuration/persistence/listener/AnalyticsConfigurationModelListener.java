/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.analytics.settings.internal.configuration.persistence.listener;

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.analytics.settings.configuration.AnalyticsConfigurationRegistry;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.secret.exception.SecretException;

import java.util.Dictionary;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Shinn Lok
 */
@Component(
	property = {
		"model.class.name=com.liferay.analytics.settings.configuration.AnalyticsConfiguration",
		"model.class.name=com.liferay.analytics.settings.configuration.AnalyticsConfiguration.scoped"
	},
	service = ConfigurationModelListener.class
)
public class AnalyticsConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeDelete(String pid) {
		AnalyticsConfiguration analyticsConfiguration =
			_analyticsConfigurationRegistry.getAnalyticsConfiguration(pid);

		_deleteSecret(
			"liferayAnalyticsFaroBackendSecuritySignature",
			analyticsConfiguration.
				liferayAnalyticsFaroBackendSecuritySignature());
		_deleteSecret("token", analyticsConfiguration.token());
	}

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		AnalyticsConfiguration analyticsConfiguration =
			_analyticsConfigurationRegistry.getAnalyticsConfiguration(pid);

		properties.put(
			"previousContentRecommenderMostPopularItemsEnabled",
			analyticsConfiguration.contentRecommenderMostPopularItemsEnabled());
		properties.put(
			"previousContentRecommenderUserPersonalizationEnabled",
			analyticsConfiguration.
				contentRecommenderUserPersonalizationEnabled());

		properties.put(
			"previousSyncAllAccounts",
			analyticsConfiguration.syncAllAccounts());

		properties.put(
			"previousSyncAllContacts",
			analyticsConfiguration.syncAllContacts());

		String[] syncedAccountFieldNames =
			analyticsConfiguration.syncedAccountFieldNames();

		if (ArrayUtil.isNotEmpty(syncedAccountFieldNames)) {
			properties.put(
				"previousSyncedAccountFieldNames", syncedAccountFieldNames);
		}

		String[] syncedAccountGroupIds =
			analyticsConfiguration.syncedAccountGroupIds();

		if (!analyticsConfiguration.syncAllAccounts() &&
			ArrayUtil.isNotEmpty(syncedAccountGroupIds)) {

			properties.put(
				"previousSyncedAccountGroupIds", syncedAccountGroupIds);
		}
		else if (analyticsConfiguration.syncAllAccounts()) {
			properties.put("previousSyncedAccountGroupIds", new String[0]);
		}

		String[] syncedContactFieldNames =
			analyticsConfiguration.syncedContactFieldNames();

		if (ArrayUtil.isNotEmpty(syncedContactFieldNames)) {
			properties.put(
				"previousSyncedContactFieldNames", syncedContactFieldNames);
		}

		String[] syncedGroupIds = analyticsConfiguration.syncedGroupIds();

		if (syncedGroupIds == null) {
			syncedGroupIds = new String[0];
		}

		properties.put("previousSyncedGroupIds", syncedGroupIds);

		String[] syncedOrganizationIds =
			analyticsConfiguration.syncedOrganizationIds();

		if (!analyticsConfiguration.syncAllContacts()) {
			if (syncedOrganizationIds == null) {
				syncedOrganizationIds = new String[0];
			}

			properties.put(
				"previousSyncedOrganizationIds", syncedOrganizationIds);
		}
		else if (analyticsConfiguration.syncAllContacts()) {
			properties.put("previousSyncedOrganizationIds", new String[0]);
		}

		String[] syncedUserFieldNames =
			analyticsConfiguration.syncedUserFieldNames();

		if (ArrayUtil.isNotEmpty(syncedUserFieldNames)) {
			properties.put(
				"previousSyncedUserFieldNames", syncedUserFieldNames);
		}

		String[] syncedUserGroupIds =
			analyticsConfiguration.syncedUserGroupIds();

		if (!analyticsConfiguration.syncAllContacts()) {
			if (syncedUserGroupIds == null) {
				syncedUserGroupIds = new String[0];
			}

			properties.put("previousSyncedUserGroupIds", syncedUserGroupIds);
		}
		else if (analyticsConfiguration.syncAllContacts()) {
			properties.put("previousSyncedUserGroupIds", new String[0]);
		}

		String token = analyticsConfiguration.token();

		if (token != null) {
			properties.put("previousToken", token);
		}

		long companyId = GetterUtil.getLong(
			properties.get(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey()));

		_storeSecret(
			companyId, "liferayAnalyticsFaroBackendSecuritySignature",
			analyticsConfiguration.
				liferayAnalyticsFaroBackendSecuritySignature(),
			properties);
		_storeSecret(companyId, "token", token, properties);
	}

	private void _deleteSecret(String name, String value) {
		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(value);

		if (keyReference == null) {
			return;
		}

		String[] parts = StringUtil.split(
			keyReference.getIdentifier(), CharPool.SLASH);

		if ((parts.length != 4) ||
			!Objects.equals(
				parts[0], AnalyticsConfiguration.class.getSimpleName()) ||
			!Objects.equals(parts[2], name)) {

			return;
		}

		try {
			SecretManager secretManager = _getSecretManager();

			secretManager.deleteSecret(
				GetterUtil.getLong(parts[1]), keyReference);
		}
		catch (SecretException secretException) {
			_log.error(
				"Unable to delete the stored value of " + name,
				secretException);
		}
	}

	private KeyReference _fetchKeyReference(
		long companyId, String name, String value) {

		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(value);

		if (keyReference == null) {
			return null;
		}

		String identifier = keyReference.getIdentifier();

		if (!identifier.startsWith(_getIdentifierPrefix(companyId, name))) {
			return null;
		}

		return keyReference;
	}

	private String _getIdentifierPrefix(long companyId, String name) {
		return StringBundler.concat(
			AnalyticsConfiguration.class.getSimpleName(), StringPool.SLASH,
			companyId, StringPool.SLASH, name, StringPool.SLASH);
	}

	private SecretManager _getSecretManager() {
		SecretManager secretManager = _secretManagerSnapshot.get();

		if (secretManager == null) {
			throw new IllegalStateException("Secret manager is unavailable");
		}

		return secretManager;
	}

	private void _storeSecret(
			long companyId, String name, String previousValue,
			Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		String value = GetterUtil.getString(properties.get(name));

		if (KeyReferenceUtil.isKeyReference(value) &&
			(_fetchKeyReference(companyId, name, value) == null)) {

			throw new ConfigurationModelListenerException(
				"The value of " + name +
					" references a secret outside this configuration",
				AnalyticsConfiguration.class,
				AnalyticsConfigurationModelListener.class, properties);
		}

		try {
			if (PropsValues.FIPS_ENABLED &&
				(companyId != CompanyConstants.SYSTEM) &&
				Validator.isNotNull(value) &&
				!KeyReferenceUtil.isKeyReference(value)) {

				SecretManager secretManager = _getSecretManager();

				try (Secret secret = new Secret(
						new KeyReference(
							_getIdentifierPrefix(companyId, name) +
								PortalUUIDUtil.generate(),
							StringPool.STAR, KeyReference.Type.SECRET),
						value)) {

					value = KeyReferenceUtil.toKeyReferenceString(
						secretManager.putSecret(companyId, secret));
				}

				properties.put(name, value);
			}

			KeyReference previousKeyReference = _fetchKeyReference(
				companyId, name, previousValue);

			if ((previousKeyReference != null) &&
				!Objects.equals(previousValue, value)) {

				SecretManager secretManager = _getSecretManager();

				secretManager.deleteSecret(companyId, previousKeyReference);
			}
		}
		catch (SecretException secretException) {
			throw new ConfigurationModelListenerException(
				secretException, AnalyticsConfiguration.class,
				AnalyticsConfigurationModelListener.class, properties);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyticsConfigurationModelListener.class);

	private static final Snapshot<SecretManager> _secretManagerSnapshot =
		new Snapshot<>(
			AnalyticsConfigurationModelListener.class, SecretManager.class,
			null, true);

	@Reference
	private AnalyticsConfigurationRegistry _analyticsConfigurationRegistry;

}