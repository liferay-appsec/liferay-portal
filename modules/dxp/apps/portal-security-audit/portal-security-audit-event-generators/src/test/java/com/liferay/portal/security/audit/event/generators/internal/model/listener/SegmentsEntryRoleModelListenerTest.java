/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.internal.model.listener;

import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsEntryRole;
import com.liferay.segments.model.impl.SegmentsEntryRoleImpl;
import com.liferay.segments.service.SegmentsEntryLocalService;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Istvan Sajtos
 */
public class SegmentsEntryRoleModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_setUpSegmentsEntryRoleModelListener();
	}

	@Test
	public void testOnBeforeCreate() throws Exception {
		_executeModelListenerMethod(
			_segmentsEntryRoleModelListener::onBeforeCreate,
			_getSegmentsEntryRole());
	}

	@Test
	public void testOnBeforeRemove() throws Exception {
		_executeModelListenerMethod(
			_segmentsEntryRoleModelListener::onBeforeRemove,
			_getSegmentsEntryRole());
	}

	private void _assertAdditionalInfoJSONObject(
			JSONObject jsonObject, SegmentsEntryRole segmentsEntryRole)
		throws Exception {

		Role role = _roleLocalService.getRole(0L);

		SegmentsEntry segmentsEntry =
			_segmentsEntryLocalService.getSegmentsEntry(0L);

		Assert.assertEquals(
			jsonObject.getLong("roleId"), segmentsEntryRole.getRoleId());
		Assert.assertEquals(jsonObject.getString("roleName"), role.getName());
		Assert.assertEquals(
			jsonObject.getLong("segmentsEntryId"),
			segmentsEntryRole.getSegmentsEntryId());
		Assert.assertEquals(
			jsonObject.getString("segmentsEntryName"),
			segmentsEntry.getNameCurrentValue());
	}

	private void _executeModelListenerMethod(
			Consumer<SegmentsEntryRole> consumer,
			SegmentsEntryRole segmentsEntryRole)
		throws Exception {

		try (MockedStatic<PortalUtil> portalUtilMockedStatic =
				Mockito.mockStatic(PortalUtil.class)) {

			portalUtilMockedStatic.when(
				() -> PortalUtil.getUserName(
					ArgumentMatchers.anyLong(), ArgumentMatchers.anyString())
			).thenReturn(
				RandomTestUtil.randomString()
			);

			consumer.accept(segmentsEntryRole);

			ArgumentCaptor<AuditMessage> auditMessageCaptor =
				ArgumentCaptor.forClass(AuditMessage.class);

			Mockito.verify(
				_auditRouter
			).route(
				auditMessageCaptor.capture()
			);

			AuditMessage auditMessage = auditMessageCaptor.getValue();

			JSONObject additionalInfoJSONObject =
				auditMessage.getAdditionalInfo();

			_assertAdditionalInfoJSONObject(
				additionalInfoJSONObject, segmentsEntryRole);
		}
	}

	private SegmentsEntryRole _getSegmentsEntryRole() {
		SegmentsEntryRole segmentsEntryRole = new SegmentsEntryRoleImpl();

		segmentsEntryRole.setUserId(RandomTestUtil.randomLong());
		segmentsEntryRole.setSegmentsEntryId(RandomTestUtil.randomLong());
		segmentsEntryRole.setRoleId(RandomTestUtil.randomLong());

		return segmentsEntryRole;
	}

	private void _setUpSegmentsEntryRoleModelListener() throws Exception {
		_segmentsEntryRoleModelListener = new SegmentsEntryRoleModelListener();

		_segmentsEntryRoleModelListener.auditRouter = _auditRouter;

		Role role = Mockito.mock(Role.class);

		Mockito.when(
			role.getName()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_roleLocalService.getRole(ArgumentMatchers.anyLong())
		).thenReturn(
			role
		);

		_segmentsEntryRoleModelListener.roleLocalService = _roleLocalService;

		SegmentsEntry segmentsEntry = Mockito.mock(SegmentsEntry.class);

		Mockito.when(
			segmentsEntry.getSegmentsEntryId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);
		Mockito.when(
			segmentsEntry.getNameCurrentValue()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_segmentsEntryLocalService.getSegmentsEntry(
				ArgumentMatchers.anyLong())
		).thenReturn(
			segmentsEntry
		);

		_segmentsEntryRoleModelListener.segmentsEntryLocalService =
			_segmentsEntryLocalService;
	}

	private final AuditRouter _auditRouter = Mockito.mock(AuditRouter.class);
	private final RoleLocalService _roleLocalService = Mockito.mock(
		RoleLocalService.class);
	private final SegmentsEntryLocalService _segmentsEntryLocalService =
		Mockito.mock(SegmentsEntryLocalService.class);
	private SegmentsEntryRoleModelListener _segmentsEntryRoleModelListener;

}