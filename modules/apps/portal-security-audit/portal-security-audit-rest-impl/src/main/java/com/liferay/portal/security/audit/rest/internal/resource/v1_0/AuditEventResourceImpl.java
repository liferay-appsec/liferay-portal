/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.rest.internal.resource.v1_0;

import com.liferay.headless.delivery.dto.v1_0.util.CreatorUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.rest.dto.v1_0.AuditEvent;
import com.liferay.portal.security.audit.rest.resource.v1_0.AuditEventResource;
import com.liferay.portal.security.audit.storage.service.AuditEventService;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Date;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Rafael Praxedes
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/audit-event.properties",
	scope = ServiceScope.PROTOTYPE, service = AuditEventResource.class
)
public class AuditEventResourceImpl extends BaseAuditEventResourceImpl {

	@Override
	public AuditEvent getAuditEvent(Long auditEventId) throws Exception {
		return _toAuditEvent(_auditEventService.getAuditEvent(auditEventId));
	}

	@Override
	public Page<AuditEvent> getAuditEventsPage(
			Long accountEntryId, Date endDate, Long entityId, String entityType,
			String eventType, String search, Date startDate, Long userId,
			Filter filter, Pagination pagination, Sort[] sorts)
		throws Exception {

		long[] accountEntryIds = null;

		if (accountEntryId != null) {
			accountEntryIds = new long[] {accountEntryId};
		}

		String classPK = null;

		if (entityId != null) {
			classPK = String.valueOf(entityId);
		}

		long companyId = contextCompany.getCompanyId();

		long resolvedUserId = 0L;

		if (userId != null) {
			resolvedUserId = userId;
		}

		return Page.of(
			TransformUtil.transform(
				_auditEventService.getAuditEvents(
					companyId, accountEntryIds, 0L, resolvedUserId, null,
					startDate, endDate, eventType, entityType, classPK, null,
					null, null, 0, null, true, pagination.getStartPosition(),
					pagination.getEndPosition(), null),
				this::_toAuditEvent),
			pagination,
			_auditEventService.getAuditEventsCount(
				companyId, accountEntryIds, 0L, resolvedUserId, null, startDate,
				endDate, eventType, entityType, classPK, null, null, null, 0,
				null, true));
	}

	private AuditEvent _toAuditEvent(
			com.liferay.portal.security.audit.storage.model.AuditEvent
				serviceBuilderAuditEvent)
		throws PortalException {

		return new AuditEvent() {
			{
				setAccountEntryId(serviceBuilderAuditEvent::getAccountEntryId);
				setAdditionalInfo(
					() -> _toMap(serviceBuilderAuditEvent.getAdditionalInfo()));
				setClientHost(serviceBuilderAuditEvent::getClientHost);
				setClientIP(serviceBuilderAuditEvent::getClientIP);
				setCreator(
					() -> CreatorUtil.toCreator(
						null, _portal,
						_userLocalService.fetchUser(
							serviceBuilderAuditEvent.getUserId())));
				setDateCreated(serviceBuilderAuditEvent::getCreateDate);
				setEntityId(
					() -> GetterUtil.getLong(
						serviceBuilderAuditEvent.getClassPK()));
				setEntityType(serviceBuilderAuditEvent::getClassName);
				setEventType(serviceBuilderAuditEvent::getEventType);
				setId(serviceBuilderAuditEvent::getAuditEventId);
				setMessage(serviceBuilderAuditEvent::getMessage);
				setServerName(serviceBuilderAuditEvent::getServerName);
				setServerPort(serviceBuilderAuditEvent::getServerPort);
				setSessionId(serviceBuilderAuditEvent::getSessionID);
			}
		};
	}

	private Map<String, ?> _toMap(String additionalInfo) throws JSONException {
		if (Validator.isBlank(additionalInfo)) {
			return null;
		}

		JSONObject jsonObject = _jsonFactory.createJSONObject(additionalInfo);

		return jsonObject.toMap();
	}

	@Reference
	private AuditEventService _auditEventService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}