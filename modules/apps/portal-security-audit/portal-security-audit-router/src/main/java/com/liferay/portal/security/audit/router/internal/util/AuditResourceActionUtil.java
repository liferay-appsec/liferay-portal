/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Regisson Aguiar
 */
public class AuditResourceActionUtil {

	public static void resolve(AuditMessage auditMessage) {
		String resourceAction = auditMessage.getResourceAction();

		if (Validator.isNull(resourceAction)) {
			return;
		}

		Matcher matcher = _resourceActionPattern.matcher(resourceAction);

		if (!matcher.matches()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"The resource action \"", resourceAction,
						"\" is invalid for event type ",
						auditMessage.getEventType()));
			}

			return;
		}

		if (Validator.isNull(auditMessage.getResourceType())) {
			String[] resourceActionParts = StringUtil.split(
				resourceAction, CharPool.PERIOD);

			auditMessage.setResourceType(resourceActionParts[1]);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuditResourceActionUtil.class);

	private static final Pattern _resourceActionPattern = Pattern.compile(
		"[a-z0-9_]+\\.[a-z0-9_]+\\.[a-z0-9_]+");

}