/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.lang.SafeCloseable;

/**
 * @author Michael C. Han
 */
public class AuditRequestThreadLocal {

	public static AuditRequest getAuditRequest() {
		return _auditRequest.get();
	}

	public static void removeAuditRequest() {
		_auditRequest.remove();
	}

	public static SafeCloseable setAuditRequestWithSafeCloseable(
		AuditRequest auditRequest) {

		return _auditRequest.setWithSafeCloseable(auditRequest);
	}

	private static final CentralizedThreadLocal<AuditRequest> _auditRequest =
		new CentralizedThreadLocal<>(
			AuditRequestThreadLocal.class + "._auditRequest",
			AuditRequest::new);

}