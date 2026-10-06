/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.json.web.service.client.internal;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Igor Beslic
 */
public class JSONWebServiceClientImplTest
	extends BaseJSONWebServiceClientTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testActivateForBasicProxy() throws Exception {
		JSONWebServiceClientImpl jsonWebServiceClientImpl =
			new JSONWebServiceClientImpl();

		Map<String, Object> properties = getBaseProperties();

		properties.put("proxyHostName", "proxyhost.net");
		properties.put("proxyHostPort", "443");
		properties.put("proxyLogin", "proxylogin");
		properties.put("proxyPassword", "proxypass");

		jsonWebServiceClientImpl.activate(properties);

		Assert.assertEquals(
			properties.get("hostName"), jsonWebServiceClientImpl.getHostName());
		Assert.assertEquals(
			properties.get("protocol"), jsonWebServiceClientImpl.getProtocol());

		Assert.assertNull(jsonWebServiceClientImpl.getProxyAuthType());

		Assert.assertEquals(
			properties.get("proxyHostName"),
			jsonWebServiceClientImpl.getProxyHostName());
		Assert.assertEquals(
			GetterUtil.getInteger(
				String.valueOf(properties.get("proxyHostPort"))),
			jsonWebServiceClientImpl.getProxyHostPort());
		Assert.assertEquals(
			properties.get("proxyLogin"),
			jsonWebServiceClientImpl.getProxyLogin());
		Assert.assertEquals(
			properties.get("proxyPassword"),
			jsonWebServiceClientImpl.getProxyPassword());
	}

	@Test
	public void testActivateForNTLMProxy() throws Exception {
		JSONWebServiceClientImpl jsonWebServiceClientImpl =
			new JSONWebServiceClientImpl();

		Map<String, Object> properties = getBaseProperties();

		properties.put("proxyAuthType", "ntlm");
		properties.put("proxyDomain", "liferay.com");
		properties.put("proxyWorkstation", "lrdcom2003");

		jsonWebServiceClientImpl.activate(properties);

		Assert.assertEquals(
			properties.get("proxyAuthType"),
			jsonWebServiceClientImpl.getProxyAuthType());
		Assert.assertEquals(
			properties.get("proxyDomain"),
			jsonWebServiceClientImpl.getProxyDomain());
		Assert.assertEquals(
			properties.get("proxyWorkstation"),
			jsonWebServiceClientImpl.getProxyWorkstation());
	}

	@Test
	public void testActivateWithHeaders() throws Exception {
		JSONWebServiceClientImpl jsonWebServiceClientImpl =
			new JSONWebServiceClientImpl();

		Map<String, Object> properties = getBaseProperties();

		properties.put(
			"headers", "headerKey1=headerValue1;headerKey2=headerValue2");

		jsonWebServiceClientImpl.activate(properties);

		Map<String, String> headers = jsonWebServiceClientImpl.getHeaders();

		Assert.assertTrue(headers.containsKey("headerKey1"));
	}

	@Test
	public void testGetSSLIOSessionStrategy() throws Exception {
		JSONWebServiceClientImpl jsonWebServiceClientImpl =
			new JSONWebServiceClientImpl();

		jsonWebServiceClientImpl.setTrustSelfSignedCertificates(false);

		SSLIOSessionStrategy sslIOSessionStrategy = ReflectionTestUtil.invoke(
			jsonWebServiceClientImpl, "getSSLIOSessionStrategy",
			new Class<?>[0]);

		Assert.assertNull(
			ReflectionTestUtil.getFieldValue(
				sslIOSessionStrategy, "supportedCipherSuites"));
		Assert.assertNull(
			ReflectionTestUtil.getFieldValue(
				sslIOSessionStrategy, "supportedProtocols"));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			sslIOSessionStrategy = ReflectionTestUtil.invoke(
				jsonWebServiceClientImpl, "getSSLIOSessionStrategy",
				new Class<?>[0]);

			String[] cipherSuites = ReflectionTestUtil.getFieldValue(
				sslIOSessionStrategy, "supportedCipherSuites");

			Assert.assertEquals(
				SetUtil.fromArray(_CIPHER_SUITES),
				SetUtil.fromArray(cipherSuites));

			String[] protocols = ReflectionTestUtil.getFieldValue(
				sslIOSessionStrategy, "supportedProtocols");

			Assert.assertEquals(
				SetUtil.fromArray(_PROTOCOLS), SetUtil.fromArray(protocols));
		}
	}

	private static final String[] _CIPHER_SUITES = {
		"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
	};

	private static final String[] _PROTOCOLS = {"TLSv1.2", "TLSv1.3"};

}