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

package com.liferay.oauth.resource.server.internal.auth.verifier;

import com.liferay.oauth.resource.server.internal.configuration.OAuthResourceServerConfiguration;
import com.liferay.oauth.resource.server.internal.token.consumer.AccessTokenJOSEJWTConsumer;
import com.liferay.oauth2.provider.constants.OAuth2ProviderConstants;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProvider;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProviderAccessor;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.AuthException;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Arthur Chan
 */
public abstract class BaseAuthVerifier implements AuthVerifier {

	@Override
	public AuthVerifierResult verify(
			AccessControlContext accessControlContext, Properties properties)
		throws AuthException {

		AuthVerifierResult authVerifierResult = new AuthVerifierResult();

		String bearerAccessToken = _getBearerAccessToken(
			accessControlContext.getRequest());

		if (bearerAccessToken == null) {
			return authVerifierResult;
		}

		BearerTokenProvider.AccessToken liferayAccessToken =
			_getLiferayAccessToken(bearerAccessToken);

		HttpServletResponse httpServletResponse =
			accessControlContext.getResponse();

		if (liferayAccessToken == null) {
			httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			authVerifierResult.setState(
				AuthVerifierResult.State.INVALID_CREDENTIALS);

			return authVerifierResult;
		}

		OAuth2Application oAuth2Application =
			liferayAccessToken.getOAuth2Application();

		BearerTokenProvider bearerTokenProvider =
			bearerTokenProviderAccessor.getBearerTokenProvider(
				oAuth2Application.getCompanyId(),
				oAuth2Application.getClientId());

		if ((bearerTokenProvider == null) ||
			!bearerTokenProvider.isValid(liferayAccessToken)) {

			httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			authVerifierResult.setState(
				AuthVerifierResult.State.INVALID_CREDENTIALS);

			return authVerifierResult;
		}

		authVerifierResult.setState(AuthVerifierResult.State.SUCCESS);

		postProcess(liferayAccessToken, authVerifierResult);

		return authVerifierResult;
	}

	@Activate
	@SuppressWarnings("unchecked")
	protected void activate(Map<String, Object> properties) {
		_oAuthResourceServerConfiguration = ConfigurableUtil.createConfigurable(
			OAuthResourceServerConfiguration.class, properties);
	}

	protected abstract void postProcess(
		BearerTokenProvider.AccessToken accessToken,
		AuthVerifierResult authVerifierResult);

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected volatile BearerTokenProviderAccessor bearerTokenProviderAccessor;

	@Reference
	protected OAuth2ApplicationLocalService oAuth2ApplicationLocalService;

	@Reference
	protected OAuth2ApplicationScopeAliasesLocalService
		oAuth2ApplicationScopeAliasesLocalService;

	@Reference
	protected OAuth2AuthorizationLocalService oAuth2AuthorizationLocalService;

	private String _getBearerAccessToken(
		HttpServletRequest httpServletRequest) {

		String authorization = httpServletRequest.getHeader(
			HttpHeaders.AUTHORIZATION);

		if (Validator.isBlank(authorization)) {
			return null;
		}

		String[] authorizationParts = authorization.split("\\s");

		String scheme = authorizationParts[0];

		if (!StringUtil.equalsIgnoreCase(scheme, _TOKEN_TYPE)) {
			return null;
		}

		if (authorizationParts.length < 2) {
			return StringPool.BLANK;
		}

		return authorizationParts[1];
	}

	private BearerTokenProvider.AccessToken _getLiferayAccessToken(
		String bearerAccessToken) {

		if (Validator.isBlank(bearerAccessToken)) {
			return null;
		}

		if ((_oAuthResourceServerConfiguration != null) &&
			_oAuthResourceServerConfiguration.acceptJWTAccessToken()) {

			return _getLiferayAccessTokenFromJWTAccessToken(bearerAccessToken);
		}

		return _getLiferayAccessTokenFromOpaqueAccessToken(bearerAccessToken);
	}

	private BearerTokenProvider.AccessToken
		_getLiferayAccessTokenFromJWTAccessToken(String bearerAccessToken) {

		JwtToken jwtToken = _accessTokenJOSEJWTConsumer.getJwtToken(
			bearerAccessToken);

		JwtClaims jwtClaims = jwtToken.getClaims();

		OAuth2Application oAuth2Application;

		try {
			oAuth2Application =
				oAuth2ApplicationLocalService.getOAuth2Application(
					CompanyThreadLocal.getCompanyId(),
					(String)jwtClaims.getClaim("client_id"));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return null;
		}

		return new BearerTokenProvider.AccessToken(
			oAuth2Application, jwtClaims.getAudiences(), StringPool.BLANK,
			(jwtClaims.getExpiryTime() - jwtClaims.getIssuedAt()) / 1000,
			new HashMap<>(), StringPool.BLANK, StringPool.BLANK,
			jwtClaims.getIssuedAt() / 1000, jwtClaims.getIssuer(),
			StringPool.BLANK, new HashMap<>(), StringPool.BLANK,
			StringPool.BLANK, scopeAliasesList, bearerAccessToken, _TOKEN_TYPE,
			oAuth2Authorization.getUserId(), oAuth2Authorization.getUserName());
	}

	private BearerTokenProvider.AccessToken
		_getLiferayAccessTokenFromOpaqueAccessToken(String bearerAccessToken) {

		OAuth2Authorization oAuth2Authorization =
			oAuth2AuthorizationLocalService.
				fetchOAuth2AuthorizationByAccessTokenContent(bearerAccessToken);

		if (oAuth2Authorization == null) {
			return null;
		}

		bearerAccessToken = oAuth2Authorization.getAccessTokenContent();

		if (OAuth2ProviderConstants.EXPIRED_TOKEN.equals(bearerAccessToken)) {
			return null;
		}

		OAuth2Application oAuth2Application;

		try {
			oAuth2Application =
				oAuth2ApplicationLocalService.getOAuth2Application(
					oAuth2Authorization.getOAuth2ApplicationId());
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return null;
		}

		Date createDate = oAuth2Authorization.getAccessTokenCreateDate();
		Date expirationDate =
			oAuth2Authorization.getAccessTokenExpirationDate();

		long expiresIn =
			(expirationDate.getTime() - createDate.getTime()) / 1000;

		long issuedAt = createDate.getTime() / 1000;

		List<String> scopeAliasesList = Collections.emptyList();

		long oAuth2ApplicationScopeAliasesId =
			oAuth2Authorization.getOAuth2ApplicationScopeAliasesId();

		if (oAuth2ApplicationScopeAliasesId > 0) {
			scopeAliasesList =
				oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
					oAuth2ApplicationScopeAliasesId);
		}

		return new BearerTokenProvider.AccessToken(
			oAuth2Application, new ArrayList<>(), StringPool.BLANK, expiresIn,
			new HashMap<>(), StringPool.BLANK, StringPool.BLANK, issuedAt,
			StringPool.BLANK, StringPool.BLANK, new HashMap<>(),
			StringPool.BLANK, StringPool.BLANK, scopeAliasesList,
			bearerAccessToken, _TOKEN_TYPE, oAuth2Authorization.getUserId(),
			oAuth2Authorization.getUserName());
	}

	private static final String _TOKEN_TYPE = "Bearer";

	private static final Log _log = LogFactoryUtil.getLog(
		BaseAuthVerifier.class);

	@Reference
	private AccessTokenJOSEJWTConsumer _accessTokenJOSEJWTConsumer;

	private OAuthResourceServerConfiguration _oAuthResourceServerConfiguration;

}