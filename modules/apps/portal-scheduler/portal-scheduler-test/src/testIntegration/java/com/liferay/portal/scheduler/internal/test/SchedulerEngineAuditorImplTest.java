/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.scheduler.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRequestThreadLocal;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.scheduler.SchedulerEngine;
import com.liferay.portal.kernel.scheduler.SchedulerEngineAuditor;
import com.liferay.portal.kernel.scheduler.TriggerState;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Álvaro Saugar
 */
@RunWith(Arquillian.class)
public class SchedulerEngineAuditorImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() {
		AuditRequestThreadLocal.removeAuditThreadLocal();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testAuditSchedulerJobs() throws Exception {
		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		String clientIP = RandomTestUtil.randomString();
		String sessionID = RandomTestUtil.randomString();

		auditRequestThreadLocal.setClientIP(clientIP);
		auditRequestThreadLocal.setSessionID(sessionID);

		AuditMessage auditMessage = _auditSchedulerJobs();

		Assert.assertEquals(clientIP, auditMessage.getClientIP());
		Assert.assertEquals(sessionID, auditMessage.getSessionID());

		Assert.assertNull(auditMessage.getRequestId());
	}

	private AuditMessage _auditSchedulerJobs() throws Exception {
		List<AuditMessage> auditMessages = new ArrayList<>();

		Bundle bundle = FrameworkUtil.getBundle(
			SchedulerEngineAuditorImplTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<AuditMessageProcessor> serviceRegistration =
			bundleContext.registerService(
				AuditMessageProcessor.class, auditMessages::add,
				HashMapDictionaryBuilder.<String, Object>put(
					"eventTypes", SchedulerEngine.SCHEDULER
				).build());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					"com.liferay.portal.scheduler.internal.configuration." +
						"SchedulerEngineHelperConfiguration",
					HashMapDictionaryBuilder.<String, Object>put(
						"auditSchedulerJobEnabled", true
					).build())) {

			_schedulerEngineAuditor.auditSchedulerJobs(
				new Message(), TriggerState.COMPLETE);
		}
		finally {
			serviceRegistration.unregister();
		}

		Assert.assertEquals(auditMessages.toString(), 1, auditMessages.size());

		return auditMessages.get(0);
	}

	@Inject
	private SchedulerEngineAuditor _schedulerEngineAuditor;

}