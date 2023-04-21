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

package com.liferay.portal.security.sso.openid.connect.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsUtil;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectAuthenticationHandler;
import com.liferay.portal.test.rule.Inject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;

/**
 * @author Olivér Kecskeméty
 */
public abstract class BaseClientTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		_defaultIssuer = TestPropsUtil.get(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY);

		_defaultRegularRole = TestPropsUtil.get(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY);
	}

	@AfterClass
	public static void tearDownClass() throws PortalException {
		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY,
			_defaultIssuer);

		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY,
			_defaultRegularRole);
	}

	protected String generateUserInfoJSON() {
		return JSONUtil.put(
			"email",
			StringBundler.concat(
				RandomTestUtil.randomString(), RandomTestUtil.nextLong(), "@",
				RandomTestUtil.randomString(), ".com")
		).put(
			"family_name", RandomTestUtil.randomString()
		).put(
			"given_name", RandomTestUtil.randomString()
		).put(
			"jobTitle", RandomTestUtil.randomString()
		).put(
			"middle_name", RandomTestUtil.randomString()
		).put(
			"screen_name", RandomTestUtil.randomString()
		).toString();
	}

	protected void setIssuer(String issuer) {
		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY, issuer);
	}

	protected void setRegularRole(String roleName) {
		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY,
			roleName);
	}

	@Inject
	protected static OpenIdConnectAuthenticationHandler
		openIdConnectAuthenticationHandler;

	@Inject
	private static OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Inject
	private static OAuthClientASLocalMetadataLocalService _oAuthClientASLocalMetadataLocalService;

	protected MockHttpServletRequest generateHttpRequest()
		throws PortalException, JsonProcessingException {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();

		long clientId = RandomTestUtil.randomLong();

		String authUri = "test.local"; // Must end with local

		createOAuthClientEntry(clientId, authUri);
		createOAuthClientASLocalMetadata(authUri);

		openIdConnectAuthenticationHandler.requestAuthentication(clientId, mockHttpServletRequest, mockHttpServletResponse);

		// Workaround to be able to get state from session because
		// OpenIdConnectAuthenticationSession is internal
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		String attribute = objectMapper.writeValueAsString(mockHttpServletRequest.getSession().getAttribute(
			mockHttpServletRequest.getSession().getAttributeNames().nextElement()));

		HashMap<String, String> map = (HashMap<String, String>)objectMapper.readValue(attribute, HashMap.class).get("_state");

		mockHttpServletRequest.setRequestURI("intergrationTest?code=12345678&state=" + map.get("value"));

		return mockHttpServletRequest;
	}
	protected final MockHttpServletResponse mockHttpServletResponse =
		new MockHttpServletResponse();
	protected OAuthClientEntry createOAuthClientEntry (long clientId, String authServerWellKnownURI) {
		OAuthClientEntry oAuthClientEntry = _oAuthClientEntryLocalService.createOAuthClientEntry(clientId);

		String OIDCUserInfoMapper =  JSONUtil.put(
			"address", JSONUtil.put("street", "street")
		).put(
			"contact", JSONUtil.put("birthdate", "birthdate")
		).put(
			"user",
			JSONUtil.put(
				"emailAddress", "email"
			).put(
				"firstName", "given_name"
			).put(
				"jobTitle", "jobTitle"
			).put(
				"lastName", "family_name"
			).put(
				"middleName", "middle_name"
			).put(
				"screenName", "screen_name"
			)
		).toString();

		oAuthClientEntry.setOIDCUserInfoMapperJSON(OIDCUserInfoMapper);

		oAuthClientEntry.setAuthServerWellKnownURI(authServerWellKnownURI);

		String authRequestParametersJSON =  JSONUtil.put(
			"response_type", "code"
		).put(
			"scope", "openid"
		).toString();

		oAuthClientEntry.setAuthRequestParametersJSON(authRequestParametersJSON);

		String infoJSON =  JSONUtil.put(
			"client_id", RandomTestUtil.randomString()
		).put(
			"client_secret", RandomTestUtil.randomString()
		).toString();

		oAuthClientEntry.setInfoJSON(infoJSON);

		oAuthClientEntry.setTokenRequestParametersJSON("{}");

		oAuthClientEntry.setClientId(String.valueOf(clientId));

		return _oAuthClientEntryLocalService.addOAuthClientEntry(oAuthClientEntry);
	}

	protected OAuthClientASLocalMetadata createOAuthClientASLocalMetadata (String localWellKnownURI) {
		OAuthClientASLocalMetadata oAuthClientASLocalMetadata = _oAuthClientASLocalMetadataLocalService.createOAuthClientASLocalMetadata(RandomTestUtil.randomLong());
		oAuthClientASLocalMetadata.setLocalWellKnownURI(localWellKnownURI);

		String metadataJSON = JSONUtil.put(
			"issuer", RandomTestUtil.randomString()
		).put(
			"authorization_endpoint", RandomTestUtil.randomString()
		).put(
			"token_endpoint", RandomTestUtil.randomString()
		).put(
			"jwks_uri", RandomTestUtil.randomString()
		).put(
			"response_types_supported", new String[]{"code"}
		).put(
			"subject_types_supported", new String[]{"pairwise", "public"}
		).toString();

		oAuthClientASLocalMetadata.setMetadataJSON(metadataJSON);

		return _oAuthClientASLocalMetadataLocalService.addOAuthClientASLocalMetadata(oAuthClientASLocalMetadata);
	}

	private static final String
		_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY =
			"open.id.connect.user.info.processor.impl.issuer";

	private static final String
		_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY =
			"open.id.connect.user.info.processor.impl.regular.role";

	private static String _defaultIssuer;
	private static String _defaultRegularRole;

}