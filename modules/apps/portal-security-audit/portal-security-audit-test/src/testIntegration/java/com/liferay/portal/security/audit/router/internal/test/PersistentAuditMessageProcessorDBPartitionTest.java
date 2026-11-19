/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.feature.flag.test.util.FeatureFlagTestHelper;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.AssumeTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorCompanyConfiguration;
import com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorConfiguration;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lucas Miranda
 */
@RunWith(Arquillian.class)
public class PersistentAuditMessageProcessorDBPartitionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new AssumeTestRule("assume"), new LiferayIntegrationTestRule());

	public static void assume() {
		Assume.assumeTrue(PropsValues.DATABASE_PARTITION_ENABLED);
	}

	@Test
	public void testProcess() throws Exception {
		_company = CompanyTestUtil.addCompany();

		FeatureFlagTestHelper featureFlagTestHelper =
			new FeatureFlagTestHelper();

		boolean featureFlagValue = featureFlagTestHelper.getFeatureFlagValue(
			_company.getCompanyId(), "LPD-114718");

		featureFlagTestHelper.setFeatureFlagValue(
			_company.getCompanyId(), "LPD-114718", true);

		try {
			String className =
				PersistentAuditMessageProcessorCompanyConfiguration.class.
					getName();

			String factoryPid = className + ".scoped";

			String pid = ConfigurationTestUtil.createFactoryConfiguration(
				factoryPid,
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", _company.getCompanyId()
				).put(
					"enabled", false
				).build());

			try (ConfigurationTemporarySwapper configurationTemporarySwapper =
					new ConfigurationTemporarySwapper(
						PersistentAuditMessageProcessorConfiguration.class.
							getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"bufferSize", 1
						).build())) {

				String eventType = StringUtil.toUpperCase(
					RandomTestUtil.randomString());

				_route(_company.getCompanyId(), eventType);
				_route(TestPropsValues.getCompanyId(), eventType);

				Assert.assertEquals(
					0,
					_getAuditEventsCount(_company.getCompanyId(), eventType));
				Assert.assertEquals(
					1,
					_getAuditEventsCount(
						TestPropsValues.getCompanyId(), eventType));

				ConfigurationTestUtil.deleteFactoryConfiguration(
					pid, factoryPid);

				_route(_company.getCompanyId(), eventType);

				Assert.assertEquals(
					1,
					_getAuditEventsCount(_company.getCompanyId(), eventType));
				Assert.assertEquals(
					1,
					_getAuditEventsCount(
						TestPropsValues.getCompanyId(), eventType));
			}
		}
		finally {
			featureFlagTestHelper.setFeatureFlagValue(
				_company.getCompanyId(), "LPD-114718", featureFlagValue);

			featureFlagTestHelper.tearDown();
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

	private void _route(long companyId, String eventType) throws Exception {
		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			_auditRouter.route(
				new AuditMessage(
					companyId, RandomTestUtil.randomLong(),
					RandomTestUtil.randomString(), eventType));
		}
	}

	@Inject
	private AuditEventLocalService _auditEventLocalService;

	@Inject
	private AuditRouter _auditRouter;

	@DeleteAfterTestRun
	private Company _company;

}