/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.maxrequestlength;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.MaxRequestLengthUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.servlet.filters.BasePortalFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jorge García Jiménez
 * @author Alejandro Tardín
 */
public class MaxRequestLengthFilter extends BasePortalFilter {

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		String contentType = httpServletRequest.getContentType();

		if ((contentType != null) &&
			contentType.startsWith(ContentTypes.MULTIPART_FORM_DATA)) {

			processFilter(
				MaxRequestLengthFilter.class.getName(), httpServletRequest,
				httpServletResponse, filterChain);

			return;
		}

		long contentLength = httpServletRequest.getContentLengthLong();

		if (contentLength > 0) {
			long maxRequestLength = Math.max(
				MaxRequestLengthUtil.getBatchMaxRequestLength(),
				MaxRequestLengthUtil.getSignedInMaxRequestLength());

			if (contentLength > maxRequestLength) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						StringBundler.concat(
							"Request body exceeds the maximum length of ",
							maxRequestLength, " bytes"));
				}

				httpServletResponse.setStatus(
					HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);

				return;
			}
		}

		MaxRequestLengthHttpServletRequestWrapper
			maxRequestLengthHttpServletRequestWrapper =
				new MaxRequestLengthHttpServletRequestWrapper(
					httpServletRequest, httpServletResponse);

		try {
			processFilter(
				MaxRequestLengthFilter.class.getName(),
				maxRequestLengthHttpServletRequestWrapper, httpServletResponse,
				filterChain);
		}
		catch (Exception exception) {
			if (!maxRequestLengthHttpServletRequestWrapper.
					isMaxRequestLengthExceeded()) {

				throw exception;
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MaxRequestLengthFilter.class);

}