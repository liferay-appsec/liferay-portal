/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Brian Wing Shun Chan
 */
public class AuditPseudonymValueException extends PortalException {

	public AuditPseudonymValueException() {
	}

	public AuditPseudonymValueException(String msg) {
		super(msg);
	}

	public AuditPseudonymValueException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public AuditPseudonymValueException(Throwable throwable) {
		super(throwable);
	}

}