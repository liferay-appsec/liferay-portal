/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.click.to.chat.web.internal.configuration;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.util.Validator;

import java.util.Objects;

/**
 * @author José Abelenda
 */
public class ClickToChatConfigurationUtil {

	public static ClickToChatConfiguration getClickToChatConfiguration(
		long companyId, long groupId) {

		try {
			return ConfigurationProviderUtil.getConfiguration(
				ClickToChatConfiguration.class,
				getSettingsLocator(companyId, groupId));
		}
		catch (ConfigurationException configurationException) {
			return ReflectionUtil.throwException(configurationException);
		}
	}

	public static SettingsLocator getSettingsLocator(
		long companyId, long groupId) {

		try {
			SettingsLocator companySettingsLocator =
				new CompanyServiceSettingsLocator(
					companyId, ClickToChatConfiguration.class.getName());

			ClickToChatConfiguration companyClickToChatConfiguration =
				ConfigurationProviderUtil.getConfiguration(
					ClickToChatConfiguration.class, companySettingsLocator);

			if ((groupId == 0) ||
				Objects.equals(
					companyClickToChatConfiguration.siteSettingsStrategy(),
					"always-inherit")) {

				return companySettingsLocator;
			}

			SettingsLocator groupSettingsLocator =
				new GroupServiceSettingsLocator(
					companyId, groupId,
					ClickToChatConfiguration.class.getName(),
					ClickToChatConfiguration.class.getName());

			if (Objects.equals(
					companyClickToChatConfiguration.siteSettingsStrategy(),
					"always-override")) {

				return groupSettingsLocator;
			}

			if (Objects.equals(
					companyClickToChatConfiguration.siteSettingsStrategy(),
					"inherit-or-override")) {

				ClickToChatConfiguration groupClickToChatConfiguration =
					ConfigurationProviderUtil.getConfiguration(
						ClickToChatConfiguration.class, groupSettingsLocator);

				if (Validator.isNotNull(
						groupClickToChatConfiguration.
							chatProviderAccountId()) &&
					Validator.isNotNull(
						groupClickToChatConfiguration.chatProviderId())) {

					return groupSettingsLocator;
				}

				return companySettingsLocator;
			}

			return companySettingsLocator;
		}
		catch (ConfigurationException configurationException) {
			return ReflectionUtil.throwException(configurationException);
		}
	}

}