/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.configuration.definition;

import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;

/**
 * @author Christopher Kian
 */
public abstract class BaseAWSConfigurationPidMapping
	implements ConfigurationPidMapping {

	public BaseAWSConfigurationPidMapping(
		Class<?> configurationBeanClass, String configurationPid) {

		_configurationBeanClass = configurationBeanClass;
		_configurationPid = configurationPid;
	}

	@Override
	public Class<?> getConfigurationBeanClass() {
		return _configurationBeanClass;
	}

	@Override
	public String getConfigurationPid() {
		return _configurationPid;
	}

	private final Class<?> _configurationBeanClass;
	private final String _configurationPid;

}