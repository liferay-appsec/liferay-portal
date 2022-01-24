/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.oauth2.provider.rest.endpoint.access.token.grant.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.client.test.BaseTestPreparatorBundleActivator;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.net.URI;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.rs.security.jose.common.JoseType;
import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKeys;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsJwtCompactProducer;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.rs.security.oauth2.grants.jwt.Constants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleActivator;

/**
 * @author Arthur Chan
 */
@RunWith(Arquillian.class)
public class JWTBearerGrantHandlerTest extends BaseGrantHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void test() throws Exception {
		JwsHeaders jwsHeaders = new JwsHeaders(
			JoseType.JWT, SignatureAlgorithm.RS256);

		jwsHeaders.setKeyId(_kid_01);

		JwtToken jwtToken = new JwtToken(jwsHeaders, _jwtClaims);

		JwsJwtCompactProducer jwsJwtCompactProducer = new JwsJwtCompactProducer(
			jwtToken);

		JsonWebKeys jsonWebKeys = JwkUtils.readJwkSet(_jwks_string);

		String jwtAssertion = jwsJwtCompactProducer.signWith(
			jsonWebKeys.getKey(jwsHeaders.getKeyId()));

		Invocation.Builder tokenBuilder = _tokenWebTarget.request();

		MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();

		formData.add("client_id", "oauthTestApplication");
		formData.add("client_secret", "oauthTestApplicationSecret");
		formData.add("grant_type", Constants.JWT_BEARER_GRANT);
		formData.add("assertion", jwtAssertion);

		Assert.assertFalse(
			Validator.isNull(
				parseTokenString(tokenBuilder.post(Entity.form(formData)))));
	}

	protected void doSetup() throws Exception {
		if (_user != null) {
			return;
		}

		_user = UserTestUtil.addOmniAdminUser();

		_initJWKS();

		_tokenWebTarget = getTokenWebTarget();

		_initJWTClaims(_user.getUuid(), _tokenWebTarget.getUri());
	}

	@Override
	protected BundleActivator getBundleActivator() {
		return new TokenExpeditionTestPreparatorBundleActivator();
	}

	private JSONObject _createAsymmetricPrivateKeyJSONObject(
		String kty, String use, String alg, String kid, String d) {

		JSONObject keyJSONObject = _createKeyJSONObject(kty, use, alg, kid);

		keyJSONObject.put("d", d);

		return keyJSONObject;
	}

	private JSONObject _createKeyJSONObject(
		String kty, String use, String alg, String kid) {

		JSONObject keyJSONObject = JSONUtil.put(
			"alg", alg
		).put(
			"kty", kty
		).put(
			"use", use
		);

		if (kid != null) {
			keyJSONObject.put("kid", kid);
		}

		return keyJSONObject;
	}

	private JSONObject _createRSAKeyPairJSONObject(
		String kty, String e, String use, String kid, String alg, String p,
		String q, String d, String qi, String dp, String dq, String n) {

		JSONObject rsaKeyJSONObject = _createAsymmetricPrivateKeyJSONObject(
			kty, use, alg, kid, d);

		if (e != null) {
			rsaKeyJSONObject.put("e", e);
		}

		if (p != null) {
			rsaKeyJSONObject.put("p", p);
		}

		if (q != null) {
			rsaKeyJSONObject.put("q", q);
		}

		if (qi != null) {
			rsaKeyJSONObject.put("qi", qi);
		}

		if (dp != null) {
			rsaKeyJSONObject.put("dp", dp);
		}

		if (dq != null) {
			rsaKeyJSONObject.put("dq", dq);
		}

		if (n != null) {
			rsaKeyJSONObject.put("n", n);
		}

		return rsaKeyJSONObject;
	}

	private JSONObject _createTestRSAKeyPairJSONWebKey01() {
		_kid_01 = "_createTestRSAKeyPairJSONWebKey01";

		return _createRSAKeyPairJSONObject(
			"RSA", "AQAB", "sig", _kid_01, "RS256",
			StringBundler.concat(
				"37P2B_RQiNP4M-khV2Z0qTlkfcrFy02v2xko6xqqYqxJTnD2eM0_WGqKQVTBb",
				"q2thTPkw44Kw18jhqYcVm7jyQcN4zcYKEAElQ3jztJOWKLkTOiuu5D-DXuF3P",
				"yUaL7klMbqp8EGBYh23abM3i0jkNWT0HWJfnEpQ8FzlLChptc"),
			StringBundler.concat(
				"ttr9tDJZc8Swf4TpzV7qY2r36k9lSH7eLVA35KpQJ9FNj9JqAminUvWyvqFJb",
				"oN_3zVxsxUrJdNxhrOfsogSxOkF8364ShECWBCgP2fBC8U_dIVfc_XRYNiTts",
				"S7MbCsbe_HhXaKFArRFt3eq_erFn5qU2W28ip5Hgw5d3eV_1U"),
			StringBundler.concat(
				"iNf_TOwPcGZyxldVnEXEJOzkjX1IVAp2RJF9Z6LMDLiJ",
				"P2HaeWQ1pJHmV_KaTAX-QQs-_9yIym0Y_7ybmFMF3IKd5rSdWUgDopiluH7lj",
				"B-ed_9grsRWHFkIctlNDzmVba-kCYEdLyZ43xDSFwVeNosQh3FnqMRl2xnbJt",
				"p43PiSZS6Gf_2hE189uA4sGLNbkFTyZ2Y61w550aHbGACZqgG1gY2SToYJxeD",
				"M08bwbgCfgv1dR1OrQCNBLkUluWoUfrm7kBpkWrEugOmtGlZ0vynbFMoXMaRs",
				"NySxUWfwA1xd49NjC0nXVGJKggLbYTyHqyA5eJNAX8IM7LBfGRENyQ"),
			StringBundler.concat(
				"olPn50M0v0XPWrJKy_e34C5GRilM5fHZgI89MYKnvSWw",
				"WwPlqKvvMxRamTxlzofXMex52zYyfI-AWJhCW2djpX-wU7ifWyAx7VLiPDfMq",
				"ljb2eeolRKoywK3zu4EU_OI8doOG3kAyjunNOU96tfy4XbLuUM0Cr_BRlDp4c",
				"HuA2s"),
			StringBundler.concat(
				"M9QuHImuXwU9bEmHQAis3sg9UCe6y--j3s9le5MiBtqD",
				"4XiqojJrisCkZ56Lcmkq2sG7LtOqCrnwMTIrPptSizDnNs18-1ZZuW8OaMyw-",
				"jhDTM5cLXjaY6VKznh2qg4QR6gOle9SxdVoNNKAhLlQlC3noVSaFoGBXgFkFf",
				"8WdOE"),
			StringBundler.concat(
				"DRR9MXaoj29ycKzBTL-NZK8yLMChLh5lJjimxuSn9zEx",
				"qygSDToPPg_1SU2gQxeE_iKEj5rkC0Ckzk3rDopNTWid1F0sMaAl2sbVr7NsS",
				"7tAXsVrno_m-laDun84JMXOj86nJxTjq6taaZhVZVfCFUnVsUGFZK1FHLEjKz",
				"iSskE"),
			StringBundler.concat(
				"n8lN23sleK5k1Lhp3r8xhdmJ3qFezuT3xZ30bdmfISXo",
				"MyvYVVdMoA41Fx_cPB3NqylbBYDLWL6YknRi_38dHHx0pF_t0ay6V2Hut_zju",
				"KuCNBrp20m04c5oCa1vUM_Jqj9TKIoj4PJSR6Tknnxw7pr0PUFMBTfYHZdMAS",
				"zPtZNqsqkT4scEsAy3fsE9twiG3S9u4tmKOEQqX7wLtL1kwBig_Hh5_RXPQfI",
				"4MoV3iMzw-k-urHJQ5cRJxzYOxNqoj1oDJxWCDXmrm9idFH0Lrs6rb0rQ6jCk",
				"BjEM9Q_rM0ZzoiB0NXbaQTrgxlHGUrpTDlEukKGQObWyYNvktv-OYw"));
	}

	private void _initJWKS() {
		if (_jwks_string != null) {
			return;
		}

		JSONObject jwksJSONObject = JSONUtil.put(
			"keys", JSONUtil.put(_createTestRSAKeyPairJSONWebKey01()));

		_jwks_string = jwksJSONObject.toString();
	}

	private void _initJWTClaims(String uuid, URI accessTokenEndpoint) {
		if (_jwtClaims != null) {
			return;
		}

		_jwtClaims = new JwtClaims();

		_jwtClaims.setIssuer(_ASSERTION_ISSUER);

		_jwtClaims.setIssuedAt(OAuthUtils.getIssuedAt());

		_jwtClaims.setExpiryTime(_jwtClaims.getIssuedAt() + 3600L);

		_jwtClaims.setAudience(accessTokenEndpoint.toString());

		_jwtClaims.setSubject(uuid);
	}

	private static final String _ASSERTION_ISSUER = "self-assertion-issuer";

	private static String _jwks_string;
	private static JwtClaims _jwtClaims;
	private static String _kid_01;
	private static WebTarget _tokenWebTarget;
	private static User _user;

	private static class TokenExpeditionTestPreparatorBundleActivator
		extends BaseTestPreparatorBundleActivator {

		@Override
		protected void prepareTest() throws Exception {
			createFactoryConfiguration(
				"com.liferay.oauth2.provider.rest.internal.configuration." +
					"OAuth2InAssertionConfiguration",
				HashMapDictionaryBuilder.<String, Object>put(
					"oauth2.in.assertion.issuer", _ASSERTION_ISSUER
				).put(
					"oauth2.in.assertion.signature.json.web.key.set",
					_jwks_string
				).put(
					"oauth2.in.assertion.user.auth.type", "UUID"
				).build());

			createOAuth2Application(
				_user.getCompanyId(), _user, "oauthTestApplication",
				Arrays.asList(GrantType.JWT_BEARER),
				Collections.singletonList("everything"));
		}

	}

}