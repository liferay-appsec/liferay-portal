/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import java.util.Map;

/**
 * @author Pedro Victor Silvestre
 */
public class ConfigurationSecretContext extends SecretContext {

	public Map<String, Object> getProperties() {
		return _properties;
	}

	protected ConfigurationSecretContext(
		String key, Map<String, Object> properties) {

		super(key);

		_properties = properties;
	}

	private final Map<String, Object> _properties;

}