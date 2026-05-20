/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.rest.internal.graphql.query.v1_0;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.security.audit.rest.dto.v1_0.AuditEvent;
import com.liferay.portal.security.audit.rest.resource.v1_0.AuditEventResource;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.annotation.Generated;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.ws.rs.core.UriInfo;

import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Rafael Praxedes
 * @generated
 */
@Generated("")
public class Query {

	public static void setAuditEventResourceComponentServiceObjects(
		ComponentServiceObjects<AuditEventResource>
			auditEventResourceComponentServiceObjects) {

		_auditEventResourceComponentServiceObjects =
			auditEventResourceComponentServiceObjects;
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {auditEvent(auditEventId: ___){accountEntryId, additionalInfo, clientHost, clientIP, creator, dateCreated, entityId, entityType, eventType, id, message, serverName, serverPort, sessionId}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrieves a single audit log entry by ID, including the full structured payload."
	)
	public AuditEvent auditEvent(@GraphQLName("auditEventId") Long auditEventId)
		throws Exception {

		return _applyComponentServiceObjects(
			_auditEventResourceComponentServiceObjects,
			this::_populateResourceContext,
			auditEventResource -> auditEventResource.getAuditEvent(
				auditEventId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {auditEvents(accountEntryId: ___, endDate: ___, entityId: ___, entityType: ___, eventType: ___, filter: ___, page: ___, pageSize: ___, search: ___, sorts: ___, startDate: ___, userId: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrieves a paginated list of audit log entries, scoped to the caller's accessible accounts."
	)
	public AuditEventPage auditEvents(
			@GraphQLName("accountEntryId") Long accountEntryId,
			@GraphQLName("endDate") Date endDate,
			@GraphQLName("entityId") Long entityId,
			@GraphQLName("entityType") String entityType,
			@GraphQLName("eventType") String eventType,
			@GraphQLName("search") String search,
			@GraphQLName("startDate") Date startDate,
			@GraphQLName("userId") Long userId,
			@GraphQLName("filter") String filterString,
			@GraphQLName("pageSize") int pageSize,
			@GraphQLName("page") int page,
			@GraphQLName("sort") String sortsString)
		throws Exception {

		return _applyComponentServiceObjects(
			_auditEventResourceComponentServiceObjects,
			this::_populateResourceContext,
			auditEventResource -> new AuditEventPage(
				auditEventResource.getAuditEventsPage(
					accountEntryId, endDate, entityId, entityType, eventType,
					search, startDate, userId,
					_filterBiFunction.apply(auditEventResource, filterString),
					Pagination.of(page, pageSize),
					_sortsBiFunction.apply(auditEventResource, sortsString))));
	}

	@GraphQLName("AuditEventPage")
	public class AuditEventPage {

		public AuditEventPage(Page auditEventPage) {
			actions = auditEventPage.getActions();

			items = auditEventPage.getItems();
			lastPage = auditEventPage.getLastPage();
			page = auditEventPage.getPage();
			pageSize = auditEventPage.getPageSize();
			totalCount = auditEventPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map<String, String>> actions;

		@GraphQLField
		protected java.util.Collection<AuditEvent> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	private <T, R, E1 extends Throwable, E2 extends Throwable> R
			_applyComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeFunction<T, R, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			return unsafeFunction.apply(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(AuditEventResource auditEventResource)
		throws Exception {

		auditEventResource.setContextAcceptLanguage(_acceptLanguage);
		auditEventResource.setContextCompany(_company);
		auditEventResource.setContextHttpServletRequest(_httpServletRequest);
		auditEventResource.setContextHttpServletResponse(_httpServletResponse);
		auditEventResource.setContextUriInfo(_uriInfo);
		auditEventResource.setContextUser(_user);
		auditEventResource.setGroupLocalService(_groupLocalService);
		auditEventResource.setResourceActionLocalService(
			_resourceActionLocalService);
		auditEventResource.setResourcePermissionLocalService(
			_resourcePermissionLocalService);
		auditEventResource.setRoleLocalService(_roleLocalService);
	}

	private static ComponentServiceObjects<AuditEventResource>
		_auditEventResourceComponentServiceObjects;

	private AcceptLanguage _acceptLanguage;
	private com.liferay.portal.kernel.model.Company _company;
	private BiFunction
		<Object, String, com.liferay.portal.kernel.search.filter.Filter>
			_filterBiFunction;
	private GroupLocalService _groupLocalService;
	private HttpServletRequest _httpServletRequest;
	private HttpServletResponse _httpServletResponse;
	private ResourceActionLocalService _resourceActionLocalService;
	private ResourcePermissionLocalService _resourcePermissionLocalService;
	private RoleLocalService _roleLocalService;
	private BiFunction<Object, String, com.liferay.portal.kernel.search.Sort[]>
		_sortsBiFunction;
	private UriInfo _uriInfo;
	private com.liferay.portal.kernel.model.User _user;

}
// LIFERAY-REST-BUILDER-HASH:1294345883