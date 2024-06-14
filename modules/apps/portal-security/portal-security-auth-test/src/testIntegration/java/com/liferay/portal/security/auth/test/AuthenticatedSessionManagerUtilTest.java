/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.auth.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cookies.CookiesManagerUtil;
import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.RememberMeToken;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.RememberMeTokenLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.auth.session.AuthenticatedSessionManagerUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.Assert;

/**
 * @author Manuele Castro
 */
@RunWith(Arquillian.class)
public class AuthenticatedSessionManagerUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_user = UserTestUtil.addUser(_company);

		_user = _userLocalService.updatePassword(
			_user.getUserId(), "test", "test", false, false);
	}

	@Test
	public void testRememberMeLogin() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		mockHttpServletRequest.setAttribute(
			WebKeys.COMPANY_ID, _company.getCompanyId());

		mockHttpServletRequest.setCookies(
			_buildCookie(CookiesConstants.NAME_COOKIE_SUPPORT, "true", -1));

		AuthenticatedSessionManagerUtil.login(
			mockHttpServletRequest, mockHttpServletResponse, _user.getLogin(),
			"test", true, null);

		Assert.notNull(
			mockHttpServletResponse.getCookie(
				CookiesConstants.NAME_REMEMBER_ME_TOKEN_ID));
		Assert.notNull(
			mockHttpServletResponse.getCookie(
				CookiesConstants.NAME_REMEMBER_ME_TOKEN_TOKEN));
	}

	@Test
	public void testRemoveExpiredRememberMeTokenOnLogin() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		// set cookies and attributes
		// add expired remember me token to user

		AuthenticatedSessionManagerUtil.login(
			mockHttpServletRequest, mockHttpServletResponse, _user.getLogin(),
			"test", true, null);

		// check if cookies were added
		// check if new remember me token was added
		// check if expired remember me token was removed

	}

	@Test
	public void testRemoveExpiredRememberMeTokenOnLogout() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		// add expired remember me token to user
		// set cookies and attributes

		CookiesManagerUtil.addCookie(
			CookiesConstants.CONSENT_TYPE_FUNCTIONAL,
			_buildCookie(
				CookiesConstants.NAME_REMEMBER_ME, Boolean.TRUE.toString(), 1),
			mockHttpServletRequest, mockHttpServletResponse);

		Cookie cookie = _buildCookie(
			CookiesConstants.NAME_REMEMBER_ME_TOKEN_TOKEN, StringPool.BLANK, 1);

		RememberMeToken rememberMeToken =
			RememberMeTokenLocalServiceUtil.addRememberMeToken(
				_user.getCompanyId(), _user.getUserId(),
				new Date(System.currentTimeMillis()), cookie::setValue);

		CookiesManagerUtil.addCookie(
			CookiesConstants.CONSENT_TYPE_FUNCTIONAL, cookie,
			mockHttpServletRequest, mockHttpServletResponse);

		CookiesManagerUtil.addCookie(
			CookiesConstants.CONSENT_TYPE_FUNCTIONAL,
			_buildCookie(
				CookiesConstants.NAME_REMEMBER_ME_TOKEN_ID,
				String.valueOf(rememberMeToken.getRememberMeTokenId()), 1),
			mockHttpServletRequest, mockHttpServletResponse);

		AuthenticatedSessionManagerUtil.logout(
			mockHttpServletRequest, mockHttpServletResponse);

		// check if cookies were removed after logout
		// check if expired remember me token was removed after logout

		org.junit.Assert.assertNull(
			CookiesManagerUtil.getCookieValue(
				CookiesConstants.NAME_REMEMBER_ME_TOKEN_ID,
				mockHttpServletRequest, false));

		org.junit.Assert.assertNull(
			CookiesManagerUtil.getCookieValue(
				CookiesConstants.NAME_REMEMBER_ME_TOKEN_TOKEN,
				mockHttpServletRequest, false));

		org.junit.Assert.assertNull(
			RememberMeTokenLocalServiceUtil.fetchRememberMeToken(
				rememberMeToken.getRememberMeTokenId()));
	}

	@Test
	public void testRemoveRememberMeOnLogout() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		// set cookies and attributes
		// add remember me token to user

		AuthenticatedSessionManagerUtil.logout(
			mockHttpServletRequest, mockHttpServletResponse);

		// check if cookies were removed
		// check if remember me token was removed

	}

	private Cookie _buildCookie(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);

		cookie.setDomain("");

		cookie.setMaxAge(maxAge);
		cookie.setPath(StringPool.SLASH);

		return cookie;
	}

	@DeleteAfterTestRun
	private static Company _company;

	@DeleteAfterTestRun
	private static User _user;

	@Inject
	private static UserLocalService _userLocalService;

}