/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.configuration.persistence.listener;

import com.liferay.keymanager.provider.aws.internal.configuration.AWSKMSCompanyCryptoVaultProviderConfiguration;
import com.liferay.keymanager.spi.configuration.listener.BaseConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;

import org.osgi.service.component.annotations.Component;

/**
 * @author Christopher Kian
 */
@Component(
	property = "model.class.name=com.liferay.keymanager.provider.aws.internal.configuration.AWSKMSCompanyCryptoVaultProviderConfiguration",
	service = ConfigurationModelListener.class
)
public class AWSKMSCryptoVaultProviderConfigurationModelListener
	extends BaseConfigurationModelListener
		<AWSKMSCompanyCryptoVaultProviderConfiguration> {

	public AWSKMSCryptoVaultProviderConfigurationModelListener() {
		super(AWSKMSCompanyCryptoVaultProviderConfiguration.class);
	}

}