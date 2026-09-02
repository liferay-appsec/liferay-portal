/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.util;

import com.liferay.petra.string.StringPool;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

/**
 * @author Jorge García Jiménez
 */
public class OAuth2RequestUtil {

	public static String getRequestURI() {
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