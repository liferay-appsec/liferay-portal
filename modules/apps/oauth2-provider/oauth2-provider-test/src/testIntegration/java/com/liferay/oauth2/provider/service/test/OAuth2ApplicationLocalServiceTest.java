/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.internal.test.TestSecretProvider;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class OAuth2ApplicationLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		ConfigurationTestUtil.saveConfiguration(
			_KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID,
			HashMapDictionaryBuilder.<String, Object>put(
				"companySecretProviderId", TestSecretProvider.PROVIDER_ID
			).put(
				"systemSecretProviderId", TestSecretProvider.PROVIDER_ID
			).build());

		_user = UserTestUtil.addUser();
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationTestUtil.deleteConfiguration(
			_KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID);
	}

	@Test
	public void testResolveClientSecret() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			String clientSecret = RandomTestUtil.randomString();

			_oAuth2Application = _addOAuth2Application(clientSecret);

			Assert.assertEquals(
				clientSecret,
				_oAuth2ApplicationLocalService.resolveClientSecret(
					_oAuth2Application));

			String storedClientSecret = _oAuth2Application.getClientSecret();

			Assert.assertTrue(
				storedClientSecret,
				storedClientSecret.startsWith("${secretRef:"));
		}
	}

	@Test
	public void testResolveClientSecretWhenDisabled() throws Exception {
		String clientSecret = RandomTestUtil.randomString();

		_oAuth2Application = _addOAuth2Application(clientSecret);

		Assert.assertEquals(clientSecret, _oAuth2Application.getClientSecret());
		Assert.assertEquals(
			clientSecret,
			_oAuth2ApplicationLocalService.resolveClientSecret(
				_oAuth2Application));
	}

	private OAuth2Application _addOAuth2Application(String clientSecret)
		throws Exception {

		return _oAuth2ApplicationLocalService.addOAuth2Application(
			TestPropsValues.getCompanyId(), _user.getUserId(),
			_user.getFullName(),
			ListUtil.fromArray(GrantType.CLIENT_CREDENTIALS),
			"client_secret_post", _user.getUserId(), null, 0, clientSecret,
			null, null, null, 0, null, RandomTestUtil.randomString(), null,
			null, false, null, false, new ServiceContext());
	}

	private static final String _KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID =
		"com.liferay.portal.security.key.internal.profile.configuration." +
			"KeyManagerCustomProfileConfiguration";

	@DeleteAfterTestRun
	private OAuth2Application _oAuth2Application;

	@Inject
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@DeleteAfterTestRun
	private User _user;

}