/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.util.TestPropsValues;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.ServiceRegistration;

/**
 * @author Allen Ziegenfus
 */
@RunWith(Arquillian.class)
public class ScopeFinderReconcileTriggerTest
	extends BaseUnresolvedScopeAliasesTestCase {

	@Test
	public void testAddingService() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			Collection<String> scopeAliases = scopeLocator.getScopeAliases(
				companyId);

			ServiceRegistration<ScopeFinder> serviceRegistration =
				registerScopeFinder();
			String scopeAlias;

			try {
				scopeAlias = waitForNewScopeAlias(companyId, scopeAliases);
			}
			finally {
				serviceRegistration.unregister();
			}

			Assert.assertNotNull(scopeAlias);

			waitForUnresolvableScopeAlias(companyId, scopeAlias);

			OAuth2Application oAuth2Application = saveConfiguration(
				companyId, scopeAlias);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			Assert.assertTrue(
				waitFor(() -> isUnresolved(companyId, oAuth2ApplicationId)));

			serviceRegistration = registerScopeFinder();

			try {
				Assert.assertTrue(
					waitFor(
						() -> hasScopeAlias(oAuth2ApplicationId, scopeAlias)));
				Assert.assertTrue(
					waitFor(
						() -> !isUnresolved(companyId, oAuth2ApplicationId)));
			}
			finally {
				serviceRegistration.unregister();
			}
		}
	}

}