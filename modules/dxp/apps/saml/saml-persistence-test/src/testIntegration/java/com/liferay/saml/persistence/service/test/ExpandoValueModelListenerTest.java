/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.persistence.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.model.ExpandoTableConstants;
import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
import com.liferay.expando.test.util.ExpandoTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
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
public class ExpandoValueModelListenerTest {

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

		_classNameLocalService.getClassNameId(User.class);

		_expandoTable = ExpandoTestUtil.addTable(
			_classNameLocalService.getClassNameId(User.class),
			ExpandoTableConstants.DEFAULT_TABLE_NAME);

		_expandoColumn = ExpandoTestUtil.addColumn(
			_expandoTable, RandomTestUtil.randomString(),
			ExpandoColumnConstants.STRING);
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

		_expandoValue = ExpandoTestUtil.addValue(
			_expandoTable, _expandoColumn, _user.getUserId(),
			RandomTestUtil.randomString());
	}

	@Test
	public void testUpdateIdpUserExpandoPeerBindingExpando() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection();

			_expandoValue.setData(RandomTestUtil.randomString());

			_expandoValue = _expandoValueLocalService.updateExpandoValue(
				_expandoValue);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateIdpUserOtherExpandoPeerBindingExpando()
		throws Exception {

		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			ExpandoColumn newExpandoColumn = ExpandoTestUtil.addColumn(
				_expandoTable, RandomTestUtil.randomString(),
				ExpandoColumnConstants.STRING);

			ExpandoValue newExpandoValue = ExpandoTestUtil.addValue(
				_expandoTable, newExpandoColumn, _user.getUserId(),
				RandomTestUtil.randomString());

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection();

			_expandoValueLocalService.updateExpandoValue(newExpandoValue);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserExpandoPeerBindingExpando() throws Exception {
		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection();

			_expandoValue.setData(RandomTestUtil.randomString());

			_expandoValue = _expandoValueLocalService.updateExpandoValue(
				_expandoValue);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertTrue(samlPeerBinding.isDeleted());
		}
	}

	@Test
	public void testUpdateSpUserOtherExpandoPeerBindingExpando()
		throws Exception {

		try (SafeCloseable safeCloseable = _updateSamlRoleWithSafeCloseable(
				SamlProviderConfigurationKeys.SAML_ROLE_SP)) {

			ExpandoColumn newExpandoColumn = ExpandoTestUtil.addColumn(
				_expandoTable, RandomTestUtil.randomString(),
				ExpandoColumnConstants.STRING);

			ExpandoValue newExpandoValue = ExpandoTestUtil.addValue(
				_expandoTable, newExpandoColumn, _user.getUserId(),
				RandomTestUtil.randomString());

			SamlPeerBinding samlPeerBinding =
				_createSamlPeerBindingAndConnection();

			_expandoValueLocalService.updateExpandoValue(newExpandoValue);

			samlPeerBinding = _samlPeerBindingLocalService.getSamlPeerBinding(
				samlPeerBinding.getSamlPeerBindingId());

			Assert.assertFalse(samlPeerBinding.isDeleted());
		}
	}

	private SamlPeerBinding _createSamlPeerBindingAndConnection()
		throws Exception {

		String samlEntityId = RandomTestUtil.randomString();

		if (_samlProviderConfigurationHelper.isRoleIdp()) {
			SamlIdpSpConnection samlIdpSpConnection =
				_samlIdpSpConnectionLocalService.createSamlIdpSpConnection(
					_counterLocalService.increment());

			samlIdpSpConnection.setNameIdAttribute(
				"expando:" + _expandoColumn.getName());
			samlIdpSpConnection.setNameIdFormat(NameIDType.UNSPECIFIED);
			samlIdpSpConnection.setSamlSpEntityId(samlEntityId);

			_samlIdpSpConnectionLocalService.updateSamlIdpSpConnection(
				samlIdpSpConnection);
		}
		else {
			SamlSpIdpConnection samlSpIdpConnection =
				_samlSpIdpConnectionLocalService.createSamlSpIdpConnection(
					_counterLocalService.increment());

			samlSpIdpConnection.setNameIdFormat(NameIDType.UNSPECIFIED);
			samlSpIdpConnection.setSamlIdpEntityId(samlEntityId);
			samlSpIdpConnection.setUserIdentifierExpression(
				"attribute:expando:" + _expandoColumn.getName());

			_samlSpIdpConnectionLocalService.updateSamlSpIdpConnection(
				samlSpIdpConnection);
		}

		return _samlPeerBindingLocalService.addSamlPeerBinding(
			_user.getUserId(), NameIDType.UNSPECIFIED, StringPool.BLANK,
			StringPool.BLANK, StringPool.BLANK, _expandoValue.getData(),
			samlEntityId);
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
	private static ClassNameLocalService _classNameLocalService;

	@Inject
	private static CounterLocalService _counterLocalService;

	private static boolean _enabled;
	private static ExpandoColumn _expandoColumn;
	private static ExpandoTable _expandoTable;
	private static ExpandoValue _expandoValue;

	@Inject
	private static ExpandoValueLocalService _expandoValueLocalService;

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

}