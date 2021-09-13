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

package com.liferay.portal.service.resource.permission.test.util;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class RoleProxy {

	public RoleProxy(
			RoleLocalService roleLocalService, long companyId, String roleName)
		throws Exception {

		_roleLocalService = roleLocalService;
		_companyId = companyId;
		_roleName = roleName;

		_role = Mockito.mock(Role.class);
		_roleId = RandomTestUtil.randomLong();

		_mockRole();
	}

	public Role getRole() {
		return _role;
	}

	private void _mockRole() throws Exception {
		Mockito.doReturn(
			_roleId
		).when(
			_role
		).getRoleId();

		Mockito.doReturn(
			_role
		).when(
			_roleLocalService
		).getRole(
			_roleId
		);

		Mockito.doReturn(
			_role
		).when(
			_roleLocalService
		).getRole(
			_companyId, _roleName
		);

		Mockito.doReturn(
			_roleName
		).when(
			_role
		).getDescriptiveName();

		if (_roleName.equals(RoleConstants.SITE_MEMBER)) {
			Mockito.doReturn(
				RoleConstants.TYPE_SITE
			).when(
				_role
			).getType();
		}
	}

	private final long _companyId;
	private final Role _role;
	private final long _roleId;
	private final RoleLocalService _roleLocalService;
	private final String _roleName;

}