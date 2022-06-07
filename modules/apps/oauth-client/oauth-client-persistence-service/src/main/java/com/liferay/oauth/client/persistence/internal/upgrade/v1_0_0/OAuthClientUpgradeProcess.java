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

package com.liferay.oauth.client.persistence.internal.upgrade.v1_0_0;

import com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Base64;

import java.net.URI;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Arthur Chan
 */
public class OAuthClientUpgradeProcess extends UpgradeProcess {

	public OAuthClientUpgradeProcess(
		CompanyLocalService companyLocalService,
		ConfigurationAdmin configurationAdmin,
		ResourceLocalService resourceLocalService,
		UserLocalService userLocalService) {

		_companyLocalService = companyLocalService;
		_configurationAdmin = configurationAdmin;
		_resourceLocalService = resourceLocalService;
		_userLocalService = userLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		if (!hasTable("OAuthClientASLocalMetadata")) {
			runSQL(
				StringBundler.concat(
					"create table OAuthClientASLocalMetadata (mvccVersion ",
					"LONG default 0 not null, oAuthClientASLocalMetadataId ",
					"LONG not null primary key, companyId LONG, userId LONG, ",
					"userName VARCHAR(75) null, createDate DATE null, ",
					"modifiedDate DATE null, localWellKnownURI VARCHAR(256) ",
					"null, metadataJSON TEXT null);"));
		}

		if (!hasTable("OAuthClientEntry")) {
			runSQL(
				StringBundler.concat(
					"create table OAuthClientEntry (mvccVersion LONG default ",
					"0 not null, oAuthClientEntryId LONG not null primary ",
					"key, companyId LONG, userId LONG, userName VARCHAR(75) ",
					"null, createDate DATE null, modifiedDate DATE null, ",
					"authRequestParametersJSON VARCHAR(3999) null, ",
					"authServerWellKnownURI VARCHAR(256) null, clientId ",
					"VARCHAR(128) null, infoJSON TEXT null, ",
					"tokenRequestParametersJSON VARCHAR(3999) null);"));
		}

		Map<Long, Map<String, Dictionary<String, ?>>> companiesProperties =
			_getCompaniesProperties();

		Date date = new Date(System.currentTimeMillis());

		for (Map.Entry<Long, Map<String, Dictionary<String, ?>>> entry :
				companiesProperties.entrySet()) {

			long companyId = entry.getKey();

			long defaultUserId = 0;

			try {
				defaultUserId = _userLocalService.getDefaultUserId(companyId);
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to find a default user for company: " +
							companyId,
						portalException);
				}
			}

			Map<String, Dictionary<String, ?>> companyProperties =
				entry.getValue();

			for (Dictionary<String, ?> properties :
					companyProperties.values()) {

				String discoveryEndPoint = (String)properties.get(
					"discoveryEndPoint");

				if (discoveryEndPoint.length() < 1) {
					discoveryEndPoint = _generateLocalWellKnownURI(
						(String)properties.get("issuerURL"),
						(String)properties.get("tokenEndPoint"));

					_addOAuthClientASLocalMetadata(
						companyId, defaultUserId, date, discoveryEndPoint,
						properties);
				}

				_addOAuthClientEntry(
					companyId, defaultUserId, date, properties,
					discoveryEndPoint);
			}
		}
	}

	private void _addOAuthClientASLocalMetadata(
		long companyId, long defaultUserId, Date date, String localWellKnownURI,
		Dictionary<String, ?> properties) {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select count(*) from OAuthClientASLocalMetadata where " +
					"companyId = ? and localWellKnownURI = ?")) {

			preparedStatement.setLong(1, companyId);
			preparedStatement.setString(2, localWellKnownURI);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next() && (resultSet.getInt(1) > 0)) {
				return;
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"insert into OAuthClientASLocalMetadata (mvccVersion, ",
					"oAuthClientASLocalMetadataId, companyId, userId, ",
					"userName, createDate, modifiedDate, localWellKnownURI, ",
					"metadataJSON) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"))) {

			long oAuthClientASLocalMetadataId = increment();

			preparedStatement.setLong(1, 0);
			preparedStatement.setLong(2, oAuthClientASLocalMetadataId);
			preparedStatement.setLong(3, companyId);
			preparedStatement.setLong(4, defaultUserId);
			preparedStatement.setString(5, String.valueOf(defaultUserId));
			preparedStatement.setDate(6, date);
			preparedStatement.setDate(7, date);
			preparedStatement.setString(8, localWellKnownURI);
			preparedStatement.setString(9, _generateMetadataJSON(properties));

			preparedStatement.executeUpdate();

			_resourceLocalService.addResources(
				companyId, GroupConstants.DEFAULT_LIVE_GROUP_ID, defaultUserId,
				OAuthClientASLocalMetadata.class.getName(),
				oAuthClientASLocalMetadataId, false, false, false);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to add OAuth Client authorization server local " +
					"metadata",
				exception);
		}
	}

	private void _addOAuthClientEntry(
		long companyId, long defaultUserId, Date date,
		Dictionary<String, ?> properties, String wellKnownURI) {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"insert into OAuthClientEntry (mvccVersion, ",
					"oAuthClientEntryId, companyId, userId, userName, ",
					"createDate, modifiedDate, authRequestParametersJSON, ",
					"authServerWellKnownURI, clientId, infoJSON, ",
					"tokenRequestParametersJSON) values (?, ?, ?, ?, ?, ?, ?, ",
					"?, ?, ?, ?, ?)"))) {

			long oAuthClientEntryId = increment();

			preparedStatement.setLong(1, 0);
			preparedStatement.setLong(2, oAuthClientEntryId);
			preparedStatement.setLong(3, companyId);
			preparedStatement.setLong(4, defaultUserId);
			preparedStatement.setString(5, String.valueOf(defaultUserId));
			preparedStatement.setDate(6, date);
			preparedStatement.setDate(7, date);
			preparedStatement.setString(
				8,
				_generateAuthRequestParametersJSON(
					properties, "customAuthorizationRequestParameters"));
			preparedStatement.setString(9, wellKnownURI);
			preparedStatement.setString(
				10, (String)properties.get("openIdConnectClientId"));
			preparedStatement.setString(11, _generateInfoJSON(properties));
			preparedStatement.setString(
				12,
				_generateTokenRequestParametersJSON(
					properties, "customTokenRequestParameters"));

			preparedStatement.executeUpdate();

			_resourceLocalService.addResources(
				companyId, GroupConstants.DEFAULT_LIVE_GROUP_ID, defaultUserId,
				OAuthClientEntry.class.getName(), oAuthClientEntryId, false,
				false, false);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to add OAuth Client authorization server entry",
				exception);
		}
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

		if (clientId.length() > 0) {
			infoJSONObject.put("client_id", clientId);
		}

		String clientSecret = (String)properties.get(
			"openIdConnectClientSecret");

		if (clientSecret.length() > 0) {
			infoJSONObject.put("client_secret", clientSecret);
		}

		String providerName = (String)properties.get("providerName");

		if (providerName.length() > 0) {
			infoJSONObject.put("client_name", "client to " + providerName);
		}

		String scopes = (String)properties.get("scopes");

		if (scopes.length() > 0) {
			infoJSONObject.put("scope", scopes);
		}

		String registeredIdTokenSigningAlg = (String)properties.get(
			"registeredIdTokenSigningAlg");

		if (registeredIdTokenSigningAlg.length() > 0) {
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
		String issuer, String tokenEndPoint) {

		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			_log.error(noSuchAlgorithmException);
		}

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

		if (authorizationEndPoint.length() > 0) {
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

		if (issuerURL.length() > 0) {
			metadataJSONObject.put("issuer", issuerURL);
		}

		String jwksURI = (String)properties.get("jwksURI");

		if (jwksURI.length() > 0) {
			metadataJSONObject.put("jwks_uri", jwksURI);
		}

		String scopes = (String)properties.get("scopes");

		if (scopes.length() > 0) {
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

		if (tokenEndPoint.length() > 0) {
			metadataJSONObject.put("token_endpoint", tokenEndPoint);
		}

		String userInfoEndPoint = (String)properties.get("userInfoEndPoint");

		if (userInfoEndPoint.length() > 0) {
			metadataJSONObject.put("userinfo_endpoint", userInfoEndPoint);
		}

		return metadataJSONObject.toString();
	}

	private JSONObject _generateRequestParametersJSONObject(
		Dictionary<String, ?> properties, String parametersName) {

		JSONObject requestParametersJSONObject =
			JSONFactoryUtil.createJSONObject();

		String scopes = (String)properties.get("scopes");

		if (scopes.length() > 0) {
			requestParametersJSONObject.put("scope", scopes);
		}

		String[] parameters = (String[])properties.get(parametersName);

		if ((parameters == null) || (parameters.length < 1)) {
			return requestParametersJSONObject;
		}

		for (String parameter : parameters) {
			String[] pair = parameter.split("=");

			if (pair.length != 2) {
				if (_log.isWarnEnabled()) {
					_log.warn("Parameter: " + parameter + " is not valid");
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

	/**
	 * 1. Two sets of properties are considered duplicated to each other if they have same value of
	 * 		1.1 clientId + discoveryEndpoint when discoveryEndpoint is available
	 * 		1.2 clientId + tokenEndpoint when discoveryEndpoint is not available
	 * 2. Duplicated set of properties in each company will be ignored.
	 * 3. System properties will be inserted into each instance company, while 2 is true.
	 * 4. This method is required because we need to ensure table entry uniqueness.
	 */
	private Map<Long, Map<String, Dictionary<String, ?>>>
			_getCompaniesProperties()
		throws Exception {

		Map<Long, Map<String, Dictionary<String, ?>>> companiesProperties =
			new HashMap<>();

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			StringBundler.concat(
				"(service.factoryPid=",
				"com.liferay.portal.security.sso.openid.connect.internal.",
				"configuration.OpenIdConnectProviderConfiguration)"));

		if (configurations == null) {
			return companiesProperties;
		}

		for (Configuration configuration : configurations) {
			Dictionary<String, ?> properties = configuration.getProperties();

			Long companyId = (Long)properties.get("companyId");

			if (companyId == null) {
				companyId = (long)0;
			}

			Map<String, Dictionary<String, ?>> companyProperties =
				companiesProperties.getOrDefault(companyId, new HashMap<>());

			if (companyProperties.isEmpty()) {
				companiesProperties.put(companyId, companyProperties);
			}

			String clientId = (String)properties.get("openIdConnectClientId");
			String discoveryEndPoint = (String)properties.get(
				"discoveryEndPoint");
			String tokenEndPoint = (String)properties.get("tokenEndPoint");

			if (discoveryEndPoint.length() > 0) {
				companyProperties.putIfAbsent(
					clientId + discoveryEndPoint, properties);
			}
			else {
				companyProperties.putIfAbsent(
					clientId + tokenEndPoint, properties);
			}
		}

		Map<String, Dictionary<String, ?>> systemProperties =
			companiesProperties.remove(0L);

		if (systemProperties == null) {
			return companiesProperties;
		}

		_companyLocalService.forEachCompanyId(
			(Long companyId) -> {
				Map<String, Dictionary<String, ?>> companyProperties =
					companiesProperties.getOrDefault(
						companyId, new HashMap<>());

				if (companyProperties.isEmpty()) {
					companiesProperties.put(companyId, companyProperties);
				}

				for (Map.Entry<String, Dictionary<String, ?>> entry :
						systemProperties.entrySet()) {

					companyProperties.putIfAbsent(
						entry.getKey(), entry.getValue());
				}
			});

		return companiesProperties;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OAuthClientUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final ConfigurationAdmin _configurationAdmin;
	private final ResourceLocalService _resourceLocalService;
	private final UserLocalService _userLocalService;

}