/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

/**
 * @author Jorge García Jiménez
 */
public enum FIPSAuditSeverity {

	CRITICAL("critical"), INFO("info");

	public String getValue() {
		return _value;
	}

	private FIPSAuditSeverity(String value) {
		_value = value;
	}

	private final String _value;

}