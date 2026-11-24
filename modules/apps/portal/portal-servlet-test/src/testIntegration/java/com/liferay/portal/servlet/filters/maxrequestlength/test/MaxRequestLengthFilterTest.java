/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.maxrequestlength.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class MaxRequestLengthFilterTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testProcessFilter() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"MAX_REQUEST_LENGTH", 1000L)) {

			HTTPTestUtil.customize(
			).withGuest(
			).apply(
				() -> {

					// GraphQL

					Assert.assertEquals(
						HttpServletResponse.SC_OK,
						HTTPTestUtil.invokeToHttpCode(
							_getGraphQLBody(1), "graphql", Http.Method.POST));
					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						HTTPTestUtil.invokeToHttpCode(
							_getGraphQLBody(1000), "graphql",
							Http.Method.POST));

					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						_invokeToHttpCode(
							_getGraphQLBody(1000),
							ContentTypes.MULTIPART_MIXED + "; boundary=test",
							"o/graphql"));
					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						_invokeToHttpCode(
							_getGraphQLBody(1000),
							"Multipart/form-data; boundary=test", "o/graphql"));
					Assert.assertEquals(
						HttpServletResponse.SC_OK,
						_invokeToHttpCode(
							_getGraphQLBody(1), ContentTypes.APPLICATION_JSON,
							"en/o/graphql"));
					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						_invokeToHttpCode(
							_getGraphQLBody(1000),
							ContentTypes.APPLICATION_JSON, "en/o/graphql"));

					// Headless REST

					Assert.assertNotEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						HTTPTestUtil.invokeToHttpCode(
							_getBody(1), "headless-admin-user/v1.0/accounts",
							Http.Method.POST));
					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						HTTPTestUtil.invokeToHttpCode(
							_getBody(1000), "headless-admin-user/v1.0/accounts",
							Http.Method.POST));
					Assert.assertEquals(
						HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
						_invokeToHttpCode(
							_getBody(1000), ContentTypes.APPLICATION_JSON,
							"en/o/headless-admin-user/v1.0/accounts"));

					// JSON web services

					HTTPTestUtil.customize(
					).withGuest(
					).withoutModulePath(
					).apply(
						() -> {
							Assert.assertEquals(
								HttpServletResponse.SC_OK,
								HTTPTestUtil.invokeToHttpCode(
									JSONUtil.putAll(
									).toString(),
									"api/jsonws/invoke", Http.Method.POST));
							Assert.assertEquals(
								HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
								HTTPTestUtil.invokeToHttpCode(
									JSONUtil.putAll(
										RandomTestUtil.randomString(1000)
									).toString(),
									"api/jsonws/invoke", Http.Method.POST));
							Assert.assertEquals(
								HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
								_invokeToHttpCode(
									JSONUtil.putAll(
										RandomTestUtil.randomString(1000)
									).toString(),
									ContentTypes.APPLICATION_JSON,
									"en/api/jsonws/invoke"));
						}
					);
				}
			);
		}
	}

	private String _getBody(int length) {
		return JSONUtil.put(
			"name", RandomTestUtil.randomString(length)
		).toString();
	}

	private String _getGraphQLBody(int length) {
		return JSONUtil.put(
			"query", "{__typename}"
		).put(
			"variables", JSONUtil.put("x", RandomTestUtil.randomString(length))
		).toString();
	}

	private int _invokeToHttpCode(String body, String contentType, String path)
		throws Exception {

		Http.Options options = new Http.Options();

		options.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
		options.setBody(body, contentType, StringPool.UTF8);
		options.setLocation(
			StringBundler.concat(
				"http://localhost:", PortalUtil.getPortalServerPort(false), "/",
				path));
		options.setMethod(Http.Method.POST);

		HttpUtil.URLtoString(options);

		Http.Response response = options.getResponse();

		return response.getResponseCode();
	}

}