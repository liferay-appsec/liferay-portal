/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.maxrequestlength;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Alejandro Tardín
 */
public class MaxRequestLengthHttpServletRequestWrapper
	extends HttpServletRequestWrapper {

	public MaxRequestLengthHttpServletRequestWrapper(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, long maxRequestLength) {

		super(httpServletRequest);

		_httpServletResponse = httpServletResponse;
		_maxRequestLength = maxRequestLength;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (_servletInputStream == null) {
			_servletInputStream = new MaxRequestLengthServletInputStream(
				super.getInputStream());
		}

		return _servletInputStream;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (_bufferedReader == null) {
			String characterEncoding = getCharacterEncoding();

			if (characterEncoding == null) {
				characterEncoding = StringPool.UTF8;
			}

			_bufferedReader = new BufferedReader(
				new InputStreamReader(getInputStream(), characterEncoding));
		}

		return _bufferedReader;
	}

	public boolean isMaxRequestLengthExceeded() {
		return _maxRequestLengthExceeded;
	}

	private BufferedReader _bufferedReader;
	private final HttpServletResponse _httpServletResponse;
	private final long _maxRequestLength;
	private boolean _maxRequestLengthExceeded;
	private ServletInputStream _servletInputStream;

	private class MaxRequestLengthServletInputStream
		extends ServletInputStream {

		@Override
		public int available() throws IOException {
			return _servletInputStream.available();
		}

		@Override
		public void close() throws IOException {
			_servletInputStream.close();
		}

		@Override
		public boolean isFinished() {
			return _servletInputStream.isFinished();
		}

		@Override
		public boolean isReady() {
			return _servletInputStream.isReady();
		}

		@Override
		public int read() throws IOException {
			int value = _servletInputStream.read();

			if (value != -1) {
				_checkLength(1);
			}

			return value;
		}

		@Override
		public int read(byte[] bytes, int offset, int length)
			throws IOException {

			int count = _servletInputStream.read(bytes, offset, length);

			if (count > 0) {
				_checkLength(count);
			}

			return count;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			_servletInputStream.setReadListener(readListener);
		}

		private MaxRequestLengthServletInputStream(
			ServletInputStream servletInputStream) {

			_servletInputStream = servletInputStream;
		}

		private void _checkLength(int count) throws IOException {
			_length += count;

			if (_length > _maxRequestLength) {
				_maxRequestLengthExceeded = true;

				if (!_httpServletResponse.isCommitted()) {
					_httpServletResponse.setStatus(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);

					_httpServletResponse.flushBuffer();
				}

				throw new IOException(
					StringBundler.concat(
						"Request body exceeds the maximum length of ",
						_maxRequestLength, " bytes"));
			}
		}

		private long _length;
		private final ServletInputStream _servletInputStream;

	}

}