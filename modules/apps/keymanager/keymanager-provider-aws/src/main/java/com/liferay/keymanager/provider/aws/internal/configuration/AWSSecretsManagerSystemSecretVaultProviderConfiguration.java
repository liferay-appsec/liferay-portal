/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Christopher Kian
 */
@ExtendedObjectClassDefinition(category = "keymanager")
@Meta.OCD(
	id = "com.liferay.keymanager.provider.aws.internal.configuration.AWSSecretsManagerSystemSecretVaultProviderConfiguration",
	localization = "content/Language",
	name = "aws-secrets-manager-system-secret-vault-provider-configuration-name"
)
public interface AWSSecretsManagerSystemSecretVaultProviderConfiguration {

	@Meta.AD(deflt = "", name = "aws-account-id", required = false)
	public String awsAccountId();

	@Meta.AD(deflt = "", name = "aws-region", required = false)
	public String awsRegion();

	@Meta.AD(deflt = "false", name = "enabled", required = false)
	public boolean enabled();

	@Meta.AD(
		deflt = "arn:aws:secretsmanager:{region}:{accountId}:secret:liferay-system-{identifier}",
		name = "secret-arn-template", required = false
	)
	public String secretArnTemplate();

	@Meta.AD(deflt = "false", name = "use-fips-endpoint", required = false)
	public boolean useFipsEndpoint();

}