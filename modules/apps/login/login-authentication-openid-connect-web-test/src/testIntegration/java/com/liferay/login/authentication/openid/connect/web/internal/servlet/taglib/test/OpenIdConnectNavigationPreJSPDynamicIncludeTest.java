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
import com.liferay.oauth.client.persistence.service.persistence.OAuthClientEntryPersistence;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testIncludeOnUtilityPageWithFacebookConnectEnable()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				 new ConfigurationTemporarySwapper(
					 OpenIdConnectConfiguration.class.getName(),
					 HashMapDictionaryBuilder.<String, Object>put(
						 "enabled", true
					 ).put(
						 "tokenRefreshOffset", 400
					 ).build())) {

			_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
				null, TestPropsValues.getUserId(), _group.getGroupId(), 0, 0,
				true, RandomTestUtil.randomString(),
				LayoutUtilityPageEntryConstants.TYPE_LOGIN, 0, _serviceContext);

			Layout layout =
				_layoutUtilityPageEntryLayoutProvider.
					getDefaultLayoutUtilityPageEntryLayout(
						_group.getGroupId(),
						LayoutUtilityPageEntryConstants.TYPE_LOGIN);

			layout.setType("notUtility");

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest(layout);

			MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

			String _tokenRequestParametersJSON = "{\"grant_type\":\"authorization_code\",\"scope\":\"openid email profile\"}";
			String _infoJson = "{\"removed Secrets and client ids, probably the test wont run without these, but I can not git push\"}";
			String _authRequestParametersJSON = "{\"scope\":\"openid email profile\",\"response_type\":\"code\"}";
			String _oidcUserInfoMapperJSON = "{\"address\":{\"zip\":\"address->postal_code\",\"country\":\"address->country\",\"city\":\"address->locality\",\"addressType\":\"\",\"street\":\"address->street_address\",\"region\":\"address->region\"},\"phone\":{\"phoneType\":\"\",\"phone\":\"phone_number\"},\"contact\":{\"birthdate\":\"birthdate\",\"gender\":\"gender\"},\"users_roles\":{\"roles\":\"\"},\"user\":{\"firstName\":\"given_name\",\"lastName\":\"family_name\",\"emailAddress\":\"email\",\"jobTitle\":\"\",\"languageId\":\"locale\",\"middleName\":\"middle_name\",\"screenName\":\"\"}}";

			_oAuthClientEntryLocalService.addOAuthClientEntry(TestPropsValues.getUserId(), _authRequestParametersJSON,
				"https://accounts.google.com/.well-known/openid-configuration", _infoJson,
				_oidcUserInfoMapperJSON, _tokenRequestParametersJSON);

			//it is ok

			/*OAuthClientEntry oAuthClientEntry = _persistence.create(RandomTestUtil.nextLong());
			oAuthClientEntry.setMvccVersion(RandomTestUtil.nextLong());
			oAuthClientEntry.setUserName(RandomTestUtil.randomString());

			oAuthClientEntry.setCreateDate(RandomTestUtil.nextDate());

			oAuthClientEntry.setModifiedDate(RandomTestUtil.nextDate());

			oAuthClientEntry.setAuthRequestParametersJSON(
				RandomTestUtil.randomString());

			oAuthClientEntry.setAuthServerWellKnownURI(
				RandomTestUtil.randomString());

			oAuthClientEntry.setClientId(RandomTestUtil.randomString());

			oAuthClientEntry.setInfoJSON(RandomTestUtil.randomString());

			oAuthClientEntry.setOIDCUserInfoMapperJSON(
				RandomTestUtil.randomString());

			oAuthClientEntry.setTokenRequestParametersJSON(
				RandomTestUtil.randomString());


			oAuthClientEntry.setAuthServerWellKnownURI("openid-configuration");
			oAuthClientEntry.setCompanyId(_group.getCompanyId());
			oAuthClientEntry.setUserId(TestPropsValues.getUserId());
			*//*ouauthBaeimpl.persist();*//*
			OAuthClientEntryLocalServiceUtil.addOAuthClientEntry(_persistence.update(oAuthClientEntry));
			*//*_persistence.update(oAuthClientEntry);*/

			//1setup = mvcRenderCommandName = "/login/login"
			/*String mvcRenderCommandName = ParamUtil.getString(
				httpServletRequest, "mvcRenderCommandName");*/

			//!_openIdConnect.isEnabled(themeDisplay.getCompanyId()) = true

			mockHttpServletRequest.setParameter("hello Alvaro", OpenIdConnectWebKeys.OPEN_ID_CONNECT_REQUEST_ACTION_NAME);


			_dynamicInclude.include(
				mockHttpServletRequest, mockHttpServletResponse,
				RandomTestUtil.randomString());



			Assert.assertTrue(searchStringByAlvaro("openid"));

	/*		BaseJSPDynamicInclude trialMock = Mockito.mock(BaseJSPDynamicInclude.class);

			Mockito.verifyNoInteractions(trialMock); //.include(mockHttpServletRequest, mockHttpServletResponse, "key");*/
		}

	}

	private boolean searchStringByAlvaro(String path) throws IOException {
		URL url = new URL("http://localhost:8080");

		HttpURLConnection connection =
			(HttpURLConnection)url.openConnection();

		connection.setRequestMethod("GET");

		BufferedReader reader = new BufferedReader(
			new InputStreamReader(connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String line;
		boolean findText = false;

		while (((line = reader.readLine()) != null) && !findText) {
			findText = line.contains(path);
			response.append(line);
		}

		writeToFile(response.toString());

		reader.close();

		return findText;
	}

	private void writeToFile(String text){
		String fileName = "c:myFile.txt"; // Replace with your desired filename
		boolean append = false; // Set to true to append content

		try {
			FileWriter fileWriter = new FileWriter(fileName, append);
			BufferedWriter writer = new BufferedWriter(fileWriter);

			// Write to the file
			writer.write(text);

			// Important: Close the writer to flush data and release resources
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIncludeWithOpenIdConnectDisabled() throws Exception {
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
	public void testIncludeWithOpenIdConnectEnabled() throws Exception {
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

	@Inject
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Inject
	private OAuthClientEntryPersistence _persistence;

	private ServiceContext _serviceContext;

}