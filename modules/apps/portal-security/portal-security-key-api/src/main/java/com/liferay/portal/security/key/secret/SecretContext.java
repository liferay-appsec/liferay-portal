/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Caio Farias
 */
public class SecretContext {

	public static SecretContext createCompanySecretContext(
		long companyId, String key) {

		return new SecretContext(
			null, companyId, 0, key, 0, null, null, Type.COMPANY);
	}

	public static SecretContext createConfigurationSecretContext(
		String key, Dictionary<String, ?> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties are null");
		}

		Map<String, Object> propertiesMap = new HashMap<>();

		Enumeration<String> enumeration = properties.keys();

		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();

			propertiesMap.put(name, properties.get(name));
		}

		return createConfigurationSecretContext(key, propertiesMap);
	}

	public static SecretContext createConfigurationSecretContext(
		String key, Map<String, ?> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties are null");
		}

		return new SecretContext(
			null, 0, 0, key, 0,
			Collections.unmodifiableMap(new HashMap<>(properties)), null,
			Type.CONFIGURATION);
	}

	public static SecretContext createEntitySecretContext(
		Class<?> clazz, long companyId, String key, long primaryKey) {

		if (clazz == null) {
			throw new IllegalArgumentException("Class is null");
		}

		return new SecretContext(
			clazz.getName(), companyId, 0, key, primaryKey, null, null,
			Type.ENTITY);
	}

	public static SecretContext createGroupSecretContext(
		long groupId, String key) {

		return new SecretContext(
			null, 0, groupId, key, 0, null, null, Type.GROUP);
	}

	public static SecretContext createSettingsSecretContext(
		String key, SettingsLocator settingsLocator) {

		if (settingsLocator == null) {
			throw new IllegalArgumentException("Settings locator is null");
		}

		return new SecretContext(
			null, 0, 0, key, 0, null, settingsLocator, Type.SETTINGS);
	}

	public String getClassName() {
		return _className;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public long getGroupId() {
		return _groupId;
	}

	public String getKey() {
		return _key;
	}

	public long getPrimaryKey() {
		return _primaryKey;
	}

	public Map<String, Object> getProperties() {
		return _properties;
	}

	public SettingsLocator getSettingsLocator() {
		return _settingsLocator;
	}

	public Type getType() {
		return _type;
	}

	public enum Type {

		COMPANY, CONFIGURATION, ENTITY, GROUP, SETTINGS

	}

	private SecretContext(
		String className, long companyId, long groupId, String key,
		long primaryKey, Map<String, Object> properties,
		SettingsLocator settingsLocator, Type type) {

		if (Validator.isNull(key)) {
			throw new IllegalArgumentException("Key is null");
		}

		_className = className;
		_companyId = companyId;
		_groupId = groupId;
		_key = key;
		_primaryKey = primaryKey;
		_properties = properties;
		_settingsLocator = settingsLocator;
		_type = type;
	}

	private final String _className;
	private final long _companyId;
	private final long _groupId;
	private final String _key;
	private final long _primaryKey;
	private final Map<String, Object> _properties;
	private final SettingsLocator _settingsLocator;
	private final Type _type;

}