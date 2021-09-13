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

import com.liferay.portal.kernel.model.BaseChildModel;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.util.ArrayList;
import java.util.List;

import jodd.util.StringUtil;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class JournalArticleProxy {

	public JournalArticleProxy(
			PersistedModelLocalService journalArticlePersistedModelLocalService,
			ResourceActionLocalService resourceActionLocalService,
			ResourceAction viewArticleResourceAction,
			RoleProxyFactory roleProxyFactory, String[] roleNames,
			long ownerUserId, JournalFolderProxy journalFolderProxy)
		throws Exception {

		_journalArticlePersistedModelLocalService =
			journalArticlePersistedModelLocalService;
		_resourceActionLocalService = resourceActionLocalService;
		_viewArticleResourceAction = viewArticleResourceAction;
		_roleProxyFactory = roleProxyFactory;

		_userId = ownerUserId;

		_roleNamesWithViewPermission = roleNames;

		if (journalFolderProxy != null) {
			_parentClassPK = journalFolderProxy.getResourcePrimKey();
		}
		else {
			_parentClassPK = "";
		}

		_primKey = StringUtil.toString(RandomTestUtil.randomLong());

		_resourcePrimKey = StringUtil.toString(RandomTestUtil.randomLong());

		_createTreePath(journalFolderProxy);

		_mockPersistedModel();
		_mockRolesWithViewPermissions(roleNames);
	}

	public String getPrimKey() {
		return _primKey;
	}

	public String getResourcePrimKey() {
		return _resourcePrimKey;
	}

	public abstract class PersistenceBaseChild
		implements BaseChildModel, PersistedModel {
	}

	private void _createTreePath(JournalFolderProxy journalFolderProxy) {
		_treePath =
			(journalFolderProxy != null) ? journalFolderProxy.getTreePath() :
				"/";
	}

	private void _mockPersistedModel() throws Exception {
		_journalArticlePersistedBaseChildModel = Mockito.mock(
			PersistenceBaseChild.class);

		Mockito.doReturn(
			_treePath
		).when(
			_journalArticlePersistedBaseChildModel
		).getTreePath();

		Mockito.doReturn(
			_CLASS_NAME_JOURNAL_FOLDER
		).when(
			_journalArticlePersistedBaseChildModel
		).getParentClassName();

		Mockito.doReturn(
			_parentClassPK
		).when(
			_journalArticlePersistedBaseChildModel
		).getParentClassPK();

		Mockito.doReturn(
			_journalArticlePersistedBaseChildModel
		).when(
			_journalArticlePersistedModelLocalService
		).getPersistedModel(
			Long.valueOf(_primKey)
		);
	}

	private void _mockRolesWithViewPermissions(String[] roleNames)
		throws Exception {

		for (String roleName : roleNames) {
			_rolesWithViewPermission.add(_roleProxyFactory.getRole(roleName));
		}

		_roleProxyFactory.mockResourceActionWithRolesOnAsset(
			_viewArticleResourceAction, _rolesWithViewPermission,
			_CLASS_NAME_JOURNAL_ARTICLE, _resourcePrimKey, _primKey, _userId);
	}

	private static final String _CLASS_NAME_JOURNAL_ARTICLE =
		"com.liferay.journal.model.JournalArticle";

	private static final String _CLASS_NAME_JOURNAL_FOLDER =
		"com.liferay.journal.model.JournalFolder";

	private PersistenceBaseChild _journalArticlePersistedBaseChildModel;
	private final PersistedModelLocalService
		_journalArticlePersistedModelLocalService;
	private final String _parentClassPK;
	private final String _primKey;
	private final ResourceActionLocalService _resourceActionLocalService;
	private final String _resourcePrimKey;
	private final String[] _roleNamesWithViewPermission;
	private final RoleProxyFactory _roleProxyFactory;
	private final List<Role> _rolesWithViewPermission = new ArrayList<>();
	private String _treePath;
	private final long _userId;
	private final ResourceAction _viewArticleResourceAction;

}