/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.login.authentication.openid.connect.web.internal.servlet.taglib.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.kernel.provider.LayoutUtilityPageEntryLayoutProvider;
import com.liferay.layout.utility.page.model.LayoutUtilityPageEntry;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalService;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
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
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
		_userId = TestPropsValues.getUserId();

		_group = GroupTestUtil.addGroup(
			TestPropsValues.getCompanyId(), _userId, 0);

		_oAuthClientEntry = _addOAuthClientEntries(_userId);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		_layoutUtilityPageEntry = _addLayoutUtilityPageEntryToLocalService();
	}

	@After
	public void tearDown() throws PortalException {
		ServiceContextThreadLocal.popServiceContext();

		_oAuthClientEntryLocalService.deleteOAuthClientEntry(_oAuthClientEntry);

		//_layoutUtilityPageEntryLocalService.deleteLayoutUtilityPageEntry(_layoutUtilityPageEntry);
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

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest(_getLayout("notUtility"));

			MockHttpServletResponse _mockHttpServletResponse =
				new MockHttpServletResponse();

			_layoutUtilityPageEntry = _addLayoutUtilityPageEntryToLocalService();

			_dynamicInclude.include(
				mockHttpServletRequest, _mockHttpServletResponse,
				RandomTestUtil.randomString());

			Assert.assertTrue(_isStringInResponse("openid_connect_request"));
		}
	}

	@Test
	public void testIncludeOnUtilityPageWithOpenIdConnectDisabled()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					OpenIdConnectConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", false
					).put(
						"tokenRefreshOffset", 400
					).build())) {

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest(_getLayout("utility"));

			MockHttpServletResponse _mockHttpServletResponse =
				new MockHttpServletResponse();

			_dynamicInclude.include(
				mockHttpServletRequest, _mockHttpServletResponse,
				RandomTestUtil.randomString());

			Assert.assertFalse(_isStringInResponse("openid_connect_request"));
		}
	}

	@Test
	public void testIncludeOnUtilityPageWithOpenIdConnectEnabled()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					OpenIdConnectConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", true
					).put(
						"tokenRefreshOffset", 400
					).build())) {

			MockHttpServletRequest mockHttpServletRequest =
				_getMockHttpServletRequest(_getLayout("utility"));

			MockHttpServletResponse _mockHttpServletResponse =
				new MockHttpServletResponse();

			_dynamicInclude.include(
				mockHttpServletRequest, _mockHttpServletResponse,
				RandomTestUtil.randomString());

			Assert.assertFalse(_isStringInResponse("openid_connect_request"));
		}
	}

	private LayoutUtilityPageEntry _addLayoutUtilityPageEntryToLocalService()
		throws PortalException {

		return _layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
			null, TestPropsValues.getUserId(), _group.getGroupId(), 0, 0, true,
			RandomTestUtil.randomString(),
			LayoutUtilityPageEntryConstants.TYPE_LOGIN, 0, _serviceContext);
	}

	private OAuthClientEntry _addOAuthClientEntries(long userId)
		throws PortalException {

		String authRequestParametersJSON = "{}";
		String authServerWellKnowUri =
			"https://accounts.google.com/.well-known/openid-configuration";
		String infoJSON = "{\"client_id\":\"mocked_id\"}";
		String oidcUserInfoMapperJSON =
			"{\"address\":{\"zip\":\"address->postal_code\",\"country\":\"address->country\",\"city\":\"address->locality\",\"addressType\":\"\"," +
				"\"street\":\"address->street_address\",\"region\":\"address->region\"},\"phone\":{\"phoneType\":\"\",\"phone\":\"phone_number\"}," +
					"\"contact\":{\"birthdate\":\"birthdate\",\"gender\":\"gender\"},\"users_roles\":{\"roles\":\"\"},\"user\":{\"firstName\":\"given_name\"," +
						"\"lastName\":\"family_name\",\"emailAddress\":\"email\",\"jobTitle\":\"\",\"languageId\":\"locale\",\"middleName\":\"middle_name\",\"screenName\":\"\"}}";
		String tokenRequestParametersJSON = "{}";

		return _oAuthClientEntryLocalService.addOAuthClientEntry(
			userId, authRequestParametersJSON, authServerWellKnowUri, infoJSON,
			oidcUserInfoMapperJSON, tokenRequestParametersJSON);
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

	private boolean _isStringInResponse(String searchString)
		throws IOException {

		URL url = new URL("http://localhost:8080/c/portal/login");

		HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		connection.setRequestMethod("GET");

		BufferedReader reader = new BufferedReader(
			new InputStreamReader(connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String line;
		boolean isContain = false;

		while (((line = reader.readLine()) != null) && !isContain) {
			isContain = line.contains(searchString);
			response.append(line);
		}

		writeToFile(response.toString());

		reader.close();

		return isContain;
	}

	private void writeToFile(String text) {
		String fileName = "c:myFile.txt"; // Replace with your desired filename
		boolean append = false; // Set to true to append content

		try {
			FileWriter fileWriter = new FileWriter(fileName, append);

			BufferedWriter writer = new BufferedWriter(fileWriter);

			// Write to the file

			writer.write(text);

			// Important: Close the writer to flush data and release resources

			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(
		filter = "component.name=com.liferay.login.authentication.openid.connect.web.internal.servlet.taglib.OpenIdConnectNavigationPreJSPDynamicInclude"
	)
	private DynamicInclude _dynamicInclude;

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private LayoutUtilityPageEntry _layoutUtilityPageEntry;

	@Inject
	private LayoutUtilityPageEntryLayoutProvider
		_layoutUtilityPageEntryLayoutProvider;

	@Inject
	private LayoutUtilityPageEntryLocalService
		_layoutUtilityPageEntryLocalService;

	@DeleteAfterTestRun
	private OAuthClientEntry _oAuthClientEntry;

	@Inject
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	private ServiceContext _serviceContext;
	private long _userId;

}