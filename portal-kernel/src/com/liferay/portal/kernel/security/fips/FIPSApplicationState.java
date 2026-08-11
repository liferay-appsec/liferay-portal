/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

/**
 * @author Jorge García Jiménez
 */
public enum FIPSApplicationState {

	ERROR("Error"), INITIALIZING("Initializing"),
	KEY_CSP_ENTRY("Key/CSP Entry"), OPERATIONAL("Operational"),
	POWER_OFF("Power-off"), QUIESCENT("Quiescent"), SELF_TEST("Self-Test");

	public String getValue() {
		return _value;
	}

	private FIPSApplicationState(String value) {
		_value = value;
	}

	private final String _value;

}