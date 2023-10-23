package com.liferay.cookies.internal.manager.util;

import java.util.HashMap;
import java.util.Map;

import com.liferay.portal.kernel.util.Validator;

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
