/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.configuration.persistence.listener;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.definitions.ExtendedMetaTypeInformation;
import com.liferay.portal.configuration.metatype.definitions.ExtendedMetaTypeService;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.PortalCacheHelperUtil;
import com.liferay.portal.kernel.cache.PortalCacheManagerNames;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.internal.secret.SecretCacheUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

/**
 * @author Pedro Victor Silvestre
 */
@Component(
	property = "model.class.name=*", service = ConfigurationModelListener.class
)
public class VaultCredentialConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		ObjectClassDefinition objectClassDefinition = _getObjectClassDefinition(
			pid, properties);

		if (objectClassDefinition == null) {
			return;
		}

		long companyId = GetterUtil.getLong(
			properties.get(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey()),
			CompanyConstants.SYSTEM);

		for (AttributeDefinition attributeDefinition :
				objectClassDefinition.getAttributeDefinitions(
					ObjectClassDefinition.ALL)) {

			if (attributeDefinition.getType() != AttributeDefinition.PASSWORD) {
				continue;
			}

			String id = attributeDefinition.getID();

			Object value = properties.get(id);

			if (!(value instanceof String) || Validator.isNull((String)value)) {
				continue;
			}

			String identifier = _getIdentifier(companyId, id, pid);

			if (KeyReferenceUtil.isValidKeyReference((String)value)) {
				_checkKeyReference(identifier, pid, (String)value);

				continue;
			}

			try {
				properties.put(
					id, _vault(companyId, identifier, (String)value));
			}
			catch (Exception exception) {
				_log.error(
					StringBundler.concat(
						"Unable to vault property \"", id,
						"\" of configuration \"", pid, "\""),
					exception);
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_portalCache = PortalCacheHelperUtil.getPortalCache(
			PortalCacheManagerNames.SINGLE_VM,
			SecretCacheUtil.PORTAL_CACHE_NAME);
	}

	private void _checkKeyReference(String identifier, String pid, String value)
		throws ConfigurationModelListenerException {

		KeyReference keyReference = KeyReferenceUtil.toKeyReference(value);

		String valueIdentifier = keyReference.getIdentifier();

		if (!valueIdentifier.startsWith(_IDENTIFIER_PREFIX) ||
			valueIdentifier.equals(identifier)) {

			return;
		}

		throw new ConfigurationModelListenerException(
			StringBundler.concat(
				"Configuration \"", pid,
				"\" cannot reference a value belonging to another ",
				"configuration"),
			Object.class, VaultCredentialConfigurationModelListener.class,
			null);
	}

	private String _getIdentifier(long companyId, String id, String pid) {
		return StringBundler.concat(
			_IDENTIFIER_PREFIX, pid, ":", companyId, ":", id);
	}

	private ObjectClassDefinition _getObjectClassDefinition(
		String pid, Dictionary<String, Object> properties) {

		String factoryPid = GetterUtil.getString(
			properties.get(ConfigurationAdmin.SERVICE_FACTORYPID));

		String metaTypePid = pid;

		if (Validator.isNotNull(factoryPid)) {
			metaTypePid = factoryPid;
		}

		for (Bundle bundle : _bundleContext.getBundles()) {
			ExtendedMetaTypeInformation extendedMetaTypeInformation =
				_extendedMetaTypeService.getMetaTypeInformation(bundle);

			if ((extendedMetaTypeInformation == null) ||
				(!ArrayUtil.contains(
					extendedMetaTypeInformation.getPids(), metaTypePid) &&
				 !ArrayUtil.contains(
					 extendedMetaTypeInformation.getFactoryPids(),
					 metaTypePid))) {

				continue;
			}

			return extendedMetaTypeInformation.getObjectClassDefinition(
				metaTypePid, null);
		}

		return null;
	}

	private String _vault(long companyId, String identifier, String value)
		throws Exception {

		try (Secret secret = new Secret(
				new KeyReference(
					identifier, StringPool.STAR, KeyReference.Type.SECRET),
				value)) {

			String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
				_secretManager.putSecret(companyId, secret));

			_portalCache.remove(
				SecretCacheUtil.getKey(companyId, keyReferenceString));

			return keyReferenceString;
		}
	}

	private static final String _IDENTIFIER_PREFIX = "config:";

	private static final Log _log = LogFactoryUtil.getLog(
		VaultCredentialConfigurationModelListener.class);

	private BundleContext _bundleContext;

	@Reference
	private ExtendedMetaTypeService _extendedMetaTypeService;

	private PortalCache<String, String> _portalCache;

	@Reference
	private SecretManager _secretManager;

}