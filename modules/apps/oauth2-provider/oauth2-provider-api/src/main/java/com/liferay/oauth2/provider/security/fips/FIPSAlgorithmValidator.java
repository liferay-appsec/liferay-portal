/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;

import java.math.BigInteger;

import java.util.Base64;
import java.util.Set;

/**
 * @author Pedro Victor Silvestre
 */
public class FIPSAlgorithmValidator {

	public static void validateJWK(String json) {
		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		JSONObject jsonObject = null;

		try {
			jsonObject = JSONFactoryUtil.createJSONObject(json);
		}
		catch (JSONException jsonException) {
			throw new IllegalStateException(
				"Unable to parse JWK", jsonException);
		}

		_validateJWK(jsonObject);
	}

	public static void validateJWKS(String json) {
		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		JSONObject jsonObject = null;

		try {
			jsonObject = JSONFactoryUtil.createJSONObject(json);
		}
		catch (JSONException jsonException) {
			throw new IllegalStateException(
				"Unable to parse JWKS", jsonException);
		}

		JSONArray keysJSONArray = jsonObject.getJSONArray("keys");

		if (keysJSONArray == null) {
			return;
		}

		for (int i = 0; i < keysJSONArray.length(); i++) {
			_validateJWK(keysJSONArray.getJSONObject(i));
		}
	}

	public static void validateJWSAlgorithm(String algorithm) {
		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		if ((algorithm == null) ||
			!_approvedJWSAlgorithms.contains(algorithm)) {

			throw new IllegalStateException(
				"JWS algorithm \"" + algorithm +
					"\" is not FIPS 140-3 approved");
		}
	}

	private static int _decodeBase64URLBitLength(String value) {
		String padded = StringUtil.replace(
			value, new char[] {'-', '_'}, new char[] {'+', '/'});

		int remainder = padded.length() % 4;

		if (remainder == 2) {
			padded += "==";
		}
		else if (remainder == 3) {
			padded += "=";
		}

		Base64.Decoder decoder = Base64.getDecoder();

		try {
			byte[] bytes = decoder.decode(padded);

			BigInteger bigInteger = new BigInteger(1, bytes);

			return bigInteger.bitLength();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to decode JWK value", illegalArgumentException);
			}

			return 0;
		}
	}

	private static void _validateJWK(JSONObject jsonObject) {
		validateJWSAlgorithm(jsonObject.getString("alg"));

		String keyType = jsonObject.getString("kty");

		if (keyType.equals("RSA")) {
			int bits = _decodeBase64URLBitLength(jsonObject.getString("n"));

			if (bits < _MIN_RSA_KEY_BITS) {
				throw new IllegalStateException(
					StringBundler.concat(
						"RSA key of ", bits,
						" bits is not FIPS 140-3 approved"));
			}
		}
		else if (keyType.equals("oct")) {
			int bits = _decodeBase64URLBitLength(jsonObject.getString("k"));

			if (bits < _MIN_HMAC_KEY_BITS) {
				throw new IllegalStateException(
					StringBundler.concat(
						"HMAC key of ", bits,
						" bits is not FIPS 140-3 approved"));
			}
		}
	}

	private static final int _MIN_HMAC_KEY_BITS = 112;

	private static final int _MIN_RSA_KEY_BITS = 2048;

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSAlgorithmValidator.class);

	private static final Set<String> _approvedJWSAlgorithms = Set.of(
		"ES256", "ES384", "ES512", "HS256", "HS384", "HS512", "PS256", "PS384",
		"PS512", "RS256", "RS384", "RS512");

}