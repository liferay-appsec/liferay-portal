/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class DefaultAuditRouterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		CompanyLocalServiceUtil.deleteCompany(_company.getCompanyId());
	}

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			AuditMessageProcessor.class, _auditMessages::add,
			HashMapDictionaryBuilder.<String, Object>put(
				"eventTypes", "*"
			).build());
	}

	@After
	public void tearDown() throws Exception {
		_serviceRegistration.unregister();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRoute() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			Assert.assertFalse(
				_auditMessages.contains(_route(_company.getCompanyId())));
			Assert.assertTrue(
				_auditMessages.contains(
					_route(TestPropsValues.getCompanyId())));
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper1 =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build());
			CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper2 =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			Assert.assertFalse(
				_auditMessages.contains(
					_route(TestPropsValues.getCompanyId())));
			Assert.assertTrue(
				_auditMessages.contains(_route(_company.getCompanyId())));
		}

		_testRoute(
			User.class.getName(), "AI_HUB", "ADD", "ai_hub.user.add", "user");
		_testRoute(
			User.class.getName(), null, "LOGIN", "system.user.login", "user");
		_testRoute(null, null, "ADD", "system.unknown.add", "unknown");

		AuditMessage auditMessage = _getAuditMessage(
			User.class.getName(), null, "ASSIGN");

		auditMessage.setResourceType("organization");

		_auditRouter.route(auditMessage);

		Assert.assertEquals(
			"system.organization.assign", auditMessage.getResourceAction());
		Assert.assertEquals("organization", auditMessage.getResourceType());

		auditMessage = _getAuditMessage(
			User.class.getName(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		auditMessage.setResourceAction("ai_hub.prompt.submit");

		_auditRouter.route(auditMessage);

		Assert.assertEquals(
			"ai_hub.prompt.submit", auditMessage.getResourceAction());
		Assert.assertEquals("prompt", auditMessage.getResourceType());

		auditMessage = _getAuditMessage(
			User.class.getName(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		String resourceType = RandomTestUtil.randomString();

		auditMessage.setResourceAction("ai_hub.prompt.submit");
		auditMessage.setResourceType(resourceType);

		_auditRouter.route(auditMessage);

		Assert.assertEquals(
			"ai_hub.prompt.submit", auditMessage.getResourceAction());
		Assert.assertEquals(resourceType, auditMessage.getResourceType());

		auditMessage = _route(CompanyConstants.SYSTEM);

		Assert.assertTrue(_auditMessages.contains(auditMessage));
		Assert.assertNull(auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.security.audit.router.internal." +
					"DefaultAuditRouter",
				LoggerTestUtil.WARN)) {

			auditMessage = _getAuditMessage(
				User.class.getName(), null, RandomTestUtil.randomString());

			String resourceAction = RandomTestUtil.randomString();

			auditMessage.setResourceAction(resourceAction);

			_auditRouter.route(auditMessage);

			Assert.assertEquals(
				resourceAction, auditMessage.getResourceAction());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				StringBundler.concat(
					"Resource action ", resourceAction,
					" does not match <featureContext>.<resource>.<action>"),
				logEntry.getMessage());
		}
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testRouteWhenFeatureFlagIsDisabled() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			Assert.assertTrue(
				_auditMessages.contains(_route(_company.getCompanyId())));
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build());
			ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					AuditConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", false
					).build())) {

			Assert.assertFalse(
				_auditMessages.contains(_route(_company.getCompanyId())));
		}

		AuditMessage auditMessage = _getAuditMessage(
			User.class.getName(), null, RandomTestUtil.randomString());

		auditMessage.setResourceType(RandomTestUtil.randomString());

		_auditRouter.route(auditMessage);

		Assert.assertNull(auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());
	}

	private AuditMessage _getAuditMessage(
			String className, String contextName, String eventType)
		throws Exception {

		return new AuditMessage(
			0, TestPropsValues.getCompanyId(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), null, 0, null, className,
			RandomTestUtil.randomString(), contextName, eventType, null);
	}

	private AuditMessage _route(long companyId) throws Exception {
		AuditMessage auditMessage = new AuditMessage(
			companyId, RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		_auditRouter.route(auditMessage);

		return auditMessage;
	}

	private void _testRoute(
			String className, String contextName, String eventType,
			String expectedResourceAction, String expectedResourceType)
		throws Exception {

		AuditMessage auditMessage = _getAuditMessage(
			className, contextName, eventType);

		_auditRouter.route(auditMessage);

		Assert.assertEquals(
			expectedResourceAction, auditMessage.getResourceAction());
		Assert.assertEquals(
			expectedResourceType, auditMessage.getResourceType());
	}

	private static Company _company;

	private final List<AuditMessage> _auditMessages = new ArrayList<>();

	@Inject
	private AuditRouter _auditRouter;

	private ServiceRegistration<AuditMessageProcessor> _serviceRegistration;

}