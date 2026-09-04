/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.lang.SafeCloseable;

/**
 * @author Christian Moura
 */
public class AuditCorrelationThreadLocal {

	public static String getCorrelationId() {
		return _correlationId.get();
	}

	public static SafeCloseable setCorrelationIdWithSafeCloseable(
		String correlationId) {

		return _correlationId.setWithSafeCloseable(correlationId);
	}

	private static final CentralizedThreadLocal<String> _correlationId =
		new CentralizedThreadLocal<>(
			AuditCorrelationThreadLocal.class + "._correlationId");

}