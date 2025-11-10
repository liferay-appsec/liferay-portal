/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.servlet.filter.backchannel.logout;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Lucas Miranda
 */
public class OpenIdConnectBackchannelLogoutFilterTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(
			_openIdConnectBackchannelLogoutFilter,
			"_openIdConnectSessionLocalService",
			_openIdConnectSessionLocalService);
	}

	@Test
	public void testProcessFilter() throws Exception {
		Map<String, String> mockJwksUriMap = HashMapBuilder.put(
			_ISSUER_URL, "http://mocked.jwks.uri/key-set.json"
		).build();

		Mockito.doReturn(
			mockJwksUriMap
		).when(
			_openIdConnectBackchannelLogoutFilter
		).getConfigurationData();

		SignedJWT idToken = _createIdToken();

		SignedJWT logoutToken = _createLogoutToken();

		HttpServletRequest httpServletRequest = _mockHttpServletRequest(
			logoutToken.serialize());

		HttpServletResponse httpServletResponse = Mockito.mock(
			HttpServletResponse.class);

		OpenIdConnectSession openIdConnectSession = Mockito.mock(
			OpenIdConnectSession.class);

		Mockito.when(
			_openIdConnectSessionLocalService.fetchOpenIdConnectSessionBySid(
				Mockito.eq(_SESSION_ID))
		).thenReturn(
			openIdConnectSession
		);

		Mockito.when(
			openIdConnectSession.getIdToken()
		).thenReturn(
			idToken.serialize()
		);

		JWTClaimsSet logoutTokenClaims = new JWTClaimsSet.Builder(
		).issuer(
			_ISSUER_URL
		).audience(
			Collections.singletonList(_CLIENT_ID)
		).claim(
			"sid", _SESSION_ID
		).claim(
			"events",
			HashMapBuilder.put(
				"http://schemas.openid.net/event/backchannel-logout",
				Collections.emptyMap()
			).build()
		).issueTime(
			new Date()
		).jwtID(
			UUID.randomUUID(
			).toString()
		).expirationTime(
			new Date(System.currentTimeMillis() + 60000)
		).build();

		LogoutTokenClaimsSet dummyLogoutClaims = new LogoutTokenClaimsSet(
			logoutTokenClaims);

		try (MockedConstruction<LogoutTokenValidator> mockedConstruction =
				Mockito.mockConstruction(
					LogoutTokenValidator.class,
					(mock, context) -> Mockito.when(
						mock.validate(Mockito.any(JWT.class))
					).thenReturn(
						dummyLogoutClaims
					))) {

			_openIdConnectBackchannelLogoutFilter.processFilter(
				httpServletRequest, httpServletResponse,
				Mockito.mock(FilterChain.class));

			LogoutTokenValidator mockValidator = mockedConstruction.constructed(
			).get(
				0
			);

			Mockito.verify(
				mockValidator
			).validate(
				Mockito.any(JWT.class)
			);
		}

		Mockito.verify(
			_openIdConnectSessionLocalService
		).deleteOpenIdConnectSession(
			Mockito.eq(openIdConnectSession)
		);

		Mockito.verify(
			httpServletResponse
		).setStatus(
			HttpServletResponse.SC_OK
		);
	}

	private SignedJWT _createIdToken() throws Exception {
		RSAKey testRSAKey = new RSAKeyGenerator(
			2048
		).keyID(
			_KEY_ID
		).generate();

		JWSSigner signer = new RSASSASigner(testRSAKey.toPrivateKey());

		Date now = new Date();

		JWTClaimsSet idTokenClaims = new JWTClaimsSet.Builder(
		).issuer(
			_ISSUER_URL
		).audience(
			_CLIENT_ID
		).subject(
			_SUBJECT
		).issueTime(
			now
		).expirationTime(
			new Date(now.getTime() + 60_000)
		).claim(
			"sid", _SESSION_ID
		).claim(
			"typ", "ID"
		).build();

		SignedJWT idToken = new SignedJWT(
			new JWSHeader.Builder(
				_JWS_ALGORITHM
			).keyID(
				_KEY_ID
			).build(),
			idTokenClaims);

		idToken.sign(signer);

		return idToken;
	}

	private SignedJWT _createLogoutToken() throws Exception {
		JSONObject eventsClaimValueJSONObject = new JSONObject();

		try {
			eventsClaimValueJSONObject.put(
				"http://schemas.openid.net/event/backchannel-logout",
				new JSONObject());
		}
		catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}

		RSAKey testRSAKey = new RSAKeyGenerator(
			2048
		).keyID(
			_KEY_ID
		).generate();

		JWSSigner signer = new RSASSASigner(testRSAKey.toPrivateKey());

		Date now = new Date();

		JWTClaimsSet logoutClaims = new JWTClaimsSet.Builder(
		).issuer(
			_ISSUER_URL
		).audience(
			_CLIENT_ID
		).subject(
			_SUBJECT
		).issueTime(
			now
		).expirationTime(
			new Date(now.getTime() + 60_000)
		).jwtID(
			UUID.randomUUID(
			).toString()
		).claim(
			"sid", _SESSION_ID
		).claim(
			"typ", "Logout"
		).claim(
			"events", eventsClaimValueJSONObject.toString()
		).build();

		SignedJWT logoutToken = new SignedJWT(
			new JWSHeader.Builder(
				_JWS_ALGORITHM
			).keyID(
				_KEY_ID
			).build(),
			logoutClaims);

		logoutToken.sign(signer);

		return logoutToken;
	}

	private HttpServletRequest _mockHttpServletRequest(
		String logoutTokenSerialized) {

		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Mockito.when(
			ParamUtil.get(httpServletRequest, "logout_token", StringPool.BLANK)
		).thenReturn(
			logoutTokenSerialized
		);

		return httpServletRequest;
	}

	private static final String _CLIENT_ID = "liferay";

	private static final String _ISSUER_URL =
		"http://localhost:8180/realms/lucas";

	private static final JWSAlgorithm _JWS_ALGORITHM = JWSAlgorithm.RS256;

	private static final String _KEY_ID = "test-kid-logout";

	private static final String _SESSION_ID =
		"055bbd56-a07a-43a4-bb57-03bb42543e7d";

	private static final String _SUBJECT =
		"98259e32-a701-41fa-9dc5-719e00182326";

	private final OpenIdConnectBackchannelLogoutFilter
		_openIdConnectBackchannelLogoutFilter = Mockito.spy(
			new OpenIdConnectBackchannelLogoutFilter());
	private final OpenIdConnectSessionLocalService
		_openIdConnectSessionLocalService = Mockito.mock(
			OpenIdConnectSessionLocalService.class);

}