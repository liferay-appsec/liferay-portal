/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import com.liferay.portal.kernel.settings.SettingsLocator;

/**
 * @author Pedro Victor Silvestre
 */
public class SettingsSecretContext extends SecretContext {

	public SettingsLocator getSettingsLocator() {
		return _settingsLocator;
	}

	protected SettingsSecretContext(
		String key, SettingsLocator settingsLocator) {

		super(key);

		_settingsLocator = settingsLocator;
	}

	private final SettingsLocator _settingsLocator;

}