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
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.constants.SamlAdminPortletKeys;
import com.liferay.saml.saas.internal.configuration.SaasConfiguration;

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

		String generalTab = ParamUtil.getString(
			renderRequest, "tabs1", "general");
		String mvcrenderCommandName = ParamUtil.getString(
			renderRequest, "mvcRenderCommandName", null);

		if (!generalTab.equals("general") || (mvcrenderCommandName != null)) {
			return;
		}

		try {
			SaasConfiguration saasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SaasConfiguration.class,
					_portal.getCompanyId(renderRequest));

			if (saasConfiguration.productionEnvironment() ||
				Validator.isBlank(saasConfiguration.preSharedKey()) ||
				Validator.isBlank(
					saasConfiguration.targetInstanceImportURL())) {

				return;
			}
		}
		catch (ConfigurationException configurationException) {
			_log.error(
				"Unable to get SaaS instance configuration",
				configurationException);

			return;
		}

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