/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.sso.openid.connect.internal.configuration.OpenIdConnectProviderConfiguration;

import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;

import java.util.Dictionary;
import java.util.Objects;

import net.minidev.json.JSONObject;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Caio Farias
 */
public class OpenIdConnectClientInformationUtil {

	public static OIDCClientInformation getOIDCClientInformation(
			long companyId, ConfigurationAdmin configurationAdmin,
			OAuthClientEntry oAuthClientEntry, SecretResolver secretResolver,
			UserLocalService userLocalService)
		throws Exception {

		JSONObject jsonObject = JSONObjectUtils.parse(
			oAuthClientEntry.getInfoJSON());

		String clientSecret = GetterUtil.getString(
			jsonObject.get("client_secret"));

		SecretContext secretContext = _buildSecretContext(
			clientSecret, configurationAdmin, oAuthClientEntry,
			userLocalService);

		if (secretContext != null) {
			jsonObject.put(
				"client_secret",
				secretResolver.resolve(companyId, secretContext, clientSecret));
		}

		return OIDCClientInformation.parse(jsonObject);
	}

	private static SecretContext _buildSecretContext(
			String clientSecret, ConfigurationAdmin configurationAdmin,
			OAuthClientEntry oAuthClientEntry,
			UserLocalService userLocalService)
		throws Exception {

		if (!KeyReferenceUtil.isKeyReference(clientSecret)) {
			return null;
		}

		if (oAuthClientEntry.getUserId() == userLocalService.getGuestUserId(
				oAuthClientEntry.getCompanyId())) {

			Configuration configuration =
				_fetchOpenIdConnectProviderConfiguration(
					oAuthClientEntry.getCompanyId(), configurationAdmin,
					clientSecret);

			if (configuration == null) {
				return null;
			}

			return SecretContext.createConfigurationSecretContext(
				"openIdConnectClientSecret", configuration.getProperties());
		}

		return SecretContext.createEntitySecretContext(
			OAuthClientEntry.class, oAuthClientEntry.getCompanyId(),
			"clientSecret", oAuthClientEntry.getPrimaryKey());
	}

	private static Configuration _fetchOpenIdConnectProviderConfiguration(
			long companyId, ConfigurationAdmin configurationAdmin,
			String keyReferenceString)
		throws Exception {

		Configuration[] configurations = configurationAdmin.listConfigurations(
			StringBundler.concat(
				"(|(service.factoryPid=",
				OpenIdConnectProviderConfiguration.class.getName(),
				")(service.factoryPid=",
				OpenIdConnectProviderConfiguration.class.getName(),
				".scoped))"));

		if (configurations == null) {
			return null;
		}

		for (Configuration configuration : configurations) {
			Dictionary<String, Object> properties =
				configuration.getProperties();

			long configurationCompanyId = GetterUtil.getLong(
				properties.get("companyId"));

			if (((configurationCompanyId == CompanyConstants.SYSTEM) ||
				 (configurationCompanyId == companyId)) &&
				Objects.equals(
					keyReferenceString,
					properties.get("openIdConnectClientSecret"))) {

				return configuration;
			}
		}

		return null;
	}

}