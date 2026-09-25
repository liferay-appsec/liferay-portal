/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth.client.persistence.service.impl;

import com.liferay.oauth.client.persistence.configuration.OAuthClientCompanyConfiguration;
import com.liferay.oauth.client.persistence.exception.OAuthClientEntryAuthServerWellKnownURIException;
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.net.HttpURLConnection;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 * @author Alvaro Saugar
 */
public class OAuthClientEntryLocalServiceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testValidateAuthServerWellKnownURI() throws Exception {
		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		_validateAuthServerWellKnownURI(
			_createOAuthClientEntryLocalServiceImpl(http, true, null),
			_getWellKnownURI(_getRandomHost(_HOST_PREFIX_LOOPBACK, 3)));

		ArgumentCaptor<Http.Options> argumentCaptor = ArgumentCaptor.forClass(
			Http.Options.class);

		Mockito.verify(
			http
		).URLtoString(
			argumentCaptor.capture()
		);

		Http.Options httpOptions = argumentCaptor.getValue();

		Assert.assertEquals(
			Http.CookieSpec.STANDARD, httpOptions.getCookieSpec());
		Assert.assertFalse(httpOptions.isFollowRedirects());
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithCarrierGradeNATHost()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(http, false, null),
				_getWellKnownURI(
					_getRandomHost(_HOST_PREFIX_CARRIER_GRADE_NAT, 2))));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithFileScheme()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(http, true, null),
				"file:///" + RandomTestUtil.randomString()));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithHostAllowedByWildcard()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		_validateAuthServerWellKnownURI(
			_createOAuthClientEntryLocalServiceImpl(
				http, true, new String[] {StringPool.STAR}),
			_getWellKnownURI(_getRandomHost(_HOST_PREFIX_LOOPBACK, 3)));

		Mockito.verify(
			http
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithHostAllowedIgnoringPort()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		_validateAuthServerWellKnownURI(
			_createOAuthClientEntryLocalServiceImpl(
				http, true,
				new String[] {
					StringBundler.concat(
						host, StringPool.COLON,
						RandomTestUtil.randomInt(1, 32767))
				}),
			StringBundler.concat(
				Http.HTTPS_WITH_SLASH, host, StringPool.COLON,
				RandomTestUtil.randomInt(32768, 65535), _WELL_KNOWN_PATH));

		Mockito.verify(
			http
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithHostNotAllowed()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(
					http, true,
					new String[] {_getRandomHost(_HOST_PREFIX_PRIVATE, 3)}),
				_getWellKnownURI(_getRandomHost(_HOST_PREFIX_LOOPBACK, 3))));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithLinkLocalHost()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(http, false, null),
				_getWellKnownURI(_getRandomHost(_HOST_PREFIX_LINK_LOCAL, 2))));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithLocalNetworkAccessDisabled()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(
					http, false, new String[] {host}),
				_getWellKnownURI(host)));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithLocalWellKnownURI()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		OAuthClientEntryLocalServiceImpl oAuthClientEntryLocalServiceImpl =
			_createOAuthClientEntryLocalServiceImpl(http, false, null);

		ReflectionTestUtil.setFieldValue(
			oAuthClientEntryLocalServiceImpl,
			"_oAuthClientASLocalMetadataLocalService",
			Mockito.mock(OAuthClientASLocalMetadataLocalService.class));

		_validateAuthServerWellKnownURI(
			oAuthClientEntryLocalServiceImpl,
			_getWellKnownURI(_getRandomHost(_HOST_PREFIX_LOOPBACK, 3)) +
				"/local");

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithMulticastHost()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(http, true, null),
				_getWellKnownURI(_getRandomHost(_HOST_PREFIX_MULTICAST, 3))));

		Mockito.verifyNoInteractions(http);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithRedirect()
		throws Exception {

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		Http http = _mockHttp(
			_mockHttpResponse(
				HttpURLConnection.HTTP_MOVED_TEMP, _getWellKnownURI(host)),
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		_validateAuthServerWellKnownURI(
			_createOAuthClientEntryLocalServiceImpl(
				http, true, new String[] {host}),
			_getWellKnownURI(host));

		Mockito.verify(
			http, Mockito.times(2)
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithRedirectToHostNotAllowed()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(
				HttpURLConnection.HTTP_MOVED_TEMP,
				_getWellKnownURI(_getRandomHost(_HOST_PREFIX_PRIVATE, 3))));

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(
					http, true, new String[] {host}),
				_getWellKnownURI(host)));

		Mockito.verify(
			http
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithRelativeRedirect()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(
				HttpURLConnection.HTTP_MOVED_TEMP, _WELL_KNOWN_PATH),
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		_validateAuthServerWellKnownURI(
			_createOAuthClientEntryLocalServiceImpl(
				http, true, new String[] {host}),
			_getWellKnownURI(host));

		Mockito.verify(
			http, Mockito.times(2)
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithTooManyRedirects()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(
				HttpURLConnection.HTTP_MOVED_TEMP, _WELL_KNOWN_PATH));

		String host = _getRandomHost(_HOST_PREFIX_LOOPBACK, 3);

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(
					http, true, new String[] {host}),
				_getWellKnownURI(host)));

		Mockito.verify(
			http, Mockito.times(_MAXIMUM_REDIRECTS + 1)
		).URLtoString(
			Mockito.any(Http.Options.class)
		);
	}

	@Test
	public void testValidateAuthServerWellKnownURIWithUniqueLocalHost()
		throws Exception {

		Http http = _mockHttp(
			_mockHttpResponse(HttpURLConnection.HTTP_OK, null));

		Assert.assertThrows(
			OAuthClientEntryAuthServerWellKnownURIException.class,
			() -> _validateAuthServerWellKnownURI(
				_createOAuthClientEntryLocalServiceImpl(http, false, null),
				_getWellKnownURI(_getRandomUniqueLocalHost())));

		Mockito.verifyNoInteractions(http);
	}

	private OAuthClientEntryLocalServiceImpl
			_createOAuthClientEntryLocalServiceImpl(
				Http http, boolean authServerLocalNetworkAccessEnabled,
				String[] authServerHostsAllowed)
		throws Exception {

		OAuthClientEntryLocalServiceImpl oAuthClientEntryLocalServiceImpl =
			new OAuthClientEntryLocalServiceImpl();

		OAuthClientCompanyConfiguration oAuthClientCompanyConfiguration =
			Mockito.mock(OAuthClientCompanyConfiguration.class);

		Mockito.when(
			oAuthClientCompanyConfiguration.authServerHostsAllowed()
		).thenReturn(
			authServerHostsAllowed
		);

		Mockito.when(
			oAuthClientCompanyConfiguration.
				authServerLocalNetworkAccessEnabled()
		).thenReturn(
			authServerLocalNetworkAccessEnabled
		);

		ConfigurationProvider configurationProvider = Mockito.mock(
			ConfigurationProvider.class);

		Mockito.when(
			configurationProvider.getCompanyConfiguration(
				Mockito.eq(OAuthClientCompanyConfiguration.class),
				Mockito.anyLong())
		).thenReturn(
			oAuthClientCompanyConfiguration
		);

		ReflectionTestUtil.setFieldValue(
			oAuthClientEntryLocalServiceImpl, "_configurationProvider",
			configurationProvider);

		ReflectionTestUtil.setFieldValue(
			oAuthClientEntryLocalServiceImpl, "_http", http);

		return oAuthClientEntryLocalServiceImpl;
	}

	private String _getRandomHost(String prefix, int octetsCount) {
		StringBundler sb = new StringBundler(octetsCount * 2);

		sb.append(prefix);

		for (int i = 0; i < octetsCount; i++) {
			if (i > 0) {
				sb.append(StringPool.PERIOD);
			}

			sb.append(RandomTestUtil.randomInt(0, 255));
		}

		return sb.toString();
	}

	private String _getRandomUniqueLocalHost() {
		return StringBundler.concat(
			"[fd", Integer.toHexString(RandomTestUtil.randomInt(16, 255)),
			StringPool.COLON,
			Integer.toHexString(RandomTestUtil.randomInt(16, 65535)), "::1]");
	}

	private String _getWellKnownURI(String host) {
		return Http.HTTP_WITH_SLASH + host + _WELL_KNOWN_PATH;
	}

	private Http _mockHttp(Http.Response... httpResponses) throws Exception {
		Http http = Mockito.mock(Http.class);

		AtomicInteger atomicInteger = new AtomicInteger();

		Mockito.when(
			http.URLtoString(Mockito.any(Http.Options.class))
		).thenAnswer(
			invocation -> {
				Http.Options httpOptions = invocation.getArgument(0);

				int index = Math.min(
					atomicInteger.getAndIncrement(), httpResponses.length - 1);

				httpOptions.setResponse(httpResponses[index]);

				return "{}";
			}
		);

		return http;
	}

	private Http.Response _mockHttpResponse(int responseCode, String redirect) {
		Http.Response httpResponse = new Http.Response();

		httpResponse.setRedirect(redirect);
		httpResponse.setResponseCode(responseCode);

		return httpResponse;
	}

	private void _validateAuthServerWellKnownURI(
		OAuthClientEntryLocalServiceImpl oAuthClientEntryLocalServiceImpl,
		String authServerWellKnownURI) {

		ReflectionTestUtil.invoke(
			oAuthClientEntryLocalServiceImpl, "_validateAuthServerWellKnownURI",
			new Class<?>[] {long.class, String.class},
			RandomTestUtil.randomLong(), authServerWellKnownURI);
	}

	private static final String _HOST_PREFIX_CARRIER_GRADE_NAT = "100.64.";

	private static final String _HOST_PREFIX_LINK_LOCAL = "169.254.";

	private static final String _HOST_PREFIX_LOOPBACK = "127.";

	private static final String _HOST_PREFIX_MULTICAST = "224.";

	private static final String _HOST_PREFIX_PRIVATE = "10.";

	private static final int _MAXIMUM_REDIRECTS = 5;

	private static final String _WELL_KNOWN_PATH =
		"/.well-known/openid-configuration";

}