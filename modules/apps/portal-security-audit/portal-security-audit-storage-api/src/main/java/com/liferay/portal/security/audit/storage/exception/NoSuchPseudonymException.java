/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.exception;

import com.liferay.portal.kernel.exception.NoSuchModelException;

/**
 * @author Brian Wing Shun Chan
 */
public class NoSuchPseudonymException extends NoSuchModelException {

	public NoSuchPseudonymException() {
	}

	public NoSuchPseudonymException(String msg) {
		super(msg);
	}

	public NoSuchPseudonymException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NoSuchPseudonymException(Throwable throwable) {
		super(throwable);
	}

}