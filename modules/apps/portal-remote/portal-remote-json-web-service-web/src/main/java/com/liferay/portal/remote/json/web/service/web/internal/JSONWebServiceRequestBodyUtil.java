/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.remote.json.web.service.web.internal;

import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.remote.json.web.service.web.internal.exception.RequestEntityTooLargeException;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * @author Jorge García Jiménez
 */
public class JSONWebServiceRequestBodyUtil {

	public static String getRequestBody(HttpServletRequest httpServletRequest)
		throws IOException {

		long maxRequestLength = _getMaxRequestLength();

		if (httpServletRequest.getContentLengthLong() > maxRequestLength) {
			throw new RequestEntityTooLargeException(maxRequestLength);
		}

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		StreamUtil.transfer(
			httpServletRequest.getInputStream(), unsyncByteArrayOutputStream,
			StreamUtil.BUFFER_SIZE, false, maxRequestLength + 1);

		if (unsyncByteArrayOutputStream.size() > maxRequestLength) {
			throw new RequestEntityTooLargeException(maxRequestLength);
		}

		return unsyncByteArrayOutputStream.toString(StringPool.UTF8);
	}

	private static long _getMaxRequestLength() {
		if (PropsValues.JSONWS_WEB_SERVICE_MAX_REQUEST_LENGTH > 0) {
			return PropsValues.JSONWS_WEB_SERVICE_MAX_REQUEST_LENGTH;
		}

		return 1024 * 1024;
	}

}