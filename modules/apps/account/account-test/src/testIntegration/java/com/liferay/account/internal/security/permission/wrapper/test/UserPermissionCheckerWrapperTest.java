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

package com.liferay.account.internal.security.permission.wrapper.test;

import com.liferay.account.configuration.AccountEntryEmailDomainsConfiguration;
import com.liferay.account.constants.AccountActionKeys;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountRole;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountRoleLocalService;
import com.liferay.account.service.test.util.AccountEntryArgs;
import com.liferay.account.service.test.util.AccountEntryTestUtil;
import com.liferay.account.service.test.util.UserRoleTestUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.permission.UserPermission;
import com.liferay.portal.kernel.settings.SettingsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Pei-Jung Lan
 */
@RunWith(Arquillian.class)
public class UserPermissionCheckerWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testHasPermission() throws Exception {
		User impersonatingUser = UserTestUtil.addUser();
		User user = UserTestUtil.addUser();

		User userWithDefinePermissionsPermission = UserTestUtil.addUser();

		UserRoleTestUtil.addResourcePermission(
			ActionKeys.DEFINE_PERMISSIONS, Role.class.getName(),
			userWithDefinePermissionsPermission.getUserId());

		User userWithUpdateUserPermission = UserTestUtil.addUser();

		UserRoleTestUtil.addResourcePermission(
			ActionKeys.UPDATE, User.class.getName(),
			userWithUpdateUserPermission.getUserId());

		AccountEntry accountEntry = AccountEntryTestUtil.addAccountEntry(
			AccountEntryArgs.withUsers(
				impersonatingUser, user, userWithDefinePermissionsPermission,
				userWithUpdateUserPermission));

		AccountRole accountRole = _accountRoleLocalService.addAccountRole(
			TestPropsValues.getUserId(), accountEntry.getAccountEntryId(),
			RandomTestUtil.randomString(), null, null);

		_accountRoleLocalService.associateUser(
			accountEntry.getAccountEntryId(), accountRole.getAccountRoleId(),
			impersonatingUser.getUserId());

		PermissionChecker permissionChecker = _permissionCheckerFactory.create(
			impersonatingUser);

		_assertHasImpersonatePermission(
			false, permissionChecker, user.getUserId());
		_assertHasImpersonatePermission(
			false, permissionChecker,
			userWithDefinePermissionsPermission.getUserId());
		_assertHasImpersonatePermission(
			false, permissionChecker, userWithUpdateUserPermission.getUserId());

		_resourcePermissionLocalService.addResourcePermission(
			accountEntry.getCompanyId(), AccountEntry.class.getName(),
			ResourceConstants.SCOPE_GROUP,
			String.valueOf(accountEntry.getAccountEntryGroupId()),
			accountRole.getRoleId(), AccountActionKeys.IMPERSONATE_USERS);

		_assertHasImpersonatePermission(
			false, permissionChecker, user.getUserId());
		_assertHasImpersonatePermission(
			false, permissionChecker,
			userWithDefinePermissionsPermission.getUserId());
		_assertHasImpersonatePermission(
			false, permissionChecker, userWithUpdateUserPermission.getUserId());

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						AccountEntryEmailDomainsConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enableEmailDomainValidation", true
						).build(),
						SettingsFactoryUtil.getSettingsFactory())) {

			_accountEntryLocalService.updateDomains(
				accountEntry.getAccountEntryId(), new String[] {"test.com"});

			_assertHasImpersonatePermission(
				false, permissionChecker, user.getUserId());

			_accountEntryLocalService.updateDomains(
				accountEntry.getAccountEntryId(), new String[] {"liferay.com"});

			_assertHasImpersonatePermission(
				true, permissionChecker, user.getUserId());

			User userWithDifferentAccount = UserTestUtil.addUser();

			_assertHasImpersonatePermission(
				false, permissionChecker, userWithDifferentAccount.getUserId());

			AccountEntryTestUtil.addAccountEntry(
				AccountEntryArgs.withUsers(userWithDifferentAccount));

			_assertHasImpersonatePermission(
				false, permissionChecker, userWithDifferentAccount.getUserId());
		}
	}

	private void _assertHasImpersonatePermission(
			boolean hasPermission, PermissionChecker permissionChecker,
			long userId)
		throws Exception {

		Assert.assertEquals(
			hasPermission,
			_userPermission.contains(
				permissionChecker, userId, ActionKeys.IMPERSONATE));
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountRoleLocalService _accountRoleLocalService;

	@Inject
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private UserPermission _userPermission;

}