/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.batch.engine.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Álvaro Saugar
 */
@RunWith(Arquillian.class)
public class CookieEntryObjectDefinitionImportTaskPostActionTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	@TestInfo("LPD-101999")
	public void testRunGrantsGuestAndUserViewPermissions() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		for (String externalReferenceCode : _EXTERNAL_REFERENCE_CODES) {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.
					fetchObjectDefinitionByExternalReferenceCode(
						externalReferenceCode, companyId);

			for (String roleName :
					new String[] {RoleConstants.GUEST, RoleConstants.USER}) {

				Role role = _roleLocalService.getRole(companyId, roleName);

				_assertHasResourcePermission(
					ActionKeys.ADD_TO_PAGE, companyId,
					objectDefinition.getPortletId(), role.getRoleId());
				_assertHasResourcePermission(
					ActionKeys.VIEW, companyId, objectDefinition.getClassName(),
					role.getRoleId());
				_assertHasResourcePermission(
					ActionKeys.VIEW, companyId, objectDefinition.getPortletId(),
					role.getRoleId());
			}
		}
	}

	private void _assertHasResourcePermission(
			String actionId, long companyId, String name, long roleId)
		throws Exception {

		Assert.assertTrue(
			_resourcePermissionLocalService.hasResourcePermission(
				companyId, name, ResourceConstants.SCOPE_COMPANY,
				String.valueOf(companyId), roleId, actionId));
	}

	private static final String[] _EXTERNAL_REFERENCE_CODES = {
		"L_FUNCTIONAL_COOKIE_ENTRY", "L_NECESSARY_COOKIE_ENTRY",
		"L_PERFORMANCE_COOKIE_ENTRY", "L_PERSONALIZATION_COOKIE_ENTRY"
	};

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

}