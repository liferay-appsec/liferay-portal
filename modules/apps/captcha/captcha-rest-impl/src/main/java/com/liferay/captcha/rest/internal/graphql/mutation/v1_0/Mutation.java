/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.internal.graphql.mutation.v1_0;

import com.liferay.captcha.rest.dto.v1_0.SimpleCaptchaForm;
import com.liferay.captcha.rest.resource.v1_0.SimpleCaptchaResource;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineExportTaskResource;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineImportTaskResource;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;

import java.util.function.BiFunction;

import javax.annotation.Generated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Loc Pham
 * @generated
 */
@Generated("")
public class Mutation {

	public static void setSimpleCaptchaResourceComponentServiceObjects(
		ComponentServiceObjects<SimpleCaptchaResource>
			simpleCaptchaResourceComponentServiceObjects) {

		_simpleCaptchaResourceComponentServiceObjects =
			simpleCaptchaResourceComponentServiceObjects;
	}

	@GraphQLField
	public boolean createSimpleCaptcha(
			@GraphQLName("simpleCaptchaForm") SimpleCaptchaForm
				simpleCaptchaForm)
		throws Exception {

		_applyVoidComponentServiceObjects(
			_simpleCaptchaResourceComponentServiceObjects,
			this::_populateResourceContext,
			simpleCaptchaResource -> simpleCaptchaResource.postSimpleCaptcha(
				simpleCaptchaForm));

		return true;
	}

	@GraphQLField
	public Response createSimpleCaptchaBatch(
			@GraphQLName("simpleCaptchaForm") SimpleCaptchaForm
				simpleCaptchaForm,
			@GraphQLName("callbackURL") String callbackURL,
			@GraphQLName("object") Object object)
		throws Exception {

		return _applyComponentServiceObjects(
			_simpleCaptchaResourceComponentServiceObjects,
			this::_populateResourceContext,
			simpleCaptchaResource ->
				simpleCaptchaResource.postSimpleCaptchaBatch(
					simpleCaptchaForm, callbackURL, object));
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

	private <T, E1 extends Throwable, E2 extends Throwable> void
			_applyVoidComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeConsumer<T, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			unsafeFunction.accept(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(
			SimpleCaptchaResource simpleCaptchaResource)
		throws Exception {

		simpleCaptchaResource.setContextAcceptLanguage(_acceptLanguage);
		simpleCaptchaResource.setContextCompany(_company);
		simpleCaptchaResource.setContextHttpServletRequest(_httpServletRequest);
		simpleCaptchaResource.setContextHttpServletResponse(
			_httpServletResponse);
		simpleCaptchaResource.setContextUriInfo(_uriInfo);
		simpleCaptchaResource.setContextUser(_user);
		simpleCaptchaResource.setGroupLocalService(_groupLocalService);
		simpleCaptchaResource.setRoleLocalService(_roleLocalService);

		simpleCaptchaResource.setVulcanBatchEngineExportTaskResource(
			_vulcanBatchEngineExportTaskResource);

		simpleCaptchaResource.setVulcanBatchEngineImportTaskResource(
			_vulcanBatchEngineImportTaskResource);
	}

	private static ComponentServiceObjects<SimpleCaptchaResource>
		_simpleCaptchaResourceComponentServiceObjects;

	private AcceptLanguage _acceptLanguage;
	private com.liferay.portal.kernel.model.Company _company;
	private GroupLocalService _groupLocalService;
	private HttpServletRequest _httpServletRequest;
	private HttpServletResponse _httpServletResponse;
	private RoleLocalService _roleLocalService;
	private BiFunction<Object, String, Sort[]> _sortsBiFunction;
	private UriInfo _uriInfo;
	private com.liferay.portal.kernel.model.User _user;
	private VulcanBatchEngineExportTaskResource
		_vulcanBatchEngineExportTaskResource;
	private VulcanBatchEngineImportTaskResource
		_vulcanBatchEngineImportTaskResource;

}