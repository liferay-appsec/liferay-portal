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

package com.liferay.oauth.resource.server.internal.rest.auth.verifier;

import com.liferay.oauth.resource.server.internal.auth.verifier.BaseAuthVerifier;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProvider;
import com.liferay.oauth2.provider.scope.liferay.ScopeContext;
import com.liferay.oauth2.provider.scope.liferay.constants.OAuth2ProviderScopeLiferayConstants;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = "auth.verifier.OAuth2RESTAuthVerifier.urls.includes=#N/A#",
	service = AuthVerifier.class
)
public class OAuth2RESTAuthVerifier extends BaseAuthVerifier {

	@Override
	public String getAuthType() {
		return OAuth2ProviderScopeLiferayConstants.AUTH_VERIFIER_OAUTH2_TYPE;
	}

	@Override
	public void postProcess(
		BearerTokenProvider.AccessToken accessToken,
		AuthVerifierResult authVerifierResult) {

		_scopeContext.setAccessToken(accessToken.getTokenKey());

		Map<String, Object> settings = authVerifierResult.getSettings();

		settings.put(
			BearerTokenProvider.AccessToken.class.getName(), accessToken);

		authVerifierResult.setState(AuthVerifierResult.State.SUCCESS);
		authVerifierResult.setUserId(accessToken.getUserId());
	}

	@Reference
	private ScopeContext _scopeContext;

}