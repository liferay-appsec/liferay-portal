/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
 
package com.liferay.cookies.internal.manager.util;

import java.util.HashMap;
import java.util.Map;

import com.liferay.portal.kernel.util.Validator;

/**
 * @author Mirna Gama
 */
public class CookiesConsentTypesCaching {
	 
	private static Map<String, Integer> cookiesConsentTypesMapCache = 
			new HashMap<String, Integer>();
	
	public static void addCookie(String cookieName, Integer cookieConsentType) {
		if (!_isPresent(cookieName)) {
			cookiesConsentTypesMapCache.put(cookieName, cookieConsentType);
		}
	}

	private static boolean _isPresent(String cookieName) {
		return !Validator.isNull(cookiesConsentTypesMapCache.get(cookieName));
	}

	public static Map<String, Integer> getCookiesConsentTypesMapCache() {
		return cookiesConsentTypesMapCache;
	}

}
