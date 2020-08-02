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

package com.liferay.saml.saas;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.taglib.BaseJSPDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.saas.configuration.SaasConfiguration;

import java.util.Map;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marta Medio
 */
@Component(
	configurationPid = "com.liferay.saas.configuration.SaasConfiguration.scoped",
	configurationPolicy = ConfigurationPolicy.REQUIRE, service = {}
)
public class SamlSaasExportJSPDynamicInclude extends BaseJSPDynamicInclude {

	@Override
	public void register(
		DynamicInclude.DynamicIncludeRegistry dynamicIncludeRegistry) {

		dynamicIncludeRegistry.register(
			"com.liferay.saml.web#/admin/general.jsp#post");
	}

	@Activate
	@Modified
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		SaasConfiguration saasConfiguration =
			ConfigurableUtil.createConfigurable(
				SaasConfiguration.class, properties);

		boolean productionEnvironment =
			saasConfiguration.isProductionEnvironment();
		String preSharedKey = saasConfiguration.preSharedKey();
		String virtualHostURLExport = saasConfiguration.virtualHostURLExport();

		if (!productionEnvironment && !preSharedKey.isEmpty() &&
			!virtualHostURLExport.isEmpty()) {

			_serviceRegistration = bundleContext.registerService(
				DynamicInclude.class, this, new HashMapDictionary());
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	@Override
	protected String getJspPath() {
		return "/dynamic_include/export.jsp";
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.saml.saas)", unbind = "-"
	)
	protected void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SamlSaasExportJSPDynamicInclude.class);

	private ServiceRegistration<DynamicInclude> _serviceRegistration;

}