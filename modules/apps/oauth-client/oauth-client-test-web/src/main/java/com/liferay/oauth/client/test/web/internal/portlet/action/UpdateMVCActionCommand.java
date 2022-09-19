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

package com.liferay.oauth.client.test.web.internal.portlet.action;

import com.liferay.oauth.client.LocalOAuthClient;
import com.liferay.oauth.client.generator.LocalOAuthClientGenerator;
import com.liferay.oauth.client.grant.AuthorizationCodeGrant;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	property = {
		"javax.portlet.name=com_liferay_oauth_client_test_web_internal_portlet_OAuthClientTestPortlet",
		"mvc.command.name=/oauth_client_test/update"
	},
	service = MVCActionCommand.class
)
public class UpdateMVCActionCommand implements MVCActionCommand {

	@Override
	public boolean processAction(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		try {
			LocalOAuthClient localOAuthClient =
				_localOAuthClientGenerator.generateByExternalReferenceCode(
					CompanyThreadLocal.getCompanyId(),
					ParamUtil.getString(
						actionRequest, "clientExternalReferenceCode"),
					_portal.getHttpServletRequest(actionRequest));

			String authorizationCode =
				localOAuthClient.requestAuthorizationCode();

			System.out.println(
				"received authorization code: " + authorizationCode);

			AuthorizationCodeGrant authorizationCodeGrant =
				new AuthorizationCodeGrant(authorizationCode, null, null);

			String tokens = localOAuthClient.requestTokens(
				authorizationCodeGrant);

			System.out.println("received tokens: " + tokens);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to generate local oauth client: " +
					exception.getMessage());
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpdateMVCActionCommand.class);

	@Reference
	private LocalOAuthClientGenerator _localOAuthClientGenerator;

	@Reference
	private Portal _portal;

}