/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.impl;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.role.AccountRolePermissionThreadLocal;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.change.tracking.CTAware;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.audit.storage.model.AuditEvent;
import com.liferay.portal.security.audit.storage.service.base.AuditEventServiceBaseImpl;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	property = {
		"json.web.service.context.name=audit",
		"json.web.service.context.path=AuditEvent"
	},
	service = AopService.class
)
@CTAware
public class AuditEventServiceImpl extends AuditEventServiceBaseImpl {

	@Override
	public List<AuditEvent> getAuditEvents(long companyId, int start, int end)
		throws PortalException {

		PermissionChecker permissionChecker = getPermissionChecker();

		if (_hasCrossAccountAuditViewPermission(companyId, permissionChecker)) {
			return auditEventLocalService.getAuditEvents(companyId, start, end);
		}

		return auditEventLocalService.getAuditEvents(
			companyId, _getAllowedAccountEntryIds(permissionChecker), 0, 0,
			null, null, null, null, null, null, null, null, null, 0, null, true,
			start, end);
	}

	@Override
	public List<AuditEvent> getAuditEvents(
			long companyId, int start, int end,
			OrderByComparator<AuditEvent> orderByComparator)
		throws PortalException {

		PermissionChecker permissionChecker = getPermissionChecker();

		if (_hasCrossAccountAuditViewPermission(companyId, permissionChecker)) {
			return auditEventLocalService.getAuditEvents(
				companyId, start, end, orderByComparator);
		}

		return auditEventLocalService.getAuditEvents(
			companyId, _getAllowedAccountEntryIds(permissionChecker), 0, 0,
			null, null, null, null, null, null, null, null, null, 0, null, true,
			start, end, orderByComparator);
	}

	@Override
	public List<AuditEvent> getAuditEvents(
			long companyId, long[] accountEntryIds, long groupId, long userId,
			String userName, Date createDateGT, Date createDateLT,
			String eventType, String className, String classPK,
			String clientHost, String clientIP, String serverName,
			int serverPort, String sessionID, boolean andSearch, int start,
			int end)
		throws PortalException {

		return auditEventLocalService.getAuditEvents(
			companyId, _filterAccountEntryIds(companyId, accountEntryIds),
			groupId, userId, userName, createDateGT, createDateLT, eventType,
			className, classPK, clientHost, clientIP, serverName, serverPort,
			sessionID, andSearch, start, end);
	}

	@Override
	public List<AuditEvent> getAuditEvents(
			long companyId, long[] accountEntryIds, long groupId, long userId,
			String userName, Date createDateGT, Date createDateLT,
			String eventType, String className, String classPK,
			String clientHost, String clientIP, String serverName,
			int serverPort, String sessionID, boolean andSearch, int start,
			int end, OrderByComparator<AuditEvent> orderByComparator)
		throws PortalException {

		return auditEventLocalService.getAuditEvents(
			companyId, _filterAccountEntryIds(companyId, accountEntryIds),
			groupId, userId, userName, createDateGT, createDateLT, eventType,
			className, classPK, clientHost, clientIP, serverName, serverPort,
			sessionID, andSearch, start, end, orderByComparator);
	}

	@Override
	public int getAuditEventsCount(long companyId) throws PortalException {
		PermissionChecker permissionChecker = getPermissionChecker();

		if (_hasCrossAccountAuditViewPermission(companyId, permissionChecker)) {
			return auditEventLocalService.getAuditEventsCount(companyId);
		}

		return auditEventLocalService.getAuditEventsCount(
			companyId, _getAllowedAccountEntryIds(permissionChecker), 0, 0,
			null, null, null, null, null, null, null, null, null, 0, null,
			true);
	}

	@Override
	public int getAuditEventsCount(
			long companyId, long[] accountEntryIds, long groupId, long userId,
			String userName, Date createDateGT, Date createDateLT,
			String eventType, String className, String classPK,
			String clientHost, String clientIP, String serverName,
			int serverPort, String sessionID, boolean andSearch)
		throws PortalException {

		return auditEventLocalService.getAuditEventsCount(
			companyId, _filterAccountEntryIds(companyId, accountEntryIds),
			groupId, userId, userName, createDateGT, createDateLT, eventType,
			className, classPK, clientHost, clientIP, serverName, serverPort,
			sessionID, andSearch);
	}

	private long[] _filterAccountEntryIds(
			long companyId, long[] requestedAccountEntryIds)
		throws PortalException {

		PermissionChecker permissionChecker = getPermissionChecker();

		if (_hasCrossAccountAuditViewPermission(companyId, permissionChecker)) {
			return requestedAccountEntryIds;
		}

		long[] allowedAccountEntryIds = _getAllowedAccountEntryIds(
			permissionChecker);

		if (ArrayUtil.isEmpty(requestedAccountEntryIds)) {
			return allowedAccountEntryIds;
		}

		for (long requestedAccountEntryId : requestedAccountEntryIds) {
			if (!ArrayUtil.contains(
					allowedAccountEntryIds, requestedAccountEntryId)) {

				throw new PrincipalException.MustHavePermission(
					permissionChecker.getUserId(), AccountEntry.class.getName(),
					requestedAccountEntryId, "VIEW_AUDIT_LOG");
			}
		}

		return requestedAccountEntryIds;
	}

	private long[] _getAllowedAccountEntryIds(
			PermissionChecker permissionChecker)
		throws PortalException {

		long permissionAccountEntryId =
			AccountRolePermissionThreadLocal.getAccountEntryId();

		if (permissionAccountEntryId !=
				AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT) {

			return new long[] {permissionAccountEntryId};
		}

		List<AccountEntry> accountEntries =
			_accountEntryLocalService.getUserAccountEntries(
				permissionChecker.getUserId(),
				AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT, null,
				new String[] {
					AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
					AccountConstants.ACCOUNT_ENTRY_TYPE_PERSON
				},
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		if (accountEntries.isEmpty()) {
			return new long[] {AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT};
		}

		long[] accountEntryIds = new long[accountEntries.size()];

		for (int i = 0; i < accountEntries.size(); i++) {
			AccountEntry accountEntry = accountEntries.get(i);

			accountEntryIds[i] = accountEntry.getAccountEntryId();
		}

		return accountEntryIds;
	}

	private boolean _hasCrossAccountAuditViewPermission(
			long companyId, PermissionChecker permissionChecker)
		throws PortalException {

		if (permissionChecker.isCompanyAdmin(companyId)) {
			return true;
		}

		return _userLocalService.hasRoleUser(
			companyId, RoleConstants.ANALYTICS_ADMINISTRATOR,
			permissionChecker.getUserId(), true);
	}

	@Reference
	private AccountEntryLocalService _accountEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

}