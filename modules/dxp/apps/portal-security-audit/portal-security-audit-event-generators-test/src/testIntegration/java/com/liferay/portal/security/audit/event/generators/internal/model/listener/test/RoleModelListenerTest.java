/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.OrganizationTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class RoleModelListenerTest extends BaseModelListenerTestCase {

	@Test
	public void testOnBeforeAddAssociation() throws Exception {
		_company = CompanyTestUtil.addCompany();
		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_user = UserTestUtil.addUser();

		auditMessages.clear();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_userLocalService.addRoleUsers(
				_role.getRoleId(), new long[] {_user.getUserId()});
		}

		AuditMessage auditMessage = fetchAuditMessage(
			User.class.getName(), EventTypes.ASSIGN);

		Assert.assertEquals(_user.getCompanyId(), auditMessage.getCompanyId());

		Assert.assertEquals(_RESOURCE_ACTION, auditMessage.getResourceAction());
		Assert.assertEquals(_RESOURCE_TYPE, auditMessage.getResourceType());
	}

	@Test
	public void testOnBeforeAddAssociationWithOrganizationGroup()
		throws Exception {

		_company = CompanyTestUtil.addCompany();
		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_organization = OrganizationTestUtil.addOrganization();

		auditMessages.clear();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_groupLocalService.addRoleGroups(
				_role.getRoleId(), new long[] {_organization.getGroupId()});
		}

		AuditMessage auditMessage = fetchAuditMessage(
			Group.class.getName(), EventTypes.ASSIGN);

		Assert.assertEquals(_RESOURCE_ACTION, auditMessage.getResourceAction());
		Assert.assertEquals(_RESOURCE_TYPE, auditMessage.getResourceType());
	}

	@Test
	public void testOnBeforeUpdate() throws Exception {
		_company = CompanyTestUtil.addCompany();
		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		auditMessages.clear();

		_role.setName(RandomTestUtil.randomString());

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_role = _roleLocalService.updateRole(_role);
		}

		AuditMessage auditMessage = fetchAuditMessage(
			Role.class.getName(), EventTypes.UPDATE);

		Assert.assertEquals(_role.getCompanyId(), auditMessage.getCompanyId());
	}

	private static final String _RESOURCE_ACTION = "system.role.assign";

	private static final String _RESOURCE_TYPE = "role";

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private GroupLocalService _groupLocalService;

	@DeleteAfterTestRun
	private Organization _organization;

	@DeleteAfterTestRun
	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@DeleteAfterTestRun
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}