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
import com.liferay.portal.kernel.audit.AuditRequestContext;
import com.liferay.portal.kernel.audit.AuditRequestContextThreadLocal;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorConfiguration;
import com.liferay.portal.security.audit.storage.model.AuditEvent;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class PersistentAuditMessageProcessorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company1 = CompanyTestUtil.addCompany();
		_company2 = CompanyTestUtil.addCompany();
	}

	@Test
	public void testProcess() throws Exception {
		_testProcessCorrelationIdPersistsAfterClear();
		_testProcessDoesNotDropBufferedEventsOnDelete();
		_testProcessDoesNotDropBufferedEventsOnDisable();
		_testProcessWhenBufferSizesAreDifferent();
		_testProcessWhenEnabledValuesAreDifferent();
		_testProcessWhenSystemIsDisabled();
		_testProcessWhenValuesAreAbsent();
		_testProcessWhenValuesAreEqualToSystemValues();
	}

	private String _createEventType() {
		return StringUtil.toUpperCase(RandomTestUtil.randomString());
	}

	private String _createFactoryConfiguration(
			int bufferSize, long companyId, boolean enabled)
		throws Exception {

		return ConfigurationTestUtil.createFactoryConfiguration(
			_FACTORY_PID, _getProperties(bufferSize, companyId, enabled));
	}

	private AuditEvent _getAuditEvent(long companyId, String eventType) {
		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			List<AuditEvent> auditEvents =
				_auditEventLocalService.getAuditEvents(
					companyId, 0, 0, null, null, null, null, null, null, null,
					null, null, eventType, null, 0, null, false, 0, 1);

			return auditEvents.get(0);
		}
	}

	private int _getAuditEventsCount(long companyId, String eventType) {
		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			return _auditEventLocalService.getAuditEventsCount(
				companyId, 0, 0, null, null, null, null, null, null, null, null,
				null, eventType, null, 0, null, false);
		}
	}

	private Dictionary<String, Object> _getProperties(
		int bufferSize, long companyId, boolean enabled) {

		return HashMapDictionaryBuilder.<String, Object>put(
			"bufferSize", bufferSize
		).put(
			"companyId", companyId
		).put(
			"enabled", enabled
		).put(
			"flushInterval", _FLUSH_INTERVAL
		).build();
	}

	private void _route(long companyId, String eventType) throws Exception {
		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			_auditRouter.route(
				new AuditMessage(
					companyId, RandomTestUtil.randomLong(),
					RandomTestUtil.randomString(), eventType));
		}
	}

	private void _testProcessCorrelationIdPersistsAfterClear()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					PersistentAuditMessageProcessorConfiguration.class.
						getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"bufferSize", 1
					).build())) {

			_route(_company1.getCompanyId(), _createEventType());
		}

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					PersistentAuditMessageProcessorConfiguration.class.
						getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"bufferSize", 2
					).put(
						"flushInterval", _FLUSH_INTERVAL
					).build())) {

			String correlationId = RandomTestUtil.randomString();

			AuditRequestContext auditRequestContext =
				AuditRequestContextThreadLocal.getAuditRequestContext();

			auditRequestContext.setCorrelationId(correlationId);

			String eventType1 = _createEventType();

			_route(_company1.getCompanyId(), eventType1);

			auditRequestContext.setCorrelationId(null);

			Assert.assertEquals(
				0, _getAuditEventsCount(_company1.getCompanyId(), eventType1));

			String eventType2 = _createEventType();

			_route(_company1.getCompanyId(), eventType2);

			AuditEvent auditEvent1 = _getAuditEvent(
				_company1.getCompanyId(), eventType1);
			AuditEvent auditEvent2 = _getAuditEvent(
				_company1.getCompanyId(), eventType2);

			Assert.assertEquals(correlationId, auditEvent1.getCorrelationId());
			Assert.assertEquals(
				StringPool.BLANK, auditEvent2.getCorrelationId());

			AuditRequestContextThreadLocal.removeAuditRequestContext();
		}
	}

	private void _testProcessDoesNotDropBufferedEventsOnDelete()
		throws Exception {

		String pid = _createFactoryConfiguration(
			2000, _company1.getCompanyId(), true);

		String eventType = _createEventType();

		try {
			_route(_company1.getCompanyId(), eventType);

			Assert.assertEquals(
				0, _getAuditEventsCount(_company1.getCompanyId(), eventType));
		}
		finally {
			ConfigurationTestUtil.deleteFactoryConfiguration(pid, _FACTORY_PID);
		}

		Assert.assertEquals(
			1, _getAuditEventsCount(_company1.getCompanyId(), eventType));
	}

	private void _testProcessDoesNotDropBufferedEventsOnDisable()
		throws Exception {

		String pid = _createFactoryConfiguration(
			2000, _company1.getCompanyId(), true);

		try {
			String eventType = _createEventType();

			_route(_company1.getCompanyId(), eventType);

			Assert.assertEquals(
				0, _getAuditEventsCount(_company1.getCompanyId(), eventType));

			try (ConfigurationTemporarySwapper configurationTemporarySwapper =
					new ConfigurationTemporarySwapper(
						pid,
						_getProperties(
							2000, _company1.getCompanyId(), false))) {

				_route(_company1.getCompanyId(), _createEventType());

				Assert.assertEquals(
					1,
					_getAuditEventsCount(_company1.getCompanyId(), eventType));
			}
		}
		finally {
			ConfigurationTestUtil.deleteFactoryConfiguration(pid, _FACTORY_PID);
		}
	}

	private void _testProcessWhenBufferSizesAreDifferent() throws Exception {
		String pid1 = _createFactoryConfiguration(
			1, _company1.getCompanyId(), true);
		String pid2 = _createFactoryConfiguration(
			2000, _company2.getCompanyId(), true);

		try {
			String eventType1 = _createEventType();

			_route(_company1.getCompanyId(), eventType1);

			String eventType2 = _createEventType();

			_route(_company2.getCompanyId(), eventType2);

			Assert.assertEquals(
				1, _getAuditEventsCount(_company1.getCompanyId(), eventType1));
			Assert.assertEquals(
				0, _getAuditEventsCount(_company2.getCompanyId(), eventType2));
		}
		finally {
			ConfigurationTestUtil.deleteFactoryConfiguration(
				pid1, _FACTORY_PID);
			ConfigurationTestUtil.deleteFactoryConfiguration(
				pid2, _FACTORY_PID);
		}
	}

	private void _testProcessWhenEnabledValuesAreDifferent() throws Exception {
		String pid1 = _createFactoryConfiguration(
			1, _company1.getCompanyId(), true);
		String pid2 = _createFactoryConfiguration(
			1, _company2.getCompanyId(), false);

		try {
			String eventType1 = _createEventType();

			_route(_company1.getCompanyId(), eventType1);

			String eventType2 = _createEventType();

			_route(_company2.getCompanyId(), eventType2);

			Assert.assertEquals(
				1, _getAuditEventsCount(_company1.getCompanyId(), eventType1));
			Assert.assertEquals(
				0, _getAuditEventsCount(_company2.getCompanyId(), eventType2));
		}
		finally {
			ConfigurationTestUtil.deleteFactoryConfiguration(
				pid1, _FACTORY_PID);
			ConfigurationTestUtil.deleteFactoryConfiguration(
				pid2, _FACTORY_PID);
		}
	}

	private void _testProcessWhenSystemIsDisabled() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					PersistentAuditMessageProcessorConfiguration.class.
						getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"bufferSize", 1
					).put(
						"enabled", false
					).put(
						"flushInterval", _FLUSH_INTERVAL
					).build())) {

			String pid = _createFactoryConfiguration(
				1, _company1.getCompanyId(), true);

			try {
				String eventType1 = _createEventType();

				_route(_company1.getCompanyId(), eventType1);

				String eventType2 = _createEventType();

				_route(_company2.getCompanyId(), eventType2);

				Assert.assertEquals(
					1,
					_getAuditEventsCount(_company1.getCompanyId(), eventType1));
				Assert.assertEquals(
					0,
					_getAuditEventsCount(_company2.getCompanyId(), eventType2));
			}
			finally {
				ConfigurationTestUtil.deleteFactoryConfiguration(
					pid, _FACTORY_PID);
			}
		}
	}

	private void _testProcessWhenValuesAreAbsent() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					PersistentAuditMessageProcessorConfiguration.class.
						getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"bufferSize", 1
					).build())) {

			String pid = ConfigurationTestUtil.createFactoryConfiguration(
				_FACTORY_PID,
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", _company1.getCompanyId()
				).put(
					"enabled", true
				).build());

			String eventType = _createEventType();

			try {
				_route(_company1.getCompanyId(), eventType);

				Assert.assertEquals(
					0,
					_getAuditEventsCount(_company1.getCompanyId(), eventType));
			}
			finally {
				ConfigurationTestUtil.deleteFactoryConfiguration(
					pid, _FACTORY_PID);
			}

			Assert.assertEquals(
				1, _getAuditEventsCount(_company1.getCompanyId(), eventType));
		}
	}

	private void _testProcessWhenValuesAreEqualToSystemValues()
		throws Exception {

		try (ConfigurationTemporarySwapper configurationTemporarySwapper1 =
				new ConfigurationTemporarySwapper(
					PersistentAuditMessageProcessorConfiguration.class.
						getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"bufferSize", 2
					).put(
						"flushInterval", _FLUSH_INTERVAL
					).build())) {

			String pid = _createFactoryConfiguration(
				2, _company1.getCompanyId(), true);

			try {
				Map<Long, ?> companyBatchProcessors =
					ReflectionTestUtil.getFieldValue(
						_auditMessageProcessor, "_companyBatchProcessors");

				Assert.assertFalse(
					companyBatchProcessors.containsKey(
						_company1.getCompanyId()));

				String eventType = _createEventType();

				try (ConfigurationTemporarySwapper
						configurationTemporarySwapper2 =
							new ConfigurationTemporarySwapper(
								PersistentAuditMessageProcessorConfiguration.
									class.getName(),
								HashMapDictionaryBuilder.<String, Object>put(
									"bufferSize", 2000
								).put(
									"flushInterval", _FLUSH_INTERVAL
								).build())) {

					Assert.assertTrue(
						companyBatchProcessors.containsKey(
							_company1.getCompanyId()));

					_route(_company1.getCompanyId(), eventType);

					Assert.assertEquals(
						0,
						_getAuditEventsCount(
							_company1.getCompanyId(), eventType));
				}

				Assert.assertEquals(
					1,
					_getAuditEventsCount(_company1.getCompanyId(), eventType));
				Assert.assertFalse(
					companyBatchProcessors.containsKey(
						_company1.getCompanyId()));
			}
			finally {
				ConfigurationTestUtil.deleteFactoryConfiguration(
					pid, _FACTORY_PID);
			}
		}
	}

	private static final String _FACTORY_PID =
		PersistentAuditMessageProcessorConfiguration.class.getName() +
			".scoped";

	private static final long _FLUSH_INTERVAL = 86400000;

	@Inject
	private AuditEventLocalService _auditEventLocalService;

	@Inject(
		filter = "component.name=com.liferay.portal.security.audit.router.internal.PersistentAuditMessageProcessor"
	)
	private AuditMessageProcessor _auditMessageProcessor;

	@Inject
	private AuditRouter _auditRouter;

	@DeleteAfterTestRun
	private Company _company1;

	@DeleteAfterTestRun
	private Company _company2;

}