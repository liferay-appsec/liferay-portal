/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.manager.util;

import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirna Gama
 */
public class CookiesConsentTypesCaching {

	public static void addCookie(String cookieName, Integer cookieConsentType) {
		if (!_isPresent(cookieName)) {
			_cookiesConsentTypesMapCache.put(cookieName, cookieConsentType);
		}
	}

	public static Map<String, Integer> getCookiesConsentTypesMapCache() {
		return _cookiesConsentTypesMapCache;
	}

	private static boolean _isPresent(String cookieName) {
		return Validator.isNotNull(
			_cookiesConsentTypesMapCache.get(cookieName));
	}

	private static final Map<String, Integer> _cookiesConsentTypesMapCache =
		new HashMap<>();

}