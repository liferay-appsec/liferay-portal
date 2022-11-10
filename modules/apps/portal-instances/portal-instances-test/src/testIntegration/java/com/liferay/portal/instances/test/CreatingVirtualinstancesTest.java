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

package com.liferay.portal.instances.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.service.CompanyService;
import com.liferay.portal.kernel.service.UserService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alvaro Saugar
 */
@RunWith(Arquillian.class)
public class CreatingVirtualinstancesTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Test
	public void testCreateDefaultInstanceNoSettingAdminUserParametersNoDefaultAdminPasswordProperty()
		throws Exception {

		String domain = StringUtil.toLowerCase(RandomTestUtil.randomString());

		String webId = domain;
		String virtualHostname = domain;

		String mx = domain + ".com";

		int maxUsers = RandomTestUtil.randomInt();
		boolean active = true;

		String defaultAdminPassword = PropsValues.DEFAULT_ADMIN_PASSWORD;

		PropsUtil.set(PropsKeys.DEFAULT_ADMIN_PASSWORD, null);

		String screenNameAdminUser = PropsValues.DEFAULT_ADMIN_SCREEN_NAME;
		String emailAdminUser =
			PropsValues.DEFAULT_ADMIN_EMAIL_ADDRESS_PREFIX + "@" + mx;
		String firstNameAdminUser = PropsValues.DEFAULT_ADMIN_FIRST_NAME;
		String lastNameAdminUser = PropsValues.DEFAULT_ADMIN_LAST_NAME;

		Company company = _companyService.addCompany(
			webId, virtualHostname, mx, false, maxUsers, active, null, null,
			null, null, null);

		User user = _userService.getUserByScreenName(
			company.getCompanyId(), screenNameAdminUser);

		Assert.assertEquals(emailAdminUser, user.getEmailAddress());
		Assert.assertEquals(firstNameAdminUser, user.getFirstName());
		Assert.assertEquals(lastNameAdminUser, user.getLastName());
		Assert.assertNotNull(user.getPassword());
		Assert.assertEquals(
			WorkflowConstants.LABEL_PENDING, user.getReminderQueryAnswer());

		PropsUtil.set(PropsKeys.DEFAULT_ADMIN_PASSWORD, defaultAdminPassword);
		_companyService.deleteCompany(company.getCompanyId());
	}

	@Test
	public void testCreateDefaultOrVirtualInstanceNoSettingAdminUserParametersYesDefaultAdminPasswordProperty()
		throws Exception {

		String domain = StringUtil.toLowerCase(RandomTestUtil.randomString());

		String webId = domain;
		String virtualHostname = domain;

		String mx = domain + ".com";

		int maxUsers = RandomTestUtil.randomInt();
		boolean active = true;

		String defaultAdminPassword = PropsValues.DEFAULT_ADMIN_PASSWORD;

		String passwordUser = RandomTestUtil.randomString();

		PropsUtil.set(PropsKeys.DEFAULT_ADMIN_PASSWORD, passwordUser);

		String screenNameAdminUser = PropsValues.DEFAULT_ADMIN_SCREEN_NAME;
		String emailAdminUser =
			PropsValues.DEFAULT_ADMIN_EMAIL_ADDRESS_PREFIX + "@" + mx;
		String firstNameAdminUser = PropsValues.DEFAULT_ADMIN_FIRST_NAME;
		String lastNameAdminUser = PropsValues.DEFAULT_ADMIN_LAST_NAME;

		Company company = _companyService.addCompany(
			webId, virtualHostname, mx, false, maxUsers, active, null, null,
			null, null, null);

		User user = _userService.getUserByScreenName(
			company.getCompanyId(), screenNameAdminUser);

		String encryptedPassword = PasswordEncryptorUtil.encrypt(
			passwordUser, user.getPassword());

		Assert.assertEquals(emailAdminUser, user.getEmailAddress());
		Assert.assertEquals(firstNameAdminUser, user.getFirstName());
		Assert.assertEquals(lastNameAdminUser, user.getLastName());
		Assert.assertEquals(encryptedPassword, user.getPassword());

		PropsUtil.set(PropsKeys.DEFAULT_ADMIN_PASSWORD, defaultAdminPassword);
		_companyService.deleteCompany(company.getCompanyId());
	}

	@Test
	public void testCreateVirtualInstanceSettingAllParameters()
		throws Exception {

		String domain = StringUtil.toLowerCase(RandomTestUtil.randomString());

		String webId = domain;
		String virtualHostname = domain;

		String mx = domain + ".com";

		int maxUsers = RandomTestUtil.randomInt();
		boolean active = true;

		String defaultAdminPassword = PropsValues.DEFAULT_ADMIN_PASSWORD;

		String screenNameAdminUser = RandomTestUtil.randomString();
		String emailAdminUser =
			StringUtil.toLowerCase(RandomTestUtil.randomString()) + "@" + mx;
		String passwordAdminUser = RandomTestUtil.randomString();
		String firstNameAdminUser = RandomTestUtil.randomString();
		String lastNameAdminUser = RandomTestUtil.randomString();

		Company company = _companyService.addCompany(
			webId, virtualHostname, mx, false, maxUsers, active,
			screenNameAdminUser, emailAdminUser, passwordAdminUser,
			firstNameAdminUser, lastNameAdminUser);

		User user = _userService.getUserByScreenName(
			company.getCompanyId(), screenNameAdminUser);

		String encryptedPassword = PasswordEncryptorUtil.encrypt(
			passwordAdminUser, user.getPassword());

		Assert.assertEquals(emailAdminUser, user.getEmailAddress());
		Assert.assertEquals(firstNameAdminUser, user.getFirstName());
		Assert.assertEquals(lastNameAdminUser, user.getLastName());
		Assert.assertEquals(encryptedPassword, user.getPassword());

		PropsUtil.set(PropsKeys.DEFAULT_ADMIN_PASSWORD, defaultAdminPassword);
		_companyService.deleteCompany(company.getCompanyId());
	}

	@Inject
	private CompanyService _companyService;

	@Inject
	private UserService _userService;

}