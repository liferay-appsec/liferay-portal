/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.login.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.kernel.service.UserLocalService;



import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * @author Alvaro Saugar
 */
@RunWith(Arquillian.class)
public class LoginMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		//_originalName = PrincipalThreadLocal.getName();

		_group = GroupTestUtil.addGroup();
/*
		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);


 */


		_company = _companyLocalService.getCompany(_group.getCompanyId());
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}


	@Test
	public void testNormalStateWhenLoginFromAnUtilityPage() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				 new ConfigurationTemporarySwapper(
					 "com.liferay.multi.factor.authentication.email.otp." +
					 "configuration.MFAEmailOTPConfiguration",
					 HashMapDictionaryBuilder.<String, Object>put(
						 "enabled", true
					 ).build())) {
			User user = UserTestUtil.addUser(_company);

			try {
				ServiceContextThreadLocal.pushServiceContext(
					ServiceContextTestUtil.getServiceContext(
						user.getGroupId(), user.getUserId()));
				//crear usuario


				_userLocalService.updatePassword(user.getUserId(), "passs",
					"passs", user.getPasswordReset(), false);

				String passoword = user.getPassword();


				try (ConfigurationTemporarySwapper configurationTemporarySwapper1 =
						 new ConfigurationTemporarySwapper(
							 "com.liferay.multi.factor.authentication.ip.address.internal.configuration.MFAIPAddressConfiguration",
							 HashMapDictionaryBuilder.<String, Object>put(
								 "enabled", true
							 ).build())) {


					ReflectionTestUtil.invoke(
						_mvcActionCommand, "doProcessAction",
						new Class<?>[]{
							ActionRequest.class, ActionResponse.class},
						_getMockLiferayPortletActionRequest(
							user.getEmailAddress(), user.getPassword()),
						new MockLiferayPortletActionResponse());

					User user1 = _userLocalService.getUser(user.getUserId());

					Assert.assertEquals(
						user.getPasswordReset(),
						user1.getPasswordReset());
				}

				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}


		}
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
		String email, String password)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay());

		mockLiferayPortletActionRequest.setParameter("login", email);
		mockLiferayPortletActionRequest.setParameter("password", "passs");

		return mockLiferayPortletActionRequest;
	}


	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(_company);

		LayoutSet layoutSet = _group.getPublicLayoutSet();

		themeDisplay.setLookAndFeel(layoutSet.getTheme(), null);

		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setRealUser(TestPropsValues.getUser());
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}
	/*
		public void testNormalStateWhenLoginFromAnUtilityPage() throws Exception {
			try (ConfigurationTemporarySwapper configurationTemporarySwapper =
					 new ConfigurationTemporarySwapper(
						 "com.liferay.login.web.internal.configuration." +
						 "AuthLoginConfiguration",
						 HashMapDictionaryBuilder.<String, Object>put(
							 "promptEnabled", true
						 ).build())) {

				UserTestUtil.setUser(TestPropsValues.getUr());

		/*		SiteInitializer siteInitializer =
					_siteInitializerRegistry.getSiteInitializer(
						"com.liferay.site.initializer.welcome");

				siteInitializer.initialize(_group.getGroupId());

			Layout layout = _addTypeContentLayout(true);

			_removeGuestViewPermission(layout);

			PrincipalThreadLocal.setName(_originalName);

			URL url = new URL(
				"http://localhost:8080/web" + _group.getFriendlyURL() +
				layout.getFriendlyURL());

			HttpURLConnection connection =
				(HttpURLConnection)url.openConnection();

			connection.setRequestMethod("GET");

			Assert.assertEquals(200, connection.getResponseCode());

			String queryString = connection.getURL(
			).getQuery();

			Assert.assertTrue(queryString.contains("p_p_state=normal"));
		}
	}
		*/
	private Layout _addTypeContentLayout(boolean publish) throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group, TestPropsValues.getUserId());

		Layout layout = _layoutLocalService.addLayout(
			TestPropsValues.getUserId(), _group.getGroupId(), false,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID,
			RandomTestUtil.randomString(), StringPool.BLANK, StringPool.BLANK,
			LayoutConstants.TYPE_CONTENT, false, StringPool.BLANK,
			serviceContext);

		if (publish) {
			Layout draftLayout = layout.fetchDraftLayout();

			Assert.assertNotNull(draftLayout);

			ContentLayoutTestUtil.publishLayout(draftLayout, layout);

			layout = _layoutLocalService.getLayout(layout.getPlid());

			Assert.assertTrue(layout.isPublished());
		}
		else {
			Assert.assertFalse(layout.isPublished());
		}

		return layout;
	}

	private void _removeGuestViewPermission(Layout layout) throws Exception {
		Role guestRole = _roleLocalService.getRole(
			layout.getCompanyId(), RoleConstants.GUEST);

		_resourcePermissionLocalService.removeResourcePermission(
			layout.getCompanyId(), Layout.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(layout.getPlid()), guestRole.getRoleId(),
			ActionKeys.VIEW);
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	private String _originalName;

	@Inject
	private Portal _portal;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	private ServiceContext _serviceContext;

	@Inject(
		filter = "mvc.command.name=/login/login"
	)
	private MVCActionCommand _mvcActionCommand;

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private UserLocalService _userLocalService;

}