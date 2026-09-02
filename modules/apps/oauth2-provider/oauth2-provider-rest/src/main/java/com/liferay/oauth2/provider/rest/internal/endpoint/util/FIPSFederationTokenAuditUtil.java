/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.fips.FIPSApplicationState;
import com.liferay.portal.kernel.security.fips.FIPSApplicationStateMachineUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

/**
 * @author Jorge García Jiménez
 */
public class FIPSFederationTokenAuditUtil {

	public static void writeRejected(
		String offendingValue, String tokenIssuer) {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"federation-token-rejected", FIPSAuditEvent.Severity.WARNING);

		FIPSApplicationState fipsApplicationState =
			FIPSApplicationStateMachineUtil.getFIPSApplicationState();

		fipsAuditEvent.put("fips-state", fipsApplicationState.name());

		fipsAuditEvent.put(
			"offending-value", GetterUtil.getString(offendingValue));
		fipsAuditEvent.put("receiving-endpoint", _getReceivingEndpoint());
		fipsAuditEvent.put("token-issuer", GetterUtil.getString(tokenIssuer));
		fipsAuditEvent.put("token-type", "JWT");

		FIPSAuditUtil.write(fipsAuditEvent);
	}

	private static String _getReceivingEndpoint() {
		Message message = JAXRSUtils.getCurrentMessage();

		if (message == null) {
			return StringPool.BLANK;
		}

		HttpServletRequest httpServletRequest = (HttpServletRequest)message.get(
			AbstractHTTPDestination.HTTP_REQUEST);

		if (httpServletRequest == null) {
			return StringPool.BLANK;
		}

		return httpServletRequest.getRequestURI();
	}

}