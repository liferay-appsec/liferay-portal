/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.LiferayOAuth2Scope;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.oauth2.provider.util.builder.OAuth2ScopeBuilder;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Allen Ziegenfus
 */
@RunWith(Arquillian.class)
public class OAuth2ApplicationScopeAliasesLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_user = UserTestUtil.addUser();

		_oAuth2Application =
			_oAuth2ApplicationLocalService.addOAuth2Application(
				TestPropsValues.getCompanyId(), _user.getUserId(),
				_user.getFullName(),
				ListUtil.fromArray(GrantType.CLIENT_CREDENTIALS),
				"client_secret_post", _user.getUserId(), null, 0,
				RandomTestUtil.randomString(), null, null, null, 0, null,
				RandomTestUtil.randomString(), null, null, false, null, false,
				new ServiceContext());
	}

	@Test
	public void testAddOAuth2ApplicationScopeAliasesAndUpdateApplication()
		throws Exception {

		long companyId = _oAuth2Application.getCompanyId();

		String scopeAlias = _getScopeAlias(companyId);

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationScopeAliasesLocalService.
				addOAuth2ApplicationScopeAliasesAndUpdateApplication(
					companyId, _user.getUserId(), _user.getFullName(),
					_oAuth2Application.getOAuth2ApplicationId(),
					oAuth2ScopeBuilder -> _assignScope(
						oAuth2ScopeBuilder, companyId, scopeAlias));

		long oAuth2ApplicationScopeAliasesId =
			oAuth2Application.getOAuth2ApplicationScopeAliasesId();

		Assert.assertNotEquals(
			_oAuth2Application.getOAuth2ApplicationScopeAliasesId(),
			oAuth2ApplicationScopeAliasesId);

		List<String> scopeAliasesList =
			_oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
				oAuth2ApplicationScopeAliasesId);

		Assert.assertTrue(scopeAliasesList.contains(scopeAlias));
	}

	@Test
	public void testAddOAuth2ApplicationScopeAliasesAndUpdateApplicationWithFailedUpdate()
		throws Exception {

		long companyId = _oAuth2Application.getCompanyId();

		String scopeAlias = _getScopeAlias(companyId);

		int count =
			_oAuth2ApplicationScopeAliasesLocalService.
				getOAuth2ApplicationScopeAliasesesCount();

		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		// Fail the application update, so the new snapshot and the update must
		// roll back together

		ServiceRegistration<ModelListener<OAuth2Application>>
			serviceRegistration = bundleContext.registerService(
				(Class<ModelListener<OAuth2Application>>)
					(Class<?>)ModelListener.class,
				new BaseModelListener<OAuth2Application>() {

					@Override
					public void onBeforeUpdate(
							OAuth2Application originalOAuth2Application,
							OAuth2Application oAuth2Application)
						throws ModelListenerException {

						throw new ModelListenerException(
							RandomTestUtil.randomString());
					}

				},
				null);

		try {
			_oAuth2ApplicationScopeAliasesLocalService.
				addOAuth2ApplicationScopeAliasesAndUpdateApplication(
					companyId, _user.getUserId(), _user.getFullName(),
					_oAuth2Application.getOAuth2ApplicationId(),
					oAuth2ScopeBuilder -> _assignScope(
						oAuth2ScopeBuilder, companyId, scopeAlias));

			Assert.fail();
		}
		catch (ModelListenerException modelListenerException) {
		}
		finally {
			serviceRegistration.unregister();
		}

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.getOAuth2Application(
				_oAuth2Application.getOAuth2ApplicationId());

		Assert.assertEquals(
			_oAuth2Application.getOAuth2ApplicationScopeAliasesId(),
			oAuth2Application.getOAuth2ApplicationScopeAliasesId());

		Assert.assertEquals(
			count,
			_oAuth2ApplicationScopeAliasesLocalService.
				getOAuth2ApplicationScopeAliasesesCount());
	}

	private void _assignScope(
		OAuth2ScopeBuilder oAuth2ScopeBuilder, long companyId,
		String scopeAlias) {

		for (LiferayOAuth2Scope liferayOAuth2Scope :
				_scopeLocator.getLiferayOAuth2Scopes(companyId, scopeAlias)) {

			Bundle bundle = liferayOAuth2Scope.getBundle();

			oAuth2ScopeBuilder.forApplication(
				liferayOAuth2Scope.getApplicationName(),
				bundle.getSymbolicName(),
				applicationScopeAssigner ->
					applicationScopeAssigner.assignScope(
						liferayOAuth2Scope.getScope()
					).mapToScopeAlias(
						scopeAlias
					));
		}
	}

	private String _getScopeAlias(long companyId) {
		Collection<String> scopeAliases = _scopeLocator.getScopeAliases(
			companyId);

		Assert.assertFalse(scopeAliases.isEmpty());

		return Collections.min(scopeAliases);
	}

	@DeleteAfterTestRun
	private OAuth2Application _oAuth2Application;

	@Inject
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Inject
	private OAuth2ApplicationScopeAliasesLocalService
		_oAuth2ApplicationScopeAliasesLocalService;

	@Inject
	private ScopeLocator _scopeLocator;

	@DeleteAfterTestRun
	private User _user;

}