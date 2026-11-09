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

		return new SecretContext(companyId, key, null, null, Type.COMPANY);
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
			0, key, Collections.unmodifiableMap(new HashMap<>(properties)),
			null, Type.CONFIGURATION);
	}

	public static SecretContext createSettingsSecretContext(
		String key, SettingsLocator settingsLocator) {

		if (settingsLocator == null) {
			throw new IllegalArgumentException("Settings locator is null");
		}

		return new SecretContext(0, key, null, settingsLocator, Type.SETTINGS);
	}

	public long getCompanyId() {
		return _companyId;
	}

	public String getKey() {
		return _key;
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

		COMPANY, CONFIGURATION, SETTINGS

	}

	private SecretContext(
		long companyId, String key, Map<String, Object> properties,
		SettingsLocator settingsLocator, Type type) {

		if (Validator.isNull(key)) {
			throw new IllegalArgumentException("Key is null");
		}

		_companyId = companyId;
		_key = key;
		_properties = properties;
		_settingsLocator = settingsLocator;
		_type = type;
	}

	private final long _companyId;
	private final String _key;
	private final Map<String, Object> _properties;
	private final SettingsLocator _settingsLocator;
	private final Type _type;

}