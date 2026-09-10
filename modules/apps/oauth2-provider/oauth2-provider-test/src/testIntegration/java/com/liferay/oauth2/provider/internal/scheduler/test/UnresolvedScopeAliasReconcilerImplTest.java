/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.ServiceRegistration;

/**
 * @author Allen Ziegenfus
 */
@RunWith(Arquillian.class)
public class UnresolvedScopeAliasReconcilerImplTest
	extends BaseUnresolvedScopeAliasesTestCase {

	@Test
	public void testReconcile() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String scopeAlias = getResolvableScopeAlias(companyId);

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(scopeAlias));

			Assert.assertTrue(_unresolvedScopeAliasReconciler.reconcile());

			Assert.assertTrue(hasScopeAlias(oAuth2ApplicationId, scopeAlias));
			Assert.assertFalse(isUnresolved(companyId, oAuth2ApplicationId));
		}
	}

	@Test
	public void testReconcileConcurrently() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String scopeAlias = getResolvableScopeAlias(companyId);

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(scopeAlias));

			int count =
				oAuth2ApplicationScopeAliasesLocalService.
					getOAuth2ApplicationScopeAliasesesCount();

			CountDownLatch startCountDownLatch = new CountDownLatch(1);
			CountDownLatch doneCountDownLatch = new CountDownLatch(2);

			List<Throwable> throwables = Collections.synchronizedList(
				new ArrayList<>());

			Runnable runnable = () -> {
				try {
					startCountDownLatch.await();

					_unresolvedScopeAliasReconciler.reconcile();
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

			Assert.assertTrue(doneCountDownLatch.await(1, TimeUnit.MINUTES));

			Assert.assertEquals(throwables.toString(), 0, throwables.size());

			Assert.assertTrue(hasScopeAlias(oAuth2ApplicationId, scopeAlias));
			Assert.assertFalse(isUnresolved(companyId, oAuth2ApplicationId));
			Assert.assertEquals(
				count + 1,
				oAuth2ApplicationScopeAliasesLocalService.
					getOAuth2ApplicationScopeAliasesesCount());
		}
	}

	@Test
	public void testReconcileRetainsUnresolvedScopeAliasAfterPartialBind()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String scopeAlias = getResolvableScopeAlias(companyId);
			String unresolvableScopeAlias = RandomTestUtil.randomString();

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Arrays.asList(scopeAlias, unresolvableScopeAlias));

			Assert.assertTrue(_unresolvedScopeAliasReconciler.reconcile());

			Assert.assertTrue(hasScopeAlias(oAuth2ApplicationId, scopeAlias));
			Assert.assertFalse(
				hasScopeAlias(oAuth2ApplicationId, unresolvableScopeAlias));
			Assert.assertEquals(
				Collections.singleton(unresolvableScopeAlias),
				unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId));
		}
	}

	@Test
	public void testReconcileSkipsGrantedScopeAlias() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String scopeAlias = getResolvableScopeAlias(companyId);

			oAuth2Application =
				oAuth2ApplicationLocalService.updateScopeAliases(
					oAuth2Application.getUserId(),
					oAuth2Application.getUserName(), oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

			Assert.assertTrue(hasScopeAlias(oAuth2ApplicationId, scopeAlias));

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(scopeAlias));

			_unresolvedScopeAliasReconciler.reconcile();

			OAuth2Application reconciledOAuth2Application =
				oAuth2ApplicationLocalService.getOAuth2Application(
					oAuth2ApplicationId);

			Assert.assertEquals(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId(),
				reconciledOAuth2Application.
					getOAuth2ApplicationScopeAliasesId());

			Assert.assertFalse(isUnresolved(companyId, oAuth2ApplicationId));
		}
	}

	@Test
	public void testReconcileWithDeclaredCase() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = null;

			for (String curScopeAlias :
					scopeLocator.getScopeAliases(companyId)) {

				if (!curScopeAlias.equals(
						StringUtil.toUpperCase(curScopeAlias))) {

					scopeAlias = curScopeAlias;

					break;
				}
			}

			Assume.assumeNotNull(scopeAlias);

			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String declaredScopeAlias = StringUtil.toUpperCase(scopeAlias);

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(declaredScopeAlias));

			Assert.assertTrue(_unresolvedScopeAliasReconciler.reconcile());

			Assert.assertTrue(
				hasScopeAlias(oAuth2ApplicationId, declaredScopeAlias));
			Assert.assertFalse(isUnresolved(companyId, oAuth2ApplicationId));
		}
	}

	@Test
	public void testReconcileWithoutRevokingUnresolvableGrant()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			String scopeAlias = getResolvableScopeAlias(companyId);

			Collection<String> scopeAliases = scopeLocator.getScopeAliases(
				companyId);

			ServiceRegistration<ScopeFinder> serviceRegistration =
				registerScopeFinder();

			try {
				String grantedScopeAlias = waitForNewScopeAlias(
					companyId, scopeAliases);

				Assert.assertNotNull(grantedScopeAlias);

				OAuth2Application oAuth2Application = addOAuth2Application(
					companyId);

				long oAuth2ApplicationId =
					oAuth2Application.getOAuth2ApplicationId();

				oAuth2ApplicationLocalService.updateScopeAliases(
					oAuth2Application.getUserId(),
					oAuth2Application.getUserName(), oAuth2ApplicationId,
					Collections.singletonList(grantedScopeAlias));

				Assert.assertTrue(
					hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));

				unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId,
					Collections.singletonList(scopeAlias));

				serviceRegistration.unregister();

				serviceRegistration = null;

				waitForUnresolvableScopeAlias(companyId, grantedScopeAlias);

				Assert.assertTrue(_unresolvedScopeAliasReconciler.reconcile());

				Assert.assertTrue(
					hasScopeAlias(oAuth2ApplicationId, grantedScopeAlias));
				Assert.assertTrue(
					hasScopeAlias(oAuth2ApplicationId, scopeAlias));
			}
			finally {
				if (serviceRegistration != null) {
					serviceRegistration.unregister();
				}
			}
		}
	}

	@Test
	public void testReconcileWritesOnlyOnProgress() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			_unresolvedScopeAliasReconciler.reconcile();

			OAuth2Application reconciledOAuth2Application =
				oAuth2ApplicationLocalService.getOAuth2Application(
					oAuth2ApplicationId);

			Assert.assertEquals(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId(),
				reconciledOAuth2Application.
					getOAuth2ApplicationScopeAliasesId());

			Assert.assertTrue(isUnresolved(companyId, oAuth2ApplicationId));
		}
	}

	@Inject
	private UnresolvedScopeAliasReconciler _unresolvedScopeAliasReconciler;

}