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

package com.liferay.portal.service;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.service.PersistedModelLocalServiceRegistry;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.persistence.ResourcePermissionPersistence;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.service.impl.ResourcePermissionLocalServiceImpl;
import com.liferay.portal.service.resource.permission.test.util.JournalArticleProxy;
import com.liferay.portal.service.resource.permission.test.util.JournalFolderProxy;
import com.liferay.portal.service.resource.permission.test.util.JournalProxyFactory;
import com.liferay.portal.service.resource.permission.test.util.RoleProxyFactory;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.registry.BasicRegistryImpl;
import com.liferay.registry.RegistryUtil;

import java.util.HashSet;
import java.util.Set;

import jodd.util.StringUtil;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Joshua Cords
 */
public class ResourcePermissionImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_initializeBasicVariables();

		RegistryUtil.setRegistry(new BasicRegistryImpl());

		_resourcePermissionLocalService =
			new ResourcePermissionLocalServiceImpl();

		MockitoAnnotations.initMocks(this);

		_roleProxyFactory = new RoleProxyFactory(
			_resourcePermissionLocalService, _roleLocalService, _companyId);

		_initializeReflections();
		_mockPersistedModelLocalServices();

		_journalProxyFactory = new JournalProxyFactory(
			_journalArticlePersistedModelLocalService,
			_resourceActionLocalService, _roleProxyFactory, _companyId);
	}

	@Test
	public void testDynamicInheritanceRolesAccess() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		String[] journalFolderAccessRoleNames = {RoleConstants.SITE_MEMBER};

		String[] journalFolderViewRoleNames = {RoleConstants.OWNER};

		JournalFolderProxy journalFolderProxy =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, journalFolderAccessRoleNames,
				journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy, creatorUserId, RoleConstants.SITE_MEMBER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.SITE_MEMBER);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesAccessAndView() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER);

		String[] journalFolderAccessRoleNames = {RoleConstants.SITE_MEMBER};

		String[] journalFolderViewRoleNames = {RoleConstants.OWNER};

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId,
				journalFolderAccessRoleNames, journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.USER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.SITE_MEMBER, RoleConstants.USER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.OWNER, RoleConstants.USER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesAccessNoRedundancy()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER);

		String[] journalFolderAccessRoleNames = {RoleConstants.SITE_MEMBER};

		String[] journalFolderViewRoleNames = {RoleConstants.OWNER};

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId,
				journalFolderAccessRoleNames, journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.GUEST);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.OWNER, RoleConstants.SITE_MEMBER);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesAccessNoView() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER);

		String[] journalFolderAccessRoleNames = {RoleConstants.SITE_MEMBER};

		String[] journalFolderViewRoleNames = {};

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId,
				journalFolderAccessRoleNames, journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.USER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.SITE_MEMBER, RoleConstants.USER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesDifferentOwners() throws Exception {
		long creatorUserId1 = RandomTestUtil.randomLong();
		long creatorUserId2 = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId1, RoleConstants.OWNER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy, creatorUserId2, RoleConstants.OWNER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesDifferentOwnersWithGuest()
		throws Exception {

		long creatorUserId1 = RandomTestUtil.randomLong();
		long creatorUserId2 = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId1, RoleConstants.OWNER, RoleConstants.SITE_MEMBER);

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId2, RoleConstants.OWNER,
				RoleConstants.USER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId2, RoleConstants.GUEST);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId1, RoleConstants.SITE_MEMBER, RoleConstants.USER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId1, RoleConstants.OWNER, RoleConstants.USER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId2, RoleConstants.OWNER,
				RoleConstants.SITE_MEMBER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesDoubleDoubleCombination()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER, RoleConstants.USER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy1, creatorUserId, RoleConstants.SITE_MEMBER,
				RoleConstants.POWER_USER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.OWNER, RoleConstants.SITE_MEMBER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.OWNER, RoleConstants.POWER_USER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.USER, RoleConstants.SITE_MEMBER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.USER, RoleConstants.POWER_USER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesGuestAccess() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		String[] journalFolderAccessRoleNames = {RoleConstants.GUEST};

		String[] journalFolderViewRoleNames = {RoleConstants.OWNER};

		JournalFolderProxy journalFolderProxy =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, journalFolderAccessRoleNames,
				journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy, creatorUserId, RoleConstants.GUEST);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.GUEST);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesGuestAccessWildcard()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		String[] journalFolderAccessRoleNames = {RoleConstants.GUEST};

		String[] journalFolderViewRoleNames = {RoleConstants.OWNER};

		JournalFolderProxy journalFolderProxy =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, journalFolderAccessRoleNames,
				journalFolderViewRoleNames);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy, creatorUserId, RoleConstants.USER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.USER);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesGuestAsWildcard() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.GUEST, RoleConstants.USER);

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId, RoleConstants.USER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.GUEST,
				RoleConstants.OWNER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.USER);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesGuestAsWildcardBridge()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.USER);

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId, RoleConstants.GUEST);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.OWNER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.OWNER, RoleConstants.USER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesNoFolders() throws Exception {
		long creatorUserId = RandomTestUtil.randomLong();

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				creatorUserId, RoleConstants.GUEST, RoleConstants.OWNER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.GUEST);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesOnlyGuestIsNecessary()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.GUEST, RoleConstants.USER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy, creatorUserId, RoleConstants.GUEST,
				RoleConstants.OWNER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.GUEST);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesTripleCombination()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER, RoleConstants.USER);

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId, RoleConstants.OWNER,
				RoleConstants.SITE_MEMBER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.OWNER,
				RoleConstants.POWER_USER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = new HashSet<>();

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(
				creatorUserId, RoleConstants.POWER_USER,
				RoleConstants.SITE_MEMBER, RoleConstants.USER));

		expectedRoleIdSets.add(
			getExpectedRoleIdSet(creatorUserId, RoleConstants.OWNER));

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	@Test
	public void testDynamicInheritanceRolesTwoGuestAsWildcard()
		throws Exception {

		long creatorUserId = RandomTestUtil.randomLong();

		JournalFolderProxy journalFolderProxy1 =
			_journalProxyFactory.createJournalFolderProxy(
				creatorUserId, RoleConstants.OWNER);

		JournalFolderProxy journalFolderProxy2 =
			_journalProxyFactory.createJournalFolderProxy(
				journalFolderProxy1, creatorUserId, RoleConstants.GUEST,
				RoleConstants.SITE_MEMBER);

		JournalArticleProxy journalArticleProxy =
			_journalProxyFactory.createJournalArticleProxy(
				journalFolderProxy2, creatorUserId, RoleConstants.GUEST,
				RoleConstants.SITE_MEMBER);

		Set<Set<String>> roleIdSets =
			_resourcePermissionLocalService.getInheritedRoleIdCombinations(
				_companyId, _groupId, _journalArticleClassName, _scope,
				journalArticleProxy.getResourcePrimKey(),
				journalArticleProxy.getPrimKey(), _viewActionId);

		Set<Set<String>> expectedRoleIdSets = getExpectedRoleIdSets(
			creatorUserId, RoleConstants.OWNER);

		assertContainsRoleSets(expectedRoleIdSets, roleIdSets);
	}

	protected void assertContainsRoleSets(
			Set<Set<String>> expectedRoleIdSets,
			Set<Set<String>> actualRoleIdSets)
		throws Exception {

		if (expectedRoleIdSets.size() != actualRoleIdSets.size()) {
			StringBuilder sb = new StringBuilder(4);

			sb.append("expectedRoleIdSets size ");
			sb.append(expectedRoleIdSets.size());
			sb.append(" ");

			_appendRoleSets(expectedRoleIdSets, sb);

			sb.append(" is not actualRoleIdSets size ");
			sb.append(actualRoleIdSets.size());
			sb.append(" ");

			_appendRoleSets(actualRoleIdSets, sb);

			throw new AssertionError(sb.toString());
		}

		for (Set<String> expectedRoleIdSet : expectedRoleIdSets) {
			if (!actualRoleIdSets.contains(expectedRoleIdSet)) {
				StringBuilder sb = new StringBuilder();

				sb.append("expectedRoleIdSet [");

				_appendRoleSet(expectedRoleIdSet, sb);

				sb.append("] from expectedRoleIdSets [");

				_appendRoleSets(expectedRoleIdSets, sb);

				sb.append("] was not found in actualRoleIdSets [");

				_appendRoleSets(actualRoleIdSets, sb);

				sb.append("]");

				throw new AssertionError(sb.toString());
			}
		}
	}

	protected Set<String> getExpectedRoleIdSet(long userId, String... roleNames)
		throws Exception {

		Set<String> roleIdSet = new HashSet<>();

		for (String roleName : roleNames) {
			Role role = _roleProxyFactory.getRole(roleName);

			if (roleName.equals(RoleConstants.OWNER)) {
				roleIdSet.add(
					String.valueOf(userId) + StringPool.DASH +
						role.getRoleId());
			}
			else if (roleName.equals(RoleConstants.SITE_MEMBER)) {
				roleIdSet.add(
					String.valueOf(_groupId) + StringPool.DASH +
						role.getRoleId());
			}
			else {
				roleIdSet.add(String.valueOf(role.getRoleId()));
			}
		}

		return roleIdSet;
	}

	protected Set<Set<String>> getExpectedRoleIdSets(
			long userId, String... roleNames)
		throws Exception {

		Set<Set<String>> roleIdSets = new HashSet<>();

		for (String roleName : roleNames) {
			roleIdSets.add(getExpectedRoleIdSet(userId, roleName));
		}

		return roleIdSets;
	}

	private StringBuilder _appendRoleSet(
			Set<String> roleIdSet, StringBuilder sb)
		throws Exception {

		for (String roleId : roleIdSet) {
			Role role = _roleProxyFactory.getRoleById(roleId);

			if (role != null) {
				sb.append(role.getDescriptiveName());
			}
			else {
				sb.append(roleId);
			}

			sb.append(", ");
		}

		return sb;
	}

	private StringBuilder _appendRoleSets(
			Set<Set<String>> roleIdSets, StringBuilder sb)
		throws Exception {

		for (Set<String> roleSet : roleIdSets) {
			sb.append("[");

			_appendRoleSet(roleSet, sb);
			sb.append("], ");
		}

		return sb;
	}

	private void _initializeBasicVariables() {
		_companyId = RandomTestUtil.randomLong();
		_groupId = RandomTestUtil.randomLong();
		_journalArticleClassName = "com.liferay.journal.model.JournalArticle";
		_journalFolderClassName = "com.liferay.journal.model.JournalFolder";
		_scope = ResourceConstants.SCOPE_INDIVIDUAL;
		_resourcePrimKey = StringUtil.toString(RandomTestUtil.randomLong());
		_primKey = StringUtil.toString(RandomTestUtil.randomLong());
		_viewActionId = ActionKeys.VIEW;
	}

	private void _initializeReflections() {
		ReflectionTestUtil.setFieldValue(
			_resourcePermissionLocalService, "resourceActionLocalService",
			_resourceActionLocalService);
		ReflectionTestUtil.setFieldValue(
			_resourcePermissionLocalService, "roleLocalService",
			_roleLocalService);
		ReflectionTestUtil.setFieldValue(
			_resourcePermissionLocalService,
			"persistedModelLocalServiceRegistry",
			_persistedModelLocalServiceRegistry);
	}

	private void _mockPersistedModelLocalServices() throws Exception {
		Mockito.doReturn(
			_journalArticlePersistedModelLocalService
		).when(
			_persistedModelLocalServiceRegistry
		).getPersistedModelLocalService(
			_journalArticleClassName
		);

		Mockito.doReturn(
			_journalFolderPersistedModelLocalService
		).when(
			_persistedModelLocalServiceRegistry
		).getPersistedModelLocalService(
			_journalFolderClassName
		);

		Mockito.doReturn(
			_roleProxyFactory.getRole(RoleConstants.GUEST)
		).when(
			_roleLocalService
		).getRole(
			_companyId, RoleConstants.GUEST
		);

		Mockito.doReturn(
			_roleProxyFactory.getRole(RoleConstants.OWNER)
		).when(
			_roleLocalService
		).fetchRole(
			_companyId, RoleConstants.OWNER
		);
	}

	@Mock
	private ResourceAction _accessFolderResourceAction;

	private long _companyId;
	private long _groupId;
	private String _journalArticleClassName;

	@Mock
	private PersistedModelLocalService
		_journalArticlePersistedModelLocalService;

	private String _journalFolderClassName;

	@Mock
	private PersistedModelLocalService _journalFolderPersistedModelLocalService;

	private JournalProxyFactory _journalProxyFactory;

	@Mock
	private PersistedModelLocalServiceRegistry
		_persistedModelLocalServiceRegistry;

	private String _primKey;

	@Mock
	private ResourceActionLocalService _resourceActionLocalService;

	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Mock
	private ResourcePermissionPersistence _resourcePermissionPersistence;

	private String _resourcePrimKey;

	@Mock
	private RoleLocalService _roleLocalService;

	private RoleProxyFactory _roleProxyFactory;
	private int _scope;
	private String _viewActionId;

	@Mock
	private ResourceAction _viewArticleResourceAction;

	@Mock
	private ResourceAction _viewFolderResourceAction;

}