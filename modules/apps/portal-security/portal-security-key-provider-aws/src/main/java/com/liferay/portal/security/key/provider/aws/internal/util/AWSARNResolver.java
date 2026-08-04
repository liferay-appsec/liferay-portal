/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.provider.aws.internal.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Christopher Kian
 */
public class AWSARNResolver {

	public static String resolve(
		String accountId, String arnTemplate, long companyId, String identifier,
		String region) {

		if (Validator.isNull(arnTemplate) || (identifier == null) ||
			identifier.startsWith("arn:") || identifier.startsWith("alias/")) {

			return identifier;
		}

		if (accountId != null) {
			arnTemplate = StringUtil.replace(
				arnTemplate, "{accountId}", accountId);
		}

		arnTemplate = StringUtil.replace(
			arnTemplate, "{companyId}", String.valueOf(companyId));
		arnTemplate = StringUtil.replace(
			arnTemplate, "{identifier}", identifier);

		if (region != null) {
			arnTemplate = StringUtil.replace(arnTemplate, "{region}", region);
		}

		return arnTemplate;
	}

}