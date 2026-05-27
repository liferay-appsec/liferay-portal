/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.consent.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.consent.management.platform.integration.configuration.ConsentManagementPlatformConfiguration;
import com.liferay.cookies.consent.CookiesConsentResolver;
import com.liferay.cookies.consent.CookiesConsentResolverProvider;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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

	@Test
	public void testGetCookiesConsentResolver() throws Exception {
		Assert.assertSame(
			_defaultCookiesConsentResolver,
			_cookiesConsentResolverProvider.getCookiesConsentResolver(null));

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _swap(false)) {

			Assert.assertSame(
				_defaultCookiesConsentResolver,
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					_createMockHttpServletRequest()));
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper = _swap(true)) {

			Assert.assertSame(
				_defaultCookiesConsentResolver,
				_cookiesConsentResolverProvider.getCookiesConsentResolver(
					_createMockHttpServletRequest()));
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

}