/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.web.internal.portlet.action;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.saml.constants.SamlPortletKeys;
import com.liferay.saml.constants.SamlWebKeys;
import com.liferay.saml.runtime.certificate.CertificateTool;

import jakarta.portlet.PortletException;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stian Sigvartsen
 */
@Component(
	property = {
		"jakarta.portlet.name=" + SamlPortletKeys.SAML_ADMIN,
		"mvc.command.name=/admin/update_certificate"
	},
	service = MVCRenderCommand.class
)
public class UpdateCertificateMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		renderRequest.setAttribute(
			SamlWebKeys.SAML_CERTIFICATE_TOOL, _certificateTool);

		if (PropsValues.PORTAL_SECURITY_FIPS_MODE_ENABLED) {
			renderRequest.setAttribute(
				"certificateKeyAlgorithms", _FIPS_KEY_ALGORITHMS);
			renderRequest.setAttribute(
				"certificateKeySizes", _FIPS_KEY_SIZES);
		}
		else {
			renderRequest.setAttribute(
				"certificateKeyAlgorithms", _DEFAULT_KEY_ALGORITHMS);
			renderRequest.setAttribute(
				"certificateKeySizes", _DEFAULT_KEY_SIZES);
		}

		return "/admin/update_certificate.jsp";
	}

	private static final String[] _DEFAULT_KEY_ALGORITHMS = {"RSA", "DSA"};

	private static final String[] _DEFAULT_KEY_SIZES =
		{"4096", "2048", "1024", "512"};

	private static final String[] _FIPS_KEY_ALGORITHMS = {"RSA"};

	private static final String[] _FIPS_KEY_SIZES =
		{"4096", "3072", "2048"};

	@Reference
	private CertificateTool _certificateTool;

}