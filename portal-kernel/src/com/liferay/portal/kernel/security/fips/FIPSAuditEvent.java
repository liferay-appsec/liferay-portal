/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jorge García Jiménez
 */
public class FIPSAuditEvent {

	public FIPSAuditEvent(
		String eventType, FIPSAuditSeverity fipsAuditSeverity) {

		_eventType = eventType;
		_fipsAuditSeverity = fipsAuditSeverity;
	}

	public String getEventType() {
		return _eventType;
	}

	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(_fields);
	}

	public FIPSAuditSeverity getFIPSAuditSeverity() {
		return _fipsAuditSeverity;
	}

	public FIPSAuditEvent put(String key, Object value) {
		_fields.put(key, value);

		return this;
	}

	private final String _eventType;
	private final Map<String, Object> _fields = new LinkedHashMap<>();
	private final FIPSAuditSeverity _fipsAuditSeverity;

}