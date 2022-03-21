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

package com.liferay.oauth.client.admin.web.internal.portlet.action;

import com.liferay.oauth.client.admin.web.internal.constants.OAuthClientAdminPortletKeys;
import com.liferay.oauth.client.persistence.service.OAuthClientAuthServerService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	property = {
		"javax.portlet.name=" + OAuthClientAdminPortletKeys.OAUTH_CLIENT_ADMIN,
		"mvc.command.name=/oauth_client_admin/update_o_auth_client"
	},
	service = MVCRenderCommand.class
)
public class UpdateOAuthClientMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		renderRequest.setAttribute(
			"oAuthClientAuthServers",
			_oAuthClientAuthServerService.getOAuthClientAuthServers(
				themeDisplay.getCompanyId(), QueryUtil.ALL_POS,
				QueryUtil.ALL_POS));

		renderRequest.setAttribute("infoTemplate", _INFO_TEMPLATE);

		renderRequest.setAttribute(
			"requestParamsTemplate", _REQUEST_PARAMS_TEMPLATE);

		return "/admin/update_oauth_client.jsp";
	}

	private static final String _INFO_TEMPLATE = StringBundler.concat(
		"{\"example_information_returned_from_authorization_server\":\"\",\"cl",
		"ient_id\":\"\",\"client_secret\":\"\",\"example_oauth_client_metadata",
		"\":\"\",\"token_endpoint_auth_method\":\"client_secret_basic\",\"redi",
		"rect_uris\":[\"\",\"\"],\"client_name\":\"example_client_for_example_",
		"authorization_server\",\"grant_types\":[\"authorization_code\"],\"sco",
		"pe\":\"openid email profile\",\"jwks_uri\":\"https://client.example.o",
		"rg/my_public_keys.jwks\",\"example_oidc_client_metadata\":\"\",\"id_t",
		"oken_signed_response_alg\":\"RS256\",\"subject_type\":\"public\"}");

	private static final String _REQUEST_PARAMS_TEMPLATE = StringBundler.concat(
		"{\"authorization_request_parameters\":{\"resource\":[\"resource1\",\"",
		"resource2\"]},\"token_request_parameters\":{\"audience\":\"audience1",
		"\",\"resource\":[\"resource1\",\"resource2\"]}}");

	@Reference
	private OAuthClientAuthServerService _oAuthClientAuthServerService;

}