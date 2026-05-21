/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.rest.internal.configuration.DynamicRegistrationConfiguration;
import com.liferay.oauth2.provider.rest.internal.endpoint.constants.OAuth2ProviderRESTEndpointConstants;
import com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration.container.request.filter.DynamicRegistrationServiceContainerRequestFilter;
import com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration.model.LiferayClientRegistration;
import com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration.model.LiferayClientRegistrationResponse;
import com.liferay.oauth2.provider.rest.internal.endpoint.util.OAuth2ErrorUtil;
import com.liferay.oauth2.provider.util.OAuth2SecureRandomGenerator;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthError;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;
import org.apache.cxf.rs.security.oauth2.services.DynamicRegistrationService;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;

/**
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
@Path("/register")
public class LiferayDynamicRegistrationService
	extends DynamicRegistrationService {

	@DELETE
	@Path("{clientId}")
	public Response deleteClientRegistration(
		@PathParam("clientId") String clientId) {

		super.deleteClientRegistration(clientId);

		Response.ResponseBuilder responseBuilder = JAXRSUtils.toResponseBuilder(
			204);

		return responseBuilder.build();
	}

	@GET
	@Override
	@Path("{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public ClientRegistration readClientRegistrationWithPath(
		@PathParam("clientId") String clientId) {

		return super.readClientRegistrationWithPath(clientId);
	}

	@GET
	@Override
	@Produces(MediaType.APPLICATION_JSON)
	public ClientRegistration readClientRegistrationWithQuery(
		@QueryParam("client_id") String clientId) {

		return super.readClientRegistrationWithQuery(clientId);
	}

	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(
		LiferayClientRegistration liferayClientRegistration) {

		try {
			Response response = super.register(liferayClientRegistration);

			_auditRegistrationSuccess(response);

			return response;
		}
		catch (WebApplicationException webApplicationException) {
			_auditRegistrationFailure(
				liferayClientRegistration, webApplicationException);

			throw webApplicationException;
		}
	}

	public void setConfigurationProvider(
		ConfigurationProvider configurationProvider) {

		_configurationProvider = configurationProvider;
	}

	public void setPortal(Portal portal) {
		_portal = portal;
	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public ClientRegistration updateClientRegistration(
		@PathParam("clientId") String clientId,
		LiferayClientRegistration liferayClientRegistration) {

		return super.updateClientRegistration(
			clientId, liferayClientRegistration);
	}

	@Override
	protected void checkRegistrationAccessToken(
		Client client, String accessToken) {
	}

	@Override
	protected String createRegAccessToken(Client client) {
		String registrationAccessToken = OAuthUtils.generateRandomTokenKey();

		Map<String, String> properties = client.getProperties();

		properties.put(
			"registration_access_token", "reg-" + registrationAccessToken);

		return registrationAccessToken;
	}

	@Override
	protected void fromClientRegistrationToClient(
		ClientRegistration clientRegistration, Client client) {

		MessageContext messageContext = getMessageContext();

		Object anonymous = messageContext.getHttpServletRequest(
		).getAttribute(
			DynamicRegistrationServiceContainerRequestFilter.
				REQUEST_PROPERTY_ANONYMOUS_REGISTRATION
		);

		if (Boolean.TRUE.equals(anonymous)) {
			Map<String, String> clientProperties = client.getProperties();

			clientProperties.put(
				OAuth2ProviderRESTEndpointConstants.
					PROPERTY_KEY_DYNAMIC_REGISTRATION_MODE,
				OAuth2ProviderRESTEndpointConstants.
					PROPERTY_VALUE_DYNAMIC_REGISTRATION_MODE_ANONYMOUS);
		}

		_validate(client, clientRegistration);

		_promotePublicClientAuthorizationCode(client);

		client.setApplicationName(clientRegistration.getClientName());

		clientRegistration.setApplicationType(
			_getApplicationType(clientRegistration));

		List<String> redirectUris = clientRegistration.getRedirectUris();

		if (redirectUris != null) {
			client.setRedirectUris(redirectUris);
		}

		Map<String, String> properties = client.getProperties();

		properties.put(
			"application_type", clientRegistration.getApplicationType());

		String jwks = clientRegistration.getStringProperty("jwks");

		if (Validator.isNotNull(jwks)) {
			properties.put("jwks", jwks);
		}

		String jwksURI = clientRegistration.getStringProperty("jwks_uri");

		if (Validator.isNotNull(jwksURI)) {
			properties.put("jwks_uri", jwksURI);
		}

		String softwareId = clientRegistration.getStringProperty("software_id");

		if (Validator.isNotNull(softwareId)) {
			properties.put("software_id", softwareId);
		}

		String tosUri = clientRegistration.getTosUri();

		if (Validator.isNotNull(tosUri)) {
			properties.put("tos_uri", tosUri);
		}

		String logoUri = clientRegistration.getLogoUri();

		if (Validator.isNotNull(logoUri)) {
			client.setApplicationLogoUri(logoUri);
		}

		String clientUri = clientRegistration.getClientUri();

		if (clientUri != null) {
			client.setApplicationWebUri(clientUri);
		}

		List<String> resourceUris = clientRegistration.getResourceUris();

		if (resourceUris != null) {
			client.setRegisteredAudiences(resourceUris);
		}

		String scope = clientRegistration.getScope();

		if (!Validator.isBlank(scope)) {
			client.setRegisteredScopes(OAuthUtils.parseScope(scope));
		}
	}

	@Override
	protected LiferayClientRegistrationResponse
		fromClientToRegistrationResponse(Client client) {

		LiferayClientRegistrationResponse liferayClientRegistrationResponse =
			new LiferayClientRegistrationResponse();

		liferayClientRegistrationResponse.setClientId(client.getClientId());
		liferayClientRegistrationResponse.setClientIdIssuedAt(
			client.getRegisteredAt());

		if (Validator.isNotNull(client.getApplicationName())) {
			liferayClientRegistrationResponse.setClientName(
				client.getApplicationName());
		}

		if (client.getClientSecret() != null) {
			liferayClientRegistrationResponse.setClientSecret(
				client.getClientSecret());
			liferayClientRegistrationResponse.setClientSecretExpiresAt(0L);
		}

		liferayClientRegistrationResponse.setGrantTypes(
			_toWireGrantTypes(client.getAllowedGrantTypes()));
		liferayClientRegistrationResponse.setLogoUri(
			client.getApplicationLogoUri());
		liferayClientRegistrationResponse.setRedirectUris(
			client.getRedirectUris());

		Map<String, String> properties = client.getProperties();

		if (properties.get("jwks") != null) {
			liferayClientRegistrationResponse.setJwks(properties.get("jwks"));
		}

		if (properties.get("jwks_uri") != null) {
			liferayClientRegistrationResponse.setJwksUri(
				properties.get("jwks_uri"));
		}

		liferayClientRegistrationResponse.setRegistrationAccessToken(
			properties.get("registration_access_token"));

		MessageContext messageContext = getMessageContext();

		UriInfo uriInfo = messageContext.getUriInfo();

		UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();

		liferayClientRegistrationResponse.setRegistrationClientUri(
			uriBuilder.path(
				client.getClientId()
			).build(
				new Object[0]
			).toString());

		if (ListUtil.isNotEmpty(client.getRegisteredScopes())) {
			liferayClientRegistrationResponse.setScope(
				client.getRegisteredScopes());
		}

		if (properties.get("software_id") != null) {
			liferayClientRegistrationResponse.setSoftwareId(
				properties.get("software_id"));
		}

		if (properties.get("tos_uri") != null) {
			liferayClientRegistrationResponse.setTosUri(
				properties.get("tos_uri"));
		}

		return liferayClientRegistrationResponse;
	}

	@Override
	protected String generateClientId() {
		return OAuth2SecureRandomGenerator.generateClientId();
	}

	@Override
	protected String generateClientSecret(
		ClientRegistration clientRegistration) {

		return OAuth2SecureRandomGenerator.generateClientSecret();
	}

	private void _auditRegistrationFailure(
		LiferayClientRegistration liferayClientRegistration,
		WebApplicationException webApplicationException) {

		AuditRouter auditRouter = _auditRouterSnapshot.get();

		if (auditRouter == null) {
			return;
		}

		try {
			String error = "server_error";
			String errorDescription = webApplicationException.getMessage();

			Response response = webApplicationException.getResponse();

			Object entity = null;

			if (response != null) {
				entity = response.getEntity();
			}

			if (entity instanceof OAuthError) {
				OAuthError oAuthError = (OAuthError)entity;

				error = GetterUtil.getString(oAuthError.getError(), error);

				if (Validator.isNotNull(oAuthError.getErrorDescription())) {
					errorDescription = oAuthError.getErrorDescription();
				}
			}

			String requestedClientName = null;
			JSONArray requestedGrantTypesJSONArray = _toJSONArray(null);
			JSONArray requestedRedirectUrisJSONArray = _toJSONArray(null);
			String requestedScope = null;

			if (liferayClientRegistration != null) {
				requestedClientName = liferayClientRegistration.getClientName();
				requestedGrantTypesJSONArray = _toJSONArray(
					liferayClientRegistration.getGrantTypes());
				requestedRedirectUrisJSONArray = _toJSONArray(
					liferayClientRegistration.getRedirectUris());
				requestedScope = liferayClientRegistration.getScope();
			}

			AuditMessage auditMessage = new AuditMessage(
				_EVENT_TYPE_DCR_REJECT, _getCompanyId(), 0, 0, StringPool.BLANK,
				OAuth2Application.class.getName(), StringPool.BLANK,
				StringPool.BLANK, null,
				_baseAuditInfo(
				).put(
					"error", error
				).put(
					"errorDescription", errorDescription
				).put(
					"requestedClientName", requestedClientName
				).put(
					"requestedGrantTypes", requestedGrantTypesJSONArray
				).put(
					"requestedRedirectUris", requestedRedirectUrisJSONArray
				).put(
					"requestedScope", requestedScope
				));

			auditRouter.route(auditMessage);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private void _auditRegistrationSuccess(Response response) {
		AuditRouter auditRouter = _auditRouterSnapshot.get();

		if (auditRouter == null) {
			return;
		}

		try {
			Object entity = null;

			if (response != null) {
				entity = response.getEntity();
			}

			if (!(entity instanceof LiferayClientRegistrationResponse)) {
				return;
			}

			LiferayClientRegistrationResponse
				liferayClientRegistrationResponse =
					(LiferayClientRegistrationResponse)entity;

			String scope = null;

			if (liferayClientRegistrationResponse.getScope() != null) {
				scope = String.join(
					StringPool.SPACE,
					liferayClientRegistrationResponse.getScope());
			}

			AuditMessage auditMessage = new AuditMessage(
				"ADD", _getCompanyId(), 0, 0, StringPool.BLANK,
				OAuth2Application.class.getName(),
				GetterUtil.getString(
					liferayClientRegistrationResponse.getClientId()),
				StringPool.BLANK, null,
				_baseAuditInfo(
				).put(
					"clientId", liferayClientRegistrationResponse.getClientId()
				).put(
					"clientName",
					liferayClientRegistrationResponse.getClientName()
				).put(
					"grantTypes",
					_toJSONArray(
						liferayClientRegistrationResponse.getGrantTypes())
				).put(
					"redirectUris",
					_toJSONArray(
						liferayClientRegistrationResponse.getRedirectUris())
				).put(
					"scope", scope
				));

			auditRouter.route(auditMessage);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private JSONObject _baseAuditInfo() {
		MessageContext messageContext = getMessageContext();

		HttpServletRequest httpServletRequest = null;

		if (messageContext != null) {
			httpServletRequest = messageContext.getHttpServletRequest();
		}

		String mode = "authenticated";

		if ((httpServletRequest != null) &&
			Boolean.TRUE.equals(
				httpServletRequest.getAttribute(
					DynamicRegistrationServiceContainerRequestFilter.
						REQUEST_PROPERTY_ANONYMOUS_REGISTRATION))) {

			mode = "anonymous";
		}

		String clientHost = StringPool.BLANK;
		String userAgent = StringPool.BLANK;

		if (httpServletRequest != null) {
			String forwardedFor = httpServletRequest.getHeader(
				"X-Forwarded-For");

			if (Validator.isNotNull(forwardedFor)) {
				int index = forwardedFor.indexOf(',');

				if (index > 0) {
					clientHost = forwardedFor.substring(
						0, index
					).trim();
				}
				else {
					clientHost = forwardedFor.trim();
				}
			}
			else {
				clientHost = GetterUtil.getString(
					httpServletRequest.getRemoteAddr());
			}

			userAgent = GetterUtil.getString(
				httpServletRequest.getHeader("User-Agent"));
		}

		return JSONUtil.put(
			"clientHost", clientHost
		).put(
			"mode", mode
		).put(
			"userAgent", userAgent
		);
	}

	private String _getApplicationType(ClientRegistration clientRegistration) {
		String applicationType = clientRegistration.getApplicationType();

		if (applicationType == null) {
			applicationType = "web";
		}

		return applicationType;
	}

	private long _getCompanyId() {
		MessageContext messageContext = getMessageContext();

		if ((messageContext == null) || (_portal == null)) {
			return 0;
		}

		return _portal.getCompanyId(messageContext.getHttpServletRequest());
	}

	private DynamicRegistrationConfiguration
			_getDynamicRegistrationConfiguration(long companyId)
		throws ConfigurationException {

		return _configurationProvider.getConfiguration(
			DynamicRegistrationConfiguration.class,
			new CompanyServiceSettingsLocator(
				companyId, DynamicRegistrationConfiguration.class.getName()));
	}

	private Pattern _globToPattern(String glob) {
		StringBuilder sb = new StringBuilder("^");

		for (int i = 0; i < glob.length(); i++) {
			char c = glob.charAt(i);

			if (c == '*') {
				sb.append(".*");
			}
			else if ("\\.+?()[]{}^$|".indexOf(c) >= 0) {
				sb.append('\\');
				sb.append(c);
			}
			else {
				sb.append(c);
			}
		}

		sb.append('$');

		return Pattern.compile(sb.toString());
	}

	private boolean _isAnonymousRegistration(Client client) {
		Map<String, String> clientProperties = client.getProperties();

		String mode = clientProperties.get(
			OAuth2ProviderRESTEndpointConstants.
				PROPERTY_KEY_DYNAMIC_REGISTRATION_MODE);

		return OAuth2ProviderRESTEndpointConstants.
			PROPERTY_VALUE_DYNAMIC_REGISTRATION_MODE_ANONYMOUS.equals(mode);
	}

	private void _promotePublicClientAuthorizationCode(Client client) {
		if (!OAuthConstants.TOKEN_ENDPOINT_AUTH_NONE.equals(
				client.getTokenEndpointAuthMethod())) {

			return;
		}

		List<String> allowedGrantTypes = client.getAllowedGrantTypes();

		if (allowedGrantTypes == null) {
			return;
		}

		int index = allowedGrantTypes.indexOf(
			OAuthConstants.AUTHORIZATION_CODE_GRANT);

		if (index < 0) {
			return;
		}

		List<String> promotedAllowedGrantTypes = new ArrayList<>(
			allowedGrantTypes);

		promotedAllowedGrantTypes.set(
			index,
			OAuth2ProviderRESTEndpointConstants.AUTHORIZATION_CODE_PKCE_GRANT);

		client.setAllowedGrantTypes(promotedAllowedGrantTypes);
	}

	private JSONArray _toJSONArray(List<String> list) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		if (list == null) {
			return jsonArray;
		}

		for (String item : list) {
			jsonArray.put(item);
		}

		return jsonArray;
	}

	private List<String> _toWireGrantTypes(List<String> allowedGrantTypes) {
		if (allowedGrantTypes == null) {
			return null;
		}

		List<String> wireGrantTypes = new ArrayList<>(allowedGrantTypes.size());

		for (String allowedGrantType : allowedGrantTypes) {
			if (OAuth2ProviderRESTEndpointConstants.
					AUTHORIZATION_CODE_PKCE_GRANT.equals(allowedGrantType)) {

				wireGrantTypes.add(OAuthConstants.AUTHORIZATION_CODE_GRANT);
			}
			else {
				wireGrantTypes.add(allowedGrantType);
			}
		}

		return wireGrantTypes;
	}

	private void _validate(
		Client client, ClientRegistration clientRegistration) {

		List<String> allowedGrantTypes = client.getAllowedGrantTypes();

		if (allowedGrantTypes == null) {
			return;
		}

		List<String> redirectUris = clientRegistration.getRedirectUris();

		if (redirectUris != null) {
			String applicationType = _getApplicationType(clientRegistration);

			for (String redirectUri : redirectUris) {
				validateRequestUri(
					redirectUri, applicationType,
					client.getAllowedGrantTypes());
			}
		}

		if (ListUtil.isEmpty(redirectUris) &&
			(allowedGrantTypes.contains("authorization_code") ||
			 allowedGrantTypes.contains("implicit"))) {

			OAuth2ErrorUtil.reportInvalidRequestError(
				"At least one redirect URI is required for the provided " +
					"grant types " + allowedGrantTypes,
				OAuthConstants.INVALID_REQUEST, Response.Status.BAD_REQUEST);
		}

		List<String> allowedResponseTypes = new ArrayList<>();

		for (String grantType : allowedGrantTypes) {
			if (_allowedResponseTypes.containsKey(grantType)) {
				allowedResponseTypes.add(_allowedResponseTypes.get(grantType));
			}
		}

		List<String> responseTypes = clientRegistration.getResponseTypes();

		if (ListUtil.isNotEmpty(allowedResponseTypes) &&
			ListUtil.isEmpty(responseTypes)) {

			OAuth2ErrorUtil.reportInvalidRequestError(
				"At least one response type is required for the provided " +
					"grant types " + allowedGrantTypes,
				"invalid_client_metadata", Response.Status.BAD_REQUEST);
		}

		if (responseTypes != null) {
			for (String responseType : responseTypes) {
				if (!allowedResponseTypes.contains(responseType)) {
					OAuth2ErrorUtil.reportInvalidRequestError(
						"Invalid response type " + responseType,
						"invalid_client_metadata", Response.Status.BAD_REQUEST);
				}
			}
		}

		if (_isAnonymousRegistration(client)) {
			_validateAnonymousPolicy(clientRegistration);
		}
	}

	private void _validateAnonymousPolicy(
		ClientRegistration clientRegistration) {

		MessageContext messageContext = getMessageContext();

		long companyId = _portal.getCompanyId(
			messageContext.getHttpServletRequest());

		DynamicRegistrationConfiguration dynamicRegistrationConfiguration;

		try {
			dynamicRegistrationConfiguration =
				_getDynamicRegistrationConfiguration(companyId);
		}
		catch (ConfigurationException configurationException) {
			OAuth2ErrorUtil.reportInvalidRequestError(
				"Unable to load dynamic registration configuration: " +
					configurationException.getMessage(),
				OAuthConstants.SERVER_ERROR,
				Response.Status.INTERNAL_SERVER_ERROR);

			return;
		}

		_validateAnonymousScopes(
			clientRegistration,
			dynamicRegistrationConfiguration.anonymousAllowedScopes());

		_validateAnonymousRedirectURIs(
			clientRegistration,
			dynamicRegistrationConfiguration.
				anonymousAllowedRedirectURIPatterns());
	}

	private void _validateAnonymousRedirectURIs(
		ClientRegistration clientRegistration, String[] allowedPatterns) {

		if (ArrayUtil.isEmpty(allowedPatterns)) {
			return;
		}

		List<String> redirectUris = clientRegistration.getRedirectUris();

		if (ListUtil.isEmpty(redirectUris)) {
			return;
		}

		List<Pattern> compiledPatterns = new ArrayList<>(
			allowedPatterns.length);

		for (String allowedPattern : allowedPatterns) {
			if (Validator.isBlank(allowedPattern)) {
				continue;
			}

			for (String line : allowedPattern.split("\\s+")) {
				if (Validator.isBlank(line)) {
					continue;
				}

				compiledPatterns.add(_globToPattern(line));
			}
		}

		if (compiledPatterns.isEmpty()) {
			return;
		}

		for (String redirectUri : redirectUris) {
			boolean matched = false;

			for (Pattern compiledPattern : compiledPatterns) {
				Matcher matcher = compiledPattern.matcher(redirectUri);

				if (matcher.matches()) {
					matched = true;

					break;
				}
			}

			if (!matched) {
				OAuth2ErrorUtil.reportInvalidRequestError(
					"Redirect URI " + redirectUri +
						" is not permitted for anonymous registration",
					"invalid_redirect_uri", Response.Status.BAD_REQUEST);
			}
		}
	}

	private void _validateAnonymousScopes(
		ClientRegistration clientRegistration, String[] allowedScopes) {

		if (ArrayUtil.isEmpty(allowedScopes)) {
			return;
		}

		String scope = clientRegistration.getScope();

		if (Validator.isBlank(scope)) {
			return;
		}

		Set<String> normalizedAllowedScopes = new HashSet<>();

		for (String allowedScope : allowedScopes) {
			if (Validator.isBlank(allowedScope)) {
				continue;
			}

			for (String line : allowedScope.split("\\s+")) {
				if (Validator.isBlank(line)) {
					continue;
				}

				normalizedAllowedScopes.add(line);
			}
		}

		List<String> requestedScopes = OAuthUtils.parseScope(scope);

		for (String requestedScope : requestedScopes) {
			if (!normalizedAllowedScopes.contains(requestedScope)) {
				OAuth2ErrorUtil.reportInvalidRequestError(
					"Scope " + requestedScope +
						" is not permitted for anonymous registration",
					OAuthConstants.INVALID_SCOPE, Response.Status.BAD_REQUEST);
			}
		}
	}

	private static final String _EVENT_TYPE_DCR_REJECT =
		"DYNAMIC_REGISTRATION_REJECT";

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayDynamicRegistrationService.class);

	private static final Map<String, String> _allowedResponseTypes =
		HashMapBuilder.put(
			"authorization_code", "code"
		).put(
			"implicit", "token"
		).build();
	private static final Snapshot<AuditRouter> _auditRouterSnapshot =
		new Snapshot<>(
			LiferayDynamicRegistrationService.class, AuditRouter.class, null,
			true);

	private ConfigurationProvider _configurationProvider;
	private Portal _portal;

}