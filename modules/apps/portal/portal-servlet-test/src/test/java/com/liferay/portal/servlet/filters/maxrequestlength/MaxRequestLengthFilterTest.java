/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.maxrequestlength;

import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upload.configuration.UploadServletRequestConfigurationProviderUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

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
		_batchMaxRequestLength = PropsValues.MAX_REQUEST_LENGTH_BATCH;
		_maxRequestLength = PropsValues.MAX_REQUEST_LENGTH;
		_signedInMaxRequestLength = PropsValues.MAX_REQUEST_LENGTH_SIGNED_IN;

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH", (long)_BYTES.length);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH_BATCH",
			(long)_BYTES.length * 3);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH_SIGNED_IN",
			(long)_BYTES.length * 2);
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH", _maxRequestLength);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH_BATCH",
			_batchMaxRequestLength);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH_SIGNED_IN",
			_signedInMaxRequestLength);
	}

	@Test
	public void testProcessFilter() throws Exception {
		_testProcessFilter(
			_BYTES, -1, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_OK, false);
		_testProcessFilter(
			_BYTES, -1, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_OK, true);
		_testProcessFilter(
			_BYTES, _BYTES.length, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_OK, false);

		byte[] bytes = new byte[_BYTES.length + 1];

		_testProcessFilter(
			bytes, -1, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
		_testProcessFilter(
			bytes, -1, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, true);
		_testProcessFilter(
			bytes, -1,
			ContentTypes.MULTIPART_FORM_DATA + "; boundary=" +
				RandomTestUtil.randomString(),
			HttpServletResponse.SC_OK, false);
		_testProcessFilter(
			bytes, -1,
			ContentTypes.MULTIPART_MIXED + "; boundary=" +
				RandomTestUtil.randomString(),
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, true);
		_testProcessFilter(
			bytes, bytes.length,
			"Multipart/form-data; boundary=" + RandomTestUtil.randomString(),
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
		_testProcessFilter(
			bytes, bytes.length, ContentTypes.APPLICATION_JSON,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
		_testProcessFilter(
			bytes, bytes.length, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED,
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
		_testProcessFilter(
			bytes, bytes.length,
			ContentTypes.MULTIPART_FORM_DATA + "; boundary=" +
				RandomTestUtil.randomString(),
			HttpServletResponse.SC_OK, false);
		_testProcessFilter(
			bytes, bytes.length,
			ContentTypes.MULTIPART_MIXED + "; boundary=" +
				RandomTestUtil.randomString(),
			HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);

		_testProcessFilterWithParameter(bytes);
		_testProcessFilterWithSession(bytes, HttpServletResponse.SC_OK);

		PermissionThreadLocal.setPermissionChecker(
			Mockito.mock(PermissionChecker.class));

		try {
			_testProcessFilterWithSession(
				bytes, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(null);
		}

		PermissionChecker permissionChecker = Mockito.mock(
			PermissionChecker.class);

		Mockito.when(
			permissionChecker.isSignedIn()
		).thenReturn(
			true
		);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		try {
			_testProcessFilter(
				bytes, -1, ContentTypes.APPLICATION_JSON,
				HttpServletResponse.SC_OK, true);
			_testProcessFilter(
				bytes, bytes.length, ContentTypes.APPLICATION_JSON,
				HttpServletResponse.SC_OK, false);

			byte[] signedInBytes = new byte[(_BYTES.length * 2) + 1];

			_testProcessFilterWithBatch(
				signedInBytes, HttpServletResponse.SC_OK);

			byte[] batchBytes = new byte[(_BYTES.length * 3) + 1];

			_testProcessFilterWithBatch(
				batchBytes, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);

			_testProcessFilter(
				signedInBytes, -1, ContentTypes.APPLICATION_JSON,
				HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
			_testProcessFilter(
				signedInBytes, signedInBytes.length,
				ContentTypes.APPLICATION_JSON,
				HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(null);
		}

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "MAX_REQUEST_LENGTH_SIGNED_IN", 0L);

		try (MockedStatic<UploadServletRequestConfigurationProviderUtil>
				uploadServletRequestConfigurationProviderUtilMockedStatic =
					Mockito.mockStatic(
						UploadServletRequestConfigurationProviderUtil.class)) {

			_testProcessFilter(
				_BYTES, -1, ContentTypes.APPLICATION_JSON,
				HttpServletResponse.SC_OK, false);

			uploadServletRequestConfigurationProviderUtilMockedStatic.
				verifyNoInteractions();
		}
	}

	private void _testProcessFilter(
			byte[] bytes, long contentLength, String contentType,
			int expectedStatus, boolean reader)
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

		mockHttpServletResponse.setHeader("X-Before", "before");

		ReflectionTestUtil.invoke(
			new MaxRequestLengthFilter(), "processFilter",
			new Class<?>[] {
				HttpServletRequest.class, HttpServletResponse.class,
				FilterChain.class
			},
			mockHttpServletRequest, mockHttpServletResponse,
			(FilterChain)(servletRequest, servletResponse) -> {
				try {
					if (reader) {
						BufferedReader bufferedReader =
							servletRequest.getReader();

						while (bufferedReader.read() != -1) {
						}
					}
					else {
						InputStream inputStream =
							servletRequest.getInputStream();

						inputStream.readAllBytes();
					}
				}
				catch (IOException ioException) {
					HttpServletResponse httpServletResponse =
						(HttpServletResponse)servletResponse;

					httpServletResponse.setStatus(
						HttpServletResponse.SC_BAD_REQUEST);
				}
			});

		Assert.assertEquals(
			expectedStatus, mockHttpServletResponse.getStatus());
		Assert.assertEquals(
			"before", mockHttpServletResponse.getHeader("X-Before"));
	}

	private void _testProcessFilterWithBatch(byte[] bytes, int expectedStatus)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setContent(bytes);
		mockHttpServletRequest.setContentType(ContentTypes.APPLICATION_JSON);

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
				InputStream inputStream = servletRequest.getInputStream();

				servletRequest.setAttribute(
					WebKeys.MAX_REQUEST_LENGTH_BATCH, Boolean.TRUE);

				inputStream.readAllBytes();
			});

		Assert.assertEquals(
			expectedStatus, mockHttpServletResponse.getStatus());
	}

	private void _testProcessFilterWithParameter(byte[] bytes)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setContent(bytes);
		mockHttpServletRequest.setContentType(
			ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);

		String name = RandomTestUtil.randomString();
		String value = RandomTestUtil.randomString();

		mockHttpServletRequest.setParameter(name, value);

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		ReflectionTestUtil.invoke(
			new MaxRequestLengthFilter(), "processFilter",
			new Class<?>[] {
				HttpServletRequest.class, HttpServletResponse.class,
				FilterChain.class
			},
			mockHttpServletRequest, mockHttpServletResponse,
			(FilterChain)
				(servletRequest, servletResponse) -> Assert.assertEquals(
					value, servletRequest.getParameter(name)));

		Assert.assertEquals(
			HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
	}

	private void _testProcessFilterWithSession(byte[] bytes, int expectedStatus)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setContent(bytes);
		mockHttpServletRequest.setContentType(ContentTypes.APPLICATION_JSON);

		MockHttpSession mockHttpSession = new MockHttpSession();

		mockHttpSession.setAttribute(
			WebKeys.USER_ID, RandomTestUtil.randomLong());

		mockHttpServletRequest.setSession(mockHttpSession);

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
				InputStream inputStream = servletRequest.getInputStream();

				inputStream.readAllBytes();
			});

		Assert.assertEquals(
			expectedStatus, mockHttpServletResponse.getStatus());
	}

	private static final byte[] _BYTES = RandomTestUtil.randomBytes();

	private long _batchMaxRequestLength;
	private long _maxRequestLength;
	private long _signedInMaxRequestLength;

}