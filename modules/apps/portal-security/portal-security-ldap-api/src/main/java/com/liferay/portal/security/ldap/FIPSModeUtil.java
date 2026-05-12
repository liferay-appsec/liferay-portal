/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap;

import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Jorge García Jiménez
 */
public class FIPSModeUtil {

	public static boolean isApprovedPasswordAlgorithm(String algorithm) {
		if (Validator.isNull(algorithm)) {
			return false;
		}

		String normalizedString = StringUtil.toUpperCase(algorithm);

		if (normalizedString.startsWith("PBKDF2") ||
			normalizedString.equals("SHA-256") ||
			normalizedString.equals("SHA-384") ||
			normalizedString.equals("SHA-512")) {

			return true;
		}

		return false;
	}

	public static boolean isEnabled() {
		return PropsValues.FIPS_ENABLED;
	}

}