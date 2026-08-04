/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.provider.aws.internal.fips;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.ServiceIndicator;
import com.liferay.portal.security.key.crypto.exception.CryptoException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christopher Kian
 */
public abstract class BaseAWSFipsValidator {

	public BaseAWSFipsValidator(String cipherMode, boolean useFipsEndpoint) {
		_cipherMode = cipherMode;
		_useFipsEndpoint = useFipsEndpoint;

		_fipsEnforced =
			useFipsEndpoint ||
			GetterUtil.getBoolean(System.getenv(_FIPS_ENFORCED_ENV_NAME));
	}

	public boolean isFipsEnforced() {
		return _fipsEnforced;
	}

	public boolean isUseFipsEndpoint() {
		return _useFipsEndpoint;
	}

	public ServiceIndicator toServiceIndicator(
			boolean fipsApproved, String securityFunctionName)
		throws CryptoException {

		if (_fipsEnforced && !fipsApproved) {
			throw new CryptoException(
				"Security function \"" + securityFunctionName +
					"\" is not FIPS approved under strict mode");
		}

		return new ServiceIndicator(fipsApproved, securityFunctionName);
	}

	public void validateCipherMode() throws CryptoException {
		if (_fipsEnforced && !_isAEADCipherMode(_cipherMode)) {
			throw new CryptoException(
				"FIPS enforcement requires an AEAD cipher mode but \"" +
					_cipherMode + "\" is not AEAD");
		}
	}

	private boolean _isAEADCipherMode(String cipherMode) {
		if (Validator.isNull(cipherMode)) {
			return false;
		}

		return _aeadCipherModes.contains(StringUtil.toUpperCase(cipherMode));
	}

	private static final String _FIPS_ENFORCED_ENV_NAME =
		"LIFERAY_KEYMANAGER_FIPS_ENFORCED";

	private static final Set<String> _aeadCipherModes = new HashSet<>(
		Arrays.asList("AES_256_GCM", "AES_GCM", "SYMMETRIC_DEFAULT"));

	private final String _cipherMode;
	private final boolean _fipsEnforced;
	private final boolean _useFipsEndpoint;

}