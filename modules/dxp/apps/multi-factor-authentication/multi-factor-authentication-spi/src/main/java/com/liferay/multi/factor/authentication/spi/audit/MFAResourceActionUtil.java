/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.spi.audit;

import com.liferay.portal.kernel.audit.AuditMessage;

/**
 * @author Regisson Aguiar
 */
public class MFAResourceActionUtil {

	public static final String ACTION_VERIFY = "verify";

	public static final String ACTION_VERIFY_FAILURE = "verify_failure";

	public static AuditMessage setResourceActionAndType(
		AuditMessage auditMessage, String action) {

		auditMessage.setResourceAction("system.mfa." + action);
		auditMessage.setResourceType("mfa");

		return auditMessage;
	}

}