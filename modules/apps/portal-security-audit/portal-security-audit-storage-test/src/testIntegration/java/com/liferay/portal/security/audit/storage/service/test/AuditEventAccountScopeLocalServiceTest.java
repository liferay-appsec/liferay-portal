/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.security.audit.storage.model.AuditEvent;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class AuditEventAccountScopeLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		BundleContext bundleContext = FrameworkUtil.getBundle(
			AuditEventAccountScopeLocalServiceTest.class
		).getBundleContext();

		_auditEventLocalService = bundleContext.getService(
			bundleContext.getServiceReference(AuditEventLocalService.class));

		_companyId = PortalUtil.getDefaultCompanyId();
		_eventType = "TEST_" + RandomTestUtil.randomString();

		_accountEntryIdA = RandomTestUtil.randomLong();
		_accountEntryIdB = RandomTestUtil.randomLong();

		_addAuditEvent(_accountEntryIdA);
		_addAuditEvent(_accountEntryIdA);
		_addAuditEvent(_accountEntryIdB);
		_addAuditEvent(0);
	}

	@Test
	public void testFilterByMultipleAccountEntryIds() throws Exception {
		List<AuditEvent> auditEvents = _auditEventLocalService.getAuditEvents(
			_companyId, new long[] {_accountEntryIdA, _accountEntryIdB}, 0, 0,
			null, null, null, _eventType, null, null, null, null, null, 0, null,
			true, 0, Integer.MAX_VALUE);

		Assert.assertEquals(auditEvents.toString(), 3, auditEvents.size());

		for (AuditEvent auditEvent : auditEvents) {
			long accountEntryId = auditEvent.getAccountEntryId();

			Assert.assertTrue(
				"Unexpected accountEntryId " + accountEntryId,
				(accountEntryId == _accountEntryIdA) ||
				(accountEntryId == _accountEntryIdB));
		}
	}

	@Test
	public void testFilterBySingleAccountEntryId() throws Exception {
		List<AuditEvent> auditEvents = _auditEventLocalService.getAuditEvents(
			_companyId, new long[] {_accountEntryIdA}, 0, 0, null, null, null,
			_eventType, null, null, null, null, null, 0, null, true, 0,
			Integer.MAX_VALUE);

		Assert.assertEquals(auditEvents.toString(), 2, auditEvents.size());

		for (AuditEvent auditEvent : auditEvents) {
			Assert.assertEquals(
				_accountEntryIdA, auditEvent.getAccountEntryId());
		}
	}

	@Test
	public void testNoFilterReturnsAllForEventType() throws Exception {
		List<AuditEvent> auditEvents = _auditEventLocalService.getAuditEvents(
			_companyId, null, 0, 0, null, null, null, _eventType, null, null,
			null, null, null, 0, null, true, 0, Integer.MAX_VALUE);

		Assert.assertEquals(auditEvents.toString(), 4, auditEvents.size());
	}

	private void _addAuditEvent(long accountEntryId) {
		AuditEvent auditEvent = _auditEventLocalService.createAuditEvent(
			RandomTestUtil.randomLong());

		auditEvent.setCompanyId(_companyId);
		auditEvent.setAccountEntryId(accountEntryId);
		auditEvent.setEventType(_eventType);
		auditEvent.setCreateDate(new Date());

		_auditEventLocalService.addAuditEvent(auditEvent);
	}

	private long _accountEntryIdA;
	private long _accountEntryIdB;
	private AuditEventLocalService _auditEventLocalService;
	private long _companyId;
	private String _eventType;

}