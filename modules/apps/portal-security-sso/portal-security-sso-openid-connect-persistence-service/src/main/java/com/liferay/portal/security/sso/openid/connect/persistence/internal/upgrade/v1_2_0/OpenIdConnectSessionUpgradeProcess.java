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

package com.liferay.portal.security.sso.openid.connect.persistence.internal.upgrade.v1_2_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Base64;

import java.net.URI;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Dictionary;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Arthur Chan
 */
public class OpenIdConnectSessionUpgradeProcess extends UpgradeProcess {

	public OpenIdConnectSessionUpgradeProcess(
		ConfigurationAdmin configurationAdmin) {

		_configurationAdmin = configurationAdmin;
	}

	@Override
	protected void doUpgrade() throws Exception {
		if (!hasColumn("OpenIdConnectSession", "authServerWellKnownURI")) {
			alterTableAddColumn(
				"OpenIdConnectSession", "authServerWellKnownURI",
				"VARCHAR(256) null");
		}

		if (!hasColumn("OpenIdConnectSession", "clientId")) {
			alterTableAddColumn(
				"OpenIdConnectSession", "clientId", "VARCHAR(128) null");
		}

		if (hasColumn("OpenIdConnectSession", "providerName")) {
			alterTableDropColumn("OpenIdConnectSession", "providerName");
		}

		String sql =
			"select openIdConnectSessionId, configurationPid from " +
				"OpenIdConnectSession";

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql);
			ResultSet resultSet = preparedStatement.executeQuery()) {

			sql =
				"update OpenIdConnectSession set authServerWellKnownURI = ?, " +
					"clientId = ? WHERE openIdConnectSessionId = ?";

			while (resultSet.next()) {
				long openIdConnectSessionId = resultSet.getLong(
					"openIdConnectSessionId");

				String configurationPid = resultSet.getString(
					"configurationPid");

				Configuration configuration =
					_configurationAdmin.getConfiguration(configurationPid, "?");

				Dictionary<String, ?> properties =
					configuration.getProperties();

				String clientId = (String)properties.get(
					"openIdConnectClientId");

				String discoveryEndPoint = (String)properties.get(
					"discoveryEndPoint");

				if (discoveryEndPoint.length() < 1) {
					discoveryEndPoint = _generateLocalWellKnownURI(
						(String)properties.get("issuerURL"),
						(String)properties.get("tokenEndPoint"));
				}

				try (PreparedStatement updatePreparedStatement =
						connection.prepareStatement(sql)) {

					updatePreparedStatement.setString(1, discoveryEndPoint);
					updatePreparedStatement.setString(2, clientId);
					updatePreparedStatement.setLong(3, openIdConnectSessionId);

					updatePreparedStatement.execute();
				}
			}

			if (hasColumn("OpenIdConnectSession", "configurationPid")) {
				alterTableDropColumn(
					"OpenIdConnectSession", "configurationPid");
			}
		}
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

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectSessionUpgradeProcess.class);

	private final ConfigurationAdmin _configurationAdmin;

}