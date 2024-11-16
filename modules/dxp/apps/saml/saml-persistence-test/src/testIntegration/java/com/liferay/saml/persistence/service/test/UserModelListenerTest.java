/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.persistence.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.saml.constants.SamlProviderConfigurationKeys;
import com.liferay.saml.persistence.model.SamlIdpSpConnection;
import com.liferay.saml.persistence.model.SamlPeerBinding;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlIdpSpConnectionLocalService;
import com.liferay.saml.persistence.service.SamlPeerBindingLocalService;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelperUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opensaml.saml.saml2.core.NameIDType;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class UserModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_samlProviderConfigurationHelper =
			SamlProviderConfigurationHelperUtil.
				getSamlProviderConfigurationHelper();

		_enabled = _samlProviderConfigurationHelper.isEnabled();

		_samlProviderConfigurationHelper.updateProperties(
			UnicodePropertiesBuilder.create(
				true
			).put(
				"saml.enabled", "true"
			).build());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_samlProviderConfigurationHelper.updateProperties(
			UnicodePropertiesBuilder.create(
				true
			).put(
				"saml.enabled", String.valueOf(_enabled)
			).build());
	}

	@Before
	public void setUp() throws Exception {
		_user = UserTestUtil.addUser();
	}

	@Test
	public void testUpdateIdpUserEmailPeerBindingEmail() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.EMAIL);

			_user.setEmailAddress(
				RandomTestUtil.randomString() + RandomTestUtil.nextLong() +
					"@liferay.com");

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateIdpUserEmailPeerBindingScreenName() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.UNSPECIFIED);

			_user.setEmailAddress(
				RandomTestUtil.randomString() + RandomTestUtil.nextLong() +
					"@liferay.com");

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateIdpUserScreenNamePeerBindingEmail() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.EMAIL);

			_user.setScreenName(RandomTestUtil.randomString());

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateIdpUserScreenNamePeerBindingScreenName()
		throws Exception {

		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.UNSPECIFIED);

			_user.setScreenName(RandomTestUtil.randomString());

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserEmailPeerBindingEmail() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.EMAIL);

			_user.setEmailAddress(
				RandomTestUtil.randomString() + RandomTestUtil.nextLong() +
					"@liferay.com");

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserEmailPeerBindingScreenName() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.UNSPECIFIED);

			_user.setEmailAddress(
				RandomTestUtil.randomString() + RandomTestUtil.nextLong() +
					"@liferay.com");

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserScreenNamePeerBindingEmail() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.EMAIL);

			_user.setScreenName(RandomTestUtil.randomString());

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserScreenNamePeerBindingScreenName()
		throws Exception {

		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection(NameIDType.UNSPECIFIED);

			_user.setScreenName(RandomTestUtil.randomString());

			_user = _userLocalService.updateUser(_user);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	private SamlPeerBinding _createSamlPeerBindingAndConnection(
			String samlNameIdFormat)
		throws Exception {

		String nameIdAttribute;
		String samlEntityId = RandomTestUtil.randomString();
		String samlNameIdValue;

		if (samlNameIdFormat.equals(NameIDType.EMAIL)) {
			nameIdAttribute = "emailAddress";
			samlNameIdValue = _user.getEmailAddress();
		}
		else {
			nameIdAttribute = "screenName";
			samlNameIdValue = _user.getScreenName();
		}

		if (_samlProviderConfigurationHelper.isRoleIdp()) {
			SamlIdpSpConnection samlIdpSpConnection =
				_samlIdpSpConnectionLocalService.createSamlIdpSpConnection(
					_counterLocalService.increment());

			samlIdpSpConnection.setNameIdAttribute(nameIdAttribute);
			samlIdpSpConnection.setNameIdFormat(samlNameIdFormat);
			samlIdpSpConnection.setSamlSpEntityId(samlEntityId);

			_samlIdpSpConnectionLocalService.updateSamlIdpSpConnection(
				samlIdpSpConnection);
		}
		else {
			SamlSpIdpConnection samlSpIdpConnection =
				_samlSpIdpConnectionLocalService.createSamlSpIdpConnection(
					_counterLocalService.increment());

			samlSpIdpConnection.setNameIdFormat(samlNameIdFormat);
			samlSpIdpConnection.setSamlIdpEntityId(samlEntityId);
			samlSpIdpConnection.setUserIdentifierExpression("dynamic");

			_samlSpIdpConnectionLocalService.updateSamlSpIdpConnection(
				samlSpIdpConnection);
		}

		return _samlPeerBindingLocalService.addSamlPeerBinding(
			_user.getUserId(), samlNameIdFormat, StringPool.BLANK,
			StringPool.BLANK, StringPool.BLANK, samlNameIdValue, samlEntityId);
	}

	private SafeCloseable _updateSamlRoleWithSafeCloseable(String role)
		throws Exception {

		String originalRole =
			_samlProviderConfigurationHelper.isRoleIdp() ? "idp" : "sp";

		_samlProviderConfigurationHelper.updateProperties(
			UnicodePropertiesBuilder.create(
				true
			).put(
				"saml.role", role
			).build());

		return () -> {
			try {
				_samlProviderConfigurationHelper.updateProperties(
					UnicodePropertiesBuilder.create(
						true
					).put(
						"saml.role", originalRole
					).build());
			}
			catch (Exception exception) {
			}
		};
	}

	@Inject
	private static CounterLocalService _counterLocalService;

	private static boolean _enabled;

	@Inject
	private static SamlIdpSpConnectionLocalService
		_samlIdpSpConnectionLocalService;

	private static SamlProviderConfigurationHelper
		_samlProviderConfigurationHelper;

	@Inject
	private static SamlSpIdpConnectionLocalService
		_samlSpIdpConnectionLocalService;

	private static User _user;

	@Inject
	private SamlPeerBindingLocalService _samlPeerBindingLocalService;

	@Inject
	private UserLocalService _userLocalService;

}