/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler.test;

import com.liferay.oauth2.provider.configuration.OAuth2ProviderApplicationHeadlessServerConfiguration;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.LiferayOAuth2Scope;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Allen Ziegenfus
 */
public abstract class BaseUnresolvedScopeAliasesTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() throws Exception {
		if (_configuration != null) {
			ConfigurationTestUtil.deleteConfiguration(_configuration);
		}
	}

	protected OAuth2Application addOAuth2Application(long companyId)
		throws Exception {

		OAuth2Application oAuth2Application = saveConfiguration(
			companyId, RandomTestUtil.randomString());

		// Wait until the configuration factory has recorded the unresolvable
		// alias, so its own scope update cannot land after the test stages the
		// registry

		Assert.assertTrue(
			waitFor(
				() -> isUnresolved(
					companyId, oAuth2Application.getOAuth2ApplicationId())));

		return oAuth2Application;
	}

	protected String getResolvableScopeAlias(long companyId) {
		Collection<String> scopeAliases = scopeLocator.getScopeAliases(
			companyId);

		Assert.assertFalse(scopeAliases.isEmpty());

		return Collections.min(scopeAliases);
	}

	protected boolean hasScopeAlias(long oAuth2ApplicationId, String scopeAlias)
		throws Exception {

		OAuth2Application oAuth2Application =
			oAuth2ApplicationLocalService.getOAuth2Application(
				oAuth2ApplicationId);

		List<String> scopeAliasesList =
			oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId());

		return scopeAliasesList.contains(scopeAlias);
	}

	protected boolean isUnresolved(long companyId, long oAuth2ApplicationId) {
		Collection<String> scopeAliases =
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId);

		return !scopeAliases.isEmpty();
	}

	protected ServiceRegistration<ScopeFinder> registerScopeFinder() {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		ScopeFinder scopeFinder = () -> Collections.singletonList(_SCOPE);

		return bundleContext.registerService(
			ScopeFinder.class, scopeFinder,
			HashMapDictionaryBuilder.<String, Object>put(
				"osgi.jaxrs.name", _APPLICATION_NAME
			).build());
	}

	protected OAuth2Application saveConfiguration(
			long companyId, String... scopeAliases)
		throws Exception {

		_configuration = configurationAdmin.getFactoryConfiguration(
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
				"scopes", scopeAliases
			).build());

		for (int i = 0; i < 200; i++) {
			OAuth2Application oAuth2Application =
				oAuth2ApplicationLocalService.
					fetchOAuth2ApplicationByExternalReferenceCode(
						_EXTERNAL_REFERENCE_CODE, companyId);

			if (oAuth2Application != null) {
				return oAuth2Application;
			}

			Thread.sleep(50);
		}

		throw new AssertionError(
			"The configuration factory did not create the OAuth 2 application");
	}

	protected boolean waitFor(UnsafeSupplier<Boolean, Exception> unsafeSupplier)
		throws Exception {

		for (int i = 0; i < 200; i++) {
			if (unsafeSupplier.get()) {
				return true;
			}

			Thread.sleep(50);
		}

		return unsafeSupplier.get();
	}

	protected String waitForNewScopeAlias(
			long companyId, Collection<String> scopeAliases)
		throws Exception {

		for (int i = 0; i < 200; i++) {
			for (String scopeAlias : scopeLocator.getScopeAliases(companyId)) {
				if (!scopeAliases.contains(scopeAlias)) {
					return scopeAlias;
				}
			}

			Thread.sleep(50);
		}

		return null;
	}

	protected void waitForUnresolvableScopeAlias(
			long companyId, String scopeAlias)
		throws Exception {

		Assert.assertTrue(
			waitFor(
				() -> {
					Collection<LiferayOAuth2Scope> liferayOAuth2Scopes =
						scopeLocator.getLiferayOAuth2Scopes(
							companyId, scopeAlias);

					return liferayOAuth2Scopes.isEmpty();
				}));
	}

	@Inject
	protected ConfigurationAdmin configurationAdmin;

	@Inject
	protected OAuth2ApplicationLocalService oAuth2ApplicationLocalService;

	@Inject
	protected OAuth2ApplicationScopeAliasesLocalService
		oAuth2ApplicationScopeAliasesLocalService;

	@Inject
	protected ScopeLocator scopeLocator;

	@Inject
	protected UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry;

	private static final String _APPLICATION_NAME =
		RandomTestUtil.randomString();

	private static final String _EXTERNAL_REFERENCE_CODE =
		RandomTestUtil.randomString();

	private static final String _SCOPE = "everything";

	private Configuration _configuration;

}