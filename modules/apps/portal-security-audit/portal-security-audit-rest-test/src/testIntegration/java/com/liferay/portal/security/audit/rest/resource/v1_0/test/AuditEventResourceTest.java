/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.audit.rest.client.dto.v1_0.AuditEvent;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.test.rule.Inject;

import java.util.Date;

import org.junit.runner.RunWith;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class AuditEventResourceTest extends BaseAuditEventResourceTestCase {

	@Override
	protected AuditEvent testGetAuditEvent_addAuditEvent() throws Exception {
		return _toClientAuditEvent(_addAuditEvent());
	}

	@Override
	protected AuditEvent testGetAuditEventsPage_addAuditEvent(
			AuditEvent auditEvent)
		throws Exception {

		return _toClientAuditEvent(_addAuditEvent());
	}

	private com.liferay.portal.security.audit.storage.model.AuditEvent
			_addAuditEvent()
		throws Exception {

		com.liferay.portal.security.audit.storage.model.AuditEvent
			serviceBuilderAuditEvent = _auditEventLocalService.createAuditEvent(
				_counterLocalService.increment(
					com.liferay.portal.security.audit.storage.model.AuditEvent.
						class.getName()));

		serviceBuilderAuditEvent.setAccountEntryId(0L);
		serviceBuilderAuditEvent.setClassName(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setClassPK(
			String.valueOf(RandomTestUtil.randomLong()));
		serviceBuilderAuditEvent.setClientHost(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setClientIP(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setCompanyId(testCompany.getCompanyId());
		serviceBuilderAuditEvent.setContextName(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setCreateDate(new Date());
		serviceBuilderAuditEvent.setEventType(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setMessage(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setServerName(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setServerPort(RandomTestUtil.randomInt());
		serviceBuilderAuditEvent.setSessionID(RandomTestUtil.randomString());
		serviceBuilderAuditEvent.setUserId(TestPropsValues.getUserId());
		serviceBuilderAuditEvent.setUserName(
			TestPropsValues.getUser(
			).getFullName());

		return _auditEventLocalService.addAuditEvent(serviceBuilderAuditEvent);
	}

	private AuditEvent _toClientAuditEvent(
		com.liferay.portal.security.audit.storage.model.AuditEvent
			serviceBuilderAuditEvent) {

		AuditEvent auditEvent = new AuditEvent();

		auditEvent.setAccountId(serviceBuilderAuditEvent.getAccountEntryId());
		auditEvent.setClientHost(serviceBuilderAuditEvent.getClientHost());
		auditEvent.setClientIP(serviceBuilderAuditEvent.getClientIP());
		auditEvent.setContextName(serviceBuilderAuditEvent.getContextName());
		auditEvent.setDateCreated(serviceBuilderAuditEvent.getCreateDate());
		auditEvent.setEntityId(
			GetterUtil.getLong(serviceBuilderAuditEvent.getClassPK()));
		auditEvent.setEntityType(serviceBuilderAuditEvent.getClassName());
		auditEvent.setEventType(serviceBuilderAuditEvent.getEventType());
		auditEvent.setId(serviceBuilderAuditEvent.getAuditEventId());
		auditEvent.setMessage(serviceBuilderAuditEvent.getMessage());
		auditEvent.setServerName(serviceBuilderAuditEvent.getServerName());
		auditEvent.setServerPort(serviceBuilderAuditEvent.getServerPort());
		auditEvent.setSessionId(serviceBuilderAuditEvent.getSessionID());

		return auditEvent;
	}

	@Inject
	private AuditEventLocalService _auditEventLocalService;

	@Inject
	private CounterLocalService _counterLocalService;

}