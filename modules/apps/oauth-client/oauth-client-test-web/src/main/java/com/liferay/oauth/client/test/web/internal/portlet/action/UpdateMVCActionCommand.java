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
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
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

	@Reference
	OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Override
	public boolean processAction(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		try {
			User user = _portal.getUser(actionRequest);

			OAuth2Application oAuth2Application =
				_oAuth2ApplicationLocalService.
					getOAuth2ApplicationByExternalReferenceCode(
						CompanyThreadLocal.getCompanyId(),
						ParamUtil.getString(
							actionRequest, "clientExternalReferenceCode"));

			String tokens = _localOAuthClient.requestTokens(
				user.getUserId(), oAuth2Application);

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
	private LocalOAuthClient _localOAuthClient;

	@Reference
	private Portal _portal;

}