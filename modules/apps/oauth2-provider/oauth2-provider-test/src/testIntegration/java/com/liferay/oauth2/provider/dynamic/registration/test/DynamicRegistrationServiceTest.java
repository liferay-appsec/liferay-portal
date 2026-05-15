/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.dynamic.registration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.client.test.BaseClientTestCase;
import com.liferay.oauth2.provider.client.test.BaseTestPreparatorBundleActivator;
import com.liferay.oauth2.provider.constants.OAuth2ApplicationConstants;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleActivator;

/**
 * @author Jorge García Jiménez
 */
@RunWith(Arquillian.class)
public class DynamicRegistrationServiceTest extends BaseClientTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@FeatureFlag("LPD-63416")
	@Test
	public void testDelete() throws Exception {
		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.fetchOAuth2Application(
				TestPropsValues.getCompanyId(), "oauthDeleteMeApplication");

		WebTarget registerWebTarget = getRegisterWebTarget(
			oAuth2Application.getClientId());

		Invocation.Builder invocationBuilder = authorize(
			registerWebTarget.request(),
			_getToken(
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					TestPropsValues.getCompanyId(),
					"oauthDynamicRegisterTestApplication")));

		Response response = invocationBuilder.delete();

		Assert.assertEquals(403, response.getStatus());

		invocationBuilder = authorize(
			registerWebTarget.request(),
			_getToken(_getDynamicRegistratorOAuth2Application()));

		response = invocationBuilder.delete();

		Assert.assertEquals(204, response.getStatus());

		response = invocationBuilder.delete();

		Assert.assertEquals(401, response.getStatus());
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPost() throws Exception {
		WebTarget registerWebTarget = getRegisterWebTarget();

		Invocation.Builder invocationBuilder = authorize(
			registerWebTarget.request(),
			_getToken(
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					TestPropsValues.getCompanyId(),
					"oauthDynamicRegisterTestApplication")));

		Response response = invocationBuilder.method(
			"post",
			Entity.json(
				JSONUtil.put(
					"client_name", RandomTestUtil.randomString()
				).toString()));

		Assert.assertEquals(401, response.getStatus());

		String clientName = RandomTestUtil.randomString();

		JSONObject jsonObject = JSONUtil.put(
			"client_name", clientName
		).put(
			"grant_types",
			new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
		).put(
			"logo_uri", RandomTestUtil.randomString()
		).put(
			"redirect_uris",
			new String[] {
				"https://client.example.org/callback",
				"https://client.example.org/callback2"
			}
		).put(
			"scope", "Liferay.Headless.Admin.Site.everything"
		);

		response = invocationBuilder.method(
			"post", Entity.json(jsonObject.toString()));

		Assert.assertEquals(401, response.getStatus());

		OAuth2Application oAuth2Application =
			_getDynamicRegistratorOAuth2Application();

		invocationBuilder = authorize(
			registerWebTarget.request(), _getToken(oAuth2Application));

		response = invocationBuilder.method(
			"post", Entity.json(jsonObject.toString()));

		Assert.assertEquals(201, response.getStatus());

		JSONObject responseJSONObject = parseJSONObject(response);

		Assert.assertEquals(
			clientName, responseJSONObject.getString("client_name"));

		String clientId = responseJSONObject.getString(
			OAuthConstants.CLIENT_ID);

		jsonObject.put(
			"response_types",
			Collections.singletonList(OAuthConstants.CODE_RESPONSE_TYPE));

		response = invocationBuilder.method(
			"post", Entity.json(jsonObject.toString()));

		Assert.assertEquals(400, response.getStatus());

		Assert.assertEquals("invalid_client_metadata", parseError(response));

		registerWebTarget = getRegisterWebTarget(clientId);

		invocationBuilder = authorize(
			registerWebTarget.request(), _getToken(oAuth2Application));

		invocationBuilder.header("Origin", RandomTestUtil.randomString());

		response = invocationBuilder.get();

		Assert.assertEquals(200, response.getStatus());

		responseJSONObject = parseJSONObject(response);

		Assert.assertEquals(
			clientName, responseJSONObject.getString("client_name"));

		Assert.assertNull(
			response.getHeaderString("Access-Control-Allow-Origin"));
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPostAnonymousAcceptedWhenOpen() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		Invocation.Builder invocationBuilder = registerWebTarget.request();

		String clientName = RandomTestUtil.randomString();

		String body = JSONUtil.put(
			"client_name", clientName
		).put(
			"grant_types",
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			"redirect_uris",
			new String[] {"https://client.example.org/callback"}
		).put(
			"response_types", new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _relaxIATSwap(
					companyId)) {

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(201, response.getStatus());

			JSONObject responseJSONObject = parseJSONObject(response);

			Assert.assertEquals(
				clientName, responseJSONObject.getString("client_name"));

			String clientId = responseJSONObject.getString(
				OAuthConstants.CLIENT_ID);

			OAuth2Application oAuth2Application =
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					companyId, clientId);

			Assert.assertNotNull(oAuth2Application);

			User serviceAccountUser = _userLocalService.getUserByScreenName(
				companyId, "default-service-account");

			Assert.assertEquals(
				serviceAccountUser.getUserId(), oAuth2Application.getUserId());

			Assert.assertFalse(oAuth2Application.isTrustedApplication());
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPostAnonymousRejectedWhenStrict() throws Exception {
		WebTarget registerWebTarget = getRegisterWebTarget();

		Invocation.Builder invocationBuilder = registerWebTarget.request();

		Response response = invocationBuilder.method(
			"post",
			Entity.json(
				JSONUtil.put(
					"client_name", RandomTestUtil.randomString()
				).toString()));

		Assert.assertEquals(401, response.getStatus());
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPostAnonymousRejectsDisallowedRedirectURI()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			"client_name", RandomTestUtil.randomString()
		).put(
			"grant_types",
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			"redirect_uris", new String[] {"https://attacker.test/callback"}
		).put(
			"response_types", new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId,
						"com.liferay.oauth2.provider.rest.internal." +
							"configuration.DynamicRegistrationConfiguration",
						HashMapDictionaryBuilder.<String, Object>put(
							"dynamic.registration.anonymous.allowed.redirect." +
								"uri.patterns",
							new String[] {"https://*.example.org/*"}
						).put(
							"dynamic.registration.require.initial.access.token",
							false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals("invalid_redirect_uri", parseError(response));
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPostAnonymousRejectsDisallowedScope() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			"client_name", RandomTestUtil.randomString()
		).put(
			"grant_types",
			new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
		).put(
			"scope", "Liferay.Headless.Admin.Site.everything"
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId,
						"com.liferay.oauth2.provider.rest.internal." +
							"configuration.DynamicRegistrationConfiguration",
						HashMapDictionaryBuilder.<String, Object>put(
							"dynamic.registration.anonymous.allowed.scopes",
							new String[] {
								"Liferay.Headless.Delivery.everything"
							}
						).put(
							"dynamic.registration.require.initial.access.token",
							false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals(
				OAuthConstants.INVALID_SCOPE, parseError(response));
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPostWithBearerInOpenMode() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _relaxIATSwap(
					companyId)) {

			Invocation.Builder invocationBuilder = authorize(
				registerWebTarget.request(),
				_getToken(_getDynamicRegistratorOAuth2Application()));

			Response response = invocationBuilder.method(
				"post",
				Entity.json(
					JSONUtil.put(
						"client_name", RandomTestUtil.randomString()
					).put(
						"grant_types",
						new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
					).put(
						"redirect_uris",
						new String[] {"https://client.example.org/callback"}
					).toString()));

			Assert.assertEquals(201, response.getStatus());
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPut() throws Exception {
		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.fetchOAuth2Application(
				TestPropsValues.getCompanyId(), "oauthDeleteMeApplication");

		WebTarget registerWebTarget = getRegisterWebTarget(
			oAuth2Application.getClientId());

		Invocation.Builder invocationBuilder = authorize(
			registerWebTarget.request(),
			_getToken(_getDynamicRegistratorOAuth2Application()));

		String clientName = RandomTestUtil.randomString();

		Response response = invocationBuilder.method(
			"put",
			Entity.json(
				JSONUtil.put(
					"client_name", clientName
				).put(
					"grant_types",
					new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
				).put(
					"logo_uri", RandomTestUtil.randomString()
				).put(
					"redirect_uris",
					new String[] {
						"https://client.example.org/callback",
						"https://client.example.org/callback2"
					}
				).put(
					"scope", "Liferay.Headless.Admin.Site.everything"
				).toString()));

		Assert.assertEquals(200, response.getStatus());

		JSONObject jsonObject = parseJSONObject(response);

		Assert.assertEquals(clientName, jsonObject.getString("client_name"));
	}

	protected static WebTarget getRegisterWebTarget() {
		WebTarget webTarget = getOAuth2WebTarget();

		return webTarget.path("register");
	}

	protected static WebTarget getRegisterWebTarget(String target) {
		WebTarget webTarget = getRegisterWebTarget();

		return webTarget.path(target);
	}

	@Override
	protected BundleActivator getBundleActivator() {
		return new DynamicRegistrationServiceTestPreparatorBundleActivator();
	}

	private OAuth2Application _getDynamicRegistratorOAuth2Application()
		throws Exception {

		DynamicQuery dynamicQuery =
			_oAuth2ApplicationLocalService.dynamicQuery();

		Property companyIdProperty = PropertyFactoryUtil.forName("companyId");

		dynamicQuery.add(companyIdProperty.eq(TestPropsValues.getCompanyId()));

		Property nameProperty = PropertyFactoryUtil.forName("name");

		dynamicQuery.add(
			nameProperty.eq(
				OAuth2ApplicationConstants.NAME_DYNAMIC_REGISTRATOR));

		List<OAuth2Application> oAuth2Applications =
			_oAuth2ApplicationLocalService.dynamicQuery(dynamicQuery);

		Assert.assertFalse(oAuth2Applications.isEmpty());

		return oAuth2Applications.get(0);
	}

	private String _getToken(OAuth2Application oAuth2Application) {
		WebTarget tokenWebTarget = getTokenWebTarget();

		Invocation.Builder invocationBuilder = tokenWebTarget.request();

		MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();

		formData.add(OAuthConstants.CLIENT_ID, oAuth2Application.getClientId());
		formData.add(
			OAuthConstants.CLIENT_SECRET, oAuth2Application.getClientSecret());
		formData.add(
			OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS_GRANT);

		String tokenString = parseTokenString(
			invocationBuilder.post(Entity.form(formData)));

		Assert.assertNotNull(tokenString);

		return tokenString;
	}

	private CompanyConfigurationTemporarySwapper _relaxIATSwap(long companyId)
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			companyId,
			"com.liferay.oauth2.provider.rest.internal.configuration." +
				"DynamicRegistrationConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"dynamic.registration.require.initial.access.token", false
			).build());
	}

	@Inject
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Inject
	private UserLocalService _userLocalService;

	private class DynamicRegistrationServiceTestPreparatorBundleActivator
		extends BaseTestPreparatorBundleActivator {

		@Override
		protected void prepareTest() throws Exception {
			long companyId = TestPropsValues.getCompanyId();

			User user = UserTestUtil.getAdminUser(companyId);

			createOAuth2Application(
				companyId, user, "oauthDynamicRegisterTestApplication");
			createOAuth2Application(
				companyId, user, "oauthDeleteMeApplication");
		}

	}

}