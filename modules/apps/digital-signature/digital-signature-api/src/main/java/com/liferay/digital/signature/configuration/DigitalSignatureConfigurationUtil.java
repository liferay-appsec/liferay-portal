/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.configuration;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.util.Validator;

import java.util.Objects;

/**
 * @author José Abelenda
 */
public class DigitalSignatureConfigurationUtil {

	public static DigitalSignatureConfiguration
		getDigitalSignatureConfiguration(long companyId, long groupId) {

		try {
			return ConfigurationProviderUtil.getConfiguration(
				DigitalSignatureConfiguration.class,
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
					companyId, DigitalSignatureConfiguration.class.getName());

			DigitalSignatureConfiguration companyDigitalSignatureConfiguration =
				ConfigurationProviderUtil.getConfiguration(
					DigitalSignatureConfiguration.class,
					companySettingsLocator);

			if ((groupId == 0) ||
				Objects.equals(
					companyDigitalSignatureConfiguration.siteSettingsStrategy(),
					"always-inherit")) {

				return companySettingsLocator;
			}

			SettingsLocator groupSettingsLocator =
				new GroupServiceSettingsLocator(
					companyId, groupId,
					DigitalSignatureConfiguration.class.getName(),
					DigitalSignatureConfiguration.class.getName());

			if (Objects.equals(
					companyDigitalSignatureConfiguration.siteSettingsStrategy(),
					"always-override")) {

				return groupSettingsLocator;
			}

			if (Objects.equals(
					companyDigitalSignatureConfiguration.siteSettingsStrategy(),
					"inherit-or-override")) {

				DigitalSignatureConfiguration
					groupDigitalSignatureConfiguration =
						ConfigurationProviderUtil.getConfiguration(
							DigitalSignatureConfiguration.class,
							groupSettingsLocator);

				if (Validator.isNotNull(
						groupDigitalSignatureConfiguration.apiUsername()) &&
					Validator.isNotNull(
						groupDigitalSignatureConfiguration.apiAccountId()) &&
					Validator.isNotNull(
						groupDigitalSignatureConfiguration.accountBaseURI()) &&
					Validator.isNotNull(
						groupDigitalSignatureConfiguration.environment()) &&
					Validator.isNotNull(
						groupDigitalSignatureConfiguration.integrationKey()) &&
					Validator.isNotNull(
						groupDigitalSignatureConfiguration.rsaPrivateKey())) {

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

	public static boolean isEmbeddedViewEnabled(long companyId, long groupId) {
		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			getDigitalSignatureConfiguration(companyId, groupId);

		return digitalSignatureConfiguration.embeddedViewEnabled();
	}

}