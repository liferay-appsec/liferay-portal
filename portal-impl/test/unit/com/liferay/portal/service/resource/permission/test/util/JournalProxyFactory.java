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
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;

import org.mockito.Mockito;

/**
 * @author Joshua Cords
 */
public class JournalProxyFactory {

	public JournalProxyFactory(
			PersistedModelLocalService journalArticlePersistedModelLocalService,
			ResourceActionLocalService resourceActionLocalService,
			RoleProxyFactory roleProxyFactory, long companyId)
		throws Exception {

		_journalArticlePersistedModelLocalService =
			journalArticlePersistedModelLocalService;
		_resourceActionLocalService = resourceActionLocalService;
		_roleProxyFactory = roleProxyFactory;
		_companyId = companyId;

		_mockAccessResourceActions();
		_mockViewResourceActions();
	}

	public JournalArticleProxy createJournalArticleProxy(
			JournalFolderProxy journalFolderProxy, long ownerUserId,
			String... roleNames)
		throws Exception {

		return new JournalArticleProxy(
			_journalArticlePersistedModelLocalService,
			_resourceActionLocalService, _viewArticleResourceAction,
			_roleProxyFactory, roleNames, ownerUserId, journalFolderProxy);
	}

	public JournalArticleProxy createJournalArticleProxy(
			long ownerUserId, String... roleNames)
		throws Exception {

		return new JournalArticleProxy(
			_journalArticlePersistedModelLocalService,
			_resourceActionLocalService, _viewArticleResourceAction,
			_roleProxyFactory, roleNames, ownerUserId, null);
	}

	public JournalFolderProxy createJournalFolderProxy(
			JournalFolderProxy journalFolderProxy, long ownerUserId,
			String... roleNames)
		throws Exception {

		return new JournalFolderProxy(
			_resourceActionLocalService, _roleProxyFactory,
			_viewFolderResourceAction, roleNames, ownerUserId,
			journalFolderProxy);
	}

	public JournalFolderProxy createJournalFolderProxy(
			JournalFolderProxy journalFolderProxy, long ownerUserId,
			String[] accessRoleNames, String[] viewRoleNames)
		throws Exception {

		return new JournalFolderProxy(
			_resourceActionLocalService, _roleProxyFactory,
			_accessFolderResourceAction, _viewFolderResourceAction,
			accessRoleNames, viewRoleNames, ownerUserId, journalFolderProxy);
	}

	public JournalFolderProxy createJournalFolderProxy(
			long ownerUserId, String... roleNames)
		throws Exception {

		return new JournalFolderProxy(
			_resourceActionLocalService, _roleProxyFactory,
			_viewFolderResourceAction, roleNames, ownerUserId, null);
	}

	public JournalFolderProxy createJournalFolderProxy(
			long ownerUserId, String[] accessRoleNames, String[] viewRoleNames)
		throws Exception {

		return new JournalFolderProxy(
			_resourceActionLocalService, _roleProxyFactory,
			_accessFolderResourceAction, _viewFolderResourceAction,
			accessRoleNames, viewRoleNames, ownerUserId, null);
	}

	private void _mockAccessResourceActions() throws Exception {
		_accessFolderResourceAction = Mockito.mock(ResourceAction.class);

		Mockito.doReturn(
			_accessFolderResourceAction
		).when(
			_resourceActionLocalService
		).getResourceAction(
			_CLASS_NAME_JOURNAL_FOLDER, _ACCESS_ACTION_ID
		);
	}

	private void _mockViewResourceActions() throws Exception {
		_viewFolderResourceAction = Mockito.mock(ResourceAction.class);

		Mockito.doReturn(
			_viewFolderResourceAction
		).when(
			_resourceActionLocalService
		).getResourceAction(
			_CLASS_NAME_JOURNAL_FOLDER, _VIEW_ACTION_ID
		);

		_viewArticleResourceAction = Mockito.mock(ResourceAction.class);

		Mockito.doReturn(
			_viewArticleResourceAction
		).when(
			_resourceActionLocalService
		).getResourceAction(
			_CLASS_NAME_JOURNAL_ARTICLE, _VIEW_ACTION_ID
		);
	}

	private static final String _ACCESS_ACTION_ID = ActionKeys.ACCESS;

	private static final String _CLASS_NAME_JOURNAL_ARTICLE =
		"com.liferay.journal.model.JournalArticle";

	private static final String _CLASS_NAME_JOURNAL_FOLDER =
		"com.liferay.journal.model.JournalFolder";

	private static final String _VIEW_ACTION_ID = ActionKeys.VIEW;

	private ResourceAction _accessFolderResourceAction;
	private final long _companyId;
	private final PersistedModelLocalService
		_journalArticlePersistedModelLocalService;
	private final ResourceActionLocalService _resourceActionLocalService;
	private final RoleProxyFactory _roleProxyFactory;
	private ResourceAction _viewArticleResourceAction;
	private ResourceAction _viewFolderResourceAction;

}