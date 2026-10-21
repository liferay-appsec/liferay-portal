/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;
import com.liferay.portal.security.audit.storage.model.AuditEventPseudonymField;
import com.liferay.portal.security.audit.storage.service.AuditEventPseudonymFieldLocalService;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class DefaultAuditRouterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company = CompanyTestUtil.addCompany();

		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			AuditMessageProcessor.class, _auditMessages::add,
			HashMapDictionaryBuilder.<String, Object>put(
				"eventTypes", "*"
			).build());

		_pid = ConfigurationTestUtil.createFactoryConfiguration(
			_FACTORY_PID, _getProperties(true));
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationTestUtil.deleteFactoryConfiguration(_pid, _FACTORY_PID);

		_serviceRegistration.unregister();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRoute() throws Exception {
		_testRoute();
		_testRouteWhenCompanyConfigurationIsDeleted();
		_testRouteWhenFeatureFlagIsDisabled();
		_testRouteWhenPseudonymizationFails();
		_testRouteWhenPseudonymizationIsDisabled();
		_testRouteWithUnmatchedValues();
		_testRouteWithUserIdUserLogin();
	}

	private void _assertAuditEventPseudonymField(
		String name, String pseudonym, String value) {

		AuditEventPseudonymField auditEventPseudonymField =
			_auditEventPseudonymFieldLocalService.fetchAuditEventPseudonymField(
				GetterUtil.getLong(pseudonym));

		Assert.assertEquals(
			_company.getCompanyId(), auditEventPseudonymField.getCompanyId());
		Assert.assertEquals(name, auditEventPseudonymField.getName());
		Assert.assertEquals(value, auditEventPseudonymField.getValue());
	}

	private AuditMessage _createAuditMessage() {
		long userId = RandomTestUtil.randomLong();

		AuditMessage auditMessage = new AuditMessage(
			_company.getCompanyId(), userId, RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		auditMessage.setClassName(RandomTestUtil.randomString());
		auditMessage.setClassPK(userId);

		String clientIP = RandomTestUtil.randomString();

		auditMessage.setClientHost(clientIP);
		auditMessage.setClientIP(clientIP);

		auditMessage.setImpersonatedUserEmailAddress(
			RandomTestUtil.randomString());
		auditMessage.setImpersonatedUserId(RandomTestUtil.randomLong());
		auditMessage.setImpersonatedUserName(RandomTestUtil.randomString());
		auditMessage.setMessage(RandomTestUtil.randomString());
		auditMessage.setObjectName(RandomTestUtil.randomString());

		String userEmailAddress = RandomTestUtil.randomString();

		auditMessage.setUserEmailAddress(userEmailAddress);
		auditMessage.setUserLogin(userEmailAddress);

		return auditMessage;
	}

	private long _getAuditEventPseudonymFieldsCount() {
		DynamicQuery dynamicQuery =
			_auditEventPseudonymFieldLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("companyId", _company.getCompanyId()));

		return _auditEventPseudonymFieldLocalService.dynamicQueryCount(
			dynamicQuery);
	}

	private Dictionary<String, Object> _getProperties(
		boolean pseudonymizationEnabled) {

		return HashMapDictionaryBuilder.<String, Object>put(
			"companyId", _company.getCompanyId()
		).put(
			"pseudonymizationEnabled", pseudonymizationEnabled
		).build();
	}

	private void _testRoute() throws Exception {
		AuditMessage auditMessage = _createAuditMessage();

		AuditMessage originalAuditMessage = new AuditMessage(
			String.valueOf(auditMessage.toJSONObject()));

		long count = _getAuditEventPseudonymFieldsCount();

		_auditRouter.route(auditMessage);

		Assert.assertTrue(_auditMessages.contains(auditMessage));
		Assert.assertEquals(
			originalAuditMessage.getClassPK(), auditMessage.getClassPK());
		Assert.assertEquals(
			auditMessage.getClientIP(), auditMessage.getClientHost());
		Assert.assertEquals(
			originalAuditMessage.getImpersonatedUserId(),
			auditMessage.getImpersonatedUserId());
		Assert.assertTrue(auditMessage.isPseudonymized());
		Assert.assertEquals(
			originalAuditMessage.getUserId(), auditMessage.getUserId());
		Assert.assertEquals(
			auditMessage.getUserEmailAddress(), auditMessage.getUserLogin());

		_assertAuditEventPseudonymField(
			"clientIP", auditMessage.getClientIP(),
			originalAuditMessage.getClientIP());
		_assertAuditEventPseudonymField(
			"objectName", auditMessage.getObjectName(),
			originalAuditMessage.getObjectName());
		_assertAuditEventPseudonymField(
			"userEmailAddress", auditMessage.getImpersonatedUserEmailAddress(),
			originalAuditMessage.getImpersonatedUserEmailAddress());
		_assertAuditEventPseudonymField(
			"userEmailAddress", auditMessage.getUserEmailAddress(),
			originalAuditMessage.getUserEmailAddress());
		_assertAuditEventPseudonymField(
			"userName", auditMessage.getImpersonatedUserName(),
			originalAuditMessage.getImpersonatedUserName());
		_assertAuditEventPseudonymField(
			"userName", auditMessage.getUserName(),
			originalAuditMessage.getUserName());

		Assert.assertEquals(count + 6, _getAuditEventPseudonymFieldsCount());

		String json = String.valueOf(auditMessage.toJSONObject());

		_auditRouter.route(auditMessage);

		JSONAssert.assertEquals(
			json, String.valueOf(auditMessage.toJSONObject()),
			JSONCompareMode.STRICT);

		_auditRouter.route(originalAuditMessage);

		Assert.assertEquals(count + 6, _getAuditEventPseudonymFieldsCount());
		JSONAssert.assertEquals(
			json, String.valueOf(originalAuditMessage.toJSONObject()),
			JSONCompareMode.STRICT);
	}

	private void _testRouteWhenCompanyConfigurationIsDeleted()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					AuditConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"pseudonymizationEnabled", false
					).build())) {

			AuditMessage auditMessage = _createAuditMessage();

			_auditRouter.route(auditMessage);

			Assert.assertTrue(auditMessage.isPseudonymized());

			ConfigurationTestUtil.deleteFactoryConfiguration(
				_pid, _FACTORY_PID);

			auditMessage = _createAuditMessage();

			String json = String.valueOf(auditMessage.toJSONObject());

			_auditRouter.route(auditMessage);

			JSONAssert.assertEquals(
				json, String.valueOf(auditMessage.toJSONObject()),
				JSONCompareMode.STRICT);
		}

		_pid = ConfigurationTestUtil.createFactoryConfiguration(
			_FACTORY_PID, _getProperties(true));
	}

	private void _testRouteWhenFeatureFlagIsDisabled() throws Exception {
		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-6417")) {

			AuditMessage auditMessage = _createAuditMessage();

			String json = String.valueOf(auditMessage.toJSONObject());

			_auditRouter.route(auditMessage);

			Assert.assertTrue(_auditMessages.contains(auditMessage));
			JSONAssert.assertEquals(
				json, String.valueOf(auditMessage.toJSONObject()),
				JSONCompareMode.STRICT);
		}
	}

	private void _testRouteWhenPseudonymizationFails() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<?> serviceRegistration =
			bundleContext.registerService(
				ModelListener.class,
				new BaseModelListener<AuditEventPseudonymField>() {

					@Override
					public void onBeforeCreate(
							AuditEventPseudonymField auditEventPseudonymField)
						throws ModelListenerException {

						throw new ModelListenerException();
					}

				},
				null);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.security.audit.router.internal." +
					"AuditMessagePseudonymizer",
				LoggerTestUtil.ERROR)) {

			AuditMessage auditMessage = _createAuditMessage();

			AuditMessage originalAuditMessage = new AuditMessage(
				String.valueOf(auditMessage.toJSONObject()));

			long count = _getAuditEventPseudonymFieldsCount();

			_auditRouter.route(auditMessage);

			Assert.assertTrue(_auditMessages.contains(auditMessage));
			Assert.assertEquals(
				originalAuditMessage.getClassPK(), auditMessage.getClassPK());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getClientHost());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getClientIP());
			Assert.assertEquals(
				StringPool.BLANK,
				auditMessage.getImpersonatedUserEmailAddress());
			Assert.assertEquals(
				originalAuditMessage.getImpersonatedUserId(),
				auditMessage.getImpersonatedUserId());
			Assert.assertEquals(
				StringPool.BLANK, auditMessage.getImpersonatedUserName());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getObjectName());
			Assert.assertTrue(auditMessage.isPseudonymized());
			Assert.assertEquals(
				StringPool.BLANK, auditMessage.getUserEmailAddress());
			Assert.assertEquals(
				originalAuditMessage.getUserId(), auditMessage.getUserId());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getUserLogin());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getUserName());

			Assert.assertEquals(count, _getAuditEventPseudonymFieldsCount());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to pseudonymize the audit message",
				logEntry.getMessage());
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private void _testRouteWhenPseudonymizationIsDisabled() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_pid, _getProperties(false))) {

			AuditMessage auditMessage = _createAuditMessage();

			String json = String.valueOf(auditMessage.toJSONObject());

			_auditRouter.route(auditMessage);

			Assert.assertTrue(_auditMessages.contains(auditMessage));
			JSONAssert.assertEquals(
				json, String.valueOf(auditMessage.toJSONObject()),
				JSONCompareMode.STRICT);
		}
	}

	private void _testRouteWithUnmatchedValues() throws Exception {
		AuditMessage auditMessage = new AuditMessage(
			_company.getCompanyId(), RandomTestUtil.randomLong(),
			StringPool.BLANK, RandomTestUtil.randomString());

		auditMessage.setClientHost(RandomTestUtil.randomString());
		auditMessage.setClientIP(RandomTestUtil.randomString());
		auditMessage.setUserLogin(RandomTestUtil.randomString());

		long count = _getAuditEventPseudonymFieldsCount();

		_auditRouter.route(auditMessage);

		Assert.assertEquals(StringPool.BLANK, auditMessage.getClientHost());
		Assert.assertEquals(StringPool.BLANK, auditMessage.getUserLogin());

		Assert.assertEquals(count + 1, _getAuditEventPseudonymFieldsCount());
	}

	private void _testRouteWithUserIdUserLogin() throws Exception {
		AuditMessage auditMessage = _createAuditMessage();

		String userLogin = String.valueOf(auditMessage.getUserId());

		auditMessage.setUserLogin(userLogin);

		_auditRouter.route(auditMessage);

		Assert.assertEquals(userLogin, auditMessage.getUserLogin());
	}

	private static final String _FACTORY_PID =
		AuditConfiguration.class.getName() + ".scoped";

	@Inject
	private AuditEventPseudonymFieldLocalService
		_auditEventPseudonymFieldLocalService;

	private final List<AuditMessage> _auditMessages = new ArrayList<>();

	@Inject
	private AuditRouter _auditRouter;

	@DeleteAfterTestRun
	private Company _company;

	private String _pid;
	private ServiceRegistration<AuditMessageProcessor> _serviceRegistration;

}