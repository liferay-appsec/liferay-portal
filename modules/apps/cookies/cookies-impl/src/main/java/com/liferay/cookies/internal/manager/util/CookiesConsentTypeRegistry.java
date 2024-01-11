/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.manager.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirna Gama
 */
public class CookiesConsentTypeRegistry {

	public static int getCookieConsentType(String cookieName) {
		int consentType = -1;

		if (_cookiesConsentTypeRegistry.get(cookieName) != null) {
			consentType = _cookiesConsentTypeRegistry.get(cookieName);
		}

		return consentType;
	}

	public static void registerCookieConsentType(
		String cookieName, Integer cookieConsentType) {

		_cookiesConsentTypeRegistry.put(cookieName, cookieConsentType);
	}

	private static final Map<String, Integer> _cookiesConsentTypeRegistry =
		new HashMap<>();

}