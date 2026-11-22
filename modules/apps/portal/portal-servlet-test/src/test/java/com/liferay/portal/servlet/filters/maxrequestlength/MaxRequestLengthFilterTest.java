/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.maxrequestlength;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Alejandro Tardín
 */
public class MaxRequestLengthFilterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_maxRequestLength = PropsValues.MAX_REQUEST_LENGTH;

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH", (long)_BYTES.length);
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH", _maxRequestLength);
	}

	@Test
	public void testProcessFilter() throws Exception {
		_testProcessFilter(
			_BYTES, ContentTypes.APPLICATION_JSON, _BYTES.length, false,
			HttpServletResponse.SC_OK);
		_testProcessFilter(
			_BYTES, ContentTypes.APPLICATION_JSON, -1, false,
			HttpServletResponse.SC_OK);
		_testProcessFilter(
			_BYTES, ContentTypes.APPLICATION_JSON, -1, true,
			HttpServletResponse.SC_OK);

		byte[] bytes = new byte[_BYTES.length + 1];

		_testProcessFilter(
			bytes, ContentTypes.APPLICATION_JSON, bytes.length, false,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		_testProcessFilter(
			bytes, ContentTypes.APPLICATION_JSON, -1, false,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		_testProcessFilter(
			bytes, ContentTypes.APPLICATION_JSON, -1, true,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		_testProcessFilter(
			bytes, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED, bytes.length,
			false, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		_testProcessFilter(
			bytes, ContentTypes.MULTIPART_FORM_DATA + "; boundary=test",
			bytes.length, false, HttpServletResponse.SC_OK);
	}

	private void _testProcessFilter(
			byte[] bytes, String contentType, long contentLength,
			boolean reader, int expectedStatus)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest = Mockito.spy(
			new MockHttpServletRequest());

		mockHttpServletRequest.setContent(bytes);
		mockHttpServletRequest.setContentType(contentType);

		Mockito.when(
			mockHttpServletRequest.getContentLengthLong()
		).thenReturn(
			contentLength
		);

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		ReflectionTestUtil.invoke(
			new MaxRequestLengthFilter(), "processFilter",
			new Class<?>[] {
				HttpServletRequest.class, HttpServletResponse.class,
				FilterChain.class
			},
			mockHttpServletRequest, mockHttpServletResponse,
			(FilterChain)(servletRequest, servletResponse) -> {
				if (reader) {
					BufferedReader bufferedReader = servletRequest.getReader();

					while (bufferedReader.read() != -1) {
					}

					return;
				}

				InputStream inputStream = servletRequest.getInputStream();

				inputStream.readAllBytes();
			});

		Assert.assertEquals(
			expectedStatus, mockHttpServletResponse.getStatus());
	}

	private static final byte[] _BYTES = RandomTestUtil.randomBytes();

	private long _maxRequestLength;

}