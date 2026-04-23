/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.configuration.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.cookies.configuration.CookiesPreferenceHandlingConfiguration;
import com.liferay.cookies.configuration.GlobalPrivacyControlProvider;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
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

/**
 * @author Christian Moura
 */
@FeatureFlag("LPD-75064")
@RunWith(Arquillian.class)
public class GlobalPrivacyControlProviderImplTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_companyId = TestPropsValues.getCompanyId();
	}

	@After
	public void tearDown() throws Exception {
		if (_companyConfigurationPid != null) {
			ConfigurationTestUtil.deleteFactoryConfiguration(
				_companyConfigurationPid, _SCOPED_FACTORY_PID);

			_companyConfigurationPid = null;
		}

		if (_groupConfigurationPid != null) {
			ConfigurationTestUtil.deleteFactoryConfiguration(
				_groupConfigurationPid, _SCOPED_FACTORY_PID);

			_groupConfigurationPid = null;
		}

		if (_systemConfigurationSaved) {
			ConfigurationTestUtil.deleteConfiguration(_SYSTEM_PID);

			_systemConfigurationSaved = false;
		}

		if (_group != null) {
			GroupTestUtil.deleteGroup(_group);

			_group = null;
			_layout = null;
		}
	}

	@Test
	public void testConsentManagerDisabled() throws Exception {
		_saveCompanyConfiguration(false, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	@Test
	public void testDefaultsToInactive() throws Exception {
		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	@FeatureFlag(enable = false, value = "LPD-75064")
	@Test
	public void testFeatureFlagDisabled() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	@Test
	public void testGlobalPrivacyControlDisabled() throws Exception {
		_saveCompanyConfiguration(true, false);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	@Test
	public void testHeaderAbsent() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest(null)));
	}

	@Test
	public void testHeaderCommaSeparatedIsIgnored() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(
				_createRequest("0, 1")));
		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(
				_createRequest("1, 0")));
	}

	@Test
	public void testHeaderUnrecognizedValue() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(
				_createRequest("true")));
	}

	@Test
	public void testHeaderWithWhitespace() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(
				_createRequest(" 1 ")));
	}

	@Test
	public void testHeaderZero() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("0")));
	}

	@Test
	public void testMultipleHeadersAtLeastOneActive() throws Exception {
		_saveCompanyConfiguration(true, true);

		MockHttpServletRequest mockHttpServletRequest = _createRequest("0");

		mockHttpServletRequest.addHeader("Sec-GPC", "1");

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(
				mockHttpServletRequest));
	}

	@Test
	public void testSignalActive() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	@Test
	public void testSiteLevelEnablesOverCompanyDisabled() throws Exception {
		_saveCompanyConfiguration(true, false);

		MockHttpServletRequest mockHttpServletRequest = _createLayoutRequest(
			"1");

		_saveGroupConfiguration(_group.getGroupId(), true, true);

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(
				mockHttpServletRequest));
	}

	@Test
	public void testSiteLevelOverridesCompany() throws Exception {
		_saveCompanyConfiguration(true, true);

		MockHttpServletRequest mockHttpServletRequest = _createLayoutRequest(
			"1");

		_saveGroupConfiguration(_group.getGroupId(), true, false);

		Assert.assertFalse(
			_globalPrivacyControlProvider.isSignalActive(
				mockHttpServletRequest));
	}

	@Test
	public void testSiteScopeFallsBackToCompany() throws Exception {
		_saveCompanyConfiguration(true, true);

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(
				_createLayoutRequest("1")));
	}

	@Test
	public void testSystemScopeFallback() throws Exception {
		_saveSystemConfiguration(true, true);

		Assert.assertTrue(
			_globalPrivacyControlProvider.isSignalActive(_createRequest("1")));
	}

	private MockHttpServletRequest _createLayoutRequest(String secGpcHeader)
		throws Exception {

		if (_group == null) {
			_group = GroupTestUtil.addGroup();

			_layout = LayoutTestUtil.addTypePortletLayout(_group);
		}

		MockHttpServletRequest mockHttpServletRequest = _createRequest(
			secGpcHeader);

		mockHttpServletRequest.setAttribute(WebKeys.LAYOUT, _layout);

		return mockHttpServletRequest;
	}

	private MockHttpServletRequest _createRequest(String secGpcHeader) {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(WebKeys.COMPANY_ID, _companyId);

		if (secGpcHeader != null) {
			mockHttpServletRequest.addHeader("Sec-GPC", secGpcHeader);
		}

		return mockHttpServletRequest;
	}

	private void _saveCompanyConfiguration(
			boolean cookiesPreferenceHandlingEnabled,
			boolean globalPrivacyControlEnabled)
		throws Exception {

		_companyConfigurationPid =
			ConfigurationTestUtil.createFactoryConfiguration(
				_SCOPED_FACTORY_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", _companyId
				).put(
					"enabled", cookiesPreferenceHandlingEnabled
				).put(
					"globalPrivacyControlEnabled", globalPrivacyControlEnabled
				).build());
	}

	private void _saveGroupConfiguration(
			long groupId, boolean cookiesPreferenceHandlingEnabled,
			boolean globalPrivacyControlEnabled)
		throws Exception {

		_groupConfigurationPid =
			ConfigurationTestUtil.createFactoryConfiguration(
				_SCOPED_FACTORY_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", _companyId
				).put(
					"enabled", cookiesPreferenceHandlingEnabled
				).put(
					"globalPrivacyControlEnabled", globalPrivacyControlEnabled
				).put(
					"groupId", groupId
				).build());
	}

	private void _saveSystemConfiguration(
			boolean cookiesPreferenceHandlingEnabled,
			boolean globalPrivacyControlEnabled)
		throws Exception {

		ConfigurationTestUtil.saveConfiguration(
			_SYSTEM_PID,
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", cookiesPreferenceHandlingEnabled
			).put(
				"globalPrivacyControlEnabled", globalPrivacyControlEnabled
			).build());

		_systemConfigurationSaved = true;
	}

	private static final String _SCOPED_FACTORY_PID =
		CookiesPreferenceHandlingConfiguration.class.getName() + ".scoped";

	private static final String _SYSTEM_PID =
		CookiesPreferenceHandlingConfiguration.class.getName();

	private String _companyConfigurationPid;
	private long _companyId;

	@Inject
	private GlobalPrivacyControlProvider _globalPrivacyControlProvider;

	private Group _group;
	private String _groupConfigurationPid;
	private Layout _layout;
	private boolean _systemConfigurationSaved;

}