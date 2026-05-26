/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.amazonaws.regions.DefaultAwsRegionProviderChain;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Christopher Kian
 */
public class AWSRegionUtil {

	public static String resolve(String configuredRegion) {
		if (Validator.isNotNull(configuredRegion)) {
			return configuredRegion;
		}

		try {
			DefaultAwsRegionProviderChain regionProviderChain =
				new DefaultAwsRegionProviderChain();

			return regionProviderChain.getRegion();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to infer AWS region from environment: " +
						exception.getMessage(),
					exception);
			}

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(AWSRegionUtil.class);

}