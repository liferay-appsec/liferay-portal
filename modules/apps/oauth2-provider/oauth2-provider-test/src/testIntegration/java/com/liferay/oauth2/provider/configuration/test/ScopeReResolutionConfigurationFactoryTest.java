/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.configuration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.configuration.OAuth2ProviderApplicationHeadlessServerConfiguration;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Allen Ziegenfus
 */
@RunWith(Arquillian.class)
public class ScopeReResolutionConfigurationFactoryTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testReconcileBindsAliasWhenScopeSourceRegisters()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, scopeAlias);

			try {
				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2Application.getOAuth2ApplicationId(),
					Collections.singletonList(scopeAlias));

				_runReconcile();

				Assert.assertTrue(
					"Expected " + scopeAlias + " to be bound",
					_hasScopeAlias(
						oAuth2Application.getOAuth2ApplicationId(),
						scopeAlias));
				Assert.assertFalse(
					_unresolvedApplicationIds().contains(
						oAuth2Application.getOAuth2ApplicationId()));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testReconcileCaseNormalizes() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, scopeAlias);

			try {

				// Declare the alias in a different case than it is registered

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2Application.getOAuth2ApplicationId(),
					Collections.singletonList(
						StringUtil.toUpperCase(scopeAlias)));

				_runReconcile();

				// It binds under the registered (resolvable) case, not the
				// declared case

				Assert.assertTrue(
					"Expected " + scopeAlias +
						" to be bound after case normalization",
					_hasScopeAlias(
						oAuth2Application.getOAuth2ApplicationId(),
						scopeAlias));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testReconcileRetainsUnresolvedAliasAfterPartialBind()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String resolvableScopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, resolvableScopeAlias);

			try {
				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2Application.getOAuth2ApplicationId(),
					Arrays.asList(
						resolvableScopeAlias, _UNRESOLVABLE_SCOPE_ALIAS));

				_runReconcile();

				Assert.assertTrue(
					"Expected " + resolvableScopeAlias + " to be bound",
					_hasScopeAlias(
						oAuth2Application.getOAuth2ApplicationId(),
						resolvableScopeAlias));

				Assert.assertFalse(
					"Expected " + _UNRESOLVABLE_SCOPE_ALIAS +
						" not to be bound before it resolves",
					_hasScopeAlias(
						oAuth2Application.getOAuth2ApplicationId(),
						_UNRESOLVABLE_SCOPE_ALIAS));

				Assert.assertTrue(
					"Expected the application to stay tracked while " +
						_UNRESOLVABLE_SCOPE_ALIAS + " is unresolved",
					_unresolvedApplicationIds().contains(
						oAuth2Application.getOAuth2ApplicationId()));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testReconcileWritesOnlyOnProgress() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, null);

			try {
				long oAuth2ApplicationScopeAliasesId =
					oAuth2Application.getOAuth2ApplicationScopeAliasesId();

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2Application.getOAuth2ApplicationId(),
					Collections.singletonList(_UNRESOLVABLE_SCOPE_ALIAS));

				_runReconcile();

				OAuth2Application reconciledOAuth2Application =
					_oAuth2ApplicationLocalService.getOAuth2Application(
						oAuth2Application.getOAuth2ApplicationId());

				// Nothing resolved, so no new snapshot is written and the
				// registry entry is retained

				Assert.assertEquals(
					oAuth2ApplicationScopeAliasesId,
					reconciledOAuth2Application.
						getOAuth2ApplicationScopeAliasesId());

				Assert.assertTrue(
					_unresolvedApplicationIds().contains(
						oAuth2Application.getOAuth2ApplicationId()));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	private OAuth2Application _addOAuth2ApplicationWithout(
			long companyId, String excludedScopeAlias)
		throws Exception {

		_configuration = _configurationAdmin.getFactoryConfiguration(
			OAuth2ProviderApplicationHeadlessServerConfiguration.class.
				getName(),
			_EXTERNAL_REFERENCE_CODE, StringPool.QUESTION);

		ConfigurationTestUtil.saveConfiguration(
			_configuration,
			HashMapDictionaryBuilder.<String, Object>put(
				"_portalK8sConfigMapModifier.cardinality.minimum", 0
			).put(
				"baseURL", "http://foo.me"
			).put(
				"companyId", companyId
			).build());

		OAuth2Application oAuth2Application = _fetchOAuth2Application(
			companyId);

		Assert.assertNotNull(oAuth2Application);

		// Reset the grant snapshot to a known state that excludes the alias
		// under test, so the reconcile has to add it

		List<String> scopeAliasesList =
			_oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId());

		if (excludedScopeAlias != null) {
			scopeAliasesList = ListUtil.remove(
				scopeAliasesList,
				Collections.singletonList(excludedScopeAlias));
		}
		else {
			scopeAliasesList = Collections.emptyList();
		}

		_oAuth2ApplicationLocalService.updateScopeAliases(
			oAuth2Application.getUserId(), oAuth2Application.getUserName(),
			oAuth2Application.getOAuth2ApplicationId(), scopeAliasesList);

		return _oAuth2ApplicationLocalService.getOAuth2Application(
			oAuth2Application.getOAuth2ApplicationId());
	}

	private void _cleanUp(OAuth2Application oAuth2Application)
		throws Exception {

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			oAuth2Application.getCompanyId(),
			oAuth2Application.getOAuth2ApplicationId());

		if (_configuration != null) {
			ConfigurationTestUtil.deleteConfiguration(_configuration);

			_configuration = null;
		}
	}

	private OAuth2Application _fetchOAuth2Application(long companyId)
		throws Exception {

		for (int i = 0; i < 50; i++) {
			try {
				return _oAuth2ApplicationLocalService.
					getOAuth2ApplicationByExternalReferenceCode(
						_EXTERNAL_REFERENCE_CODE, companyId);
			}
			catch (Exception exception) {

				// The configuration factory has not created it yet

			}

			Thread.sleep(10);
		}

		return null;
	}

	private boolean _hasScopeAlias(long oAuth2ApplicationId, String scopeAlias)
		throws Exception {

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.getOAuth2Application(
				oAuth2ApplicationId);

		List<String> scopeAliasesList =
			_oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId());

		return scopeAliasesList.contains(scopeAlias);
	}

	private String _resolvableScopeAlias(long companyId) {
		Collection<String> scopeAliases = _scopeLocator.getScopeAliases(
			companyId);

		Assume.assumeFalse(
			"No resolvable scope alias is available in this environment",
			scopeAliases.isEmpty());

		return scopeAliases.iterator(
		).next();
	}

	private void _runReconcile() throws Exception {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		Collection<ServiceReference<SchedulerJobConfiguration>>
			serviceReferences = bundleContext.getServiceReferences(
				SchedulerJobConfiguration.class, null);

		for (ServiceReference<SchedulerJobConfiguration> serviceReference :
				serviceReferences) {

			SchedulerJobConfiguration schedulerJobConfiguration =
				bundleContext.getService(serviceReference);

			try {
				if (_RECONCILER_CLASS_NAME.equals(
						schedulerJobConfiguration.getName())) {

					schedulerJobConfiguration.getJobExecutorUnsafeRunnable(
					).run();

					return;
				}
			}
			finally {
				bundleContext.ungetService(serviceReference);
			}
		}

		throw new IllegalStateException(
			"Unable to find the scheduler job " + _RECONCILER_CLASS_NAME);
	}

	private Collection<Long> _unresolvedApplicationIds() {
		Set<Long> oAuth2ApplicationIds = new HashSet<>();

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		for (Set<Long> companyOAuth2ApplicationIds :
				oAuth2ApplicationIdsByCompanyId.values()) {

			oAuth2ApplicationIds.addAll(companyOAuth2ApplicationIds);
		}

		return oAuth2ApplicationIds;
	}

	private static final String _EXTERNAL_REFERENCE_CODE =
		"scope-re-resolution-test";

	private static final String _RECONCILER_CLASS_NAME =
		"com.liferay.oauth2.provider.internal.scheduler." +
			"UnresolvedScopeAliasReconcilerSchedulerJobConfiguration";

	private static final String _UNRESOLVABLE_SCOPE_ALIAS =
		"C_Lpp64799Nonexistent.everything";

	private Configuration _configuration;

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	@Inject
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Inject
	private OAuth2ApplicationScopeAliasesLocalService
		_oAuth2ApplicationScopeAliasesLocalService;

	@Inject
	private ScopeLocator _scopeLocator;

	@Inject
	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

}