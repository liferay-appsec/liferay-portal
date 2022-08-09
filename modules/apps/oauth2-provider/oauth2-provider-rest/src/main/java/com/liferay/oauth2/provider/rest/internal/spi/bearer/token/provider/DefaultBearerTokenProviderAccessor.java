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

import com.liferay.oauth2.provider.rest.internal.configuration.OAuth2AuthorizationServerConfiguration;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProvider;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProviderAccessor;
import com.liferay.oauth2.provider.scope.liferay.ScopedServiceTrackerMap;
import com.liferay.oauth2.provider.scope.liferay.ScopedServiceTrackerMapFactory;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Tomas Polesovsky
 * @author Stian Sigvartsen
 */
@Component(immediate = true, service = BearerTokenProviderAccessor.class)
public class DefaultBearerTokenProviderAccessor
	implements BearerTokenProviderAccessor {

	@Override
	public BearerTokenProvider getBearerTokenProvider(
		long companyId, String clientId) {

		try {
			OAuth2AuthorizationServerConfiguration
				oAuth2AuthorizationServerConfiguration =
					_configurationProvider.getCompanyConfiguration(
						OAuth2AuthorizationServerConfiguration.class,
						companyId);

			if (oAuth2AuthorizationServerConfiguration.issueJWTAccessToken()) {
				return _jwtTokenFormatScopedServiceTrackerMap.getService(
					companyId, clientId);
			}

			return _opaqueTokenFormatScopedServiceTrackerMap.getService(
				companyId, clientId);
		}
		catch (ConfigurationException configurationException) {
			throw new SystemException(configurationException);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_jwtTokenFormatScopedServiceTrackerMap =
			_scopedServiceTrackerMapFactory.create(
				bundleContext, BearerTokenProvider.class,
				"liferay.oauth2.client.id", "(token.format=jwt)",
				() -> _jwtDefaultBearerTokenProvider, null);

		_opaqueTokenFormatScopedServiceTrackerMap =
			_scopedServiceTrackerMapFactory.create(
				bundleContext, BearerTokenProvider.class,
				"liferay.oauth2.client.id", "(token.format=opaque)",
				() -> _defaultBearerTokenProvider, null);
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(&(default=true)(token.format=opaque))"
	)
	private volatile BearerTokenProvider _defaultBearerTokenProvider;

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(&(default=true)(token.format=jwt))"
	)
	private volatile BearerTokenProvider _jwtDefaultBearerTokenProvider;

	private ScopedServiceTrackerMap<BearerTokenProvider>
		_jwtTokenFormatScopedServiceTrackerMap;
	private ScopedServiceTrackerMap<BearerTokenProvider>
		_opaqueTokenFormatScopedServiceTrackerMap;

	@Reference
	private ScopedServiceTrackerMapFactory _scopedServiceTrackerMapFactory;

}