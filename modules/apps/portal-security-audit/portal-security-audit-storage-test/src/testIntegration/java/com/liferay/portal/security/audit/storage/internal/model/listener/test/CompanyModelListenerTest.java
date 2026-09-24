/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.audit.storage.model.AuditEvent;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class CompanyModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testOnAfterRemove() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		AuditEvent auditEvent = _auditEventLocalService.addAuditEvent(
			new AuditMessage(
				company.getCompanyId(), RandomTestUtil.randomLong(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString()));
		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				company.getCompanyId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		_companyLocalService.deleteCompany(company.getCompanyId());

		Assert.assertNull(
			_auditEventLocalService.fetchAuditEvent(
				auditEvent.getAuditEventId()));
		Assert.assertNull(
			_auditPseudonymLocalService.fetchAuditPseudonym(
				auditPseudonym.getAuditPseudonymId()));
	}

	@Inject
	private AuditEventLocalService _auditEventLocalService;

	@Inject
	private AuditPseudonymLocalService _auditPseudonymLocalService;

	@Inject
	private CompanyLocalService _companyLocalService;

}