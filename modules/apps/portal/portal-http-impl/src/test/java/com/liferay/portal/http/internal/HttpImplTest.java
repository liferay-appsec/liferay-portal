/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.http.internal;

import com.liferay.petra.concurrent.DCLSingleton;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Tuple;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.util.PortalImpl;

import java.net.URI;

import java.security.Principal;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.DefaultManagedHttpClientConnection;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;
import org.apache.http.impl.pool.BasicPoolEntry;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miguel Pastor
 */
public class HttpImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(
			new PortalImpl() {

				@Override
				public String[] stripURLAnchor(String url, String separator) {
					return new String[] {url, StringPool.BLANK};
				}

			});
	}

	@Before
	public void setUp() {
		_httpImpl.activate(Collections.emptyMap());

		DCLSingleton<PoolingHttpClientConnectionManager>
			poolingHttpClientConnectionManagerDCLSingleton =
				ReflectionTestUtil.getFieldValue(
					_httpImpl,
					"_poolingHttpClientConnectionManagerDCLSingleton");

		_poolingHttpClientConnectionManager =
			poolingHttpClientConnectionManagerDCLSingleton.getSingleton(
				() -> ReflectionTestUtil.invoke(
					_httpImpl, "_createPoolingHttpClientConnectionManager",
					new Class<?>[0]));

		DCLSingleton<CloseableHttpClient> closeableHttpClientDCLSingleton =
			ReflectionTestUtil.getFieldValue(
				_httpImpl, "_closeableHttpClientDCLSingleton");

		_closeableHttpClient = closeableHttpClientDCLSingleton.getSingleton(
			() -> ReflectionTestUtil.invoke(
				_httpImpl, "_createCloseableHttpClient",
				new Class<?>[] {PoolingHttpClientConnectionManager.class},
				_poolingHttpClientConnectionManager));
	}

	@Test
	public void testGetProxyCredentials() throws Exception {
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
		String proxyPassword = RandomTestUtil.randomString();
		String proxyUserName = RandomTestUtil.randomString();

		Snapshot<SecretResolver> secretResolverSnapshot =
			ReflectionTestUtil.getAndSetFieldValue(
				SecretResolverUtil.class, "_secretResolverSnapshot",
				new Snapshot<SecretResolver>(
					SecretResolverUtil.class, SecretResolver.class) {

					@Override
					public SecretResolver get() {
						return (companyId, value) -> {
							Assert.assertEquals(
								CompanyConstants.SYSTEM, companyId);
							Assert.assertEquals(keyReferenceString, value);

							return proxyPassword;
						};
					}

				});

		try (AutoCloseable autoCloseable1 =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					HttpImpl.class, "_PROXY_PASSWORD", keyReferenceString);
			AutoCloseable autoCloseable2 =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					HttpImpl.class, "_PROXY_USERNAME", proxyUserName)) {

			try (AutoCloseable autoCloseable3 =
					ReflectionTestUtil.setFieldValueWithAutoCloseable(
						HttpImpl.class, "_PROXY_AUTH_TYPE",
						"username-password")) {

				Credentials credentials = ReflectionTestUtil.invoke(
					_httpImpl, "_getProxyCredentials", new Class<?>[0]);

				Assert.assertTrue(
					credentials instanceof UsernamePasswordCredentials);
				Assert.assertEquals(proxyPassword, credentials.getPassword());

				Principal principal = credentials.getUserPrincipal();

				Assert.assertEquals(proxyUserName, principal.getName());
			}

			try (AutoCloseable autoCloseable3 =
					ReflectionTestUtil.setFieldValueWithAutoCloseable(
						HttpImpl.class, "_PROXY_AUTH_TYPE", "ntlm")) {

				Credentials credentials = ReflectionTestUtil.invoke(
					_httpImpl, "_getProxyCredentials", new Class<?>[0]);

				Assert.assertTrue(credentials instanceof NTCredentials);
				Assert.assertEquals(proxyPassword, credentials.getPassword());
			}
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				SecretResolverUtil.class, "_secretResolverSnapshot",
				secretResolverSnapshot);
		}

		Credentials credentials = new UsernamePasswordCredentials(
			proxyUserName, proxyPassword);

		ReflectionTestUtil.setFieldValue(
			_httpImpl, "_proxyCredentials", credentials);

		Assert.assertSame(
			credentials,
			ReflectionTestUtil.invoke(
				_httpImpl, "_getProxyCredentials", new Class<?>[0]));
	}

	@Test
	public void testGetRequestConfigBuilder() {
		RequestConfig.Builder requestConfigBuilder = ReflectionTestUtil.invoke(
			_httpImpl, "_getRequestConfigBuilder",
			new Class<?>[] {URI.class, int.class},
			URI.create("http://" + RandomTestUtil.randomString()), 1000);

		RequestConfig requestConfig = requestConfigBuilder.build();

		Assert.assertEquals(1000, requestConfig.getConnectTimeout());
		Assert.assertEquals(1000, requestConfig.getConnectionRequestTimeout());
		Assert.assertEquals(1000, requestConfig.getSocketTimeout());

		requestConfigBuilder = ReflectionTestUtil.invoke(
			_httpImpl, "_getRequestConfigBuilder",
			new Class<?>[] {URI.class, int.class},
			URI.create("http://" + RandomTestUtil.randomString()), 0);

		requestConfig = requestConfigBuilder.build();

		int defaultTimeout = ReflectionTestUtil.getFieldValue(
			_httpImpl, "_TIMEOUT");

		Assert.assertEquals(
			defaultTimeout, requestConfig.getConnectionRequestTimeout());
		Assert.assertEquals(defaultTimeout, requestConfig.getConnectTimeout());
		Assert.assertEquals(defaultTimeout, requestConfig.getSocketTimeout());
	}

	@Test
	public void testHttpKeepAlive() {
		_httpImpl.modified(Collections.singletonMap("keepAliveTimeout", -1));

		_testHttpKeepAlive(true, Long.MAX_VALUE, -1);
		_testHttpKeepAlive(true, Long.MAX_VALUE, 0);
		_testHttpKeepAlive(true, 300000, 300);

		_httpImpl.modified(Collections.singletonMap("keepAliveTimeout", 0));

		_testHttpKeepAlive(true, Long.MAX_VALUE, -1);
		_testHttpKeepAlive(true, Long.MAX_VALUE, 0);
		_testHttpKeepAlive(true, 300000, 300);

		_httpImpl.modified(Collections.singletonMap("keepAliveTimeout", 600));

		_testHttpKeepAlive(true, 600000, -1);
		_testHttpKeepAlive(true, 600000, 0);
		_testHttpKeepAlive(true, 300000, 300);
	}

	@Test
	public void testHttpKeepAliveWithRequestClose() {
		HttpRequest httpRequest = new BasicHttpRequest("GET", "/");

		httpRequest.setHeader(
			HttpHeaders.CONNECTION, HttpHeaders.CONNECTION_CLOSE_VALUE);

		_httpContext.setAttribute(HttpCoreContext.HTTP_REQUEST, httpRequest);

		_httpResponse.setHeaders(
			new Header[] {
				new BasicHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE),
				new BasicHeader(HttpHeaders.CONTENT_LENGTH, "10")
			});

		_testHttpKeepAlive(false, -1);
	}

	@Test
	public void testHttpKeepAliveWithResponseClose() {
		HttpRequest httpRequest = new BasicHttpRequest("GET", "/");

		httpRequest.setHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE);

		_httpContext.setAttribute(HttpCoreContext.HTTP_REQUEST, httpRequest);

		_httpResponse.setHeaders(
			new Header[] {
				new BasicHeader(
					HttpHeaders.CONNECTION, HttpHeaders.CONNECTION_CLOSE_VALUE),
				new BasicHeader(HttpHeaders.CONTENT_LENGTH, "10")
			});

		_testHttpKeepAlive(false, -1);
	}

	@Test
	public void testIsNonProxyHost() throws Exception {
		String domain = "foo.com";
		String ipAddress = "192.168.0.250";
		String ipAddressWithStarWildcard = "182.*.0.250";

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					HttpImpl.class, "_NON_PROXY_HOSTS",
					new String[] {
						domain, ipAddress, ipAddressWithStarWildcard
					})) {

			Assert.assertTrue(_httpImpl.isNonProxyHost(domain));
			Assert.assertTrue(_httpImpl.isNonProxyHost(ipAddress));
			Assert.assertFalse(_httpImpl.isNonProxyHost("182.100.1.250"));
			Assert.assertTrue(_httpImpl.isNonProxyHost("182.123.0.250"));
			Assert.assertFalse(_httpImpl.isNonProxyHost("google.com"));
		}
	}

	@Test
	public void testTCPKeepAlive() {
		_testTCPKeepAlive(false);

		_httpImpl.modified(
			Collections.singletonMap("tcpKeepAliveEnabled", true));

		_testTCPKeepAlive(true);
	}

	private Tuple _getTuple() {
		ClientExecChain clientExecChain = ReflectionTestUtil.getFieldValue(
			_closeableHttpClient, "execChain");

		while (true) {
			clientExecChain = ReflectionTestUtil.getFieldValue(
				clientExecChain, "requestExecutor");

			if (clientExecChain instanceof MainClientExec) {
				return new Tuple(
					ReflectionTestUtil.getFieldValue(
						clientExecChain, "keepAliveStrategy"),
					ReflectionTestUtil.getFieldValue(
						clientExecChain, "reuseStrategy"));
			}
		}
	}

	private void _testHttpKeepAlive(
		boolean expectedKeepAlive,
		long expectedKeepAliveTimeoutInMilliseconds) {

		Tuple tuple = _getTuple();

		ConnectionReuseStrategy connectionReuseStrategy =
			(ConnectionReuseStrategy)tuple.getObject(1);

		Assert.assertEquals(
			expectedKeepAlive,
			connectionReuseStrategy.keepAlive(_httpResponse, _httpContext));

		long keepAliveTimeout = -1;

		if (expectedKeepAlive) {
			ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
				(ConnectionKeepAliveStrategy)tuple.getObject(0);

			BasicPoolEntry basicPoolEntry = new BasicPoolEntry(
				"id", new HttpHost("localhost", 8080),
				new DefaultManagedHttpClientConnection("id", 8 * 1024));

			basicPoolEntry.updateExpiry(
				connectionKeepAliveStrategy.getKeepAliveDuration(
					_httpResponse, new BasicHttpContext(null)),
				TimeUnit.MILLISECONDS);

			keepAliveTimeout = basicPoolEntry.getExpiry();

			if (keepAliveTimeout != Long.MAX_VALUE) {
				keepAliveTimeout -= basicPoolEntry.getUpdated();
			}
		}

		Assert.assertEquals(
			expectedKeepAliveTimeoutInMilliseconds, keepAliveTimeout);
	}

	private void _testHttpKeepAlive(
		boolean expectedKeepAlive, long expectedKeepAliveTimeoutInMilliseconds,
		long keepAliveTimeoutHeaderValue) {

		_httpResponse.setHeaders(
			new Header[] {
				new BasicHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE),
				new BasicHeader(HttpHeaders.CONTENT_LENGTH, "10")
			});

		if (keepAliveTimeoutHeaderValue > -1) {
			_httpResponse.setHeader(
				HttpHeaders.KEEP_ALIVE,
				"timeout=" + keepAliveTimeoutHeaderValue);
		}

		_testHttpKeepAlive(
			expectedKeepAlive, expectedKeepAliveTimeoutInMilliseconds);
	}

	private void _testTCPKeepAlive(boolean expectedEnabledTCPKeepAlive) {
		SocketConfig socketConfig = ReflectionTestUtil.invoke(
			_poolingHttpClientConnectionManager, "resolveSocketConfig",
			new Class<?>[] {HttpHost.class}, new Object[] {_httpHost});

		Assert.assertEquals(
			expectedEnabledTCPKeepAlive, socketConfig.isSoKeepAlive());
	}

	private CloseableHttpClient _closeableHttpClient;
	private final HttpContext _httpContext = new BasicHttpContext(null);
	private final HttpHost _httpHost = new HttpHost("localhost", 8080);
	private final HttpImpl _httpImpl = new HttpImpl();
	private final HttpResponse _httpResponse = new BasicHttpResponse(
		new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
	private PoolingHttpClientConnectionManager
		_poolingHttpClientConnectionManager;

}