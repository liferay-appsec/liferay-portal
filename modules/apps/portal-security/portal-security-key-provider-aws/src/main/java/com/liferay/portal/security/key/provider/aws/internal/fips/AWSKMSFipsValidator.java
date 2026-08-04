/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.provider.aws.internal.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.ServiceIndicator;
import com.liferay.portal.security.key.crypto.exception.CryptoException;

import java.util.Objects;

/**
 * @author Christopher Kian
 */
public class AWSKMSFipsValidator extends BaseAWSFipsValidator {

	public AWSKMSFipsValidator(String cipherMode, boolean useFipsEndpoint) {
		super(cipherMode, useFipsEndpoint);
	}

	public boolean isFipsApprovedKeyOrigin(String keyOrigin) {
		if (Objects.equals(keyOrigin, "AWS_CLOUDHSM")) {
			return true;
		}

		return isUseFipsEndpoint();
	}

	public ServiceIndicator toServiceIndicator(
			String keyOrigin, String securityFunctionName)
		throws CryptoException {

		return toServiceIndicator(
			isFipsApprovedKeyOrigin(keyOrigin), securityFunctionName);
	}

	public void validateKeyOrigin(String keyOrigin) throws CryptoException {
		if (!isFipsEnforced()) {
			return;
		}

		if (Validator.isNull(keyOrigin) ||
			!isFipsApprovedKeyOrigin(keyOrigin)) {

			throw new CryptoException(
				StringBundler.concat(
					"FIPS enforcement requires an HSM backed CMK (Origin ",
					"AWS_CLOUDHSM) or a FIPS KMS endpoint but the key Origin ",
					"is \"", keyOrigin, "\""));
		}
	}

}