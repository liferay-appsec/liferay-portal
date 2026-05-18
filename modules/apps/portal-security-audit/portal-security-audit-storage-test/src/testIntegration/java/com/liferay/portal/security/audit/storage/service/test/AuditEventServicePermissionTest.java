/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.security.audit.storage.model.AuditEvent;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.security.audit.storage.service.AuditEventService;
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
public class AuditEventServicePermissionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		BundleContext bundleContext = FrameworkUtil.getBundle(
			AuditEventServicePermissionTest.class
		).getBundleContext();

		_accountEntryLocalService = bundleContext.getService(
			bundleContext.getServiceReference(AccountEntryLocalService.class));
		_accountEntryUserRelLocalService = bundleContext.getService(
			bundleContext.getServiceReference(
				AccountEntryUserRelLocalService.class));
		_auditEventLocalService = bundleContext.getService(
			bundleContext.getServiceReference(AuditEventLocalService.class));
		_auditEventService = bundleContext.getService(
			bundleContext.getServiceReference(AuditEventService.class));
		_userLocalService = bundleContext.getService(
			bundleContext.getServiceReference(UserLocalService.class));

		_companyId = PortalUtil.getDefaultCompanyId();
		_eventType = "TEST_" + RandomTestUtil.randomString();

		_accountEntryA = _addAccountEntry();
		_accountEntryB = _addAccountEntry();

		_accountAUser = UserTestUtil.addUser();

		_accountEntryUserRelLocalService.addAccountEntryUserRel(
			_accountEntryA.getAccountEntryId(), _accountAUser.getUserId());

		_addAuditEvent(_accountEntryA.getAccountEntryId());
		_addAuditEvent(_accountEntryB.getAccountEntryId());
		_addAuditEvent(AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT);
	}

	@Test(expected = PrincipalException.MustHavePermission.class)
	public void testAccountAdminRequestingForbiddenAccountThrows()
		throws Exception {

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_accountAUser));

		_auditEventService.getAuditEvents(
			_companyId, new long[] {_accountEntryB.getAccountEntryId()}, 0, 0,
			null, null, null, _eventType, null, null, null, null, null, 0, null,
			true, 0, Integer.MAX_VALUE);
	}

	@Test
	public void testAccountAdminSeesOnlyOwnAccountEvents() throws Exception {
		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_accountAUser));

		List<AuditEvent> auditEvents = _auditEventService.getAuditEvents(
			_companyId, new long[] {_accountEntryA.getAccountEntryId()}, 0, 0,
			null, null, null, _eventType, null, null, null, null, null, 0, null,
			true, 0, Integer.MAX_VALUE);

		for (AuditEvent auditEvent : auditEvents) {
			Assert.assertEquals(
				_accountEntryA.getAccountEntryId(),
				auditEvent.getAccountEntryId());
		}
	}

	@Test
	public void testCompanyAdminSeesAllEvents() throws Exception {
		User adminUser = _userLocalService.getUserByEmailAddress(
			_companyId, "test@liferay.com");

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(adminUser));

		List<AuditEvent> auditEvents = _auditEventService.getAuditEvents(
			_companyId, null, 0, 0, null, null, null, _eventType, null, null,
			null, null, null, 0, null, true, 0, Integer.MAX_VALUE);

		Assert.assertEquals(auditEvents.toString(), 3, auditEvents.size());
	}

	private AccountEntry _addAccountEntry() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		return _accountEntryLocalService.addAccountEntry(
			RandomTestUtil.randomString(), serviceContext.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED, serviceContext);
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

	private User _accountAUser;
	private AccountEntry _accountEntryA;
	private AccountEntry _accountEntryB;
	private AccountEntryLocalService _accountEntryLocalService;
	private AccountEntryUserRelLocalService _accountEntryUserRelLocalService;
	private AuditEventLocalService _auditEventLocalService;
	private AuditEventService _auditEventService;
	private long _companyId;
	private String _eventType;
	private UserLocalService _userLocalService;

}