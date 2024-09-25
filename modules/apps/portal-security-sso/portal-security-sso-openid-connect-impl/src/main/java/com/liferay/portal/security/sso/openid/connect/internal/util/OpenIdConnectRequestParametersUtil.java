/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringUtil;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import java.net.URI;

import java.util.function.BiConsumer;

import net.minidev.json.JSONObject;

/**
 * @author Arthur Chan
 */
public class OpenIdConnectRequestParametersUtil {

	public static UILocaleMode calculateUILocaleMode(
			JSONObject authRequestParametersJSONObject)
		throws ParseException {

		if (authRequestParametersJSONObject.containsKey(
				"custom_request_parameters")) {

			JSONObject customRequestParametersJSONObject =
				JSONObjectUtils.getJSONObject(
					authRequestParametersJSONObject,
					"custom_request_parameters");

			if (customRequestParametersJSONObject.containsKey("ui_locale")) {
				String mode = customRequestParametersJSONObject.getAsString(
					"ui_locale");

				if (StringUtil.equalsIgnoreCase(
						mode, "[\"$LOWERCASE_BCP47_LANGUAGE_CODE$\"]")) {

					return UILocaleMode.LOWERCASE_BCP47_LANGUAGE_CODE;
				}
				else if (StringUtil.equalsIgnoreCase(
							mode, "[\"$DISABLED$\"]")) {

					return UILocaleMode.DISABLED;
				}
			}
		}

		return UILocaleMode.DEFAULT;
	}

	public static void consumeCustomRequestParameters(
			BiConsumer<String, String[]> biConsumer,
			JSONObject requestParametersJSONObject)
		throws ParseException {

		if (!requestParametersJSONObject.containsKey(
				"custom_request_parameters")) {

			return;
		}

		JSONObject customRequestParametersJSONObject =
			JSONObjectUtils.getJSONObject(
				requestParametersJSONObject, "custom_request_parameters");

		for (String key : customRequestParametersJSONObject.keySet()) {
			biConsumer.accept(
				key,
				JSONObjectUtils.getStringArray(
					customRequestParametersJSONObject, key));
		}
	}

	public static URI[] getResourceURIs(JSONObject requestParametersJSONObject)
		throws ParseException {

		if (!requestParametersJSONObject.containsKey("resource")) {
			return new URI[0];
		}

		return TransformUtil.transformToArray(
			JSONObjectUtils.getStringList(
				requestParametersJSONObject, "resource"),
			resource -> URI.create(resource), URI.class);
	}

	public static ResponseType getResponseType(
			JSONObject requestParametersJSONObject)
		throws ParseException {

		return ResponseType.parse(
			JSONObjectUtils.getString(
				requestParametersJSONObject, "response_type"));
	}

	public static Scope getScope(JSONObject requestParametersJSONObject)
		throws ParseException {

		return Scope.parse(
			JSONObjectUtils.getString(requestParametersJSONObject, "scope"));
	}

	public enum UILocaleMode {

		DEFAULT, DISABLED, LOWERCASE_BCP47_LANGUAGE_CODE

	}

}