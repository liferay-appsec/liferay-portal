/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;

/**
 * @author Jorge García Jiménez
 */
public class FIPSFederationTokenAuditUtil {

	public static void writeRejected(
		String receivingEndpoint, String rejectedValue, String tokenIssuer,
		String tokenType) {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"federation-token-rejected", FIPSAuditEvent.Severity.WARNING);

		fipsAuditEvent.put(
			"receiving-endpoint", GetterUtil.getString(receivingEndpoint));
		fipsAuditEvent.put(
			"rejected-value", GetterUtil.getString(rejectedValue));
		fipsAuditEvent.put("token-issuer", GetterUtil.getString(tokenIssuer));
		fipsAuditEvent.put("token-type", GetterUtil.getString(tokenType));

		FIPSAuditUtil.write(fipsAuditEvent);
	}

}