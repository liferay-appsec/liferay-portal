/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.login.utility.page.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.utility.page.kernel.LayoutUtilityPageEntryViewRendererRegistryUtil;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.model.LayoutUtilityPageEntry;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Nikoletta Buza
 */
@FeatureFlags("LPD-6378")
@RunWith(Arquillian.class)
public class LoginUtilityPageTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);
	}

	@Test
	public void testAddCreateAccountUtilityPageEntry() throws PortalException {
		LayoutUtilityPageEntry layoutUtilityPageEntry =
			_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
				null, _serviceContext.getUserId(), _group.getGroupId(), 0, 0,
				true, RandomTestUtil.randomString(),
				LayoutUtilityPageEntryConstants.TYPE_CREATE_ACCOUNT, 0,
				_serviceContext);

		Assert.assertNotNull(layoutUtilityPageEntry);
	}

	@Test
	public void testAddForgotPasswordUtilityPageEntry() throws PortalException {
		LayoutUtilityPageEntry layoutUtilityPageEntry =
			_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
				null, _serviceContext.getUserId(), _group.getGroupId(), 0, 0,
				true, RandomTestUtil.randomString(),
				LayoutUtilityPageEntryConstants.TYPE_FORGOT_PASSWORD, 0,
				_serviceContext);

		Assert.assertNotNull(layoutUtilityPageEntry);
	}

	@Test
	public void testAddLoginUtilityPageEntry() throws PortalException {
		LayoutUtilityPageEntry layoutUtilityPageEntry =
			_layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
				null, _serviceContext.getUserId(), _group.getGroupId(), 0, 0,
				true, RandomTestUtil.randomString(),
				LayoutUtilityPageEntryConstants.TYPE_LOGIN, 0, _serviceContext);

		Assert.assertNotNull(layoutUtilityPageEntry);
	}

	@Test
	public void testCreateAccountUtilityPageTypeHasBeenRegistered() {
		Assert.assertNotNull(
			LayoutUtilityPageEntryViewRendererRegistryUtil.
				getLayoutUtilityPageEntryViewRenderer(
					LayoutUtilityPageEntryConstants.TYPE_CREATE_ACCOUNT));
	}

	@Test
	public void testDefaultUtilityPagesAfterSiteInitialization()
		throws PortalException {

		try {
			URL url = new URL("http://localhost:8080");

			HttpURLConnection connection =
				(HttpURLConnection)url.openConnection();

			connection.setRequestMethod("GET");
		}
		catch (IOException ioException) {
			Assert.fail();
		}

		_checkDefaultUtilityPageEntries(
			LayoutUtilityPageEntryConstants.TYPE_LOGIN, "Sign In",
			"com_liferay_login_web_portlet_LoginPortlet");
		_checkDefaultUtilityPageEntries(
			LayoutUtilityPageEntryConstants.TYPE_CREATE_ACCOUNT,
			"Create Account",
			"com_liferay_login_web_portlet_CreateAccountPortlet");
		_checkDefaultUtilityPageEntries(
			LayoutUtilityPageEntryConstants.TYPE_FORGOT_PASSWORD,
			"Forgot Password",
			"com_liferay_login_web_portlet_ForgotPasswordPortlet");
	}

	@Test
	public void testForgotPasswordUtilityPageTypeHasBeenRegistered() {
		Assert.assertNotNull(
			LayoutUtilityPageEntryViewRendererRegistryUtil.
				getLayoutUtilityPageEntryViewRenderer(
					LayoutUtilityPageEntryConstants.TYPE_FORGOT_PASSWORD));
	}

	@Test
	public void testLoginUtilityPageTypeHasBeenRegistered() {
		Assert.assertNotNull(
			LayoutUtilityPageEntryViewRendererRegistryUtil.
				getLayoutUtilityPageEntryViewRenderer(
					LayoutUtilityPageEntryConstants.TYPE_LOGIN));
	}

	private void _checkDefaultUtilityPageEntries(
			String layoutUtilityPageEntryType,
			String expectedUtilityPageEntryName, String expectedPortletId)
		throws PortalException {

		Group guestGroup = _groupLocalService.getGroup(
			TestPropsValues.getCompanyId(), GroupConstants.GUEST);

		LayoutUtilityPageEntry defaultLoginUtilityPageEntry =
			_layoutUtilityPageEntryLocalService.
				fetchDefaultLayoutUtilityPageEntry(
					guestGroup.getGroupId(), layoutUtilityPageEntryType);

		Assert.assertNotNull(defaultLoginUtilityPageEntry);

		Assert.assertEquals(
			expectedUtilityPageEntryName,
			defaultLoginUtilityPageEntry.getName());

		long defaultLoginUtilityPageEntryPlid =
			defaultLoginUtilityPageEntry.getPlid();

		List<PortletPreferences> portletPreferencesByPlid =
			_portletPreferencesLocalService.getPortletPreferencesByPlid(
				defaultLoginUtilityPageEntryPlid);

		Assert.assertEquals(
			portletPreferencesByPlid.toString(), 1,
			portletPreferencesByPlid.size());

		PortletPreferences portletPreferences = portletPreferencesByPlid.get(0);

		Assert.assertEquals(
			expectedPortletId, portletPreferences.getPortletId());
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private LayoutUtilityPageEntryLocalService
		_layoutUtilityPageEntryLocalService;

	@Inject
	private PortletPreferencesLocalService _portletPreferencesLocalService;

	private ServiceContext _serviceContext;

}