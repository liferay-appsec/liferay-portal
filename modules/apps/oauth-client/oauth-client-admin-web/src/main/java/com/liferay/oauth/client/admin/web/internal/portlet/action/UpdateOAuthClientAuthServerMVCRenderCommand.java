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
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringBundler;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Arthur Chan
 */
@Component(
	property = {
		"javax.portlet.name=" + OAuthClientAdminPortletKeys.OAUTH_CLIENT_ADMIN,
		"mvc.command.name=/oauth_client_admin/update_o_auth_client_auth_server"
	},
	service = MVCRenderCommand.class
)
public class UpdateOAuthClientAuthServerMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		String issuer = ParamUtil.getString(renderRequest, "issuer", "");

		if (issuer.length() < 1) {
			renderRequest.setAttribute("metadataTemplate", _METADATA_TEMPLATE);
		}

		return "/admin/update_oauth_client_auth_server.jsp";
	}

	private static final String _METADATA_TEMPLATE = StringBundler.concat(
		"{\"issuer\":\"\",\"authorization_endpoint\":\"\",\"token_endpoint\":",
		"\"\",\"registration_endpoint\":\"\",\"introspection_endpoint\":\"\",",
		"\"device_authorization_endpoint\":\"\",\"revocation_endpoint\":\"\",",
		"\"pushed_authorization_request_endpoint\":\"\",\"jwks_uri\":\"\",",
		"\"userinfo_endpoint\":\"\"}");

}