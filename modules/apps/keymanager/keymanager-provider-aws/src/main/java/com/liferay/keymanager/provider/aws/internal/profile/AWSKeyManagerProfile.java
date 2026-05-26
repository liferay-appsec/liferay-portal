/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.profile;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.liferay.keymanager.provider.aws.internal.configuration.AWSKMSSystemCryptoVaultProviderConfiguration;
import com.liferay.keymanager.provider.aws.internal.configuration.AWSSecretsManagerSystemSecretVaultProviderConfiguration;
import com.liferay.keymanager.provider.aws.internal.profile.configuration.AWSKeyManagerProfileConfiguration;
import com.liferay.keymanager.provider.aws.internal.util.AWSAccountUtil;
import com.liferay.keymanager.provider.aws.internal.util.AWSRegionUtil;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christopher Kian
 */
@Component(
	configurationPid = "com.liferay.keymanager.provider.aws.internal.profile.configuration.AWSKeyManagerProfileConfiguration",
	property = "keymanager.profile.id=aws", service = KeyManagerProfile.class
)
public class AWSKeyManagerProfile implements KeyManagerProfile {

	@Override
	public void bootstrap() throws Exception {
		_validateCredentials();

		String region = _resolveRegion();
		String accountId = _resolveAccountId();

		if (Validator.isNull(region)) {
			throw new IllegalStateException(
				"Unable to resolve AWS region; configure aws-region or " +
					"export AWS_REGION");
		}

		boolean strictMode = _awsKeyManagerProfileConfiguration.strictMode();

		if (strictMode && _log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"AWS profile bootstrap: strict mode enabled, FIPS ",
					"endpoint required (kms-fips.", region, ".amazonaws.com)"));
		}
		else if (_log.isWarnEnabled()) {
			_log.warn(
				"AWS profile bootstrap: standard mode; software-backed CMKs " +
					"will be provisioned. This is not Strict-Mode compliant.");
		}

		_updateProviderConfiguration(
			AWSKMSSystemCryptoVaultProviderConfiguration.class.getName(),
			region, accountId, strictMode);

		if (Objects.equals(
				_awsKeyManagerProfileConfiguration.secretLayer(), "aws")) {

			_updateProviderConfiguration(
				AWSSecretsManagerSystemSecretVaultProviderConfiguration.class.
					getName(),
				region, accountId, strictMode);
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"AWS profile bootstrap complete: region=", region,
					", accountId=", accountId, ", strictMode=", strictMode));
		}
	}

	@Override
	public String getCompanyDekProviderId() {
		return "aws-company-crypto";
	}

	@Override
	public String getCompanyKekProviderId() {
		return "aws-company-crypto";
	}

	@Override
	public String getCompanySecretProviderId() {
		if (Objects.equals(
				_awsKeyManagerProfileConfiguration.secretLayer(), "db")) {

			return "db-company-secret";
		}

		return "aws-company-secret";
	}

	@Override
	public String getProfileId() {
		return "aws";
	}

	@Override
	public String getSystemDekProviderId() {
		return "aws-system-crypto";
	}

	@Override
	public String getSystemKekProviderId() {
		return "aws-system-crypto";
	}

	@Override
	public String getSystemSecretProviderId() {
		if (Objects.equals(
				_awsKeyManagerProfileConfiguration.secretLayer(), "db")) {

			return "db-system-secret";
		}

		return "aws-system-secret";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_awsKeyManagerProfileConfiguration =
			ConfigurableUtil.createConfigurable(
				AWSKeyManagerProfileConfiguration.class, properties);
	}

	private String _resolveAccountId() {
		String configured = _awsKeyManagerProfileConfiguration.awsAccountId();

		return AWSAccountUtil.inferAccountId(configured);
	}

	private String _resolveRegion() {
		return AWSRegionUtil.resolve(
			_awsKeyManagerProfileConfiguration.awsRegion());
	}

	private void _updateProviderConfiguration(
			String pid, String region, String accountId, boolean strictMode)
		throws Exception {

		Configuration configuration = _configurationAdmin.getConfiguration(
			pid, "?");

		Dictionary<String, Object> properties = configuration.getProperties();

		if (properties == null) {
			properties = new Hashtable<>();
		}

		properties.put("awsRegion", region);

		if (accountId != null) {
			properties.put("awsAccountId", accountId);
		}

		properties.put("enabled", Boolean.TRUE);
		properties.put("useFipsEndpoint", strictMode);

		configuration.update(properties);
	}

	private void _validateCredentials() throws Exception {
		try {
			DefaultAWSCredentialsProviderChain.getInstance(
			).getCredentials();
		}
		catch (Exception exception) {
			throw new IllegalStateException(
				StringBundler.concat(
					"AWS credentials are not available via the default ",
					"credential provider chain (env, EC2 instance profile, ",
					"IRSA): ", exception.getMessage()),
				exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AWSKeyManagerProfile.class);

	private volatile AWSKeyManagerProfileConfiguration
		_awsKeyManagerProfileConfiguration;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

}