/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.secret;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.FilterNameStringType;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.provider.aws.internal.util.AWSArnResolver;
import com.liferay.keymanager.provider.aws.internal.util.AWSByteBufferUtil;
import com.liferay.keymanager.provider.aws.internal.util.AWSClientManager;
import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;
import com.liferay.keymanager.spi.secret.SecretVaultReader;
import com.liferay.keymanager.spi.secret.SecretVaultWriter;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Christopher Kian
 */
public abstract class BaseAWSSecretsManagerSecretVaultProvider
	implements SecretVaultReader, SecretVaultWriter {

	@Override
	public void deleteSecret(long companyId, String identifier)
		throws SecretManagerException {

		_checkPermission(companyId);

		String secretArn = _resolveSecretArn(companyId, identifier);

		try {
			_awsClientManager.execute(
				awsSecretsManager -> awsSecretsManager.deleteSecret(
					new DeleteSecretRequest(
					).withSecretId(
						secretArn
					).withForceDeleteWithoutRecovery(
						Boolean.FALSE
					).withRecoveryWindowInDays(
						7L
					)));
		}
		catch (Exception exception) {
			throw new SecretManagerException(
				StringBundler.concat(
					"Unable to delete AWS secret ", secretArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public SecureSecret getSecret(long companyId, String identifier)
		throws SecretManagerException {

		_checkPermission(companyId);

		String secretArn = _resolveSecretArn(companyId, identifier);

		try {
			GetSecretValueResult getSecretValueResult =
				_awsClientManager.execute(
					awsSecretsManager -> awsSecretsManager.getSecretValue(
						new GetSecretValueRequest(
						).withSecretId(
							secretArn
						)));

			KeyReference keyReference = new KeyReference(
				identifier, _providerId, KeyReference.Type.SECRET);

			String secretString = getSecretValueResult.getSecretString();

			if (secretString != null) {
				return new SecureSecret(keyReference, secretString);
			}

			byte[] bytes = AWSByteBufferUtil.consumeAndZero(
				getSecretValueResult.getSecretBinary());

			try {
				return new SecureSecret(bytes, keyReference);
			}
			finally {
				Arrays.fill(bytes, (byte)0);
			}
		}
		catch (ResourceNotFoundException resourceNotFoundException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"AWS secret " + secretArn + " not found",
					resourceNotFoundException);
			}

			return null;
		}
		catch (Exception exception) {
			throw new SecretManagerException(
				StringBundler.concat(
					"Unable to read AWS secret ", secretArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public List<String> getSecretIdentifiers(long companyId)
		throws SecretManagerException {

		_checkPermission(companyId);

		String namePrefix = _resolveNamePrefix(companyId);

		if (Validator.isNull(namePrefix)) {
			return new ArrayList<>();
		}

		try {
			return _awsClientManager.execute(
				awsSecretsManager -> {
					List<String> names = new ArrayList<>();

					String nextToken = null;

					Filter filter = new Filter(
					).withKey(
						FilterNameStringType.Name
					).withValues(
						namePrefix
					);

					do {
						ListSecretsRequest listSecretsRequest =
							new ListSecretsRequest(
							).withFilters(
								filter
							).withMaxResults(
								100
							);

						if (nextToken != null) {
							listSecretsRequest.setNextToken(nextToken);
						}

						ListSecretsResult listSecretsResult =
							awsSecretsManager.listSecrets(listSecretsRequest);

						for (SecretListEntry secretListEntry :
								listSecretsResult.getSecretList()) {

							String name = secretListEntry.getName();

							if ((name != null) && name.startsWith(namePrefix)) {
								names.add(name.substring(namePrefix.length()));
							}
						}

						nextToken = listSecretsResult.getNextToken();
					}
					while (nextToken != null);

					return names;
				});
		}
		catch (Exception exception) {
			throw new SecretManagerException(
				StringBundler.concat(
					"Unable to list AWS secrets under prefix ", namePrefix,
					": ", exception.getMessage()),
				exception);
		}
	}

	@Override
	public boolean isAllowedCompany(long companyId) {
		return false;
	}

	@Override
	public void putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException {

		_checkPermission(companyId);

		KeyReference keyReference = secureSecret.getKeyReference();

		String secretArn = _resolveSecretArn(
			companyId, keyReference.getIdentifier());

		byte[] bytes = secureSecret.getBytes();

		try {
			_awsClientManager.execute(
				awsSecretsManager -> {
					try {
						return awsSecretsManager.putSecretValue(
							new PutSecretValueRequest(
							).withSecretBinary(
								ByteBuffer.wrap(bytes)
							).withSecretId(
								secretArn
							));
					}
					catch (ResourceNotFoundException
								resourceNotFoundException) {

						if (_log.isDebugEnabled()) {
							_log.debug(
								"Creating AWS secret " + secretArn,
								resourceNotFoundException);
						}

						return awsSecretsManager.createSecret(
							new CreateSecretRequest(
							).withName(
								_toSecretName(secretArn)
							).withSecretBinary(
								ByteBuffer.wrap(bytes)
							));
					}
				});
		}
		catch (Exception exception) {
			throw new SecretManagerException(
				StringBundler.concat(
					"Unable to write AWS secret ", secretArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	protected void activate(Map<String, Object> properties) {
		_companyId = GetterUtil.getLong(properties.get("companyId"));
		_secretArnTemplate = GetterUtil.getString(
			properties.get("secretArnTemplate"));
		_region = GetterUtil.getString(properties.get("awsRegion"));
		_accountId = GetterUtil.getString(properties.get("awsAccountId"));

		boolean useFipsEndpoint = GetterUtil.getBoolean(
			properties.get("useFipsEndpoint"));

		AWSClientManager<AWSSecretsManager> existingAWSClientManager =
			_awsClientManager;

		if (existingAWSClientManager != null) {
			existingAWSClientManager.updateConfiguration(
				_region, useFipsEndpoint);
		}
		else {
			String fipsEndpointTemplate =
				"secretsmanager-fips.{region}.amazonaws.com";

			_awsClientManager = new AWSClientManager<>(
				_region, useFipsEndpoint, fipsEndpointTemplate,
				(endpointConfiguration, region, credentialsProvider) -> {
					AWSSecretsManagerClientBuilder
						awsSecretsManagerClientBuilder =
							AWSSecretsManagerClientBuilder.standard(
							).withCredentials(
								credentialsProvider
							);

					if (endpointConfiguration != null) {
						awsSecretsManagerClientBuilder.
							withEndpointConfiguration(endpointConfiguration);
					}
					else if (Validator.isNotNull(region)) {
						awsSecretsManagerClientBuilder.withRegion(region);
					}

					return awsSecretsManagerClientBuilder.build();
				});
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"Activated ", _providerId, " for company ", _companyId,
					" in region ", _region));
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_awsClientManager != null) {
			_awsClientManager.close();

			_awsClientManager = null;
		}
	}

	protected long getConfiguredCompanyId() {
		return _companyId;
	}

	protected void setProviderId(String providerId) {
		_providerId = providerId;
	}

	private void _checkPermission(long companyId)
		throws SecretManagerException {

		if (!isAllowedCompany(companyId)) {
			throw new SecretManagerException(
				StringBundler.concat(
					"Provider ", _providerId, " does not handle company ID ",
					companyId));
		}
	}

	private String _resolveNamePrefix(long companyId) {
		if (Validator.isNull(_secretArnTemplate)) {
			return null;
		}

		String resolved = AWSArnResolver.resolve(
			_secretArnTemplate, _region, _accountId, companyId, "");

		return _toSecretName(resolved);
	}

	private String _resolveSecretArn(long companyId, String identifier) {
		return AWSArnResolver.resolve(
			_secretArnTemplate, _region, _accountId, companyId, identifier);
	}

	private String _toSecretName(String secretIdOrArn) {
		if (secretIdOrArn == null) {
			return null;
		}

		int index = secretIdOrArn.indexOf(":secret:");

		if (index >= 0) {
			return secretIdOrArn.substring(index + ":secret:".length());
		}

		return secretIdOrArn;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BaseAWSSecretsManagerSecretVaultProvider.class);

	private volatile String _accountId;
	private volatile AWSClientManager<AWSSecretsManager> _awsClientManager;
	private volatile long _companyId;
	private volatile String _providerId;
	private volatile String _region;
	private volatile String _secretArnTemplate;

}