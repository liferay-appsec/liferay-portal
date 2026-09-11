/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerConfiguration;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class UnresolvedScopeAliasReconcilerSchedulerJobConfigurationTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetJobExecutorUnsafeRunnable() throws Exception {
		AtomicInteger atomicInteger = new AtomicInteger();

		UnresolvedScopeAliasReconcilerSchedulerJobConfiguration
			unresolvedScopeAliasReconcilerSchedulerJobConfiguration =
				new UnresolvedScopeAliasReconcilerSchedulerJobConfiguration();

		ReflectionTestUtil.setFieldValue(
			unresolvedScopeAliasReconcilerSchedulerJobConfiguration,
			"_unresolvedScopeAliasReconciler",
			(UnresolvedScopeAliasReconciler)() -> {
				atomicInteger.incrementAndGet();

				return true;
			});

		UnsafeRunnable<Exception> unsafeRunnable =
			unresolvedScopeAliasReconcilerSchedulerJobConfiguration.
				getJobExecutorUnsafeRunnable();

		unsafeRunnable.run();

		Assert.assertEquals(1, atomicInteger.get());
	}

	@Test
	public void testGetTriggerConfiguration() {
		UnresolvedScopeAliasReconcilerSchedulerJobConfiguration
			unresolvedScopeAliasReconcilerSchedulerJobConfiguration =
				new UnresolvedScopeAliasReconcilerSchedulerJobConfiguration();

		TriggerConfiguration triggerConfiguration =
			unresolvedScopeAliasReconcilerSchedulerJobConfiguration.
				getTriggerConfiguration();

		Assert.assertEquals(5, triggerConfiguration.getInterval());
		Assert.assertEquals(
			TimeUnit.MINUTE, triggerConfiguration.getTimeUnit());
	}

}