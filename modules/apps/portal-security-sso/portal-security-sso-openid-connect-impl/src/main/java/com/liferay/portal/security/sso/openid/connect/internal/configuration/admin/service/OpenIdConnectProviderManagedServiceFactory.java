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

package com.liferay.portal.security.sso.openid.connect.internal.configuration.admin.service;

import com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.net.URI;

import java.security.MessageDigest;

import java.util.Dictionary;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	immediate = true,
	property = Constants.SERVICE_PID + "=com.liferay.portal.security.sso.openid.connect.internal.configuration.OpenIdConnectProviderConfiguration",
	service = {
		ManagedServiceFactory.class,
		OpenIdConnectProviderManagedServiceFactory.class
	}
)
public class OpenIdConnectProviderManagedServiceFactory
	implements ManagedServiceFactory {

	@Override
	public void deleted(String pid) {

		// Do not delete upgraded entries

	}

	@Override
	public String getName() {
		return "OpenId Connect Provider Managed Service Factory";
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) {
		long companyId = GetterUtil.getLong(properties.get("companyId"));

		if (companyId == CompanyConstants.SYSTEM) {
			_update(properties);

			return;
		}

		_update(companyId, properties);
	}

	private String _generateAuthRequestParametersJSON(
		Dictionary<String, ?> properties, String parametersName) {

		JSONObject requestParametersJSONObject =
			_generateRequestParametersJSONObject(properties, parametersName);

		requestParametersJSONObject.put("response_type", "code");

		return requestParametersJSONObject.toString();
	}

	private String _generateInfoJSON(Dictionary<String, ?> properties) {
		JSONObject infoJSONObject = JSONFactoryUtil.createJSONObject();

		String clientId = (String)properties.get("openIdConnectClientId");

		if (Validator.isNotNull(clientId)) {
			infoJSONObject.put("client_id", clientId);
		}

		String clientSecret = (String)properties.get(
			"openIdConnectClientSecret");

		if (Validator.isNotNull(clientSecret)) {
			infoJSONObject.put("client_secret", clientSecret);
		}

		String providerName = (String)properties.get("providerName");

		if (Validator.isNotNull(providerName)) {
			infoJSONObject.put("client_name", "client to " + providerName);
		}

		String scopes = (String)properties.get("scopes");

		if (Validator.isNotNull(scopes)) {
			infoJSONObject.put("scope", scopes);
		}

		String registeredIdTokenSigningAlg = (String)properties.get(
			"registeredIdTokenSigningAlg");

		if (Validator.isNotNull(registeredIdTokenSigningAlg)) {
			infoJSONObject.put(
				"id_token_signed_response_alg", registeredIdTokenSigningAlg);
		}

		infoJSONObject.put(
			"grant_types",
			JSONFactoryUtil.createJSONArray(
				new String[] {"authorization_code", "refresh_token"})
		).put(
			"response_types",
			JSONFactoryUtil.createJSONArray(new String[] {"code"})
		);

		return infoJSONObject.toString();
	}

	private String _generateLocalWellKnownURI(
			String issuer, String tokenEndPoint)
		throws Exception {

		MessageDigest messageDigest = MessageDigest.getInstance("MD5");

		URI issuerURI = URI.create(issuer);

		return StringBundler.concat(
			issuerURI.getScheme(), "://", issuerURI.getAuthority(),
			"/.well-known/openid-configuration", issuerURI.getPath(), '/',
			Base64.encodeToURL(messageDigest.digest(tokenEndPoint.getBytes())),
			"/local");
	}

	private String _generateMetadataJSON(Dictionary<String, ?> properties) {
		JSONObject metadataJSONObject = JSONFactoryUtil.createJSONObject();

		String authorizationEndPoint = (String)properties.get(
			"authorizationEndPoint");

		if (Validator.isNotNull(authorizationEndPoint)) {
			metadataJSONObject.put(
				"authorization_endpoint", authorizationEndPoint);
		}

		String[] idTokenSigningAlgValues = (String[])properties.get(
			"idTokenSigningAlgValues");

		if ((idTokenSigningAlgValues != null) &&
			(idTokenSigningAlgValues.length > 0)) {

			metadataJSONObject.put(
				"id_token_signing_alg_values_supported",
				JSONFactoryUtil.createJSONArray(idTokenSigningAlgValues));
		}

		String issuerURL = (String)properties.get("issuerURL");

		if (Validator.isNotNull(issuerURL)) {
			metadataJSONObject.put("issuer", issuerURL);
		}

		String jwksURI = (String)properties.get("jwksURI");

		if (Validator.isNotNull(jwksURI)) {
			metadataJSONObject.put("jwks_uri", jwksURI);
		}

		String scopes = (String)properties.get("scopes");

		if (Validator.isNotNull(scopes)) {
			String[] scopesArray = scopes.split(" ");

			metadataJSONObject.put(
				"scopes_supported",
				JSONFactoryUtil.createJSONArray(scopesArray));
		}

		String[] subjectTypes = (String[])properties.get("subjectTypes");

		if ((subjectTypes != null) && (subjectTypes.length > 0)) {
			metadataJSONObject.put(
				"subject_types_supported",
				JSONFactoryUtil.createJSONArray(subjectTypes));
		}

		String tokenEndPoint = (String)properties.get("tokenEndPoint");

		if (Validator.isNotNull(tokenEndPoint)) {
			metadataJSONObject.put("token_endpoint", tokenEndPoint);
		}

		String userInfoEndPoint = (String)properties.get("userInfoEndPoint");

		if (Validator.isNotNull(userInfoEndPoint)) {
			metadataJSONObject.put("userinfo_endpoint", userInfoEndPoint);
		}

		return metadataJSONObject.toString();
	}

	private JSONObject _generateRequestParametersJSONObject(
		Dictionary<String, ?> properties, String parametersName) {

		JSONObject requestParametersJSONObject =
			JSONFactoryUtil.createJSONObject();

		String scopes = (String)properties.get("scopes");

		if (Validator.isNotNull(scopes)) {
			requestParametersJSONObject.put("scope", scopes);
		}

		String[] parameters = (String[])properties.get(parametersName);

		if ((parameters == null) || (parameters.length < 1)) {
			return requestParametersJSONObject;
		}

		for (String parameter : parameters) {
			String[] pair = parameter.split("=");

			if (pair.length != 2) {
				if (_log.isDebugEnabled()) {
					_log.debug("Parameter: " + parameter + " is not valid");
				}
			}
			else if (pair[0].equals("resource")) {
				JSONArray valuesJSONArray =
					requestParametersJSONObject.getJSONArray(pair[0]);

				if (valuesJSONArray != null) {
					for (String value : pair[1].split(" ")) {
						valuesJSONArray.put(value);
					}
				}
				else {
					requestParametersJSONObject.put(
						pair[0],
						JSONFactoryUtil.createJSONArray(pair[1].split(" ")));
				}
			}
			else if (pair[0].equals("scope")) {
				requestParametersJSONObject.put("scope", pair[1]);
			}
			else {
				JSONObject customRequestParametersJSONObject =
					requestParametersJSONObject.getJSONObject(
						"custom_request_parameters");

				if (customRequestParametersJSONObject == null) {
					requestParametersJSONObject.put(
						"custom_request_parameters",
						JSONFactoryUtil.createJSONObject());

					customRequestParametersJSONObject =
						requestParametersJSONObject.getJSONObject(
							"custom_request_parameters");
				}

				JSONArray valuesJSONArray =
					customRequestParametersJSONObject.getJSONArray(pair[0]);

				if (valuesJSONArray != null) {
					for (String value : pair[1].split(" ")) {
						valuesJSONArray.put(value);
					}
				}
				else {
					customRequestParametersJSONObject.put(
						pair[0],
						JSONFactoryUtil.createJSONArray(pair[1].split(" ")));
				}
			}
		}

		return requestParametersJSONObject;
	}

	private String _generateTokenRequestParametersJSON(
		Dictionary<String, ?> properties, String parametersName) {

		JSONObject requestParametersJSONObject =
			_generateRequestParametersJSONObject(properties, parametersName);

		requestParametersJSONObject.put("grant_type", "authorization_code");

		return requestParametersJSONObject.toString();
	}

	private void _update(Dictionary<String, ?> properties) {
		try {
			_companyLocalService.forEachCompanyId(
				new UnsafeConsumer<Long, Exception>() {

					@Override
					public void accept(Long companyId) {
						_update(companyId, properties);
					}

				});
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private void _update(long companyId, Dictionary<String, ?> properties) {
		String clientId = (String)properties.get("openIdConnectClientId");

		String discoveryEndPoint = (String)properties.get("discoveryEndPoint");

		long defaultUserId = 0;

		try {
			defaultUserId = _userLocalService.getDefaultUserId(companyId);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to find user for company: " + companyId,
					portalException);
			}
		}

		try {
			if (Validator.isNull(discoveryEndPoint)) {
				discoveryEndPoint = _generateLocalWellKnownURI(
					(String)properties.get("issuerURL"),
					(String)properties.get("tokenEndPoint"));

				_updateOAuthClientASLocalMetadata(
					defaultUserId, properties, discoveryEndPoint);
			}

			_updateOAuthClientEntry(
				companyId, discoveryEndPoint, clientId, defaultUserId,
				properties);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to convert OIDC configuration", exception);
			}
		}
	}

	private void _updateOAuthClientASLocalMetadata(
			long defaultUserId, Dictionary<String, ?> properties,
			String localWellKnownURI)
		throws Exception {

		OAuthClientASLocalMetadata oAuthClientASLocalMetadata =
			_oAuthClientASLocalMetadataLocalService.
				fetchOAuthClientASLocalMetadata(localWellKnownURI);

		if (oAuthClientASLocalMetadata == null) {
			_oAuthClientASLocalMetadataLocalService.
				addOAuthClientASLocalMetadata(
					defaultUserId, _generateMetadataJSON(properties),
					"openid-configuration");

			return;
		}

		_oAuthClientASLocalMetadataLocalService.
			updateOAuthClientASLocalMetadata(
				oAuthClientASLocalMetadata.getOAuthClientASLocalMetadataId(),
				_generateMetadataJSON(properties), "openid-configuration");
	}

	private void _updateOAuthClientEntry(
			long companyId, String authServerWellKnownURI, String clientId,
			long defaultUserId, Dictionary<String, ?> properties)
		throws Exception {

		OAuthClientEntry oAuthClientEntry =
			_oAuthClientEntryLocalService.fetchOAuthClientEntry(
				companyId, authServerWellKnownURI, clientId);

		if (oAuthClientEntry == null) {
			_oAuthClientEntryLocalService.addOAuthClientEntry(
				defaultUserId,
				_generateAuthRequestParametersJSON(
					properties, "customAuthorizationRequestParameters"),
				authServerWellKnownURI, _generateInfoJSON(properties),
				_generateTokenRequestParametersJSON(
					properties, "customTokenRequestParameters"));

			return;
		}

		_oAuthClientEntryLocalService.updateOAuthClientEntry(
			oAuthClientEntry.getOAuthClientEntryId(),
			_generateAuthRequestParametersJSON(
				properties, "customAuthorizationRequestParameters"),
			authServerWellKnownURI, _generateInfoJSON(properties),
			_generateTokenRequestParametersJSON(
				properties, "customTokenRequestParameters"));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectProviderManagedServiceFactory.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private OAuthClientASLocalMetadataLocalService
		_oAuthClientASLocalMetadataLocalService;

	@Reference
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

}