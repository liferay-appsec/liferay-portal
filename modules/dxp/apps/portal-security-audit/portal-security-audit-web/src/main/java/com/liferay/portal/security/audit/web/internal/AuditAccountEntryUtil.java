/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.web.internal;

import com.liferay.account.constants.AccountRoleConstants;
import com.liferay.account.model.AccountRole;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.account.service.AccountRoleLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.RoleLocalService;

/**
 * @author Regisson Aguiar
 */
public class AuditAccountEntryUtil {

	public static long[] getAdministeredAccountEntryIds(
			PermissionChecker permissionChecker)
		throws PortalException {

		if (!FeatureFlagManagerUtil.isEnabled(
				permissionChecker.getCompanyId(), "LPD-113659")) {

			return new long[0];
		}

		RoleLocalService roleLocalService = _roleLocalServiceSnapshot.get();

		Role role = roleLocalService.getRole(
			permissionChecker.getCompanyId(),
			AccountRoleConstants.REQUIRED_ROLE_NAME_ACCOUNT_ADMINISTRATOR);

		AccountRoleLocalService accountRoleLocalService =
			_accountRoleLocalServiceSnapshot.get();

		AccountRole accountRole =
			accountRoleLocalService.getAccountRoleByRoleId(role.getRoleId());

		AccountEntryUserRelLocalService accountEntryUserRelLocalService =
			_accountEntryUserRelLocalServiceSnapshot.get();

		return TransformUtil.unsafeTransformToLongArray(
			accountEntryUserRelLocalService.
				getAccountEntryUserRelsByAccountUserId(
					permissionChecker.getUserId()),
			accountEntryUserRel -> {
				if (!accountRoleLocalService.hasUserAccountRole(
						accountEntryUserRel.getAccountEntryId(),
						accountRole.getAccountRoleId(),
						permissionChecker.getUserId())) {

					return null;
				}

				return accountEntryUserRel.getAccountEntryId();
			});
	}

	private static final Snapshot<AccountEntryUserRelLocalService>
		_accountEntryUserRelLocalServiceSnapshot = new Snapshot<>(
			AuditAccountEntryUtil.class, AccountEntryUserRelLocalService.class);
	private static final Snapshot<AccountRoleLocalService>
		_accountRoleLocalServiceSnapshot = new Snapshot<>(
			AuditAccountEntryUtil.class, AccountRoleLocalService.class);
	private static final Snapshot<RoleLocalService> _roleLocalServiceSnapshot =
		new Snapshot<>(AuditAccountEntryUtil.class, RoleLocalService.class);

}