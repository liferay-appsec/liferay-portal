/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.dynamic.registration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.client.test.BaseClientTestCase;
import com.liferay.oauth2.provider.client.test.BaseTestPreparatorBundleActivator;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.constants.OAuth2ApplicationConstants;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
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
	public void testBearerInOpenMode() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Invocation.Builder invocationBuilder = authorize(
				registerWebTarget.request(),
				_getToken(_getDynamicRegistratorOAuth2Application()));

			Response response = invocationBuilder.method(
				"post",
				Entity.json(
					JSONUtil.put(
						_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
					).put(
						_FIELD_GRANT_TYPES,
						new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
					).put(
						_FIELD_REDIRECT_URIS,
						new String[] {
							"https://" + RandomTestUtil.randomString() +
								".com/callback"
						}
					).toString()));

			Assert.assertEquals(201, response.getStatus());
		}
	}

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
	public void testOpenAccepted() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		Invocation.Builder invocationBuilder = registerWebTarget.request();

		String clientName = RandomTestUtil.randomString();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, clientName
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(201, response.getStatus());

			JSONObject responseJSONObject = parseJSONObject(response);

			Assert.assertEquals(
				clientName, responseJSONObject.getString(_FIELD_CLIENT_NAME));

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
	public void testOpenBlankScopeIsRejected() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES,
							new String[] {
								"Liferay.Headless.Delivery.everything"
							}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(400, response.getStatus());

			Assert.assertEquals(
				"invalid_client_metadata", parseError(response));
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenEnforcesHostAllowlist() throws Exception {
		String allowedHost = "test-allowed-" + RandomTestUtil.randomString();

		_testOpenEnforcesHostAllowlist(allowedHost, allowedHost, 201);
		_testOpenEnforcesHostAllowlist(
			allowedHost, "test-other-" + RandomTestUtil.randomString(), 403);

		String bracketedHost = "test-bracket-" + RandomTestUtil.randomString();

		_testOpenEnforcesHostAllowlist(
			bracketedHost, "[" + bracketedHost + "]:8080", 201);
		_testOpenEnforcesHostAllowlist(
			"[" + bracketedHost + "]:8080", bracketedHost, 201);

		String portHost = "test-port-" + RandomTestUtil.randomString();

		_testOpenEnforcesHostAllowlist(portHost, portHost + ":8080", 201);
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRateLimitDisabled() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		String clientHost =
			"test-rate-disabled-" + RandomTestUtil.randomString();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).put(
							_PROPERTY_TRUST_PROXY_HEADERS, true
						).build())) {

			for (int i = 0; i < 15; i++) {
				Invocation.Builder invocationBuilder =
					registerWebTarget.request();

				invocationBuilder.header("X-Forwarded-For", clientHost);

				Response response = invocationBuilder.method(
					"post", Entity.json(body));

				Assert.assertEquals(201, response.getStatus());
			}
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRateLimitTriggers() throws Exception {
		String clientHost =
			"test-rate-triggers-" + RandomTestUtil.randomString();

		_testOpenRateLimitTriggers(
			new String[] {clientHost, clientHost, clientHost}, clientHost);

		String normalizedHost = "test-rl-key-" + RandomTestUtil.randomString();

		_testOpenRateLimitTriggers(
			new String[] {
				"[" + normalizedHost + "]:8080",
				"[" + normalizedHost + "]:9090", normalizedHost
			},
			"[" + normalizedHost + "]");
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRejectedWhenStrict() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_HOSTS, new String[0]
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[0]
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[0]
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, true
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post",
				Entity.json(
					JSONUtil.put(
						_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
					).toString()));

			Assert.assertEquals(401, response.getStatus());
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRejectsDefaultGrantTypeWhenDisallowed()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES,
							new String[] {
								OAuthConstants.CLIENT_CREDENTIALS_GRANT
							}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals(
				"invalid_client_metadata", parseError(response));
		}
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRejectsDisallowedRedirectURI() throws Exception {
		_testOpenRejectsDisallowedRedirectURI("https://attacker.test/callback");
		_testOpenRejectsDisallowedRedirectURI(
			"https://attacker.test/foo.example.org/callback");
		_testOpenRejectsDisallowedRedirectURI("");
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testOpenRejectsDisallowedScope() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
		).put(
			_FIELD_SCOPE, "Liferay.Headless.Admin.Site.everything"
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES,
							new String[] {
								"Liferay.Headless.Delivery.everything"
							}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
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
					_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
				).toString()));

		Assert.assertEquals(401, response.getStatus());

		String clientName = RandomTestUtil.randomString();

		JSONObject jsonObject = JSONUtil.put(
			_FIELD_CLIENT_NAME, clientName
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
		).put(
			_FIELD_LOGO_URI, RandomTestUtil.randomString()
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback",
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_SCOPE, "Liferay.Headless.Admin.Site.everything"
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
			clientName, responseJSONObject.getString(_FIELD_CLIENT_NAME));

		String clientId = responseJSONObject.getString(
			OAuthConstants.CLIENT_ID);

		jsonObject.put(
			_FIELD_RESPONSE_TYPES,
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
			clientName, responseJSONObject.getString(_FIELD_CLIENT_NAME));

		Assert.assertNull(
			response.getHeaderString("Access-Control-Allow-Origin"));
	}

	@FeatureFlag("LPD-63416")
	@Test
	public void testPromotesPublicAuthorizationCode() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String clientName = RandomTestUtil.randomString();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, clientName
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).put(
			"token_endpoint_auth_method",
			OAuthConstants.TOKEN_ENDPOINT_AUTH_NONE
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(201, response.getStatus());

			JSONObject responseJSONObject = parseJSONObject(response);

			JSONArray grantTypesJSONArray = responseJSONObject.getJSONArray(
				_FIELD_GRANT_TYPES);

			Assert.assertEquals(1, grantTypesJSONArray.length());
			Assert.assertEquals(
				OAuthConstants.AUTHORIZATION_CODE_GRANT,
				grantTypesJSONArray.getString(0));

			String clientId = responseJSONObject.getString(
				OAuthConstants.CLIENT_ID);

			OAuth2Application oAuth2Application =
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					companyId, clientId);

			Assert.assertNotNull(oAuth2Application);
			Assert.assertEquals(
				Collections.singletonList(GrantType.AUTHORIZATION_CODE_PKCE),
				oAuth2Application.getAllowedGrantTypesList());
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
					_FIELD_CLIENT_NAME, clientName
				).put(
					_FIELD_GRANT_TYPES,
					new String[] {OAuthConstants.CLIENT_CREDENTIALS_GRANT}
				).put(
					_FIELD_LOGO_URI, RandomTestUtil.randomString()
				).put(
					_FIELD_REDIRECT_URIS,
					new String[] {
						"https://" + RandomTestUtil.randomString() +
							".com/callback",
						"https://" + RandomTestUtil.randomString() +
							".com/callback"
					}
				).put(
					_FIELD_SCOPE, "Liferay.Headless.Admin.Site.everything"
				).toString()));

		Assert.assertEquals(200, response.getStatus());

		JSONObject jsonObject = parseJSONObject(response);

		Assert.assertEquals(
			clientName, jsonObject.getString(_FIELD_CLIENT_NAME));
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

		String tokenString = parseTokenString(
			invocationBuilder.post(
				Entity.form(
					new MultivaluedHashMap<>(
						HashMapBuilder.put(
							OAuthConstants.CLIENT_ID,
							oAuth2Application.getClientId()
						).put(
							OAuthConstants.CLIENT_SECRET,
							oAuth2Application.getClientSecret()
						).put(
							OAuthConstants.GRANT_TYPE,
							OAuthConstants.CLIENT_CREDENTIALS_GRANT
						).build()))));

		Assert.assertNotNull(tokenString);

		return tokenString;
	}

	private void _testOpenEnforcesHostAllowlist(
			String allowedHost, String requestHost, int expectedStatus)
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {allowedHost}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).put(
							_PROPERTY_TRUST_PROXY_HEADERS, true
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			invocationBuilder.header("X-Forwarded-For", requestHost);

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(expectedStatus, response.getStatus());

			if (expectedStatus == 403) {
				Assert.assertEquals("access_denied", parseError(response));
			}
		}
	}

	private void _testOpenRateLimitTriggers(
			String[] acceptedHosts, String rejectedHost)
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS,
			new String[] {
				"https://" + RandomTestUtil.randomString() + ".com/callback"
			}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							acceptedHosts.length
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).put(
							_PROPERTY_TRUST_PROXY_HEADERS, true
						).build())) {

			for (String acceptedHost : acceptedHosts) {
				Invocation.Builder invocationBuilder =
					registerWebTarget.request();

				invocationBuilder.header("X-Forwarded-For", acceptedHost);

				Response response = invocationBuilder.method(
					"post", Entity.json(body));

				Assert.assertEquals(201, response.getStatus());
			}

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			invocationBuilder.header("X-Forwarded-For", rejectedHost);

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(429, response.getStatus());
			Assert.assertEquals("rate_limited", parseError(response));
			Assert.assertNotNull(response.getHeaderString("Retry-After"));
		}
	}

	private void _testOpenRejectsDisallowedRedirectURI(String redirectUri)
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		WebTarget registerWebTarget = getRegisterWebTarget();

		String body = JSONUtil.put(
			_FIELD_CLIENT_NAME, RandomTestUtil.randomString()
		).put(
			_FIELD_GRANT_TYPES,
			new String[] {OAuthConstants.AUTHORIZATION_CODE_GRANT}
		).put(
			_FIELD_REDIRECT_URIS, new String[] {redirectUri}
		).put(
			_FIELD_RESPONSE_TYPES,
			new String[] {OAuthConstants.CODE_RESPONSE_TYPE}
		).toString();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						companyId, _CONFIGURATION_PID,
						HashMapDictionaryBuilder.<String, Object>put(
							_PROPERTY_ALLOWED_GRANT_TYPES, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_HOSTS, new String[] {"*"}
						).put(
							_PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS,
							new String[] {"https://*.example.org/*"}
						).put(
							_PROPERTY_ALLOWED_SCOPES, new String[] {"*"}
						).put(
							_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR,
							0
						).put(
							_PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN, false
						).build())) {

			Invocation.Builder invocationBuilder = registerWebTarget.request();

			Response response = invocationBuilder.method(
				"post", Entity.json(body));

			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals("invalid_redirect_uri", parseError(response));
		}
	}

	private static final String _CONFIGURATION_PID =
		"com.liferay.oauth2.provider.rest.internal.configuration." +
			"DynamicRegistrationConfiguration";

	private static final String _FIELD_CLIENT_NAME = "client_name";

	private static final String _FIELD_GRANT_TYPES = "grant_types";

	private static final String _FIELD_LOGO_URI = "logo_uri";

	private static final String _FIELD_REDIRECT_URIS = "redirect_uris";

	private static final String _FIELD_RESPONSE_TYPES = "response_types";

	private static final String _FIELD_SCOPE = "scope";

	private static final String _PROPERTY_ALLOWED_GRANT_TYPES =
		"dynamic.client.registration.allowed.grant.types";

	private static final String _PROPERTY_ALLOWED_HOSTS =
		"dynamic.client.registration.allowed.hosts";

	private static final String _PROPERTY_ALLOWED_REDIRECT_URI_PATTERNS =
		"dynamic.client.registration.allowed.redirect.uri.patterns";

	private static final String _PROPERTY_ALLOWED_SCOPES =
		"dynamic.client.registration.allowed.scopes";

	private static final String
		_PROPERTY_MAXIMUM_NUMBER_OF_REGISTRATIONS_PER_HOUR =
			"dynamic.client.registration.maximum.number.of.registrations.per." +
				"hour";

	private static final String _PROPERTY_REQUIRE_INITIAL_ACCESS_TOKEN =
		"dynamic.client.registration.require.initial.access.token";

	private static final String _PROPERTY_TRUST_PROXY_HEADERS =
		"dynamic.client.registration.trust.proxy.headers";

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