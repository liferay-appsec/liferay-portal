/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.configuration.web.internal.model.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.constants.ClientProfile;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.exception.NoSuchOAuth2ApplicationException;
import com.liferay.oauth2.provider.exception.OAuth2ApplicationRequiredException;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.util.OAuth2SecureRandomGenerator;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.scim.rest.util.ScimClientUtil;
import com.liferay.scim.rest.util.ScimThreadLocal;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class ScimOAuth2ApplicationModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Company company = _companyLocalService.getCompanyById(
			TestPropsValues.getCompanyId());

		_companyId = company.getCompanyId();

		User adminUser = UserTestUtil.getAdminUser(company.getCompanyId());

		_user = UserTestUtil.addCompanyAdminUser(company);

		Assert.assertNotEquals(adminUser, _user);

		_oAuth2ApplicationName = RandomTestUtil.randomString();

		_clientId = ScimClientUtil.generateScimClientId(_oAuth2ApplicationName);

		_pid = ConfigurationTestUtil.createFactoryConfiguration(
			"com.liferay.scim.rest.internal.configuration." +
				"ScimClientOAuth2ApplicationConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", _companyId
			).put(
				"matcherField", "email"
			).put(
				"oAuth2ApplicationName", _oAuth2ApplicationName
			).put(
				"userId", _user.getUserId()
			).build());
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationTestUtil.deleteConfiguration(_pid);
	}

	@Test
	public void testOnBeforeRemoveWithoutResetInProcess() throws Exception {
		try {
			OAuth2Application oAuth2ApplicationNotRelated =
				_oAuth2ApplicationLocalService.addOAuth2Application(
					_companyId, _user.getUserId(), _user.getScreenName(),
					ListUtil.fromArray(GrantType.CLIENT_CREDENTIALS),
					"client_secret_post", _user.getUserId(),
					RandomTestUtil.randomString(),
					ClientProfile.HEADLESS_SERVER.id(),
					OAuth2SecureRandomGenerator.generateClientSecret(), null,
					Collections.emptyList(), null, 0, null,
					RandomTestUtil.randomString(), null,
					Collections.emptyList(), false, false, null,
					new ServiceContext());

			_oAuth2ApplicationLocalService.deleteOAuth2Application(
				oAuth2ApplicationNotRelated.getOAuth2ApplicationId());

			Assert.assertThrows(
				NoSuchOAuth2ApplicationException.class,
				() -> _oAuth2ApplicationLocalService.getOAuth2Application(
					oAuth2ApplicationNotRelated.getOAuth2ApplicationId()));

			OAuth2Application scimOAuth2Application =
				_oAuth2ApplicationLocalService.getOAuth2Application(
					_companyId, _clientId);

			_oAuth2ApplicationLocalService.deleteOAuth2Application(
				scimOAuth2Application.getOAuth2ApplicationId());
		}
		catch (OAuth2ApplicationRequiredException
					oAuth2ApplicationRequiredException) {

			Assert.assertEquals(
				oAuth2ApplicationRequiredException.getMessage(),
				_language.format(
					LocaleUtil.getDefault(),
					"oauth2-application-x-required-by-x",
					new String[] {_oAuth2ApplicationName, "SCIM"}, false));
		}
	}

	@Test
	public void testOnBeforeRemoveWithResetInProcess() throws Exception {
		boolean resetInProcess = ScimThreadLocal.isResetInProcess();

		try {
			ScimThreadLocal.setResetInProcess(true);

			OAuth2Application scimOAuth2Application =
				_oAuth2ApplicationLocalService.getOAuth2Application(
					_companyId, _clientId);

			_oAuth2ApplicationLocalService.deleteOAuth2Application(
				scimOAuth2Application.getOAuth2ApplicationId());

			Assert.assertThrows(
				NoSuchOAuth2ApplicationException.class,
				() -> _oAuth2ApplicationLocalService.getOAuth2Application(
					scimOAuth2Application.getOAuth2ApplicationId()));
		}
		finally {
			ScimThreadLocal.setResetInProcess(resetInProcess);
		}
	}

	@DeleteAfterTestRun
	private static User _user;

	private String _clientId;
	private Long _companyId;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private Language _language;

	@Inject
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	private String _oAuth2ApplicationName;
	private String _pid;

}