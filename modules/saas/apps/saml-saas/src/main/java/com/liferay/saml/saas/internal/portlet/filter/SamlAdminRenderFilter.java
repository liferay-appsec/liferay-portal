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

package com.liferay.saml.saas.internal.portlet.filter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.saas.configuration.SaasConfiguration;
import com.liferay.saml.constants.SamlAdminPortletKeys;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.RenderResponseWrapper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marta Medio
 */
@Component(
	immediate = true,
	property = "javax.portlet.name=" + SamlAdminPortletKeys.SAML_ADMIN,
	service = PortletFilter.class
)
public class SamlAdminRenderFilter implements RenderFilter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(
			RenderRequest renderRequest, RenderResponse renderResponse,
			FilterChain chain)
		throws IOException, PortletException {

		RenderResponseWrapper renderResponseWrapper = new RenderResponseWrapper(
			renderResponse);

		chain.doFilter(renderRequest, renderResponseWrapper);

		long companyId = _portal.getCompanyId(renderRequest);

		try {
			SaasConfiguration saasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SaasConfiguration.class, companyId);

			if (!saasConfiguration.isProductionEnvironment()) {
				_exportSamlEntryOptionMVCRenderCommand.render(
					renderRequest, renderResponse);
			}
		}
		catch (ConfigurationException configurationException) {
			_log.error(
				"Unable to SaaS instance configuration",
				configurationException);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SamlAdminRenderFilter.class);

	@Reference(target = "(mvc.command.name=/admin/saas/saml/export/content)")
	private MVCRenderCommand _exportSamlEntryOptionMVCRenderCommand;

	@Reference
	private Portal _portal;

}