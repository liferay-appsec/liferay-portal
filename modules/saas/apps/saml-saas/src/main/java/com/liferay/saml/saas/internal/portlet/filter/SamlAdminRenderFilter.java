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
import com.liferay.portal.kernel.util.ParamUtil;
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

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

		chain.doFilter(renderRequest, renderResponse);

		if (!"general".contentEquals(
				ParamUtil.getString(renderRequest, "tabs1", "general"))) {

			return;
		}

		long companyId = _portal.getCompanyId(renderRequest);

		try {
			SaasConfiguration saasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SaasConfiguration.class, companyId);

			String preSharedKey = saasConfiguration.preSharedKey();
			String virtualHostURLExport =
				saasConfiguration.targetInstanceImportURL();

			if (!saasConfiguration.productionEnvironment() &&
				!preSharedKey.isEmpty() && !virtualHostURLExport.isEmpty()) {

				RequestDispatcher requestDispatcher =
					_servletContext.getRequestDispatcher("/export.jsp");

				try {
					requestDispatcher.include(
						_portal.getHttpServletRequest(renderRequest),
						_portal.getHttpServletResponse(renderResponse));
				}
				catch (Exception exception) {
					throw new PortletException(
						"Unable to include export.jsp", exception);
				}
			}
		}
		catch (ConfigurationException configurationException) {
			_log.error(
				"Unable to get SaaS instance configuration",
				configurationException);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SamlAdminRenderFilter.class);

	@Reference
	private Portal _portal;

	@Reference(target = "(osgi.web.symbolicname=com.liferay.saml.saas)")
	private ServletContext _servletContext;

}