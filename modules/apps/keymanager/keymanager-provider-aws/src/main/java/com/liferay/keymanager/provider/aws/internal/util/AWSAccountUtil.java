/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Christopher Kian
 */
public class AWSAccountUtil {

	public static String getCallerIdentityArn(String region) {
		AWSSecurityTokenService awsSecurityTokenService = _getStsClient(region);

		try {
			GetCallerIdentityResult getCallerIdentityResult =
				awsSecurityTokenService.getCallerIdentity(
					new GetCallerIdentityRequest());

			return getCallerIdentityResult.getArn();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to obtain AWS caller identity ARN: " +
						exception.getMessage(),
					exception);
			}

			return null;
		}
		finally {
			awsSecurityTokenService.shutdown();
		}
	}

	public static String inferAccountId(String configuredAccountId) {
		if (Validator.isNotNull(configuredAccountId)) {
			return configuredAccountId;
		}

		AWSSecurityTokenService awsSecurityTokenService = _getStsClient(null);

		try {
			GetCallerIdentityResult getCallerIdentityResult =
				awsSecurityTokenService.getCallerIdentity(
					new GetCallerIdentityRequest());

			return getCallerIdentityResult.getAccount();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to infer AWS account ID via STS: " +
						exception.getMessage(),
					exception);
			}

			return null;
		}
		finally {
			awsSecurityTokenService.shutdown();
		}
	}

	private static AWSSecurityTokenService _getStsClient(String region) {
		AWSSecurityTokenServiceClientBuilder
			awsSecurityTokenServiceClientBuilder =
				AWSSecurityTokenServiceClientBuilder.standard(
				).withCredentials(
					DefaultAWSCredentialsProviderChain.getInstance()
				);

		if (Validator.isNotNull(region)) {
			awsSecurityTokenServiceClientBuilder.withRegion(region);
		}

		return awsSecurityTokenServiceClientBuilder.build();
	}

	private static final Log _log = LogFactoryUtil.getLog(AWSAccountUtil.class);

}