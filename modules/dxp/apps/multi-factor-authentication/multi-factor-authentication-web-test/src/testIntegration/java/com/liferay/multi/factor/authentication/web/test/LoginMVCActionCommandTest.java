/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.web.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.multi.factor.authentication.spi.checker.headless.HeadlessMFAChecker;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletConfig;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.PortletConfigFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
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
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portlet.internal.ActionRequestImpl;
import com.liferay.portletmvc4spring.test.mock.web.portlet.MockActionRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import org.osgi.service.component.annotations.Reference;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
		_group = GroupTestUtil.addGroup();

		_company = _companyLocalService.getCompany(_group.getCompanyId());


//		ReflectionTestUtil.setFieldValue(_mvcActionCommand, "_portal", _portal);
//		ReflectionTestUtil.setFieldValue(
//			_mvcActionCommand, "_loginMVCActionCommand",
//			_loginMVCActionCommand);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	//@Test
	public void testResetPasswordValueDoesNotChangeWhenItIsFalse()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
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

				user = _userLocalService.getUser(user.getUserId());

				Bundle bundle = FrameworkUtil.getBundle(
					LoginMVCActionCommandTest.class);

				BundleContext bundleContext = bundle.getBundleContext();


				bundleContext.registerService(
					HeadlessMFAChecker.class, new HeadlessMFATestCheckerTrue(),
					MapUtil.singletonDictionary(
						"companyId", _company.getCompanyId()));


				MockLiferayPortletActionRequest mockactionRequest =
					_getMockLiferayPortletActionRequest(
						user, password, "false");

				MockLiferayPortletActionResponse mockresponse = new MockLiferayPortletActionResponse();

				ReflectionTestUtil.invoke(
					_mvcActionCommand, "doProcessAction",
					new Class<?>[] {ActionRequest.class, ActionResponse.class},
					mockactionRequest,
					mockresponse);

				User user1 = _userLocalService.getUser(user.getUserId());

				Assert.assertEquals(
					user.isPasswordReset(), user1.isPasswordReset());
				Assert.assertFalse(user1.isPasswordReset());
			}
			catch (Exception exception) {
				_log.error("Pushing Service Context ", exception);
			}
		}
	}

	//@Test
	public void testResetPasswordValueDoesNotChangeWhenItIsTrue()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				configurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
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

				user = _userLocalService.getUser(user.getUserId());

				Bundle bundle = FrameworkUtil.getBundle(
					LoginMVCActionCommandTest.class);

				BundleContext bundleContext = bundle.getBundleContext();


				bundleContext.registerService(
					HeadlessMFAChecker.class, new HeadlessMFATestCheckerTrue(),
					MapUtil.singletonDictionary(
						"companyId", _company.getCompanyId()));


				MockLiferayPortletActionRequest mockeactive =
					_getMockLiferayPortletActionRequest(user, password, "true");


				MockLiferayPortletActionResponse mockrespone =
					new MockLiferayPortletActionResponse();

				ReflectionTestUtil.invoke(
					_mvcActionCommand, "doProcessAction",
					new Class<?>[] {ActionRequest.class, ActionResponse.class},
					mockeactive,
					mockrespone);

				User user1 = _userLocalService.getUser(user.getUserId());

				Assert.assertEquals(
					user.isPasswordReset(), user1.isPasswordReset());
				Assert.assertTrue(user1.isPasswordReset());
			}
			catch (Exception exception) {
				_log.error("Pushing Service Context ", exception);
			}
		}
	}


	@Test
	public void testResetPasswordValueDoesNotChangeWhenItIsTruePrimeraIteración()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				 configurationTemporarySwapper =
				 new CompanyConfigurationTemporarySwapper(
					 _company.getCompanyId(),
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

				user = _userLocalService.getUser(user.getUserId());

				Bundle bundle = FrameworkUtil.getBundle(
					LoginMVCActionCommandTest.class);

				BundleContext bundleContext = bundle.getBundleContext();


				bundleContext.registerService(
					HeadlessMFAChecker.class, new HeadlessMFATestCheckerFalse(),
					MapUtil.singletonDictionary(
						"companyId", _company.getCompanyId()));


				MockLiferayPortletActionRequest mockeactive =
					_getMockLiferayPortletActionRequest(user, password, "true");


				MockLiferayPortletActionResponse mockrespone =
					new MockLiferayPortletActionResponse();


				_mvcActionCommand.processAction(
					mockeactive, mockrespone);

	/*			ReflectionTestUtil.invoke(
					_mvcActionCommand, "doProcessAction",
					new Class<?>[] {ActionRequest.class, ActionResponse.class},
					mockeactive,
					mockrespone);


	 */
				User user1 = _userLocalService.getUser(user.getUserId());

				Assert.assertEquals(
					user.isPasswordReset(), user1.isPasswordReset());
				Assert.assertTrue(user1.isPasswordReset());






				bundleContext.registerService(
					HeadlessMFAChecker.class, new HeadlessMFATestCheckerTrue(),
					MapUtil.singletonDictionary(
						"companyId", _company.getCompanyId()));



				String redirect = (String) mockeactive.getAttribute("REDIRECT");
				redirect = java.net.URLDecoder.decode(redirect, StandardCharsets.UTF_8.name());
				int start = StringUtil.lastIndexOfAny(redirect, new String[]{"state="});
				String state = redirect.substring(start+6, redirect.length());

				//sacar datos sesión
				HttpServletRequest httpServletRequest =
					_portal.getOriginalServletRequest(
						_portal.getHttpServletRequest(mockeactive));

				httpServletRequest = _portal.getOriginalServletRequest(
					httpServletRequest);

				javax.servlet.http.HttpSession httpSession = httpServletRequest.getSession();

				//Key mfaWebKey = _encryptor.deserializeKey(
				//	(String)httpSession.getAttribute(MFAWebKeys.MFA_WEB_KEY));

				String key = (String)httpSession.getAttribute("MFA_WEB_KEY");
				String digest = (String)httpSession.getAttribute("MFA_WEB_DIGEST");


				ActionRequest actionRequest;
				actionRequest = new ActionRequestImpl();
		//		MockActionRequest mockLiferayPortletActionRequest = new MockActionRequest();

				MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
					new MockLiferayPortletActionRequest();

				mockLiferayPortletActionRequest.setAttribute(
					WebKeys.THEME_DISPLAY, _getThemeDisplay());

				mockLiferayPortletActionRequest.setParameter(
					"state", state);
				mockLiferayPortletActionRequest.setAttribute(
					JavaConstants.JAVAX_PORTLET_CONFIG, _getLiferayPortletConfig());
				mockLiferayPortletActionRequest.setAttribute(
					JavaConstants.JAVAX_PORTLET_RESPONSE,
					new MockLiferayPortletActionResponse());

		//		_addCookieSupportCookie(
		//			(MockHttpServletRequest)
		//				mockLiferayPortletActionRequest.getHttpServletRequest());
///////////////

				HttpServletRequest httpServletRequestr =
					_portal.getOriginalServletRequest(
						_portal.getHttpServletRequest(mockLiferayPortletActionRequest));

				httpServletRequestr = _portal.getOriginalServletRequest(
					httpServletRequestr);

				javax.servlet.http.HttpSession httpSessionr = httpServletRequestr.getSession();



				httpSessionr.setAttribute("MFA_WEB_KEY", key);
				httpSessionr.setAttribute("MFA_WEB_DIGEST", digest);


				mockLiferayPortletActionRequest.setAttribute("com.liferay.portal.kernel.servlet.PortletServletRequest", httpServletRequestr);
				//////////////////




			//	_setUpUploadPortletRequest(mockLiferayPortletActionRequest, mockrespone);


			//	_mvcActionCommand.processAction(
			//		mockLiferayPortletActionRequest, mockrespone);

				ReflectionTestUtil.invoke(
					_mvcActionCommand, "doProcessAction",
					new Class<?>[] {ActionRequest.class, ActionResponse.class},
					mockLiferayPortletActionRequest,
					new MockLiferayPortletActionResponse());




				user1 = _userLocalService.getUser(user.getUserId());

				Assert.assertEquals(
					user.isPasswordReset(), user1.isPasswordReset());
				Assert.assertTrue(user1.isPasswordReset());

			}
			catch (Exception exception) {
				_log.error("Pushing Service Context ", exception);
			}
		}
	}


	private void _setUpUploadPortletRequest(
		MockLiferayPortletActionRequest mockLiferayPortletActionRequest,
		MockLiferayPortletActionResponse mockLiferayPortletActionResponse) {

		ReflectionTestUtil.setFieldValue(
			_mvcActionCommand, "_loginMVCActionCommand",
			ProxyUtil.newProxyInstance(
				LoginMVCActionCommandTest.class.getClassLoader(),
				new Class<?>[] {Portal.class},
				(proxy, method, args) -> {
					if (Objects.equals(
						method.getName(), "processAction")) {

						SessionErrors.isEmpty(mockLiferayPortletActionRequest);

						return true;
					}

					return method.invoke(_loginMVCActionCommand, args);
				}));
	}

	private void _addCookieSupportCookie(
		MockHttpServletRequest mockHttpServletRequest) {

		mockHttpServletRequest.setCookies(
			new Cookie(CookiesConstants.NAME_COOKIE_SUPPORT, "true"));
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			User user, String password, String resetPassword)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay());

		mockLiferayPortletActionRequest.setParameter(
			"login", user.getEmailAddress());
		mockLiferayPortletActionRequest.setParameter("password", password);
		mockLiferayPortletActionRequest.setParameter(
			"RESET_PASSWORD", resetPassword);


		mockLiferayPortletActionRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG, _getLiferayPortletConfig());
		mockLiferayPortletActionRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE,
			new MockLiferayPortletActionResponse());


		_addCookieSupportCookie(
			(MockHttpServletRequest)
				mockLiferayPortletActionRequest.getHttpServletRequest());


		///

		HttpServletRequest httpServletRequestr =
			_portal.getOriginalServletRequest(
				_portal.getHttpServletRequest(mockLiferayPortletActionRequest));

		httpServletRequestr = _portal.getOriginalServletRequest(
			httpServletRequestr);

		javax.servlet.http.HttpSession httpSessionr = httpServletRequestr.getSession();



		httpSessionr.setAttribute("MFA_WEB_KEY", "key");
		//



		return mockLiferayPortletActionRequest;
	}

	private LiferayPortletConfig _getLiferayPortletConfig() {
		Portlet portlet = _portletLocalService.getPortletById(
			"com_liferay_login_web_portlet_LoginPortlet");

		return (LiferayPortletConfig) PortletConfigFactoryUtil.create(
			portlet, null);
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(_company);

		LayoutSet layoutSet = _group.getPublicLayoutSet();

		themeDisplay.setLayout(LayoutTestUtil.addTypeContentLayout(_group));
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


	@Reference(
		target = "(component.name=com.liferay.login.web.internal.portlet.action.LoginMVCActionCommand)"
	)
	private MVCActionCommand _loginMVCActionCommand;


	@Inject
	private PortletLocalService _portletLocalService;

	private static class HeadlessMFATestCheckerTrue implements HeadlessMFAChecker {

		@Override
		public boolean verifyHeadlessRequest(
			HttpServletRequest httpServletRequest, long userId) {

			return true;
		}

	}

	private static class HeadlessMFATestCheckerFalse implements HeadlessMFAChecker {

		@Override
		public boolean verifyHeadlessRequest(
			HttpServletRequest httpServletRequest, long userId) {

			return false;
		}

	}

}