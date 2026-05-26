/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.secret;

import com.liferay.keymanager.spi.secret.SecretVaultWriter;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Christopher Kian
 */
@Component(
	configurationPid = "com.liferay.keymanager.provider.aws.internal.configuration.AWSSecretsManagerCompanySecretVaultProviderConfiguration",
	property = "keymanager.provider.id=aws-company-secret",
	service = SecretVaultWriter.class
)
public class AWSSecretsManagerCompanySecretVaultWriter
	extends BaseAWSSecretsManagerSecretVaultProvider {

	@Override
	public boolean isAllowedCompany(long companyId) {
		if ((companyId > 0L) && (companyId == getConfiguredCompanyId())) {
			return true;
		}

		return false;
	}

	@Activate
	@Modified
	@Override
	protected void activate(Map<String, Object> properties) {
		setProviderId("aws-company-secret");

		super.activate(properties);
	}

}