/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.saml.web.internal.portlet.action;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.constants.MVCRenderConstants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.saml.constants.SamlPortletKeys;
import com.liferay.saml.constants.SamlWebKeys;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.servlet.profile.SingleLogoutProfile;
import com.liferay.saml.util.JspUtil;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	property = {
		"javax.portlet.name=" + SamlPortletKeys.SAML,
		"mvc.command.name=/saml/slo_sp_status"
	},
	service = MVCRenderCommand.class
)
public class SloSpStatusMVCRenderCommand extends BaseSamlMVCRenderCommand {

	@Reference(unbind = "-")
	public void setSamlProviderConfigurationHelper(
		SamlProviderConfigurationHelper samlProviderConfigurationHelper) {

		super.setSamlProviderConfigurationHelper(
			samlProviderConfigurationHelper);
	}

	@Override
	protected String doRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws Exception {

		if (!samlProviderConfigurationHelper.isRoleIdp()) {
			return MVCRenderConstants.MVC_PATH_VALUE_SKIP_DISPATCH;
		}

		_singleLogoutProfile.processIdpSpLogout(
			_portal.getHttpServletRequest(renderRequest),
			_portal.getHttpServletResponse(renderResponse));

		if (renderRequest.getAttribute(SamlWebKeys.SAML_SLO_REQUEST_INFO) ==
				null) {

			return MVCRenderConstants.MVC_PATH_VALUE_SKIP_DISPATCH;
		}

		return JspUtil.PATH_PORTAL_SAML_SLO_SP_STATUS;
	}

	@Reference
	private Portal _portal;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SingleLogoutProfile _singleLogoutProfile;

}