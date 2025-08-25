/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.exception;

import com.liferay.portal.kernel.exception.PortalException;

public class WrapperException extends PortalException {

	public WrapperException() {
	}

	public WrapperException(String msg) {
		super(msg);
	}

	public WrapperException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public WrapperException(Throwable throwable) {
		super(throwable);
	}

	public String getDetails() {
		return this._details;
	}

	public void setDetails(String details) {
		this._details = details;
	}

	private String _details;

}