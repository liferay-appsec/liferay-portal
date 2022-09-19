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

package com.liferay.oauth2.provider.rest.internal.client;

import com.liferay.oauth.client.LocalOAuthClient;
import com.liferay.oauth.client.generator.LocalOAuthClientGenerator;
import com.liferay.oauth.client.grant.AuthorizationCodeGrant;
import com.liferay.oauth.client.grant.Grant;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.rest.internal.endpoint.access.token.LiferayAccessTokenService;
import com.liferay.oauth2.provider.rest.internal.endpoint.authorize.AuthorizationCodeGrantServiceRegistrator;
import com.liferay.oauth2.provider.rest.internal.endpoint.liferay.LiferayOAuthDataProvider;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.servlet.ProtectedPrincipal;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.security.Principal;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.MessageContextImpl;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(immediate = true, service = LocalOAuthClientGenerator.class)
public class LocalOAuthClientGeneratorImpl
	implements LocalOAuthClientGenerator {

	@Override
	public LocalOAuthClient generate(
			long companyId, String clientId,
			HttpServletRequest httpServletRequest)
		throws Exception {

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.getOAuth2Application(
				companyId, clientId);

		if (!oAuth2Application.isTrustedApplication()) {
			throw new IllegalArgumentException(
				"Client with Id " + clientId +
					" must be configured to skip user consent");
		}

		return new LocalOAuthClientImpl(
			_generateAuthorizationMessageContext(httpServletRequest),
			_liferayAccessTokenService, _liferayAuthorizationCodeGrantService,
			_liferayOAuthDataProvider, oAuth2Application,
			_generateTokenMessageContext(httpServletRequest));
	}

	@Override
	public LocalOAuthClient generateByExternalReferenceCode(
			long companyId, String externalReferenceCode,
			HttpServletRequest httpServletRequest)
		throws Exception {

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.
				getOAuth2ApplicationByExternalReferenceCode(
					companyId, externalReferenceCode);

		if (!oAuth2Application.isTrustedApplication()) {
			throw new IllegalArgumentException(
				"Client with external reference code " + externalReferenceCode +
					" must be configured to skip user consent");
		}

		return new LocalOAuthClientImpl(
			_generateAuthorizationMessageContext(httpServletRequest),
			_liferayAccessTokenService, _liferayAuthorizationCodeGrantService,
			_liferayOAuthDataProvider, oAuth2Application,
			_generateTokenMessageContext(httpServletRequest));
	}

	private MessageContext _generateAuthorizationMessageContext(
			HttpServletRequest httpServletRequest)
		throws Exception {

		Message message = _generateCommonMessage(httpServletRequest);

		User user = _portal.getUser(httpServletRequest);

		AuthorizationSecurityContext authorizationSecurityContext =
			new AuthorizationSecurityContext(
				new ProtectedPrincipal(String.valueOf(user.getUserId())),
				_portal.isSecure(httpServletRequest));

		message.put(
			org.apache.cxf.security.SecurityContext.class,
			authorizationSecurityContext);

		message.put(SecurityContext.class, authorizationSecurityContext);

		return new LocalMessageContextImpl(message);
	}

	private Message _generateCommonMessage(
		HttpServletRequest httpServletRequest) {

		Message message = new MessageImpl();

		Exchange exchange = new ExchangeImpl();

		exchange.setInMessage(message);

		message.put(AbstractHTTPDestination.HTTP_REQUEST, httpServletRequest);
		message.put(HttpServletRequest.class, httpServletRequest);

		return message;
	}

	private MessageContext _generateTokenMessageContext(
			HttpServletRequest httpServletRequest)
		throws Exception {

		Message message = _generateCommonMessage(httpServletRequest);

		TokenSecurityContext tokenSecurityContext = new TokenSecurityContext(
			_portal.isSecure(httpServletRequest));

		message.put(SecurityContext.class, tokenSecurityContext);

		return new LocalMessageContextImpl(message);
	}

	@Reference
	private LiferayAccessTokenService _liferayAccessTokenService;

	@Reference
	private AuthorizationCodeGrantServiceRegistrator.
		LiferayAuthorizationCodeGrantService
			_liferayAuthorizationCodeGrantService;

	@Reference
	private LiferayOAuthDataProvider _liferayOAuthDataProvider;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private Portal _portal;

	private static class AuthorizationSecurityContext
		extends TokenSecurityContext
		implements org.apache.cxf.security.SecurityContext {

		public AuthorizationSecurityContext(
			Principal principal, boolean secured) {

			super(secured);

			_principal = principal;
		}

		@Override
		public Principal getUserPrincipal() {
			return _principal;
		}

		private final Principal _principal;

	}

	private static class LocalMessageContextImpl extends MessageContextImpl {

		public LocalMessageContextImpl(Message message) {
			super(message);
		}

		@Override
		public HttpServletRequest getHttpServletRequest() {
			HttpServletRequest httpServletRequest =
				super.getHttpServletRequest();

			if (httpServletRequest == null) {
				httpServletRequest = (HttpServletRequest)get(
					HttpServletRequest.class.getName());
			}

			return httpServletRequest;
		}

	}

	private static class TokenSecurityContext implements SecurityContext {

		public TokenSecurityContext(boolean secured) {
			_secured = secured;
		}

		@Override
		public String getAuthenticationScheme() {
			return "session";
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public boolean isUserInRole(String s) {
			return false;
		}

		private final boolean _secured;

	}

	private class LocalOAuthClientImpl implements LocalOAuthClient {

		public LocalOAuthClientImpl(
			MessageContext authorizationMessageContext,
			LiferayAccessTokenService liferayAccessTokenService,
			AuthorizationCodeGrantServiceRegistrator.
				LiferayAuthorizationCodeGrantService
					liferayAuthorizationCodeGrantService,
			LiferayOAuthDataProvider liferayOAuthDataProvider,
			OAuth2Application oAuth2Application,
			MessageContext tokenMessageContext) {

			_authorizationMessageContext = authorizationMessageContext;
			_liferayAccessTokenService = liferayAccessTokenService;
			_liferayAuthorizationCodeGrantService =
				liferayAuthorizationCodeGrantService;
			_liferayOAuthDataProvider = liferayOAuthDataProvider;
			_oAuth2Application = oAuth2Application;
			_tokenMessageContext = tokenMessageContext;
		}

		@Override
		public String requestAuthorizationCode() throws Exception {
			_initMessageContext(
				_liferayAuthorizationCodeGrantService.getMessageContext(),
				_authorizationMessageContext);

			_initMessageContext(
				_liferayOAuthDataProvider.getMessageContext(),
				_authorizationMessageContext);

			MultivaluedMap<String, String> map = new MultivaluedHashMap();

			map.add("client_id", _oAuth2Application.getClientId());
			map.add("response_type", "code");

			List<String> redirect_uris =
				_oAuth2Application.getRedirectURIsList();

			map.add("redirect_uri", redirect_uris.get(0));

			Response response =
				_liferayAuthorizationCodeGrantService.authorizePost(map);

			String redirectURI = response.getHeaderString("Location");

			if (Validator.isNull(redirectURI)) {
				return null;
			}

			Matcher matcher = _authorizationCodePattern.matcher(redirectURI);

			if (!matcher.find()) {
				return null;
			}

			return matcher.group(1);
		}

		@Override
		public String requestTokens(Grant grant) throws Exception {
			_initMessageContext(
				_liferayAccessTokenService.getMessageContext(),
				_tokenMessageContext);

			_initMessageContext(
				_liferayOAuthDataProvider.getMessageContext(),
				_tokenMessageContext);

			MultivaluedMap<String, String> map = new MultivaluedHashMap();

			map.putAll(grant.toParameters());

			if (grant.getGrantType() == AuthorizationCodeGrant.GRANT_TYPE) {
				List<String> redirect_uris =
					_oAuth2Application.getRedirectURIsList();

				map.add("redirect_uri", redirect_uris.get(0));
			}

			map.add("client_id", _oAuth2Application.getClientId());

			String clientAuthenticationMethod =
				_oAuth2Application.getClientAuthenticationMethod();

			if (clientAuthenticationMethod.equals("client_secret_post")) {
				map.add("client_secret", _oAuth2Application.getClientSecret());
			}

			Response response = _liferayAccessTokenService.handleTokenRequest(
				map);

			if (response.getStatus() != HttpServletResponse.SC_OK) {
				return null;
			}

			ClientAccessToken clientAccessToken = response.readEntity(
				ClientAccessToken.class);

			return clientAccessToken.toString();
		}

		@Override
		public void setClientPrivateKey(String jsonWebKey) throws Exception {
			_jsonWebKey = JwkUtils.readJwkKey(jsonWebKey);

			if (_jsonWebKey == null) {
				throw new Exception("Invalid private key in JWK format");
			}
		}

		private void _initMessageContext(
			MessageContext messageContext,
			MessageContext generatedMessageContext) {

			ThreadLocalMessageContext threadLocalMessageContext =
				(ThreadLocalMessageContext)messageContext;

			threadLocalMessageContext.set(generatedMessageContext);
		}

		private final Pattern _authorizationCodePattern = Pattern.compile(
			"code=([^/&]+)");
		private final MessageContext _authorizationMessageContext;
		private JsonWebKey _jsonWebKey;
		private final LiferayAccessTokenService _liferayAccessTokenService;
		private final AuthorizationCodeGrantServiceRegistrator.
			LiferayAuthorizationCodeGrantService
				_liferayAuthorizationCodeGrantService;

		@Reference
		private LiferayOAuthDataProvider _liferayOAuthDataProvider;

		private final OAuth2Application _oAuth2Application;
		private final MessageContext _tokenMessageContext;

	}

}