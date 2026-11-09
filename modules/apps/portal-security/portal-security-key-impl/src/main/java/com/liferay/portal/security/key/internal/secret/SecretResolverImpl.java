/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.secret;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.PortalCacheHelperUtil;
import com.liferay.portal.kernel.cache.PortalCacheManagerNames;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.PortletInstanceSettingsLocator;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.SettingsLocatorHelper;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileRegistry;

import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Victor Silvestre
 */
@Component(service = SecretResolver.class)
public class SecretResolverImpl implements SecretResolver {

	public static final String PORTAL_CACHE_NAME =
		SecretResolverImpl.class.getName();

	public static String getKey(long companyId, String keyReferenceString) {
		return StringBundler.concat(
			companyId, StringPool.POUND, keyReferenceString);
	}

	@Override
	public String copy(
		long companyId, SecretContext sourceSecretContext,
		SecretContext targetSecretContext, String value) {

		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(value);

		if ((keyReference == null) ||
			(keyReference.getType() != KeyReference.Type.SECRET)) {

			return value;
		}

		try {
			SequencedMap<String, Long> targetSecretIdentifierMap =
				_getSecretIdentifierMap(targetSecretContext);

			Long scopeCompanyId = _fetchScopeCompanyId(
				companyId, keyReference.getIdentifier(),
				targetSecretIdentifierMap);

			if (scopeCompanyId != null) {
				return value;
			}

			Long sourceScopeCompanyId = _fetchScopeCompanyId(
				companyId, keyReference.getIdentifier(),
				_getSecretIdentifierMap(sourceSecretContext));

			Map.Entry<String, Long> entry =
				targetSecretIdentifierMap.firstEntry();

			Long targetScopeCompanyId = _fetchScopeCompanyId(
				companyId, entry.getKey(), targetSecretIdentifierMap);

			if ((sourceScopeCompanyId == null) ||
				(targetScopeCompanyId == null)) {

				_audit(companyId, keyReference.getIdentifier(), "copy");

				return value;
			}

			try (Secret sourceSecret = _secretManager.getSecret(
					sourceScopeCompanyId, keyReference)) {

				try (Secret secret = new Secret(
						sourceSecret.getBytes(),
						new KeyReference(
							entry.getKey(), StringPool.STAR,
							KeyReference.Type.SECRET))) {

					return _putSecret(
						targetScopeCompanyId, secret, targetSecretContext);
				}
			}
		}
		catch (PortalException portalException) {
			return ReflectionUtil.throwException(portalException);
		}
	}

	@Override
	public void delete(
		long companyId, SecretContext secretContext, String value) {

		if (!KeyReferenceUtil.isKeyReference(value)) {
			return;
		}

		try {
			KeyReference keyReference = _getKeyReference(value);

			SequencedMap<String, Long> secretIdentifierMap =
				_getSecretIdentifierMap(secretContext);

			Map.Entry<String, Long> entry = secretIdentifierMap.firstEntry();

			_secretManager.deleteSecret(
				_getScopeCompanyId(
					companyId, keyReference.getIdentifier(), "delete",
					Collections.singletonMap(entry.getKey(), entry.getValue())),
				keyReference);
		}
		catch (PortalException portalException) {
			ReflectionUtil.throwException(portalException);
		}
	}

	@Override
	public String resolve(
		long companyId, SecretContext secretContext, String value) {

		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		try {
			KeyReference keyReference = _getKeyReference(value);

			long scopeCompanyId = _getScopeCompanyId(
				companyId, keyReference.getIdentifier(), "resolve",
				_getSecretIdentifierMap(secretContext));

			String key = getKey(scopeCompanyId, value);

			String resolvedValue = _portalCache.get(key);

			if (resolvedValue != null) {
				return resolvedValue;
			}

			try (Secret secret = _secretManager.getSecret(
					scopeCompanyId, keyReference)) {

				resolvedValue = new String(secret.getChars());
			}

			_portalCache.put(key, resolvedValue, 600);

			return resolvedValue;
		}
		catch (PortalException portalException) {
			return ReflectionUtil.throwException(portalException);
		}
	}

	@Override
	public String resolve(long companyId, String value) {
		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		try {
			KeyReference keyReference = _getKeyReference(value);

			if (companyId != CompanyConstants.SYSTEM) {
				KeyManagerProfile keyManagerProfile =
					_keyManagerProfileRegistry.getActiveKeyManagerProfile();

				if ((keyManagerProfile != null) &&
					Objects.equals(
						keyReference.getProviderId(),
						keyManagerProfile.getSystemSecretProviderId())) {

					companyId = CompanyConstants.SYSTEM;
				}
			}

			String key = getKey(companyId, value);

			String resolvedValue = _portalCache.get(key);

			if (resolvedValue != null) {
				return resolvedValue;
			}

			try (Secret secret = _secretManager.getSecret(
					companyId, keyReference)) {

				resolvedValue = new String(secret.getChars());
			}

			_portalCache.put(key, resolvedValue, 600);

			return resolvedValue;
		}
		catch (SecretException secretException) {
			return ReflectionUtil.throwException(secretException);
		}
	}

	@Override
	public String store(
		long companyId, SecretContext secretContext, String value) {

		if (!PropsValues.FIPS_ENABLED || Validator.isNull(value)) {
			return value;
		}

		try {
			SequencedMap<String, Long> secretIdentifierMap =
				_getSecretIdentifierMap(secretContext);

			if (KeyReferenceUtil.isKeyReference(value)) {
				KeyReference keyReference = _getKeyReference(value);

				_getScopeCompanyId(
					companyId, keyReference.getIdentifier(), "store",
					secretIdentifierMap);

				return value;
			}

			Map.Entry<String, Long> entry = secretIdentifierMap.firstEntry();

			String identifier = entry.getKey();

			try (Secret secret = new Secret(
					new KeyReference(
						identifier, StringPool.STAR, KeyReference.Type.SECRET),
					value)) {

				return _putSecret(
					_getScopeCompanyId(
						companyId, identifier, "store", secretIdentifierMap),
					secret, secretContext);
			}
		}
		catch (PortalException portalException) {
			return ReflectionUtil.throwException(portalException);
		}
	}

	@Activate
	protected void activate() {
		_portalCache = PortalCacheHelperUtil.getPortalCache(
			PortalCacheManagerNames.SINGLE_VM, PORTAL_CACHE_NAME);
	}

	@Deactivate
	protected void deactivate() {
		PortalCacheHelperUtil.removePortalCache(
			PortalCacheManagerNames.SINGLE_VM, PORTAL_CACHE_NAME);
	}

	private void _audit(long companyId, String identifier, String operation) {
		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"secret-reference-refused", FIPSAuditEvent.Severity.WARNING);

		fipsAuditEvent.put("company-id", companyId);
		fipsAuditEvent.put("identifier", identifier);
		fipsAuditEvent.put("operation", operation);

		FIPSAuditUtil.write(fipsAuditEvent);
	}

	private Long _fetchScopeCompanyId(
		long companyId, String identifier,
		Map<String, Long> secretIdentifierMap) {

		Long scopeCompanyId = secretIdentifierMap.get(identifier);

		if ((scopeCompanyId != null) &&
			((scopeCompanyId == CompanyConstants.SYSTEM) ||
			 (scopeCompanyId == companyId))) {

			return scopeCompanyId;
		}

		return null;
	}

	private long _getCounterpartGroupId(Group group) {
		if (group.isStagingGroup()) {
			return group.getLiveGroupId();
		}

		Group stagingGroup = group.getStagingGroup();

		if (stagingGroup == null) {
			return 0;
		}

		return stagingGroup.getGroupId();
	}

	private KeyReference _getKeyReference(String keyReferenceString)
		throws SecretException {

		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(
			keyReferenceString);

		if (keyReference == null) {
			throw new SecretException("Unable to parse the key reference");
		}

		if (keyReference.getType() != KeyReference.Type.SECRET) {
			throw new SecretException("Only secret references are supported");
		}

		return keyReference;
	}

	private String _getPreferenceIdentifier(
		String key, ExtendedObjectClassDefinition.Scope scope, Object scopePK,
		String settingsId) {

		return StringBundler.concat(
			"preference/", settingsId, StringPool.SLASH, scope.getValue(),
			StringPool.SLASH, scopePK, StringPool.SLASH, key);
	}

	private long _getScopeCompanyId(
			long companyId, String identifier, String operation,
			Map<String, Long> secretIdentifierMap)
		throws SecretException {

		Long scopeCompanyId = _fetchScopeCompanyId(
			companyId, identifier, secretIdentifierMap);

		if (scopeCompanyId != null) {
			return scopeCompanyId;
		}

		_audit(companyId, identifier, operation);

		throw new SecretException(
			StringBundler.concat(
				"Unable to ", operation, " the secret reference \"", identifier,
				"\" in this secret context"));
	}

	private SequencedMap<String, Long> _getSecretIdentifierMap(
			SecretContext secretContext)
		throws PortalException {

		SequencedMap<String, Long> secretIdentifierMap = new LinkedHashMap<>();

		SecretContext.Type type = secretContext.getType();

		if (type == SecretContext.Type.COMPANY) {
			_putPortalPreferenceScope(
				secretContext.getCompanyId(), secretContext.getKey(),
				secretIdentifierMap);
		}
		else if (type == SecretContext.Type.CONFIGURATION) {
			Dictionary<String, Object> properties = new HashMapDictionary<>(
				secretContext.getProperties());

			secretIdentifierMap.put(
				SecretIdentifierUtil.getSecretIdentifier(
					secretContext.getKey(),
					GetterUtil.getString(properties.get(Constants.SERVICE_PID)),
					properties),
				GetterUtil.getLong(
					properties.get(
						ExtendedObjectClassDefinition.Scope.COMPANY.
							getPropertyKey()),
					CompanyConstants.SYSTEM));
		}
		else if (type == SecretContext.Type.ENTITY) {
			secretIdentifierMap.put(
				StringBundler.concat(
					"entity/",
					StringUtil.extractLast(
						secretContext.getClassName(), CharPool.PERIOD),
					StringPool.SLASH, secretContext.getPrimaryKey(),
					StringPool.SLASH, secretContext.getKey()),
				secretContext.getCompanyId());
		}
		else if (type == SecretContext.Type.GROUP) {
			Group group = _groupLocalService.getGroup(
				secretContext.getGroupId());

			_putTypeSettingsScope(
				group.getCompanyId(), group.getGroupId(),
				secretContext.getKey(), secretIdentifierMap);

			long counterpartGroupId = _getCounterpartGroupId(group);

			if (counterpartGroupId > 0) {
				_putTypeSettingsScope(
					group.getCompanyId(), counterpartGroupId,
					secretContext.getKey(), secretIdentifierMap);
			}

			_putPortalPreferenceScope(
				group.getCompanyId(), secretContext.getKey(),
				secretIdentifierMap);
		}
		else {
			_putSettingsScopes(
				secretContext.getKey(), secretIdentifierMap,
				secretContext.getSettingsLocator());
		}

		return secretIdentifierMap;
	}

	private void _putCompanyScopes(
		long companyId, String configurationPid, String key,
		Map<String, Long> secretIdentifierMap, String settingsId) {

		_putScope(
			companyId, configurationPid, key,
			ExtendedObjectClassDefinition.Scope.COMPANY, companyId,
			secretIdentifierMap, settingsId);
		_putPortalPreferenceScope(companyId, key, secretIdentifierMap);
		_putSystemScope(configurationPid, key, secretIdentifierMap);
	}

	private void _putGroupScopes(
		String configurationPid, Group group, String key,
		Map<String, Long> secretIdentifierMap, String settingsId) {

		_putScope(
			group.getCompanyId(), configurationPid, key,
			ExtendedObjectClassDefinition.Scope.GROUP, group.getGroupId(),
			secretIdentifierMap, settingsId);

		long counterpartGroupId = _getCounterpartGroupId(group);

		if (counterpartGroupId > 0) {
			_putScope(
				group.getCompanyId(), configurationPid, key,
				ExtendedObjectClassDefinition.Scope.GROUP, counterpartGroupId,
				secretIdentifierMap, settingsId);
		}
	}

	private void _putPortalPreferenceScope(
		long companyId, String key, Map<String, Long> secretIdentifierMap) {

		secretIdentifierMap.put(
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key),
			companyId);
	}

	private void _putPortletInstanceScope(
		String key,
		PortletInstanceSettingsLocator portletInstanceSettingsLocator,
		Map<String, Long> secretIdentifierMap) {

		String portletId = portletInstanceSettingsLocator.getSettingsId();

		long userId = PortletIdCodec.decodeUserId(portletId);

		long ownerId = portletInstanceSettingsLocator.getOwnerId();

		if (userId > 0) {
			ownerId = userId;
		}

		Layout layout = portletInstanceSettingsLocator.getLayout();

		secretIdentifierMap.put(
			_getPreferenceIdentifier(
				key, ExtendedObjectClassDefinition.Scope.PORTLET_INSTANCE,
				StringBundler.concat(
					ownerId, StringPool.SLASH,
					portletInstanceSettingsLocator.getPlid()),
				portletId),
			layout.getCompanyId());
	}

	private void _putScope(
		long companyId, String configurationPid, String key,
		ExtendedObjectClassDefinition.Scope scope, long scopePK,
		Map<String, Long> secretIdentifierMap, String settingsId) {

		secretIdentifierMap.put(
			_getPreferenceIdentifier(key, scope, scopePK, settingsId),
			companyId);
		secretIdentifierMap.put(
			SecretIdentifierUtil.getSecretIdentifier(
				configurationPid, key, scope, scopePK),
			companyId);
	}

	private String _putSecret(
			long scopeCompanyId, Secret secret, SecretContext secretContext)
		throws SecretException {

		KeyReference keyReference = _secretManager.putSecret(
			scopeCompanyId, secret);

		if (secretContext.getType() == SecretContext.Type.ENTITY) {
			return KeyReferenceUtil.toKeyReferenceString(
				secret.getKeyReference());
		}

		return KeyReferenceUtil.toKeyReferenceString(keyReference);
	}

	private void _putSettingsScopes(
			String key, Map<String, Long> secretIdentifierMap,
			SettingsLocator settingsLocator)
		throws PortalException {

		if (settingsLocator instanceof
				CompanyServiceSettingsLocator companyServiceSettingsLocator) {

			_putCompanyScopes(
				companyServiceSettingsLocator.getCompanyId(),
				companyServiceSettingsLocator.getConfigurationPid(), key,
				secretIdentifierMap,
				companyServiceSettingsLocator.getSettingsId());
		}
		else if (settingsLocator instanceof
					GroupServiceSettingsLocator groupServiceSettingsLocator) {

			Group group = _groupLocalService.getGroup(
				groupServiceSettingsLocator.getGroupId());

			_putGroupScopes(
				groupServiceSettingsLocator.getConfigurationPid(), group, key,
				secretIdentifierMap,
				groupServiceSettingsLocator.getSettingsId());

			_putCompanyScopes(
				group.getCompanyId(),
				groupServiceSettingsLocator.getConfigurationPid(), key,
				secretIdentifierMap,
				groupServiceSettingsLocator.getSettingsId());
		}
		else if (settingsLocator instanceof
					PortletInstanceSettingsLocator
						portletInstanceSettingsLocator) {

			_putPortletInstanceScope(
				key, portletInstanceSettingsLocator, secretIdentifierMap);

			Layout layout = portletInstanceSettingsLocator.getLayout();

			Group group = _groupLocalService.getGroup(layout.getGroupId());

			long counterpartGroupId = _getCounterpartGroupId(group);

			String portletId = portletInstanceSettingsLocator.getSettingsId();

			if (counterpartGroupId > 0) {
				Layout counterpartLayout =
					_layoutLocalService.fetchLayoutByUuidAndGroupId(
						layout.getUuid(), counterpartGroupId,
						layout.isPrivateLayout());

				if (counterpartLayout != null) {
					_putPortletInstanceScope(
						key,
						new PortletInstanceSettingsLocator(
							counterpartLayout, portletId),
						secretIdentifierMap);
				}
			}

			Layout layoutSetPrototypeLayout =
				layout.getLayoutSetPrototypeLayout();

			if (layoutSetPrototypeLayout != null) {
				_putPortletInstanceScope(
					key,
					new PortletInstanceSettingsLocator(
						layoutSetPrototypeLayout, portletId),
					secretIdentifierMap);
			}

			String configurationPid =
				portletInstanceSettingsLocator.getConfigurationPid();

			secretIdentifierMap.put(
				SecretIdentifierUtil.getSecretIdentifier(
					configurationPid, key,
					ExtendedObjectClassDefinition.Scope.PORTLET_INSTANCE,
					portletId),
				CompanyConstants.SYSTEM);

			String portletName = PortletIdCodec.decodePortletName(portletId);

			_putGroupScopes(
				configurationPid, group, key, secretIdentifierMap, portletName);
			_putCompanyScopes(
				layout.getCompanyId(), configurationPid, key,
				secretIdentifierMap, portletName);
		}
		else if (settingsLocator instanceof SystemSettingsLocator) {
			_putSystemScope(
				settingsLocator.getSettingsId(), key, secretIdentifierMap);
		}
		else {
			throw new IllegalArgumentException(
				"Unsupported settings locator " + settingsLocator.getClass());
		}
	}

	private void _putSystemScope(
		String configurationPid, String key,
		Map<String, Long> secretIdentifierMap) {

		secretIdentifierMap.put(
			SecretIdentifierUtil.getSecretIdentifier(
				_toOCDPid(configurationPid), key,
				ExtendedObjectClassDefinition.Scope.SYSTEM,
				CompanyConstants.SYSTEM),
			CompanyConstants.SYSTEM);
		secretIdentifierMap.put("property/" + key, CompanyConstants.SYSTEM);
	}

	private void _putTypeSettingsScope(
		long companyId, long groupId, String key,
		Map<String, Long> secretIdentifierMap) {

		secretIdentifierMap.put(
			StringBundler.concat(
				"typesettings/group/", groupId, StringPool.SLASH, key),
			companyId);
	}

	private String _toOCDPid(String configurationPid) {
		ConfigurationPidMapping configurationPidMapping =
			_settingsLocatorHelper.getConfigurationPidMapping(configurationPid);

		if (configurationPidMapping == null) {
			return configurationPid;
		}

		Class<?> clazz = configurationPidMapping.getConfigurationBeanClass();

		if (clazz.getAnnotation(Settings.Config.class) != null) {
			return configurationPid;
		}

		Meta.OCD ocd = clazz.getAnnotation(Meta.OCD.class);

		if (ocd == null) {
			return configurationPid;
		}

		return ocd.id();
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private KeyManagerProfileRegistry _keyManagerProfileRegistry;

	@Reference
	private LayoutLocalService _layoutLocalService;

	private PortalCache<String, String> _portalCache;

	@Reference
	private SecretManager _secretManager;

	@Reference
	private SettingsLocatorHelper _settingsLocatorHelper;

}