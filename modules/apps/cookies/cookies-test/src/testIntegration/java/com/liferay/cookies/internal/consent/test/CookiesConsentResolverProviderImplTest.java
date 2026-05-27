/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.consent.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.consent.management.platform.integration.configuration.ConsentManagementPlatformConfiguration;
import com.liferay.cookies.consent.CookiesConsentResolver;
import com.liferay.cookies.consent.CookiesConsentResolverProvider;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.GroupConfigurationTemporarySwapper;
import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.Cookie;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Christian Moura
 */
@FeatureFlag("LPD-65299")
@RunWith(Arquillian.class)
public class CookiesConsentResolverProviderImplTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() throws Exception {
		if (_group != null) {
			GroupTestUtil.deleteGroup(_group);
		}
	}

	@Test
	public void testGetCookiesConsentResolver() throws Exception {
		Assert.assertSame(
			_defaultCookiesConsentResolver,
			_cookiesConsentResolverProvider.getCookiesConsentResolver(null));

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _swap(false)) {

			MockHttpServletRequest mockHttpServletRequest =
				_createMockHttpServletRequest();

			Assert.assertSame(
				_defaultCookiesConsentResolver,
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					mockHttpServletRequest));

			mockHttpServletRequest.setCookies(
				new Cookie(
					CookiesConstants.NAME_CONSENT_TYPE_FUNCTIONAL, "true"),
				new Cookie(
					CookiesConstants.NAME_LIFERAY_CONSENT_STATE,
					URLEncoder.encode(
						"{\"functional\":false}", StandardCharsets.UTF_8)));

			CookiesConsentResolver cookiesConsentResolver =
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					mockHttpServletRequest);

			Assert.assertTrue(
				cookiesConsentResolver.hasConsent(
					CookiesConstants.CONSENT_TYPE_FUNCTIONAL,
					mockHttpServletRequest));
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _swap(true)) {

			MockHttpServletRequest mockHttpServletRequest =
				_createMockHttpServletRequest();

			Assert.assertSame(
				_thirdPartyCookiesConsentResolver,
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					mockHttpServletRequest));
		}

		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypePortletLayout(_group);

		MockHttpServletRequest mockHttpServletRequest =
			_createMockHttpServletRequest();

		mockHttpServletRequest.setAttribute(WebKeys.LAYOUT, _layout);

		try (GroupConfigurationTemporarySwapper
				groupConfigurationTemporarySwapper =
					new GroupConfigurationTemporarySwapper(
						_group.getGroupId(),
						ConsentManagementPlatformConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build())) {

			Assert.assertSame(
				_thirdPartyCookiesConsentResolver,
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					mockHttpServletRequest));
		}
	}

	private MockHttpServletRequest _createMockHttpServletRequest()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.COMPANY_ID, TestPropsValues.getCompanyId());

		return mockHttpServletRequest;
	}

	private CompanyConfigurationTemporarySwapper _swap(boolean enabled)
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			TestPropsValues.getCompanyId(),
			ConsentManagementPlatformConfiguration.class.getName(),
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", enabled
			).build());
	}

	@Inject
	private CookiesConsentResolverProvider _cookiesConsentResolverProvider;

	@Inject(
		filter = "component.name=com.liferay.cookies.internal.consent.DefaultCookiesConsentResolver"
	)
	private CookiesConsentResolver _defaultCookiesConsentResolver;

	private Group _group;
	private Layout _layout;

	@Inject(
		filter = "component.name=com.liferay.consent.management.platform.integration.internal.consent.ThirdPartyCookiesConsentResolver"
	)
	private CookiesConsentResolver _thirdPartyCookiesConsentResolver;

}