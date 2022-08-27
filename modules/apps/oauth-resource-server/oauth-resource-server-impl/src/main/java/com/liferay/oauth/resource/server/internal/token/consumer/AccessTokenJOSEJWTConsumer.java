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

package com.liferay.oauth.resource.server.internal.token.consumer;

import com.liferay.oauth.resource.server.internal.configuration.OAuthResourceServerConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.Validator;

import java.net.URL;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.json.basic.JsonMapObjectReaderWriter;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKeys;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsSignatureVerifier;
import org.apache.cxf.rs.security.jose.jws.JwsUtils;
import org.apache.cxf.rs.security.jose.jwt.JoseJwtConsumer;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtException;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.rs.security.oauth2.services.AuthorizationMetadata;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * @author Arthur Chan
 */
@Component(
	configurationPid = "com.liferay.oauth.resource.server.internal.configuration.OAuthResourceServerConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	service = AccessTokenJOSEJWTConsumer.class
)
public class AccessTokenJOSEJWTConsumer extends JoseJwtConsumer {

	@Activate
	@SuppressWarnings("unchecked")
	protected void activate(Map<String, Object> properties) {
		_oAuthResourceServerConfiguration = ConfigurableUtil.createConfigurable(
			OAuthResourceServerConfiguration.class, properties);

		if (Validator.isNotNull(
				_oAuthResourceServerConfiguration.
					authorizationServerWellKnownURI())) {

			try {
				_reload();
			}
			catch (Exception exception) {
				throw new IllegalArgumentException(exception);
			}

			return;
		}

		_authorizationServerIssuer =
			_oAuthResourceServerConfiguration.authorizationServerIssuer();
		_authorizationServerJsonWebKeys = JwkUtils.readJwkSet(
			_oAuthResourceServerConfiguration.
				authorizationServerJSONWebKeySet());
	}

	protected JwsSignatureVerifier getInitializedSignatureVerifier(
		JwsHeaders jwsHeaders) {

		return JwsUtils.getSignatureVerifier(
			_authorizationServerJsonWebKeys.getKey(jwsHeaders.getKeyId()));
	}

	protected void validateToken(JwtToken jwtToken) {
		JwsHeaders jwsHeaders = jwtToken.getJwsHeaders();

		String type = String.valueOf(jwsHeaders.getType());

		if ((type == null) ||
			(!type.equals("at+jwt") && !type.equals("application/at+jwt"))) {

			throw new JwtException("Not a JWT access token");
		}

		JwtClaims jwtClaims = jwtToken.getClaims();

		if (!_authorizationServerIssuer.equals(jwtClaims.getIssuer())) {
			throw new JwtException(
				"Access token not issued by configured authorization server");
		}

		// TODO: add audience validation

		if (_isExpired(jwtClaims.getExpiryTime())) {
			throw new JwtException("Access token Expired");
		}
	}

	private AuthorizationMetadata _getAuthorizationMetadata(
		String wellKnownURI) {

		WebClient webClient = WebClient.create(wellKnownURI);

		webClient = webClient.accept(MediaType.APPLICATION_JSON);

		Response response = webClient.get();

		if (Response.Status.OK.getStatusCode() != response.getStatus()) {
			throw new IllegalArgumentException(
				"Unable to get server metadata from wellKnown URI: " +
					wellKnownURI);
		}

		JsonMapObjectReaderWriter jsonMapObjectReaderWriter =
			new JsonMapObjectReaderWriter();

		return new AuthorizationMetadata(
			jsonMapObjectReaderWriter.fromJson(
				response.readEntity(String.class)));
	}

	private boolean _isExpired(long expiry) {
		expiry *= 1000;

		if ((expiry < 1) || (expiry < System.currentTimeMillis())) {
			return true;
		}

		return false;
	}

	private void _reload() throws Exception {
		AuthorizationMetadata authorizationMetadata = _getAuthorizationMetadata(
			_oAuthResourceServerConfiguration.
				authorizationServerWellKnownURI());

		_authorizationServerIssuer = String.valueOf(
			authorizationMetadata.getIssuer());

		URL jwksURL = authorizationMetadata.getJwksURL();

		_authorizationServerJsonWebKeys = JwkUtils.readJwkSet(jwksURL.toURI());
	}

	private String _authorizationServerIssuer;
	private JsonWebKeys _authorizationServerJsonWebKeys;
	private OAuthResourceServerConfiguration _oAuthResourceServerConfiguration;

}