/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.servlet;

import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.upload.configuration.UploadServletRequestConfigurationProviderUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jorge García Jiménez
 */
public class MaxRequestLengthUtil {

	public static long getBatchMaxRequestLength() {
		if (PropsValues.MAX_REQUEST_LENGTH_BATCH > 0) {
			return PropsValues.MAX_REQUEST_LENGTH_BATCH;
		}

		return UploadServletRequestConfigurationProviderUtil.getMaxSize();
	}

	public static long getMaxRequestLength(
		HttpServletRequest httpServletRequest) {

		if (_isSignedIn(httpServletRequest)) {
			if (GetterUtil.getBoolean(
					httpServletRequest.getAttribute(
						WebKeys.MAX_REQUEST_LENGTH_BATCH))) {

				return getBatchMaxRequestLength();
			}

			return getSignedInMaxRequestLength();
		}

		if (PropsValues.MAX_REQUEST_LENGTH > 0) {
			return PropsValues.MAX_REQUEST_LENGTH;
		}

		return 1024 * 1024;
	}

	public static long getSignedInMaxRequestLength() {
		if (PropsValues.MAX_REQUEST_LENGTH_SIGNED_IN > 0) {
			return PropsValues.MAX_REQUEST_LENGTH_SIGNED_IN;
		}

		return UploadServletRequestConfigurationProviderUtil.getMaxSize();
	}

	public static byte[] read(HttpServletRequest httpServletRequest)
		throws IOException {

		return read(httpServletRequest, httpServletRequest.getInputStream());
	}

	public static byte[] read(
			HttpServletRequest httpServletRequest, InputStream inputStream)
		throws IOException {

		long maxRequestLength = getMaxRequestLength(httpServletRequest);

		if (httpServletRequest.getContentLengthLong() > maxRequestLength) {
			_rejectRequest(httpServletRequest, maxRequestLength);
		}

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		byte[] bytes = new byte[8192];
		int read = 0;

		while ((unsyncByteArrayOutputStream.size() <= maxRequestLength) &&
			   ((read = inputStream.read(bytes)) != -1)) {

			unsyncByteArrayOutputStream.write(bytes, 0, read);
		}

		if (unsyncByteArrayOutputStream.size() > maxRequestLength) {
			_rejectRequest(httpServletRequest, maxRequestLength);
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private static boolean _isSignedIn(HttpServletRequest httpServletRequest) {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (permissionChecker != null) {
			return permissionChecker.isSignedIn();
		}

		HttpSession httpSession = httpServletRequest.getSession(false);

		if ((httpSession != null) &&
			(httpSession.getAttribute(WebKeys.USER_ID) != null)) {

			return true;
		}

		return false;
	}

	private static void _rejectRequest(
			HttpServletRequest httpServletRequest, long maxRequestLength)
		throws IOException {

		httpServletRequest.setAttribute(
			WebKeys.MAX_REQUEST_LENGTH_EXCEEDED, Boolean.TRUE);

		throw new IOException(
			StringBundler.concat(
				"Request body exceeds the maximum length of ", maxRequestLength,
				" bytes"));
	}

}