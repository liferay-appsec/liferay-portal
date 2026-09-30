/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.petra.concurrent.NoticeableExecutorService;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.portal.kernel.cluster.ClusterMasterExecutor;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.InvocationHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class ScopeFinderReconcileTriggerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDrainReconcile() throws Exception {
		AtomicInteger atomicInteger = new AtomicInteger();

		ScopeFinderReconcileTrigger scopeFinderReconcileTrigger =
			_createScopeFinderReconcileTrigger(
				(proxy, method, arguments) -> Boolean.TRUE,
				() -> {
					atomicInteger.incrementAndGet();

					return true;
				});

		AtomicBoolean reconcilePending = ReflectionTestUtil.getFieldValue(
			scopeFinderReconcileTrigger, "_reconcilePending");
		AtomicBoolean reconcileRunning = ReflectionTestUtil.getFieldValue(
			scopeFinderReconcileTrigger, "_reconcileRunning");

		reconcilePending.set(true);
		reconcileRunning.set(true);

		ReflectionTestUtil.invoke(
			scopeFinderReconcileTrigger, "_drainReconcile", new Class<?>[0]);

		Assert.assertEquals(1, atomicInteger.get());
		Assert.assertFalse(reconcilePending.get());
		Assert.assertFalse(reconcileRunning.get());
	}

	@Test
	public void testDrainReconcileWhenRegistryThrowsException()
		throws Exception {

		ScopeFinderReconcileTrigger scopeFinderReconcileTrigger =
			_createScopeFinderReconcileTrigger(
				(proxy, method, arguments) -> {
					throw new IllegalStateException();
				},
				() -> true);

		AtomicBoolean reconcilePending = ReflectionTestUtil.getFieldValue(
			scopeFinderReconcileTrigger, "_reconcilePending");
		AtomicBoolean reconcileRunning = ReflectionTestUtil.getFieldValue(
			scopeFinderReconcileTrigger, "_reconcileRunning");

		reconcilePending.set(true);
		reconcileRunning.set(true);

		try {
			ReflectionTestUtil.invoke(
				scopeFinderReconcileTrigger, "_drainReconcile",
				new Class<?>[0]);

			Assert.fail();
		}
		catch (IllegalStateException illegalStateException) {
		}

		Assert.assertFalse(reconcilePending.get());
		Assert.assertFalse(reconcileRunning.get());
	}

	@Test
	public void testRequestReconcile() throws Exception {
		AtomicBoolean atomicBoolean = new AtomicBoolean();
		AtomicInteger atomicInteger = new AtomicInteger();

		ScopeFinderReconcileTrigger scopeFinderReconcileTrigger =
			_createScopeFinderReconcileTrigger(
				(proxy, method, arguments) -> atomicBoolean.get(),
				() -> {
					atomicInteger.incrementAndGet();

					atomicBoolean.set(true);

					return true;
				});

		ReflectionTestUtil.invoke(
			scopeFinderReconcileTrigger, "_requestReconcile", new Class<?>[0]);

		Assert.assertEquals(1, atomicInteger.get());

		atomicBoolean.set(false);

		ReflectionTestUtil.invoke(
			scopeFinderReconcileTrigger, "_requestReconcile", new Class<?>[0]);

		Assert.assertEquals(2, atomicInteger.get());
	}

	private NoticeableExecutorService _createNoticeableExecutorService() {
		return (NoticeableExecutorService)ProxyUtil.newProxyInstance(
			ScopeFinderReconcileTriggerTest.class.getClassLoader(),
			new Class<?>[] {NoticeableExecutorService.class},
			(proxy, method, arguments) -> {
				Runnable runnable = (Runnable)arguments[0];

				runnable.run();

				return null;
			});
	}

	private ScopeFinderReconcileTrigger _createScopeFinderReconcileTrigger(
		InvocationHandler invocationHandler,
		UnresolvedScopeAliasReconciler unresolvedScopeAliasReconciler) {

		ScopeFinderReconcileTrigger scopeFinderReconcileTrigger =
			new ScopeFinderReconcileTrigger();

		ReflectionTestUtil.setFieldValue(
			scopeFinderReconcileTrigger, "_clusterMasterExecutor",
			ProxyUtil.newProxyInstance(
				ScopeFinderReconcileTriggerTest.class.getClassLoader(),
				new Class<?>[] {ClusterMasterExecutor.class},
				(proxy, method, arguments) -> Boolean.TRUE));
		ReflectionTestUtil.setFieldValue(
			scopeFinderReconcileTrigger, "_portalExecutorManager",
			ProxyUtil.newProxyInstance(
				ScopeFinderReconcileTriggerTest.class.getClassLoader(),
				new Class<?>[] {PortalExecutorManager.class},
				(proxy, method, arguments) ->
					_createNoticeableExecutorService()));
		ReflectionTestUtil.setFieldValue(
			scopeFinderReconcileTrigger, "_unresolvedScopeAliasesRegistry",
			ProxyUtil.newProxyInstance(
				ScopeFinderReconcileTriggerTest.class.getClassLoader(),
				new Class<?>[] {UnresolvedScopeAliasesRegistry.class},
				invocationHandler));
		ReflectionTestUtil.setFieldValue(
			scopeFinderReconcileTrigger, "_unresolvedScopeAliasReconciler",
			unresolvedScopeAliasReconciler);

		return scopeFinderReconcileTrigger;
	}

}