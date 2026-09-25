/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.marketplace.settings.web.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;

import jakarta.portlet.PortletPreferences;

/**
 * @author Keven Leone
 */
public class MarketplaceUtil {

	public static JSONObject connect(
			long companyId, String code, String codeVerifier,
			String refreshToken, String serviceURL, String settings)
		throws Exception {

		Http.Options options = new Http.Options();

		options.addHeader(
			"Content-Type", ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
		options.addPart("client_id", PropsValues.MARKETPLACE_CLIENT_ID);
		options.addPart("code", code);
		options.addPart(
			"redirect_uri",
			PropsValues.MARKETPLACE_URL + PropsValues.MARKETPLACE_REDIRECT);

		if (refreshToken != null) {
			options.addPart("grant_type", "refresh_token");
			options.addPart("refresh_token", refreshToken);
		}
		else {
			options.addPart("code_verifier", codeVerifier);
			options.addPart("grant_type", "authorization_code");
		}

		options.setLocation(PropsValues.MARKETPLACE_URL + "/o/oauth2/token");
		options.setMethod(Http.Method.POST);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			HttpUtil.URLtoString(options));

		long accessTokenExpirationTime =
			System.currentTimeMillis() +
				(jsonObject.getLong("expires_in") * 1000);

		jsonObject.put(
			"access_token_expiration_time", accessTokenExpirationTime);

		PortletPreferences portletPreferences = PrefsPropsUtil.getPreferences(
			companyId);

		portletPreferences.setValue(
			"marketplaceAccessToken",
			_getStoredToken(
				companyId, "marketplaceAccessToken",
				jsonObject.getString("access_token")));
		portletPreferences.setValue(
			"marketplaceAccessTokenExpirationTime",
			String.valueOf(accessTokenExpirationTime));
		portletPreferences.setValue("marketplaceCode", code);
		portletPreferences.setValue(
			"marketplaceRefreshToken",
			_getStoredToken(
				companyId, "marketplaceRefreshToken",
				jsonObject.getString("refresh_token")));
		portletPreferences.setValue("marketplaceServiceURL", serviceURL);
		portletPreferences.setValue("marketplaceSettings", settings);

		portletPreferences.store();

		return jsonObject;
	}

	public static void deleteTokens(long companyId) throws Exception {
		for (String name :
				new String[] {
					"marketplaceAccessToken", "marketplaceRefreshToken"
				}) {

			KeyReference keyReference = _getKeyReference(
				companyId, name, PrefsPropsUtil.getString(companyId, name));

			if (keyReference != null) {
				SecretManager secretManager = _getSecretManager();

				secretManager.deleteSecret(companyId, keyReference);
			}
		}
	}

	public static String getToken(long companyId, String name)
		throws Exception {

		String token = PrefsPropsUtil.getString(companyId, name);

		KeyReference keyReference = _getKeyReference(companyId, name, token);

		if (keyReference == null) {
			return token;
		}

		SecretManager secretManager = _getSecretManager();

		try (Secret secret = secretManager.getSecret(companyId, keyReference)) {
			return new String(secret.getChars());
		}
	}

	private static String _getIdentifierPrefix(long companyId, String name) {
		return StringBundler.concat(
			"Marketplace/", companyId, StringPool.SLASH, name,
			StringPool.SLASH);
	}

	private static KeyReference _getKeyReference(
		long companyId, String name, String token) {

		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(token);

		if (keyReference == null) {
			return null;
		}

		String identifier = keyReference.getIdentifier();

		if (!identifier.startsWith(_getIdentifierPrefix(companyId, name))) {
			return null;
		}

		return keyReference;
	}

	private static SecretManager _getSecretManager() {
		SecretManager secretManager = _secretManagerSnapshot.get();

		if (secretManager == null) {
			throw new IllegalStateException("Secret manager is unavailable");
		}

		return secretManager;
	}

	private static String _getStoredToken(
			long companyId, String name, String token)
		throws Exception {

		if (KeyReferenceUtil.isKeyReference(token)) {
			throw new PortalException(
				"Token cannot begin with a reserved key reference prefix");
		}

		if (!PropsValues.FIPS_ENABLED || Validator.isNull(token)) {
			return token;
		}

		String identifier =
			_getIdentifierPrefix(companyId, name) + PortalUUIDUtil.generate();

		KeyReference keyReference = _getKeyReference(
			companyId, name, PrefsPropsUtil.getString(companyId, name));

		if (keyReference != null) {
			identifier = keyReference.getIdentifier();
		}

		SecretManager secretManager = _getSecretManager();

		try (Secret secret = new Secret(
				new KeyReference(
					identifier, StringPool.STAR, KeyReference.Type.SECRET),
				token)) {

			return KeyReferenceUtil.toKeyReferenceString(
				secretManager.putSecret(companyId, secret));
		}
	}

	private static final Snapshot<SecretManager> _secretManagerSnapshot =
		new Snapshot<>(MarketplaceUtil.class, SecretManager.class, null, true);

}