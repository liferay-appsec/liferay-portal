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

import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.util.ArrayList;
import java.util.List;

import jodd.util.StringUtil;

/**
 * @author Joshua Cords
 */
public class JournalFolderProxy {

	public JournalFolderProxy(
			ResourceActionLocalService resourceActionLocalService,
			RoleProxyFactory roleProxyFactory,
			ResourceAction accessFolderResourceAction,
			ResourceAction viewFolderResourceAction,
			String[] roleNamesWithAccessPermission,
			String[] roleNamesWithViewPermission, long ownerUserId,
			JournalFolderProxy journalFolderProxy)
		throws Exception {

		_resourceActionLocalService = resourceActionLocalService;
		_roleProxyFactory = roleProxyFactory;
		_accessFolderResourceAction = accessFolderResourceAction;
		_viewFolderResourceAction = viewFolderResourceAction;
		_roleNamesWithAccessPermission = roleNamesWithAccessPermission;
		_roleNamesWithViewPermission = roleNamesWithViewPermission;

		_userId = ownerUserId;

		_resourcePrimKey = StringUtil.toString(RandomTestUtil.randomLong());

		_createTreePath(journalFolderProxy);
		_mockRolesPermissions();
	}

	public JournalFolderProxy(
			ResourceActionLocalService resourceActionLocalService,
			RoleProxyFactory roleProxyFactory,
			ResourceAction viewFolderResourceAction,
			String[] roleNamesWithViewPermission, long ownerUserId,
			JournalFolderProxy journalFolderProxy)
		throws Exception {

		this(
			resourceActionLocalService, roleProxyFactory, null,
			viewFolderResourceAction, new String[0],
			roleNamesWithViewPermission, ownerUserId, journalFolderProxy);
	}

	public String getResourcePrimKey() {
		return _resourcePrimKey;
	}

	public String getTreePath() {
		return _treePath;
	}

	private void _createTreePath(JournalFolderProxy journalFolderProxy) {
		StringBuilder sb = new StringBuilder(3);

		if (journalFolderProxy != null) {
			sb.append(journalFolderProxy.getTreePath());
		}
		else {
			sb.append("/");
		}

		sb.append(_resourcePrimKey);
		sb.append("/");

		_treePath = sb.toString();
	}

	private void _mockRolesPermissions() throws Exception {
		for (String roleName : _roleNamesWithAccessPermission) {
			_rolesWithAccessPermission.add(_roleProxyFactory.getRole(roleName));
		}

		for (String roleName : _roleNamesWithViewPermission) {
			_rolesWithViewPermission.add(_roleProxyFactory.getRole(roleName));
		}

		_roleProxyFactory.mockResourceActionWithRolesOnAsset(
			_accessFolderResourceAction, _viewFolderResourceAction,
			_rolesWithAccessPermission, _rolesWithViewPermission,
			_CLASS_NAME_JOURNAL_FOLDER, _resourcePrimKey, _resourcePrimKey,
			_userId);
	}

	private static final String _CLASS_NAME_JOURNAL_FOLDER =
		"com.liferay.journal.model.JournalFolder";

	private final ResourceAction _accessFolderResourceAction;
	private final ResourceActionLocalService _resourceActionLocalService;
	private final String _resourcePrimKey;
	private final String[] _roleNamesWithAccessPermission;
	private final String[] _roleNamesWithViewPermission;
	private final RoleProxyFactory _roleProxyFactory;
	private final List<Role> _rolesWithAccessPermission = new ArrayList<>();
	private final List<Role> _rolesWithViewPermission = new ArrayList<>();
	private String _treePath;
	private final long _userId;
	private final ResourceAction _viewFolderResourceAction;

}