/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.osgi.commands;

import com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;

import java.security.MessageDigest;

import java.util.List;

import org.apache.felix.service.command.Descriptor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Caio Farias
 */
@Component(
	property = {
		"osgi.command.function=updateLocalWellKnownURIs",
		"osgi.command.scope=fips"
	},
	service = OSGiCommands.class
)
public class FIPSOSGiCommands implements OSGiCommands {

	@Descriptor(
		"Update the hash in the local well known URI of every OpenID Connect provider to SHA-256"
	)
	public String updateLocalWellKnownURIs() throws Exception {
		int count = 0;
		StringBundler sb = new StringBundler();

		for (OAuthClientASLocalMetadata oAuthClientASLocalMetadata :
				_oAuthClientASLocalMetadataLocalService.
					getOAuthClientASLocalMetadatas(
						QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			String localWellKnownURI =
				oAuthClientASLocalMetadata.getLocalWellKnownURI();

			if (!localWellKnownURI.endsWith("/local")) {
				continue;
			}

			JSONObject metadataJSONObject = _jsonFactory.createJSONObject(
				oAuthClientASLocalMetadata.getMetadataJSON());

			String tokenEndpoint = metadataJSONObject.getString(
				"token_endpoint");

			if (Validator.isNull(tokenEndpoint)) {
				continue;
			}

			String newLocalWellKnownURI = _generateLocalWellKnownURI(
				localWellKnownURI, tokenEndpoint);

			if (newLocalWellKnownURI.equals(localWellKnownURI)) {
				continue;
			}

			int maxLength = ModelHintsUtil.getMaxLength(
				OAuthClientASLocalMetadata.class.getName(),
				"localWellKnownURI");

			if (newLocalWellKnownURI.length() > maxLength) {
				sb.append(
					StringBundler.concat(
						"Unable to update the local well known URI of OAuth 2 ",
						"client authorization server local metadata ",
						oAuthClientASLocalMetadata.
							getOAuthClientASLocalMetadataId(),
						" because it would exceed ", maxLength,
						" characters\n"));

				continue;
			}

			try {
				TransactionInvokerUtil.invoke(
					_transactionConfig,
					() -> {
						_updateLocalWellKnownURI(
							newLocalWellKnownURI, oAuthClientASLocalMetadata);

						return null;
					});
			}
			catch (Throwable throwable) {
				sb.append(
					StringBundler.concat(
						"Unable to update the local well known URI of OAuth 2 ",
						"client authorization server local metadata ",
						oAuthClientASLocalMetadata.
							getOAuthClientASLocalMetadataId(),
						": ", throwable.getMessage(), StringPool.NEW_LINE));

				continue;
			}

			count++;
		}

		sb.append(
			StringBundler.concat("Updated ", count, " local well known URIs"));

		return sb.toString();
	}

	private String _generateLocalWellKnownURI(
			String localWellKnownURI, String tokenEndpoint)
		throws Exception {

		int index = localWellKnownURI.lastIndexOf(
			CharPool.SLASH, localWellKnownURI.lastIndexOf(CharPool.SLASH) - 1);

		String prefix = localWellKnownURI.substring(0, index);

		MessageDigest messageDigest = MessageDigest.getInstance(
			DigesterUtil.SHA_256);

		return StringBundler.concat(
			prefix, "/",
			Base64.encodeToURL(messageDigest.digest(tokenEndpoint.getBytes())),
			"/local");
	}

	private void _updateLocalWellKnownURI(
			String localWellKnownURI,
			OAuthClientASLocalMetadata oAuthClientASLocalMetadata)
		throws Exception {

		String oldLocalWellKnownURI =
			oAuthClientASLocalMetadata.getLocalWellKnownURI();

		oAuthClientASLocalMetadata.setLocalWellKnownURI(localWellKnownURI);

		oAuthClientASLocalMetadata =
			_oAuthClientASLocalMetadataLocalService.
				updateOAuthClientASLocalMetadata(oAuthClientASLocalMetadata);

		for (OAuthClientEntry oAuthClientEntry :
				_oAuthClientEntryLocalService.getCompanyOAuthClientEntries(
					oAuthClientASLocalMetadata.getCompanyId())) {

			if (!oldLocalWellKnownURI.equals(
					oAuthClientEntry.getAuthServerWellKnownURI())) {

				continue;
			}

			oAuthClientEntry.setAuthServerWellKnownURI(localWellKnownURI);

			_oAuthClientEntryLocalService.updateOAuthClientEntry(
				oAuthClientEntry);
		}

		DynamicQuery dynamicQuery =
			_openIdConnectSessionLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"companyId", oAuthClientASLocalMetadata.getCompanyId()));
		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"authServerWellKnownURI", oldLocalWellKnownURI));

		List<OpenIdConnectSession> openIdConnectSessions =
			_openIdConnectSessionLocalService.dynamicQuery(dynamicQuery);

		for (OpenIdConnectSession openIdConnectSession :
				openIdConnectSessions) {

			openIdConnectSession.setAuthServerWellKnownURI(localWellKnownURI);

			_openIdConnectSessionLocalService.updateOpenIdConnectSession(
				openIdConnectSession);
		}
	}

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private OAuthClientASLocalMetadataLocalService
		_oAuthClientASLocalMetadataLocalService;

	@Reference
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Reference
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

}