/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.rest.internal.graphql.query.v1_0;

import com.liferay.cookies.rest.dto.v1_0.ConsentPreference;
import com.liferay.cookies.rest.resource.v1_0.ConsentPreferenceResource;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.Generated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Christopher Kian
 * @generated
 */
@Generated("")
public class Query {

	public static void setConsentPreferenceResourceComponentServiceObjects(
		ComponentServiceObjects<ConsentPreferenceResource>
			consentPreferenceResourceComponentServiceObjects) {

		_consentPreferenceResourceComponentServiceObjects =
			consentPreferenceResourceComponentServiceObjects;
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {consentPreferences{items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrieves the consent preferences of the user who made the request."
	)
	public ConsentPreferencePage consentPreferences() throws Exception {
		return _applyComponentServiceObjects(
			_consentPreferenceResourceComponentServiceObjects,
			this::_populateResourceContext,
			consentPreferenceResource -> new ConsentPreferencePage(
				consentPreferenceResource.getConsentPreferencesPage()));
	}

	@GraphQLName("ConsentPreferencePage")
	public class ConsentPreferencePage {

		public ConsentPreferencePage(Page consentPreferencePage) {
			actions = consentPreferencePage.getActions();

			items = consentPreferencePage.getItems();
			lastPage = consentPreferencePage.getLastPage();
			page = consentPreferencePage.getPage();
			pageSize = consentPreferencePage.getPageSize();
			totalCount = consentPreferencePage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map<String, String>> actions;

		@GraphQLField
		protected java.util.Collection<ConsentPreference> items;

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

	private void _populateResourceContext(
			ConsentPreferenceResource consentPreferenceResource)
		throws Exception {

		consentPreferenceResource.setContextAcceptLanguage(_acceptLanguage);
		consentPreferenceResource.setContextCompany(_company);
		consentPreferenceResource.setContextHttpServletRequest(
			_httpServletRequest);
		consentPreferenceResource.setContextHttpServletResponse(
			_httpServletResponse);
		consentPreferenceResource.setContextUriInfo(_uriInfo);
		consentPreferenceResource.setContextUser(_user);
		consentPreferenceResource.setGroupLocalService(_groupLocalService);
		consentPreferenceResource.setResourceActionLocalService(
			_resourceActionLocalService);
		consentPreferenceResource.setResourcePermissionLocalService(
			_resourcePermissionLocalService);
		consentPreferenceResource.setRoleLocalService(_roleLocalService);
	}

	private static ComponentServiceObjects<ConsentPreferenceResource>
		_consentPreferenceResourceComponentServiceObjects;

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