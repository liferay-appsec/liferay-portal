/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.util;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;

import java.security.MessageDigest;

import javax.crypto.SecretKeyFactory;

/**
 * @author Rafael Praxedes
 */
public class PasswordEncryptionAlgorithmUtil {

	public static boolean isAllowed(String algorithm) {
		if (!PropsValues.FIPS_ENABLED) {
			return true;
		}

		if (Validator.isNull(algorithm)) {
			return false;
		}

		String name = algorithm;

		int index = algorithm.indexOf(CharPool.SLASH);

		if (index != -1) {
			name = algorithm.substring(0, index);
		}

		if (CryptoUtil.isAllowedAlgorithm(name, MessageDigest.class) ||
			CryptoUtil.isAllowedAlgorithm(name, SecretKeyFactory.class)) {

			return true;
		}

		return false;
	}

	public static void validate(String algorithm) {
		if (!isAllowed(algorithm)) {
			throw new SecurityException(
				"Algorithm \"" + algorithm + "\" is not allowed in FIPS mode");
		}
	}

}