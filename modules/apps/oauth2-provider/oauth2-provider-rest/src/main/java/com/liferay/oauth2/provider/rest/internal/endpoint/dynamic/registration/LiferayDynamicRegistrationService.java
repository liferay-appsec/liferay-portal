/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration;

import com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration.model.LiferayClientRegistration;
import com.liferay.oauth2.provider.util.OAuth2SecureRandomGenerator;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.remote.cors.annotation.CORS;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthError;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistrationResponse;
import org.apache.cxf.rs.security.oauth2.services.DynamicRegistrationService;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;

/**
 * @author Jorge García Jiménez
 */
@Path("/register")
public class LiferayDynamicRegistrationService
	extends DynamicRegistrationService {

	@Consumes(MediaType.APPLICATION_JSON)
	@CORS(allowMethods = "POST")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(LiferayClientRegistration clientRegistration) {
		return super.register(clientRegistration);
	}

	@Override
	protected void checkRegistrationAccessToken(Client c, String accessToken) {
	}

	protected void fromClientRegistrationToClient(
		ClientRegistration request, Client client) {

		List<String> grantTypes = client.getAllowedGrantTypes();

		_checkValidGrantResponseTypes(grantTypes, request.getResponseTypes());

		List<String> redirectUris = request.getRedirectUris();

		if (redirectUris != null) {
			String appType = request.getApplicationType();

			if (appType == null) {
				appType = "web";
			}

			for (String redirectUri : redirectUris) {
				validateRequestUri(redirectUri, appType, grantTypes);
			}

			client.setRedirectUris(redirectUris);
		}

		if (client.getRedirectUris(
			).isEmpty() &&
			(grantTypes.contains("authorization_code") ||
			 grantTypes.contains("implicit"))) {

			OAuthError error = new OAuthError(
				"invalid_request", "A Redirection URI is required");

			_reportInvalidRequestError(error);
		}

		List<String> resourceUris = request.getResourceUris();

		if (resourceUris != null) {
			client.setRegisteredAudiences(resourceUris);
		}

		String scope = request.getScope();

		if (!StringUtils.isEmpty(scope)) {
			client.setRegisteredScopes(OAuthUtils.parseScope(scope));
		}

		String clientUri = request.getClientUri();

		if (clientUri != null) {
			client.setApplicationWebUri(clientUri);
		}

		String clientLogoUri = request.getLogoUri();

		if (clientLogoUri != null) {
			client.setApplicationLogoUri(clientLogoUri);
		}

		Map<String, String> properties = client.getProperties();

		String jwks = request.getStringProperty("jwks");

		if (Validator.isNotNull(jwks)) {
			properties.put("jwks", jwks);
		}

		String jwksUri = request.getStringProperty("jwks_uri");

		if (Validator.isNotNull(jwksUri)) {
			properties.put("jwks_uri", jwksUri);
		}

		String softwareId = request.getStringProperty("software_id");

		if (Validator.isNotNull(softwareId)) {
			properties.put("software_id", softwareId);
		}

		client.setProperties(properties);
	}

	protected ClientRegistrationResponse fromClientToRegistrationResponse(
		Client client) {

		ClientRegistrationResponse response = new ClientRegistrationResponse();

		response.setClientId(client.getClientId());

		if (Validator.isNotNull(client.getApplicationName())) {
			response.setProperty("client_name", client.getApplicationName());
		}

		if (client.getClientSecret() != null) {
			response.setClientSecret(client.getClientSecret());
			response.setClientSecretExpiresAt(0L);
		}

		response.setClientIdIssuedAt(client.getRegisteredAt());
		response.setGrantTypes(client.getAllowedGrantTypes());

		UriBuilder uriBuilder = getMessageContext(
		).getUriInfo(
		).getAbsolutePathBuilder();

		response.setRegistrationClientUri(
			uriBuilder.path(
				client.getClientId()
			).build(
				new Object[0]
			).toString());

		response.setRegistrationAccessToken(
			(String)client.getProperties(
			).get(
				"registration_access_token"
			));

		if (!client.getRegisteredScopes(
			).isEmpty()) {

			response.setProperty("scope", client.getRegisteredScopes());
		}

		Map<String, String> properties = client.getProperties();

		for (String propertyName :
				new String[] {"jwks", "jwks_uri", "software_id"}) {

			String propertyValue = properties.get(propertyName);

			if (propertyValue != null) {
				response.setProperty(propertyName, propertyValue);
			}
		}

		return response;
	}

	@Override
	protected String generateClientId() {
		return OAuth2SecureRandomGenerator.generateClientId();
	}

	@Override
	protected String generateClientSecret(ClientRegistration request) {
		return OAuth2SecureRandomGenerator.generateClientSecret();
	}

	private void _checkValidGrantResponseTypes(
		List<String> grantTypes, List<String> responseTypes) {

		if (grantTypes == null) {
			return;
		}

		List<String> allowedResponseTypeList = new ArrayList<>();

		for (String grantType : grantTypes) {
			String allowedResponseType = _responseTypeAllowedByGrantType.get(
				grantType);

			if (Validator.isNotNull(allowedResponseType)) {
				allowedResponseTypeList.add(allowedResponseType);
			}
		}

		if (ListUtil.isEmpty(responseTypes) &&
			!allowedResponseTypeList.isEmpty()) {

			OAuthError error = new OAuthError(
				"invalid_client_metadata",
				"A response type '" + allowedResponseTypeList.get(0) +
					"' is needed to match provided grant types");

			_reportInvalidRequestError(error);
		}

		if (responseTypes != null) {
			for (String responseType : responseTypes) {
				if (!allowedResponseTypeList.contains(responseType)) {
					OAuthError error = new OAuthError(
						"invalid_client_metadata",
						"Invalid response type '" + responseType +
							"' by provided grant types");

					_reportInvalidRequestError(error);
				}
			}
		}
	}

	private void _reportInvalidRequestError(OAuthError error) {
		Response.ResponseBuilder responseBuilder = JAXRSUtils.toResponseBuilder(
			400);

		responseBuilder.type(MediaType.APPLICATION_JSON);

		throw ExceptionUtils.toBadRequestException(
			(Throwable)null,
			responseBuilder.entity(
				error
			).build());
	}

	private static final Map<String, String> _responseTypeAllowedByGrantType =
		HashMapBuilder.put(
			"authorization_code", "code"
		).put(
			"implicit", "token"
		).build();

}