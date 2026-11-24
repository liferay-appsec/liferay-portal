/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.web.internal.portlet.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.constants.AccountRoleConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountRole;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.account.service.AccountRoleLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.security.audit.AuditEvent;
import com.liferay.portal.security.audit.AuditEventManager;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.Portlet;
import jakarta.portlet.PortletException;
import jakarta.portlet.PortletRequest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Regisson Aguiar
 */
@RunWith(Arquillian.class)
public class AuditPortletTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testCheckPermission() throws Exception {
		AccountEntry accountEntry1 = _addAccountEntry();

		User user1 = _addUser(
			accountEntry1,
			AccountRoleConstants.REQUIRED_ROLE_NAME_ACCOUNT_ADMINISTRATOR);

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					true, "LPD-113659")) {

			User user2 = _addUser(
				accountEntry1,
				AccountRoleConstants.REQUIRED_ROLE_NAME_ACCOUNT_MEMBER);

			_assertCheckPermission(0, false, user2);

			_assertCheckPermission(0, true, TestPropsValues.getUser());
			_assertCheckPermission(0, true, user1);

			AuditEvent auditEvent1 = _addAuditEvent(
				accountEntry1.getAccountEntryId());

			_assertCheckPermission(auditEvent1.getAuditEventId(), true, user1);

			AuditEvent auditEvent2 = _addAuditEvent(
				0, accountEntry1.getAccountEntryId());

			_assertCheckPermission(auditEvent2.getAuditEventId(), true, user1);

			AccountEntry accountEntry2 = _addAccountEntry();

			AuditEvent auditEvent3 = _addAuditEvent(
				accountEntry2.getAccountEntryId());

			_assertCheckPermission(auditEvent3.getAuditEventId(), false, user1);
			_assertCheckPermission(
				auditEvent3.getAuditEventId(), true, TestPropsValues.getUser());

			AuditEvent auditEvent4 = _addAuditEvent(
				0, accountEntry2.getAccountEntryId());

			_assertCheckPermission(auditEvent4.getAuditEventId(), false, user1);
		}

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-113659")) {

			_assertCheckPermission(0, false, user1);
			_assertCheckPermission(0, true, TestPropsValues.getUser());
		}
	}

	private AccountEntry _addAccountEntry() throws Exception {
		return _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			null, null, RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext());
	}

	private AuditEvent _addAuditEvent(
			long accountEntryId, long... userAccountEntryIds)
		throws Exception {

		AuditMessage auditMessage = new AuditMessage(
			TestPropsValues.getCompanyId(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		auditMessage.setAccountEntryId(accountEntryId);
		auditMessage.setUserAccountEntryIds(userAccountEntryIds);

		return _auditEventManager.addAuditEvent(auditMessage);
	}

	private User _addUser(AccountEntry accountEntry, String roleName)
		throws Exception {

		User user = UserTestUtil.addUser();

		_accountEntryUserRelLocalService.addAccountEntryUserRel(
			accountEntry.getAccountEntryId(), user.getUserId());

		Role role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), roleName);

		AccountRole accountRole =
			_accountRoleLocalService.getAccountRoleByRoleId(role.getRoleId());

		_accountRoleLocalService.associateUser(
			accountEntry.getAccountEntryId(), accountRole.getAccountRoleId(),
			user.getUserId());

		return user;
	}

	private void _assertCheckPermission(
			long auditEventId, boolean permission, User user)
		throws Exception {

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setParameter(
			"auditEventId", String.valueOf(auditEventId));

		if (permission) {
			ReflectionTestUtil.invoke(
				_portlet, "_checkPermission",
				new Class<?>[] {PortletRequest.class},
				mockLiferayPortletRenderRequest);

			return;
		}

		Assert.assertThrows(
			PortletException.class,
			() -> ReflectionTestUtil.invoke(
				_portlet, "_checkPermission",
				new Class<?>[] {PortletRequest.class},
				mockLiferayPortletRenderRequest));
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountEntryUserRelLocalService _accountEntryUserRelLocalService;

	@Inject
	private AccountRoleLocalService _accountRoleLocalService;

	@Inject
	private AuditEventManager _auditEventManager;

	@Inject(
		filter = "jakarta.portlet.name=com_liferay_portal_security_audit_web_portlet_AuditPortlet"
	)
	private Portlet _portlet;

	@Inject
	private RoleLocalService _roleLocalService;

}