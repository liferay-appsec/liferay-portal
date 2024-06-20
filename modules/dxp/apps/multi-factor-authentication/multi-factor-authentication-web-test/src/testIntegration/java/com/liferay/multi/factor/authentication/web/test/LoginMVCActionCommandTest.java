/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.web.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

				String password = StringUtil.toLowerCase(
					RandomTestUtil.randomString());

				_userLocalService.updatePassword(
					user.getUserId(), password, password, false, false);

				try (ConfigurationTemporarySwapper
						configurationTemporarySwapper1 =
							new ConfigurationTemporarySwapper(
								"com.liferay.multi.factor.authentication.ip." +
									"address.internal.configuration." +
										"MFAIPAddressConfiguration",
								HashMapDictionaryBuilder.<String, Object>put(
									"enabled", true
								).build())) {

					MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
						_getMockLiferayPortletActionRequest(
							user.getEmailAddress(), password);
					ReflectionTestUtil.invoke(
						_mvcActionCommand, "doProcessAction",
						new Class<?>[] {
							ActionRequest.class, ActionResponse.class
						},
						mockLiferayPortletActionRequest,
						new MockLiferayPortletActionResponse());

					User user1 = _userLocalService.getUser(user.getUserId());

					Assert.assertFalse(user1.isPasswordReset());
				}
			}
			catch (Exception exception) {
				_log.error("Pushing Service Context ", exception);
			}

		//	HttpServletRequest hola = mockLiferayPortletActionRequest.getHttpServletRequest();
		//	hola.getAttribute("REDIRECT");


		}
	}

	@Test
	public void testNormalStateWhenLoginFromAnUtilityPage1() throws Exception {
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

				String password = StringUtil.toLowerCase(
					RandomTestUtil.randomString());

				_userLocalService.updatePassword(
					user.getUserId(), password, password, true, false);

				try (ConfigurationTemporarySwapper
						configurationTemporarySwapper1 =
							new ConfigurationTemporarySwapper(
								"com.liferay.multi.factor.authentication.ip." +
									"address.internal.configuration." +
										"MFAIPAddressConfiguration",
								HashMapDictionaryBuilder.<String, Object>put(
									"enabled", true
								).build())) {

					ReflectionTestUtil.invoke(
						_mvcActionCommand, "doProcessAction",
						new Class<?>[] {
							ActionRequest.class, ActionResponse.class
						},
						_getMockLiferayPortletActionRequest(
							user.getEmailAddress(), password),
						new MockLiferayPortletActionResponse());

					User user1 = _userLocalService.getUser(user.getUserId());

					Assert.assertEquals(
						user.isPasswordReset(), user1.isPasswordReset());
					Assert.assertTrue(user1.isPasswordReset());
				}
			}
			catch (Exception exception) {
				_log.error("Pushing Service Context ", exception);
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
		mockLiferayPortletActionRequest.setParameter("password", password);

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

	private static final Log _log = LogFactoryUtil.getLog(
		LoginMVCActionCommandTest.class);

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject(filter = "mvc.command.name=/login/login")
	private MVCActionCommand _mvcActionCommand;

	@Inject
	private Portal _portal;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}