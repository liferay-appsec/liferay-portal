/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Christopher Kian
 */
public class AWSArnResolver {

	public static String resolve(
		String template, String region, String accountId, long companyId,
		String identifier) {

		if (Validator.isNull(template) || (identifier == null) ||
			identifier.startsWith("arn:") || identifier.startsWith("alias/")) {

			return identifier;
		}

		String resolved = template;

		if (region != null) {
			resolved = StringUtil.replace(resolved, "{region}", region);
		}

		if (accountId != null) {
			resolved = StringUtil.replace(resolved, "{accountId}", accountId);
		}

		resolved = StringUtil.replace(
			resolved, "{companyId}", String.valueOf(companyId));
		resolved = StringUtil.replace(resolved, "{identifier}", identifier);

		return resolved;
	}

}