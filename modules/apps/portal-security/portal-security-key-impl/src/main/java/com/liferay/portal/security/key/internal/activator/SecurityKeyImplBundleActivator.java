/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.activator;

import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.key.internal.configuration.plugin.SecretReferenceConfigurationPluginImpl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationPlugin;

/**
 * @author Pedro Victor Silvestre
 */
public class SecurityKeyImplBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		_secretReferenceConfigurationPluginImpl =
			new SecretReferenceConfigurationPluginImpl(bundleContext);

		_secretReferenceConfigurationPluginImpl.open();

		_serviceRegistration = bundleContext.registerService(
			ConfigurationPlugin.class, _secretReferenceConfigurationPluginImpl,
			HashMapDictionaryBuilder.<String, Object>put(
				ConfigurationPlugin.CM_RANKING, 1000
			).put(
				"config.plugin.id",
				SecretReferenceConfigurationPluginImpl.class.getName()
			).build());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		_serviceRegistration.unregister();

		_secretReferenceConfigurationPluginImpl.close();
	}

	private SecretReferenceConfigurationPluginImpl
		_secretReferenceConfigurationPluginImpl;
	private ServiceRegistration<ConfigurationPlugin> _serviceRegistration;

}