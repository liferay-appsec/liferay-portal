/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth.client.persistence.configuration.OAuthClientCompanyConfiguration;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.oauth.client.test.util.OpenIdConnectProviderHttpServer;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectAuthenticationHandler;
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

/**
 * @author Brian Wulbern
 */
@RunWith(Arquillian.class)
public class OpenIdConnectAuthenticationHandlerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_companyConfigurationTemporarySwapper =
			new CompanyConfigurationTemporarySwapper(
				TestPropsValues.getCompanyId(),
				OAuthClientCompanyConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"authServerLocalNetworkAccessEnabled", true
				).build());

		_openIdConnectProviderHttpServer =
			new OpenIdConnectProviderHttpServer();

		_discoveryEndpoint = _openIdConnectProviderHttpServer.getURL();

		_pid = ConfigurationTestUtil.createFactoryConfiguration(
			"com.liferay.portal.security.sso.openid.connect.internal." +
				"configuration.OpenIdConnectProviderConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", TestPropsValues.getCompanyId()
			).put(
				"discoveryEndpoint", _discoveryEndpoint
			).put(
				"openIdConnectClientId", _CLIENT_ID
			).put(
				"scopes", "openid"
			).build());
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationTestUtil.deleteConfiguration(_pid);

		if (_companyConfigurationTemporarySwapper != null) {
			_companyConfigurationTemporarySwapper.close();
		}

		if (_openIdConnectProviderHttpServer != null) {
			_openIdConnectProviderHttpServer.close();
		}
	}

	@Test
	@TestInfo("LPD-117364")
	public void testProcessAuthenticationResponseWithOtherCompanyOAuthClientEntry()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.COMPANY_ID, TestPropsValues.getCompanyId());

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();
		OAuthClientEntry oAuthClientEntry =
			_oAuthClientEntryLocalService.getOAuthClientEntry(
				TestPropsValues.getCompanyId(), _discoveryEndpoint, _CLIENT_ID);

		_openIdConnectAuthenticationHandler.requestAuthentication(
			oAuthClientEntry.getOAuthClientEntryId(), mockHttpServletRequest,
			mockHttpServletResponse);

		_company = CompanyTestUtil.addCompany();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			MockHttpServletRequest callbackMockHttpServletRequest =
				new MockHttpServletRequest();

			callbackMockHttpServletRequest.setAttribute(
				WebKeys.COMPANY_ID, _company.getCompanyId());
			callbackMockHttpServletRequest.setQueryString(
				StringBundler.concat(
					"code=", RandomTestUtil.randomString(), "&state=",
					HttpComponentsUtil.getParameter(
						mockHttpServletResponse.getRedirectedUrl(), "state",
						false)));
			callbackMockHttpServletRequest.setSession(
				mockHttpServletRequest.getSession());

			_openIdConnectAuthenticationHandler.processAuthenticationResponse(
				callbackMockHttpServletRequest, new MockHttpServletResponse(),
				userId -> {
				});

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertSame(NoSuchModelException.class, exception.getClass());
		}
	}

	@Test
	@TestInfo("LPD-117364")
	public void testRequestAuthenticationWithOtherCompanyOAuthClientEntry()
		throws Exception {

		_company = CompanyTestUtil.addCompany();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			MockHttpServletRequest mockHttpServletRequest =
				new MockHttpServletRequest();

			mockHttpServletRequest.setAttribute(
				WebKeys.COMPANY_ID, _company.getCompanyId());

			OAuthClientEntry oAuthClientEntry =
				_oAuthClientEntryLocalService.getOAuthClientEntry(
					TestPropsValues.getCompanyId(), _discoveryEndpoint,
					_CLIENT_ID);

			_openIdConnectAuthenticationHandler.requestAuthentication(
				oAuthClientEntry.getOAuthClientEntryId(),
				mockHttpServletRequest, new MockHttpServletResponse());

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertSame(NoSuchModelException.class, exception.getClass());
		}
	}

	private static final String _CLIENT_ID = RandomTestUtil.randomString();

	@DeleteAfterTestRun
	private Company _company;

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;
	private String _discoveryEndpoint;

	@Inject
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Inject
	private OpenIdConnectAuthenticationHandler
		_openIdConnectAuthenticationHandler;

	private OpenIdConnectProviderHttpServer _openIdConnectProviderHttpServer;
	private String _pid;

}