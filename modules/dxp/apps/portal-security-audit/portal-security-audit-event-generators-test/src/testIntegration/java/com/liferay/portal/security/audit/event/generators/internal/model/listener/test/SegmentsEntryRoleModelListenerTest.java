/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.test.rule.Inject;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.service.SegmentsEntryRoleLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class SegmentsEntryRoleModelListenerTest
	extends BaseModelListenerTestCase {

	@Test
	public void testOnBeforeCreate() throws Exception {
		_group = GroupTestUtil.addGroup();
		_role = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);
		_segmentsEntry = SegmentsTestUtil.addSegmentsEntry(_group.getGroupId());

		auditMessages.clear();

		_segmentsEntryRoleLocalService.addSegmentsEntryRole(
			_segmentsEntry.getSegmentsEntryId(), _role.getRoleId(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		AuditMessage auditMessage = fetchAuditMessage(
			Role.class.getName(), EventTypes.ASSIGN);

		Assert.assertNotNull(auditMessage);

		Assert.assertEquals(_role.getCompanyId(), auditMessage.getCompanyId());

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			_role.getName(), additionalInfoJSONObject.getString("roleName"));
		Assert.assertEquals(
			_segmentsEntry.getSegmentsEntryId(),
			additionalInfoJSONObject.getLong("segmentsEntryId"));
		Assert.assertEquals(
			_segmentsEntry.getNameCurrentValue(),
			additionalInfoJSONObject.getString("segmentsEntryName"));
	}

	@Test
	public void testOnBeforeRemove() throws Exception {
		_group = GroupTestUtil.addGroup();
		_role = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);
		_segmentsEntry = SegmentsTestUtil.addSegmentsEntry(_group.getGroupId());

		_segmentsEntryRoleLocalService.addSegmentsEntryRole(
			_segmentsEntry.getSegmentsEntryId(), _role.getRoleId(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		auditMessages.clear();

		_segmentsEntryRoleLocalService.deleteSegmentsEntryRole(
			_segmentsEntry.getSegmentsEntryId(), _role.getRoleId());

		AuditMessage auditMessage = fetchAuditMessage(
			Role.class.getName(), EventTypes.UNASSIGN);

		Assert.assertNotNull(auditMessage);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			_role.getName(), additionalInfoJSONObject.getString("roleName"));
		Assert.assertEquals(
			_segmentsEntry.getNameCurrentValue(),
			additionalInfoJSONObject.getString("segmentsEntryName"));
	}

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private Role _role;

	@DeleteAfterTestRun
	private SegmentsEntry _segmentsEntry;

	@Inject
	private SegmentsEntryRoleLocalService _segmentsEntryRoleLocalService;

}