/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.configuration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.configuration.OAuth2ProviderApplicationHeadlessServerConfiguration;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.LiferayOAuth2Scope;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
	public void testConcurrentReconcilesCreateSingleSnapshot()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, null);

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				int originalCount =
					_oAuth2ApplicationScopeAliasesLocalService.
						getOAuth2ApplicationScopeAliasesesCount();

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

				// Two reconciles released at once must serialize on the
				// reconcile lock, so exactly one binds the alias and writes one
				// snapshot

				CountDownLatch startCountDownLatch = new CountDownLatch(1);
				CountDownLatch doneCountDownLatch = new CountDownLatch(2);

				List<Throwable> throwables = Collections.synchronizedList(
					new ArrayList<>());

				Runnable runnable = () -> {
					try {
						startCountDownLatch.await();

						_runReconcile();
					}
					catch (Throwable throwable) {
						throwables.add(throwable);
					}
					finally {
						doneCountDownLatch.countDown();
					}
				};

				Thread thread1 = new Thread(runnable);
				Thread thread2 = new Thread(runnable);

				thread1.start();
				thread2.start();

				startCountDownLatch.countDown();

				Assert.assertTrue(
					"The concurrent reconciles did not finish in time",
					doneCountDownLatch.await(60, TimeUnit.SECONDS));

				Assert.assertEquals(
					"Concurrent reconciles must not error",
					Collections.emptyList(), throwables);

				Assert.assertTrue(
					"The alias must be bound after concurrent reconciles",
					_hasScopeAlias(oAuth2ApplicationId, scopeAlias));

				Assert.assertFalse(
					"The registry must be cleared once the alias is bound",
					_unresolvedApplicationIds().contains(oAuth2ApplicationId));

				Assert.assertEquals(
					"Serialized reconciles must write exactly one snapshot",
					originalCount + 1,
					_oAuth2ApplicationScopeAliasesLocalService.
						getOAuth2ApplicationScopeAliasesesCount());
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testConfigurationDropClearsRegistry() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _saveConfiguration(
				companyId,
				Arrays.asList(scopeAlias, _UNRESOLVABLE_SCOPE_ALIAS));

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				Assert.assertTrue(
					"Expected the unresolvable alias to be recorded",
					_waitForUnresolved(oAuth2ApplicationId, true));

				_saveConfiguration(
					companyId, Collections.singletonList(scopeAlias));

				Assert.assertTrue(
					"Expected the registry entry to clear once the " +
						"unresolvable alias left the configuration",
					_waitForUnresolved(oAuth2ApplicationId, false));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

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
	public void testReconcileBindsUnderDeclaredCase() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			String declaredScopeAlias = StringUtil.toUpperCase(scopeAlias);

			Assert.assertNotEquals(
				"The environment must yield a scope alias with a distinct " +
					"uppercase form to exercise case normalization",
				scopeAlias, declaredScopeAlias);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, scopeAlias);

			try {

				// Declare the alias in a different case than it is registered

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2Application.getOAuth2ApplicationId(),
					Collections.singletonList(declaredScopeAlias));

				_runReconcile();

				// The alias resolves under its registered casing and the
				// grant is persisted under the declared casing the client holds

				Assert.assertTrue(
					"Expected the declared alias " + declaredScopeAlias +
						" to be bound so the client can match it",
					_hasScopeAlias(
						oAuth2Application.getOAuth2ApplicationId(),
						declaredScopeAlias));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testReconcileBindsWithoutRevokingUnresolvableGrant()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String resolvableScopeAlias = _resolvableScopeAlias(companyId);

			BundleContext bundleContext = SystemBundleUtil.getBundleContext();

			Collection<String> beforeScopeAliases =
				_scopeLocator.getScopeAliases(companyId);

			ServiceRegistration<ScopeFinder> serviceRegistration =
				_registerTestScopeFinder(bundleContext);

			OAuth2Application oAuth2Application = null;

			try {
				String grantedScopeAlias = _waitForNewScopeAlias(
					companyId, beforeScopeAliases);

				Assert.assertNotNull(
					"A test ScopeFinder must yield a resolvable alias",
					grantedScopeAlias);

				oAuth2Application = _addOAuth2ApplicationWithout(
					companyId, null);

				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				_oAuth2ApplicationLocalService.updateScopeAliases(
					oAuth2Application.getUserId(),
					oAuth2Application.getUserName(), oAuth2ApplicationId,
					Collections.singletonList(grantedScopeAlias));

				Assert.assertTrue(
					"Precondition: the test alias must be granted",
					_hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(resolvableScopeAlias));

				serviceRegistration.unregister();

				serviceRegistration = null;

				_waitForUnresolvableAlias(companyId, grantedScopeAlias);

				_runReconcile();

				Assert.assertTrue(
					"The reconcile must bind " + resolvableScopeAlias +
						" additively",
					_hasScopeAlias(oAuth2ApplicationId, resolvableScopeAlias));
				Assert.assertTrue(
					"The reconcile must copy the unresolvable grant " +
						grantedScopeAlias + " rather than revoke it",
					_hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));
			}
			finally {
				if (serviceRegistration != null) {
					serviceRegistration.unregister();
				}

				if (oAuth2Application != null) {
					_cleanUp(oAuth2Application);
				}
			}
		}
	}

	@Test
	public void testReconcileDoesNotRevokeGrantedAliasDuringChurn()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String registryScopeAlias = _resolvableScopeAlias(companyId);

			BundleContext bundleContext = SystemBundleUtil.getBundleContext();

			Collection<String> beforeScopeAliases =
				_scopeLocator.getScopeAliases(companyId);

			ServiceRegistration<ScopeFinder> serviceRegistration =
				_registerTestScopeFinder(bundleContext);

			OAuth2Application oAuth2Application = null;

			try {
				String grantedScopeAlias = _waitForNewScopeAlias(
					companyId, beforeScopeAliases);

				Assert.assertNotNull(
					"A test ScopeFinder must yield a resolvable alias",
					grantedScopeAlias);

				oAuth2Application = _addOAuth2ApplicationWithout(
					companyId, null);

				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				_oAuth2ApplicationLocalService.updateScopeAliases(
					oAuth2Application.getUserId(),
					oAuth2Application.getUserName(), oAuth2ApplicationId,
					Collections.singletonList(grantedScopeAlias));

				Assert.assertTrue(
					"Precondition: the test alias must be granted",
					_hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(registryScopeAlias));

				serviceRegistration.unregister();

				serviceRegistration = null;

				_waitForUnresolvableAlias(companyId, grantedScopeAlias);

				_runReconcile();

				Assert.assertTrue(
					"The reconcile must not revoke the already-granted alias " +
						grantedScopeAlias,
					_hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));
			}
			finally {
				if (serviceRegistration != null) {
					serviceRegistration.unregister();
				}

				if (oAuth2Application != null) {
					_cleanUp(oAuth2Application);
				}
			}
		}
	}

	@Test
	public void testReconcilePersistsScopeAliasesSnapshot() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, scopeAlias);

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

				_runReconcile();

				Assert.assertTrue(
					"Expected " + scopeAlias + " to be bound",
					_hasScopeAlias(oAuth2ApplicationId, scopeAlias));

				OAuth2Application reconciledOAuth2Application =
					_oAuth2ApplicationLocalService.getOAuth2Application(
						oAuth2ApplicationId);

				Assert.assertNotNull(
					"The reconciled scope aliases snapshot must be persisted " +
						"so a token can resolve its scopes",
					_oAuth2ApplicationScopeAliasesLocalService.
						fetchOAuth2ApplicationScopeAliases(
							reconciledOAuth2Application.
								getOAuth2ApplicationScopeAliasesId()));
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
	public void testReconcileSkipsAlreadyGrantedAlias() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, null);

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				_oAuth2ApplicationLocalService.updateScopeAliases(
					oAuth2Application.getUserId(),
					oAuth2Application.getUserName(), oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

				Assert.assertTrue(
					"Precondition: the alias must be granted",
					_hasScopeAlias(oAuth2ApplicationId, scopeAlias));

				long grantedOAuth2ApplicationScopeAliasesId =
					_oAuth2ApplicationLocalService.getOAuth2Application(
						oAuth2ApplicationId
					).getOAuth2ApplicationScopeAliasesId();

				// A stale entry, as a non-master node keeps after the master
				// binds the alias

				_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

				_runReconcile();

				OAuth2Application reconciledOAuth2Application =
					_oAuth2ApplicationLocalService.getOAuth2Application(
						oAuth2ApplicationId);

				Assert.assertEquals(
					"An already granted alias must not write a redundant " +
						"snapshot",
					grantedOAuth2ApplicationScopeAliasesId,
					reconciledOAuth2Application.
						getOAuth2ApplicationScopeAliasesId());

				Assert.assertFalse(
					_unresolvedApplicationIds().contains(oAuth2ApplicationId));
			}
			finally {
				_cleanUp(oAuth2Application);
			}
		}
	}

	@Test
	public void testReconcileWriteFailureLeavesNoOrphanSnapshot()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = _resolvableScopeAlias(companyId);

			OAuth2Application oAuth2Application = _addOAuth2ApplicationWithout(
				companyId, null);

			BundleContext bundleContext = SystemBundleUtil.getBundleContext();

			ServiceRegistration<ModelListener<OAuth2Application>>
				serviceRegistration = null;

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				long originalOAuth2ApplicationScopeAliasesId =
					oAuth2Application.getOAuth2ApplicationScopeAliasesId();

				int originalCount =
					_oAuth2ApplicationScopeAliasesLocalService.
						getOAuth2ApplicationScopeAliasesesCount();

				// Fail the application repoint so the snapshot write and the
				// repoint must roll back together

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
								"Forced application repoint failure");
						}

					},
					null);

				try {
					_oAuth2ApplicationScopeAliasesLocalService.
						addOAuth2ApplicationScopeAliasesAndUpdateApplication(
							companyId, oAuth2Application.getUserId(),
							oAuth2Application.getUserName(),
							oAuth2ApplicationId,
							oAuth2ScopeBuilder -> {
								for (LiferayOAuth2Scope liferayOAuth2Scope :
										_scopeLocator.getLiferayOAuth2Scopes(
											companyId, scopeAlias)) {

									oAuth2ScopeBuilder.forApplication(
										liferayOAuth2Scope.getApplicationName(),
										liferayOAuth2Scope.getBundle(
										).getSymbolicName(),
										applicationScopeAssigner ->
											applicationScopeAssigner.
												assignScope(
													liferayOAuth2Scope.
														getScope()
												).mapToScopeAlias(
													scopeAlias
												));
								}
							});

					Assert.fail(
						"Expected the forced repoint failure to abort the " +
							"write");
				}
				catch (Exception exception) {

					// Expected: the failure must roll back the transaction

				}

				OAuth2Application reloadedOAuth2Application =
					_oAuth2ApplicationLocalService.getOAuth2Application(
						oAuth2ApplicationId);

				Assert.assertEquals(
					"A failed write must not repoint the application",
					originalOAuth2ApplicationScopeAliasesId,
					reloadedOAuth2Application.
						getOAuth2ApplicationScopeAliasesId());

				Assert.assertEquals(
					"A failed write must not leave an orphan scope aliases " +
						"snapshot",
					originalCount,
					_oAuth2ApplicationScopeAliasesLocalService.
						getOAuth2ApplicationScopeAliasesesCount());
			}
			finally {
				if (serviceRegistration != null) {
					serviceRegistration.unregister();
				}

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

	@Test
	public void testTriggerBindsOnScopeFinderRegistration() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			BundleContext bundleContext = SystemBundleUtil.getBundleContext();

			String scopeAlias = _discoverResolvableScopeAlias(
				bundleContext, companyId);

			Assert.assertNotNull(
				"A test ScopeFinder must yield a resolvable scope alias so " +
					"the trigger has something to bind",
				scopeAlias);

			OAuth2Application oAuth2Application = _saveConfiguration(
				companyId, Collections.singletonList(scopeAlias));

			ServiceRegistration<ScopeFinder> serviceRegistration = null;

			try {
				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				Assert.assertTrue(
					"Expected the unresolvable alias to be recorded before " +
						"the ScopeFinder registers",
					_waitForUnresolved(oAuth2ApplicationId, true));

				serviceRegistration = _registerTestScopeFinder(bundleContext);

				Assert.assertTrue(
					"Expected the ScopeFinder registration to drive the " +
						"reconcile that binds " + scopeAlias,
					_waitForScopeAlias(oAuth2ApplicationId, scopeAlias));
			}
			finally {
				if (serviceRegistration != null) {
					serviceRegistration.unregister();
				}

				_cleanUp(oAuth2Application);
			}
		}
	}

	private OAuth2Application _addOAuth2ApplicationWithout(
			long companyId, String excludedScopeAlias)
		throws Exception {

		String anchorScopeAlias = excludedScopeAlias;

		if (anchorScopeAlias == null) {
			anchorScopeAlias = _resolvableScopeAlias(companyId);
		}

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
			).put(
				"scopes", new String[] {anchorScopeAlias}
			).build());

		OAuth2Application oAuth2Application = _fetchOAuth2Application(
			companyId);

		Assert.assertNotNull(oAuth2Application);

		long oAuth2ApplicationId = oAuth2Application.getOAuth2ApplicationId();

		// Wait until the configuration factory's own updateScopes has granted
		// the anchor alias, so its asynchronous write cannot land after and
		// clobber the grant snapshot the test stages below

		Assert.assertTrue(
			"The configuration factory must grant " + anchorScopeAlias +
				" before the test resets the grants",
			_waitForScopeAlias(oAuth2ApplicationId, anchorScopeAlias));

		// Reset the grant snapshot to a known empty state, so the reconcile has
		// to add whatever the test declares as unresolved

		_oAuth2ApplicationLocalService.updateScopeAliases(
			oAuth2Application.getUserId(), oAuth2Application.getUserName(),
			oAuth2ApplicationId, Collections.emptyList());

		return _oAuth2ApplicationLocalService.getOAuth2Application(
			oAuth2ApplicationId);
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

	private String _discoverResolvableScopeAlias(
			BundleContext bundleContext, long companyId)
		throws Exception {

		Collection<String> beforeScopeAliases = _scopeLocator.getScopeAliases(
			companyId);

		ServiceRegistration<ScopeFinder> serviceRegistration =
			_registerTestScopeFinder(bundleContext);

		String scopeAlias = null;

		try {
			for (int i = 0; (scopeAlias == null) && (i < 300); i++) {
				for (String curScopeAlias :
						_scopeLocator.getScopeAliases(companyId)) {

					if (!beforeScopeAliases.contains(curScopeAlias)) {
						scopeAlias = curScopeAlias;

						break;
					}
				}

				if (scopeAlias == null) {
					Thread.sleep(10);
				}
			}
		}
		finally {
			serviceRegistration.unregister();
		}

		if (scopeAlias == null) {
			return null;
		}

		_waitForUnresolvableAlias(companyId, scopeAlias);

		return scopeAlias;
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

	private ServiceRegistration<ScopeFinder> _registerTestScopeFinder(
		BundleContext bundleContext) {

		ScopeFinder scopeFinder = () -> Collections.singletonList(_SCOPE);

		return bundleContext.registerService(
			ScopeFinder.class, scopeFinder,
			HashMapDictionaryBuilder.<String, Object>put(
				"osgi.jaxrs.name", _APPLICATION_NAME
			).build());
	}

	private String _resolvableScopeAlias(long companyId) {
		Collection<String> scopeAliases = _scopeLocator.getScopeAliases(
			companyId);

		Assert.assertFalse(
			"No resolvable scope alias is available in this environment",
			scopeAliases.isEmpty());

		return Collections.min(scopeAliases);
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

	private OAuth2Application _saveConfiguration(
			long companyId, List<String> scopeAliases)
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
			).put(
				"scopes", scopeAliases.toArray(new String[0])
			).build());

		OAuth2Application oAuth2Application = _fetchOAuth2Application(
			companyId);

		Assert.assertNotNull(oAuth2Application);

		return oAuth2Application;
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

	private String _waitForNewScopeAlias(
			long companyId, Collection<String> beforeScopeAliases)
		throws Exception {

		for (int i = 0; i < 300; i++) {
			for (String scopeAlias : _scopeLocator.getScopeAliases(companyId)) {
				if (!beforeScopeAliases.contains(scopeAlias)) {
					return scopeAlias;
				}
			}

			Thread.sleep(10);
		}

		return null;
	}

	private boolean _waitForScopeAlias(
			long oAuth2ApplicationId, String scopeAlias)
		throws Exception {

		CountDownLatch countDownLatch = new CountDownLatch(100);

		while (countDownLatch.getCount() > 0) {
			if (_hasScopeAlias(oAuth2ApplicationId, scopeAlias)) {
				return true;
			}

			countDownLatch.countDown();

			countDownLatch.await(50, TimeUnit.MILLISECONDS);
		}

		return _hasScopeAlias(oAuth2ApplicationId, scopeAlias);
	}

	private void _waitForUnresolvableAlias(long companyId, String scopeAlias)
		throws Exception {

		for (int i = 0;
			 !_scopeLocator.getLiferayOAuth2Scopes(
				 companyId, scopeAlias
			 ).isEmpty() && (i < 300); i++) {

			Thread.sleep(10);
		}

		Assert.assertTrue(
			"Alias " + scopeAlias + " still resolves after deregistration",
			_scopeLocator.getLiferayOAuth2Scopes(
				companyId, scopeAlias
			).isEmpty());
	}

	private boolean _waitForUnresolved(
			long oAuth2ApplicationId, boolean unresolved)
		throws Exception {

		for (int i = 0; i < 300; i++) {
			if (_unresolvedApplicationIds().contains(oAuth2ApplicationId) ==
					unresolved) {

				return true;
			}

			Thread.sleep(10);
		}

		if (_unresolvedApplicationIds().contains(oAuth2ApplicationId) ==
				unresolved) {

			return true;
		}

		return false;
	}

	private static final String _APPLICATION_NAME =
		"Test.ScopeReResolution.Application";

	private static final String _EXTERNAL_REFERENCE_CODE =
		"scope-re-resolution-test";

	private static final String _RECONCILER_CLASS_NAME =
		"com.liferay.oauth2.provider.internal.scheduler." +
			"UnresolvedScopeAliasReconcilerSchedulerJobConfiguration";

	private static final String _SCOPE = "everything";

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