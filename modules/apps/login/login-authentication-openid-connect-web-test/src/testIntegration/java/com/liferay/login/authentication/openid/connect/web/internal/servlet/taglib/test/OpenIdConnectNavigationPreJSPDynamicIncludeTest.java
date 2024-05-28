/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.login.authentication.openid.connect.web.internal.servlet.taglib.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.kernel.provider.LayoutUtilityPageEntryLayoutProvider;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.sso.openid.connect.configuration.OpenIdConnectConfiguration;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;

/**
 * @author Tamás Biro
 */
@RunWith(Arquillian.class)
public class OpenIdConnectNavigationPreJSPDynamicIncludeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(), 0);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		_mockHttpServletResponse = new MockHttpServletResponse();
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testIncludeNotOnUtilityPageWithOpenIdConnectEnabled()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					OpenIdConnectConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", true
					).put(
						"tokenRefreshOffset", 400
					).build())) {

			_addLayoutUtilityPageEntryToLocalService();

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest(_getLayout("notUtility"));

			_addOAuthClientEntries();

			_dynamicInclude.include(
				mockHttpServletRequest, _mockHttpServletResponse,
				RandomTestUtil.randomString());

			String[] props = PropsUtil.getArray(
				PropsKeys.LOGIN_FORM_NAVIGATION_PRE);

			for (String string: props){
				System.out.println(string);
			}

			Assert.assertTrue(Arrays.asList(props).contains("openid_connect.jsp"));

			/*Assert.assertNull(
				mockHttpServletRequest.getAttribute(
					OpenIdConnectWebKeys.OPEN_ID_CONNECT_ACTION_URL)); //toDo: how to check if mockHttpServletResponse contains anything relevant to the test???*/
		}
	}

	@Test
	public void testIncludeOnUtilityPageWithOpenIdConnectDisabled() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					OpenIdConnectConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", false
					).put(
						"oauthAuthURL", RandomTestUtil.randomString()
					).build())) {

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest();

			_dynamicInclude.include(
				mockHttpServletRequest, new MockHttpServletResponse(),
				RandomTestUtil.randomString());

			Assert.assertNull(
				mockHttpServletRequest.getAttribute(
					OpenIdConnectWebKeys.OPEN_ID_CONNECT_REQUEST_ACTION_NAME));
		}
	}

	@Test
	public void testIncludeOnUtilityPageWithOpenIdConnectEnabled() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					OpenIdConnectConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", true
					).put(
						"oauthAuthURL", RandomTestUtil.randomString()
					).build())) {

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest();

			_dynamicInclude.include(
				mockHttpServletRequest, new MockHttpServletResponse(),
				RandomTestUtil.randomString());

			Assert.assertNotNull(
				mockHttpServletRequest.getAttribute(
					OpenIdConnectWebKeys.OPEN_ID_CONNECT_REQUEST_ACTION_NAME));
		}
	}

	private void _addLayoutUtilityPageEntryToLocalService()
		throws PortalException {

		_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
			null, TestPropsValues.getUserId(), _group.getGroupId(), 0, 0, true,
			RandomTestUtil.randomString(),
			LayoutUtilityPageEntryConstants.TYPE_LOGIN, 0, _serviceContext);
	}

	private void _addOAuthClientEntries() throws PortalException {
		String authRequestParametersJSON =
			"{\"scope\":\"openid email profile\",\"response_type\":\"code\"}";
		String authServerWellKnowUri =
			"https://accounts.google.com/.well-known/openid-configuration";
		String infoJSON =
			"{\"grant_types\":[\"authorization_code\",\"refresh_token\"],\"application_type\":\"web\",\"client_secret_expires_at\":0,\"scope\":\"openid email profile\",\"client_secrex\":\"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\",\"client_name\":\"Client to Google\",\"client_id\":\"310138411857-00g4940k2hn6kj778jo09rcbb5jbqp3f.apps.googleusercontent.com\",\"token_endpoint_auth_method\":\"client_secret_basic\",\"response_types\":[\"code\"],\"id_token_signed_response_alg\":\"RS256\"}";
		String oidcUserInfoMapperJSON =
			"{\"address\":{\"zip\":\"address->postal_code\",\"country\":\"address->country\",\"city\":\"address->locality\",\"addressType\":\"\",\"street\":\"address->street_address\",\"region\":\"address->region\"},\"phone\":{\"phoneType\":\"\",\"phone\":\"phone_number\"},\"contact\":{\"birthdate\":\"birthdate\",\"gender\":\"gender\"},\"users_roles\":{\"roles\":\"\"},\"user\":{\"firstName\":\"given_name\",\"lastName\":\"family_name\",\"emailAddress\":\"email\",\"jobTitle\":\"\",\"languageId\":\"locale\",\"middleName\":\"middle_name\",\"screenName\":\"\"}}";
		String tokenRequestParametersJSON =
			"{\"grant_type\":\"authorization_code\",\"scope\":\"openid email profile\"}";

		_oAuthClientEntryLocalService.addOAuthClientEntry(
			TestPropsValues.getUserId(), authRequestParametersJSON,
			authServerWellKnowUri, infoJSON, oidcUserInfoMapperJSON,
			tokenRequestParametersJSON);
	}

	private Layout _getLayout(String type) throws PortalException {
		Layout layout =
			_layoutUtilityPageEntryLayoutProvider.
				getDefaultLayoutUtilityPageEntryLayout(
					_group.getGroupId(),
					LayoutUtilityPageEntryConstants.TYPE_LOGIN);

		layout.setType(type);

		return layout;
	}

	private MockHttpServletRequest _getMockHttpServletRequest()
		throws Exception {

		Layout layout = _layoutLocalService.fetchDefaultLayout(
			TestPropsValues.getGroupId(), false);

		return _getMockHttpServletRequest(layout);
	}

	private MockHttpServletRequest _getMockHttpServletRequest(Layout layout)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.fetchCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLayout(layout);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockHttpServletRequest;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(
		filter = "component.name=com.liferay.login.authentication.openid.connect.web.internal.servlet.taglib.OpenIdConnectNavigationPreJSPDynamicInclude"
	)
	private DynamicInclude _dynamicInclude;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutUtilityPageEntryLayoutProvider
		_layoutUtilityPageEntryLayoutProvider;

	@Inject
	private LayoutUtilityPageEntryLocalService
		_layoutUtilityPageEntryLocalService;

	private MockHttpServletResponse _mockHttpServletResponse;

	@Inject
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	private ServiceContext _serviceContext;

}