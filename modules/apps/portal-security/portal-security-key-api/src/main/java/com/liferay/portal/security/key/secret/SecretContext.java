/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Caio Farias
 */
public abstract class SecretContext {

	public static ConfigurationSecretContext fromConfiguration(
		String key, Dictionary<String, ?> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties are null");
		}

		Map<String, Object> propertiesMap = new HashMap<>();

		for (String name : Collections.list(properties.keys())) {
			propertiesMap.put(name, properties.get(name));
		}

		return fromConfiguration(key, propertiesMap);
	}

	public static ConfigurationSecretContext fromConfiguration(
		String key, Map<String, ?> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties are null");
		}

		return new ConfigurationSecretContext(
			key, Collections.unmodifiableMap(properties));
	}

	public static SettingsSecretContext fromSettings(
		String key, SettingsLocator settingsLocator) {

		if (settingsLocator == null) {
			throw new IllegalArgumentException("Settings locator is null");
		}

		return new SettingsSecretContext(key, settingsLocator);
	}

	public String getKey() {
		return _key;
	}

	protected SecretContext(String key) {
		if (Validator.isNull(key)) {
			throw new IllegalArgumentException("Key is null");
		}

		_key = key;
	}

	private final String _key;

}