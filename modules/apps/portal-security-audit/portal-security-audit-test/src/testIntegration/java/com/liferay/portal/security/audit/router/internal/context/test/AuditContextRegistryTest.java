/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.constants.AuditEventConstants;
import com.liferay.portal.security.audit.context.AuditContext;
import com.liferay.portal.security.audit.context.AuditContextRegistry;
import com.liferay.portal.security.audit.context.AuditDeliveryPath;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Collections;
import java.util.List;

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
public class AuditContextRegistryTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetAuditContext() throws Exception {
		AuditContext auditContext = _auditContextRegistry.getAuditContext(
			AuditEventConstants.CONTEXT_NAME_INSTANCE);

		Assert.assertEquals(
			Collections.singleton(AuditDeliveryPath.DATABASE),
			auditContext.getDefaultAuditDeliveryPaths());
		Assert.assertEquals(1825, auditContext.getMaximumRetentionDays());
		Assert.assertEquals(30, auditContext.getMinimumRetentionDays());
		Assert.assertEquals("audit.read", auditContext.getOAuth2ScopeName());
		Assert.assertEquals(
			Collections.emptySet(),
			auditContext.getRequiredAuditDeliveryPaths());
		Assert.assertEquals(
			Collections.emptySet(), auditContext.getResourceActions());

		Assert.assertNull(_auditContextRegistry.getAuditContext(null));
		Assert.assertNull(
			_auditContextRegistry.getAuditContext(
				RandomTestUtil.randomString()));

		String name = StringUtil.toUpperCase(RandomTestUtil.randomString());

		auditContext = _getAuditContext(name);

		ServiceRegistration<AuditContext> serviceRegistration =
			_registerAuditContext(auditContext, name, 0);

		try {
			Assert.assertSame(
				auditContext, _auditContextRegistry.getAuditContext(name));

			AuditContext higherRankingAuditContext = _getAuditContext(name);

			ServiceRegistration<AuditContext> higherRankingServiceRegistration =
				_registerAuditContext(higherRankingAuditContext, name, 1);

			try {
				Assert.assertSame(
					higherRankingAuditContext,
					_auditContextRegistry.getAuditContext(name));
			}
			finally {
				higherRankingServiceRegistration.unregister();
			}

			Assert.assertSame(
				auditContext, _auditContextRegistry.getAuditContext(name));
		}
		finally {
			serviceRegistration.unregister();
		}

		Assert.assertNull(_auditContextRegistry.getAuditContext(name));

		_testGetAuditContextWithInvalidAuditContext(
			_getAuditContext(AuditEventConstants.CONTEXT_NAME_INSTANCE),
			AuditEventConstants.CONTEXT_NAME_INSTANCE, "its name is reserved");
		_testGetAuditContextWithInvalidAuditContext(
			_getAuditContext(name), StringUtil.toLowerCase(name),
			"its name does not match [A-Z0-9_]+");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.emptySet(), 30, 30, RandomTestUtil.randomString(),
				Collections.emptySet(), Collections.emptySet()),
			name, "it has no default delivery path");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.singleton(AuditDeliveryPath.DATABASE), 29, 30,
				RandomTestUtil.randomString(), Collections.emptySet(),
				Collections.emptySet()),
			name, "its maximum retention is less than its minimum retention");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.singleton(AuditDeliveryPath.DATABASE), 30, 0,
				RandomTestUtil.randomString(), Collections.emptySet(),
				Collections.emptySet()),
			name, "its minimum retention is less than 1 day");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.singleton(AuditDeliveryPath.DATABASE), 30, 30,
				RandomTestUtil.randomString(), Collections.emptySet(),
				Collections.singleton("instance.user.add")),
			name, "resource action instance.user.add does not belong to it");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.singleton(AuditDeliveryPath.DATABASE), 30, 30,
				RandomTestUtil.randomString(),
				Collections.singleton(AuditDeliveryPath.FILESYSTEM),
				Collections.emptySet()),
			name, "a required delivery path is not a default delivery path");
		_testGetAuditContextWithInvalidAuditContext(
			new TestAuditContext(
				Collections.singleton(AuditDeliveryPath.DATABASE), 30, 30, null,
				Collections.emptySet(), Collections.emptySet()),
			name, "it has no OAuth 2 scope name");
	}

	private AuditContext _getAuditContext(String name) {
		return new TestAuditContext(
			Collections.singleton(AuditDeliveryPath.DATABASE), 730, 30,
			"audit." + StringUtil.toLowerCase(name) + ".read",
			Collections.singleton(AuditDeliveryPath.DATABASE),
			Collections.singleton(StringUtil.toLowerCase(name) + ".agent.run"));
	}

	private ServiceRegistration<AuditContext> _registerAuditContext(
		AuditContext auditContext, String name, int serviceRanking) {

		Bundle bundle = FrameworkUtil.getBundle(AuditContextRegistryTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		return bundleContext.registerService(
			AuditContext.class, auditContext,
			HashMapDictionaryBuilder.<String, Object>put(
				"audit.context.name", name
			).put(
				"service.ranking", serviceRanking
			).build());
	}

	private void _testGetAuditContextWithInvalidAuditContext(
		AuditContext auditContext, String name, String reason) {

		AuditContext registeredAuditContext =
			_auditContextRegistry.getAuditContext(name);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.security.audit.router.internal.context." +
					"AuditContextRegistryImpl",
				LoggerTestUtil.WARN)) {

			ServiceRegistration<AuditContext> serviceRegistration =
				_registerAuditContext(auditContext, name, 0);

			try {
				Assert.assertSame(
					registeredAuditContext,
					_auditContextRegistry.getAuditContext(name));

				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					StringBundler.concat(
						"Ignoring audit context ", name, " because ", reason),
					logEntry.getMessage());
			}
			finally {
				serviceRegistration.unregister();
			}
		}

		Assert.assertSame(
			registeredAuditContext,
			_auditContextRegistry.getAuditContext(name));
	}

	@Inject
	private AuditContextRegistry _auditContextRegistry;

}