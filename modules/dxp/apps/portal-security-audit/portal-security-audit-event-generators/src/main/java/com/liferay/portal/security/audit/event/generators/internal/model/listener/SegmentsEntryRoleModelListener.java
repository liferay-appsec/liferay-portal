/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.internal.model.listener;

import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.event.generators.util.AuditMessageBuilder;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsEntryRole;
import com.liferay.segments.service.SegmentsEntryLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
@Component(service = ModelListener.class)
public class SegmentsEntryRoleModelListener
	extends BaseModelListener<SegmentsEntryRole> {

	@Override
	public void onBeforeCreate(SegmentsEntryRole segmentsEntryRole)
		throws ModelListenerException {

		auditOnCreateOrRemove(EventTypes.ASSIGN, segmentsEntryRole);
	}

	@Override
	public void onBeforeRemove(SegmentsEntryRole segmentsEntryRole)
		throws ModelListenerException {

		auditOnCreateOrRemove(EventTypes.UNASSIGN, segmentsEntryRole);
	}

	protected void auditOnCreateOrRemove(
			String eventType, SegmentsEntryRole segmentsEntryRole)
		throws ModelListenerException {

		try {
			AuditMessage auditMessage = AuditMessageBuilder.buildAuditMessage(
				Role.class.getName(), segmentsEntryRole.getRoleId(), eventType,
				null);

			JSONObject additionalInfoJSONObject =
				auditMessage.getAdditionalInfo();

			Role role = _roleLocalService.fetchRole(
				segmentsEntryRole.getRoleId());

			if (role != null) {
				additionalInfoJSONObject.put("roleName", role.getName());
			}

			long segmentsEntryId = segmentsEntryRole.getSegmentsEntryId();

			additionalInfoJSONObject.put("segmentsEntryId", segmentsEntryId);

			SegmentsEntry segmentsEntry =
				_segmentsEntryLocalService.fetchSegmentsEntry(segmentsEntryId);

			if (segmentsEntry != null) {
				additionalInfoJSONObject.put(
					"segmentsEntryName", segmentsEntry.getNameCurrentValue());
			}

			auditMessage.setCompanyId(segmentsEntryRole.getCompanyId());

			_auditRouter.route(auditMessage);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private AuditRouter _auditRouter;

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private SegmentsEntryLocalService _segmentsEntryLocalService;

}