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

package com.liferay.oauth2.provider.rest.internal.spi.bearer.token.provider;

import com.liferay.oauth2.provider.rest.internal.spi.bearer.token.provider.configuration.JWTDefaultBearerTokenProviderConfiguration;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProvider;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Tomas Polesovsky
 */
@Component(
	configurationPid = "com.liferay.oauth2.provider.rest.internal.spi.bearer.token.provider.configuration.JWTDefaultBearerTokenProviderConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	property = {"default=true", "token.format=jwt"},
	service = BearerTokenProvider.class
)
public class JWTBearerTokenProvider implements BearerTokenProvider {

	@Override
	public boolean isValid(AccessToken accessToken) {
		return true;
	}

	@Override
	public boolean isValid(RefreshToken refreshToken) {
		return true;
	}

	@Override
	public void onBeforeCreate(AccessToken accessToken) {
		accessToken.setExpiresIn(
			_jwtDefaultBearerTokenProviderConfiguration.accessTokenExpiresIn());
	}

	@Override
	public void onBeforeCreate(RefreshToken refreshToken) {
		_defaultBearerTokenProvider.onBeforeCreate(refreshToken);
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_jwtDefaultBearerTokenProviderConfiguration =
			ConfigurableUtil.createConfigurable(
				JWTDefaultBearerTokenProviderConfiguration.class, properties);
	}

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(token.format=opaque)"
	)
	private volatile BearerTokenProvider _defaultBearerTokenProvider;

	private JWTDefaultBearerTokenProviderConfiguration
		_jwtDefaultBearerTokenProviderConfiguration;

}