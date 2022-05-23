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
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	property = {
		"javax.portlet.name=" + OAuthClientAdminPortletKeys.OAUTH_CLIENT_ADMIN,
		"mvc.command.name=/oauth_client_admin/update_o_auth_client_as_local_metadata"
	},
	service = MVCActionCommand.class
)
public class UpdateOAuthClientASLocalMetadataMVCActionCommand
	implements MVCActionCommand {

	@Override
	public boolean processAction(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long oAuthClientASLocalMetadataId = ParamUtil.getLong(
			actionRequest, "oAuthClientASLocalMetadataId");

		String metadataJSON = ParamUtil.getString(
			actionRequest, "metadataJSON");

		try {
			if (oAuthClientASLocalMetadataId > 0) {
				_oAuthClientASLocalMetadataService.
					updateOAuthClientASLocalMetadata(
						oAuthClientASLocalMetadataId, metadataJSON,
						"openid-configuration");
			}
			else {
				_oAuthClientASLocalMetadataService.
					addOAuthClientASLocalMetadata(
						themeDisplay.getUserId(), metadataJSON,
						"openid-configuration");
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			Class<?> peClass = portalException.getClass();

			SessionErrors.add(
				actionRequest, peClass.getName(), portalException);
		}

		actionResponse.setRenderParameter(
			"redirect",
			ParamUtil.get(actionRequest, "backURL", StringPool.BLANK));

		actionResponse.setRenderParameter(
			"navigation", "oauth-client-as-local-metadata");

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpdateOAuthClientASLocalMetadataMVCActionCommand.class);

	@Reference
	private OAuthClientASLocalMetadataService
		_oAuthClientASLocalMetadataService;

}