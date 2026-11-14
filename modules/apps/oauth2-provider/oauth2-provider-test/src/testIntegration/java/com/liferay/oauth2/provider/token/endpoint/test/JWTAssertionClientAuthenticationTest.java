/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.token.endpoint.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.internal.test.ClientAuthentication;
import com.liferay.oauth2.provider.internal.test.PasswordAuthorizationGrant;
import com.liferay.oauth2.provider.internal.test.util.JWTAssertionUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtConstants;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleActivator;

/**
 * @author Allen Ziegenfus
 */
@RunWith(Arquillian.class)
public class JWTAssertionClientAuthenticationTest
	extends BaseTokenEndpointTestCase {

	@Test
	public void testExpiredJWTAssertion() throws Exception {
		JwtClaims jwtClaims = JWTAssertionUtil.getJWTClaims(
			getTokenWebTarget().getUri(), TEST_CLIENT_ID_3, TEST_CLIENT_ID_3);

		jwtClaims.setIssuedAt(jwtClaims.getIssuedAt() - 120);
		jwtClaims.setExpiryTime(jwtClaims.getIssuedAt() + 60);

		_assertInvalidClient(_getTokenResponse(jwtClaims));
	}

	@Test
	public void testJWTAssertion() throws Exception {
		Response response = _getTokenResponse(
			JWTAssertionUtil.getJWTClaims(
				getTokenWebTarget().getUri(), TEST_CLIENT_ID_3,
				TEST_CLIENT_ID_3));

		Assert.assertEquals(200, response.getStatus());
		Assert.assertTrue(
			Validator.isNotNull(parseAccessTokenString(response)));
	}

	@Test
	public void testJWTAssertionWithUnknownClientId() throws Exception {
		String clientId = RandomTestUtil.randomString();

		_assertInvalidClient(
			_getTokenResponse(
				JWTAssertionUtil.getJWTClaims(
					getTokenWebTarget().getUri(), clientId, clientId)));
	}

	@Test
	public void testJWTAssertionWithWrongAudience() throws Exception {
		_assertInvalidClient(
			_getTokenResponse(
				JWTAssertionUtil.getJWTClaims(
					getJsonWebTarget(
						"wrongPath"
					).getUri(),
					TEST_CLIENT_ID_3, TEST_CLIENT_ID_3)));
	}

	@Test
	public void testJWTAssertionWithoutExpirationTime() throws Exception {
		JwtClaims jwtClaims = JWTAssertionUtil.getJWTClaims(
			getTokenWebTarget().getUri(), TEST_CLIENT_ID_3, TEST_CLIENT_ID_3);

		jwtClaims.removeProperty(JwtConstants.CLAIM_EXPIRY);

		_assertInvalidClient(_getTokenResponse(jwtClaims));
	}

	@Override
	protected BundleActivator getBundleActivator() {
		return new JWTAssertionClientAuthenticationTestPreparatorBundleActivator();
	}

	private void _assertInvalidClient(Response response) {
		Assert.assertEquals(401, response.getStatus());
	}

	private Response _getTokenResponse(JwtClaims jwtClaims) throws Exception {
		User user = UserTestUtil.getAdminUser(TestPropsValues.getCompanyId());

		String jwtAssertion = JWTAssertionUtil.getJWTAssertionRS256(
			jwtClaims, JWTAssertionUtil.JWKS);

		ClientAuthentication clientAuthentication = () -> {
			MultivaluedMap<String, String> multivaluedMap =
				new MultivaluedHashMap<>();

			multivaluedMap.add("client_assertion", jwtAssertion);
			multivaluedMap.add(
				"client_assertion_type",
				"urn:ietf:params:oauth:client-assertion-type:jwt-bearer");

			return multivaluedMap;
		};

		return getTokenResponse(
			new PasswordAuthorizationGrant(
				user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD),
			clientAuthentication);
	}

	private class JWTAssertionClientAuthenticationTestPreparatorBundleActivator
		extends TestPreparatorBundleActivator {
	}

}