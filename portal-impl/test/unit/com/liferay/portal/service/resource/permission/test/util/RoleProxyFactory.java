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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.persistence.ResourcePermissionPersistence;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class RoleProxyFactory {

	public RoleProxyFactory(
		ResourcePermissionLocalService resourcePermissionLocalService,
		RoleLocalService roleLocalService, long companyId) {

		_resourcePermissionLocalService = resourcePermissionLocalService;
		_roleLocalService = roleLocalService;
		_companyId = companyId;

		_resourcePermissionPersistence = Mockito.mock(
			ResourcePermissionPersistence.class);

		ReflectionTestUtil.setFieldValue(
			_resourcePermissionLocalService, "resourcePermissionPersistence",
			_resourcePermissionPersistence);
	}

	public Role getRole(String roleName) throws Exception {
		if (_roleProxies.containsKey(roleName)) {
			RoleProxy roleProxy = _roleProxies.get(roleName);

			return roleProxy.getRole();
		}

		RoleProxy roleProxy = new RoleProxy(
			_roleLocalService, _companyId, roleName);

		_roleProxies.put(roleName, roleProxy);

		return roleProxy.getRole();
	}

	public Role getRoleById(String roleIdString) throws Exception {
		String[] roleIdComponents = StringUtil.split(
			roleIdString, StringPool.DASH);

		long roleId;

		if (roleIdComponents.length == 2) {
			roleId = GetterUtil.getLong(roleIdComponents[1]);
		}
		else {
			roleId = GetterUtil.getLong(roleIdComponents[0]);
		}

		for (RoleProxy roleProxy : _roleProxies.values()) {
			Role role = roleProxy.getRole();

			if (role.getRoleId() == roleId) {
				return role;
			}
		}

		return null;
	}

	public void mockResourceActionWithRolesOnAsset(
			ResourceAction viewResourceAction, List<Role> roles,
			String className, String resourcePrimKey, String primKey,
			long userId)
		throws Exception {

		_mockResourcePermissionsForRoles(
			null, viewResourceAction, new ArrayList<>(), roles, className,
			resourcePrimKey, primKey, userId);

		_mockRoleLocalServiceForRoles(roles);
	}

	public void mockResourceActionWithRolesOnAsset(
			ResourceAction accessResourceAction,
			ResourceAction viewResourceAction,
			List<Role> rolesWithAccessResourceAction,
			List<Role> rolesWithViewResourceAction, String className,
			String resourcePrimKey, String primKey, long userId)
		throws Exception {

		_mockResourcePermissionsForRoles(
			accessResourceAction, viewResourceAction,
			rolesWithAccessResourceAction, rolesWithViewResourceAction,
			className, resourcePrimKey, primKey, userId);

		_mockRoleLocalServiceForRoles(rolesWithAccessResourceAction);
		_mockRoleLocalServiceForRoles(rolesWithViewResourceAction);
	}

	private void _mockResourcePermissionsForRoles(
			ResourceAction accessResourceAction,
			ResourceAction viewResourceAction, List<Role> accessRoles,
			List<Role> viewRoles, String className, String resourcePrimKey,
			String primKey, long userId)
		throws Exception {

		List<ResourcePermission> resourcePermissions = new ArrayList<>();

		resourcePermissions.addAll(
			_mockResourcePermissionsForRolesWithResourceAction(
				accessResourceAction, accessRoles, className, primKey, userId));

		resourcePermissions.addAll(
			_mockResourcePermissionsForRolesWithResourceAction(
				viewResourceAction, viewRoles, className, primKey, userId));

		Mockito.doReturn(
			resourcePermissions
		).when(
			_resourcePermissionPersistence
		).findByC_N_S_P(
			_companyId, className, _SCOPE, resourcePrimKey
		);
	}

	private List<ResourcePermission>
			_mockResourcePermissionsForRolesWithResourceAction(
				ResourceAction resourceAction, List<Role> roles,
				String className, String primKey, long userId)
		throws Exception {

		List<ResourcePermission> resourcePermissions = new ArrayList<>();

		for (Role role : roles) {
			ResourcePermission resourcePermission = Mockito.mock(
				ResourcePermission.class);

			Mockito.doReturn(
				true
			).when(
				resourcePermission
			).hasAction(
				resourceAction
			);

			Mockito.doReturn(
				role.getRoleId()
			).when(
				resourcePermission
			).getRoleId();

			Role ownerRole = getRole(RoleConstants.OWNER);

			if (role.getRoleId() == ownerRole.getRoleId()) {
				Mockito.doReturn(
					userId
				).when(
					resourcePermission
				).getOwnerId();

				Mockito.when(
					_resourcePermissionPersistence.findByC_N_S_P_R(
						_companyId, className, _SCOPE, primKey,
						ownerRole.getRoleId())
				).thenReturn(
					resourcePermission
				);
			}

			resourcePermissions.add(resourcePermission);
		}

		return resourcePermissions;
	}

	private void _mockRoleLocalServiceForRoles(List<Role> roles)
		throws Exception {

		for (Role role : roles) {
			Mockito.when(
				_roleLocalService.getRole(role.getRoleId())
			).thenReturn(
				role
			);
		}
	}

	private static final int _SCOPE = 4;

	private final long _companyId;
	private final ResourcePermissionLocalService
		_resourcePermissionLocalService;
	private final ResourcePermissionPersistence _resourcePermissionPersistence;
	private final RoleLocalService _roleLocalService;
	private final Map<String, RoleProxy> _roleProxies = new HashMap<>();

}