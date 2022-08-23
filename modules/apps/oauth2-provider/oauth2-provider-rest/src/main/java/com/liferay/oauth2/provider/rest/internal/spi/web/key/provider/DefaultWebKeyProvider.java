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

package com.liferay.oauth2.provider.rest.internal.spi.web.key.provider;

import com.liferay.oauth2.provider.rest.internal.configuration.DefaultWebKeyProviderConfiguration;
import com.liferay.oauth2.provider.rest.spi.web.key.provider.WebKeyProvider;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * @author Stian Sigvartsen
 */
@Component(
	configurationPid = "com.liferay.oauth2.provider.rest.internal.configuration.DefaultWebKeyProviderConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	property = "name=default", service = WebKeyProvider.class
)
public class DefaultWebKeyProvider implements WebKeyProvider {

	public void activate(Map<String, Object> properties) {
		_defaultWebKeyProviderConfiguration =
			ConfigurableUtil.createConfigurable(
				DefaultWebKeyProviderConfiguration.class, properties);
	}

	@Override
	public String getWebKey() {
		return _defaultWebKeyProviderConfiguration.
			jwtAccessTokenSigningJSONWebKey();
	}

	private DefaultWebKeyProviderConfiguration
		_defaultWebKeyProviderConfiguration;

}