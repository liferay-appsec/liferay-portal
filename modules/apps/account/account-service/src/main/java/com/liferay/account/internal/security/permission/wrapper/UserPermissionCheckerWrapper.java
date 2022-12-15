/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.account.internal.security.permission.wrapper;

import com.liferay.account.constants.AccountActionKeys;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountEntryUserRel;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.wrapper.PermissionCheckerWrapper;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.function.Supplier;

/**
 * @author Pei-Jung Lan
 */
public class UserPermissionCheckerWrapper extends PermissionCheckerWrapper {

	public UserPermissionCheckerWrapper(
		PermissionChecker permissionChecker,
		ModelResourcePermission<AccountEntry>
			accountEntryModelResourcePermission,
		AccountEntryUserRelLocalService accountEntryUserRelLocalService,
		UserLocalService userLocalService) {

		super(permissionChecker);

		_permissionChecker = permissionChecker;
		_accountEntryModelResourcePermission =
			accountEntryModelResourcePermission;
		_accountEntryUserRelLocalService = accountEntryUserRelLocalService;
		_userLocalService = userLocalService;
	}

	@Override
	public boolean hasPermission(
		Group group, String name, long primKey, String actionId) {

		return _hasPermission(
			name, primKey, actionId,
			() -> super.hasPermission(group, name, primKey, actionId));
	}

	@Override
	public boolean hasPermission(
		Group group, String name, String primKey, String actionId) {

		return _hasPermission(
			name, GetterUtil.getLong(primKey), actionId,
			() -> super.hasPermission(group, name, primKey, actionId));
	}

	@Override
	public boolean hasPermission(
		long groupId, String name, long primKey, String actionId) {

		return _hasPermission(
			name, primKey, actionId,
			() -> super.hasPermission(groupId, name, primKey, actionId));
	}

	@Override
	public boolean hasPermission(
		long groupId, String name, String primKey, String actionId) {

		return _hasPermission(
			name, GetterUtil.getLong(primKey), actionId,
			() -> super.hasPermission(groupId, name, primKey, actionId));
	}

	private boolean _hasPermission(
		String name, long primKey, String actionId,
		Supplier<Boolean> hasPermissionSupplier) {

		if (!StringUtil.equals(name, User.class.getName()) ||
			!StringUtil.equals(actionId, ActionKeys.IMPERSONATE)) {

			return hasPermissionSupplier.get();
		}

		User user = _userLocalService.fetchUser(primKey);

		if (user == null) {
			return hasPermissionSupplier.get();
		}

		PermissionChecker userPermissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		if (userPermissionChecker.hasPermission(
				0, Role.class.getName(), 0, ActionKeys.DEFINE_PERMISSIONS) ||
			userPermissionChecker.hasPermission(
				0, User.class.getName(), 0, ActionKeys.UPDATE)) {

			return false;
		}

		try {
			for (AccountEntryUserRel accountEntryUserRel :
					_accountEntryUserRelLocalService.
						getAccountEntryUserRelsByAccountUserId(primKey)) {

				if (_accountEntryModelResourcePermission.contains(
						_permissionChecker,
						accountEntryUserRel.getAccountEntryId(),
						AccountActionKeys.IMPERSONATE_USERS)) {

					return true;
				}
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return hasPermissionSupplier.get();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UserPermissionCheckerWrapper.class);

	private volatile ModelResourcePermission<AccountEntry>
		_accountEntryModelResourcePermission;
	private final AccountEntryUserRelLocalService
		_accountEntryUserRelLocalService;
	private final PermissionChecker _permissionChecker;
	private final UserLocalService _userLocalService;

}