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

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import java.net.URI;

import java.util.Map;
import java.util.function.BiConsumer;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Responsible for fetching static configured common request parameters
 *
 * @author Arthur Chan
 */
public class OpenIdConnectRequestParametersUtil {

	public static void consumeCustomRequestParameters(
		JSONObject requestParametersJSONObject,
		BiConsumer<String, String[]> biConsumer) {

		try {
			JSONObject customRequestParametersJSONObject =
				JSONObjectUtils.getJSONObject(
					requestParametersJSONObject, "custom_request_parameters");

			for (Map.Entry<String, Object> entry :
					customRequestParametersJSONObject.entrySet()) {

				JSONArray valueJSONArray = (JSONArray)entry.getValue();

				biConsumer.accept(
					entry.getKey(), valueJSONArray.toArray(new String[0]));
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	public static URI[] getResources(JSONObject requestParametersJSONObject)
		throws ParseException {

		if (!requestParametersJSONObject.containsKey("resource")) {
			return new URI[0];
		}

		JSONArray resourcesJSONArray = JSONObjectUtils.getJSONArray(
			requestParametersJSONObject, "resource");

		URI[] resources = new URI[resourcesJSONArray.size()];

		for (int i = 0; i < requestParametersJSONObject.size(); ++i) {
			resources[i] = URI.create((String)resourcesJSONArray.get(i));
		}

		return resources;
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

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectRequestParametersUtil.class);

}