/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.service.service.http;

import com.liferay.cookies.service.service.ConsentPreferenceServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.HttpPrincipal;
import com.liferay.portal.kernel.service.http.TunnelUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;

/**
 * Provides the HTTP utility for the
 * <code>ConsentPreferenceServiceUtil</code> service
 * utility. The
 * static methods of this class calls the same methods of the service utility.
 * However, the signatures are different because it requires an additional
 * <code>HttpPrincipal</code> parameter.
 *
 * <p>
 * The benefits of using the HTTP utility is that it is fast and allows for
 * tunneling without the cost of serializing to text. The drawback is that it
 * only works with Java.
 * </p>
 *
 * <p>
 * Set the property <b>tunnel.servlet.hosts.allowed</b> in portal.properties to
 * configure security.
 * </p>
 *
 * <p>
 * The HTTP utility is only generated for remote services.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class ConsentPreferenceServiceHttp {

	public static void deleteConsentPreferences(
		HttpPrincipal httpPrincipal, long userId, String domain) {

		try {
			MethodKey methodKey = new MethodKey(
				ConsentPreferenceServiceUtil.class, "deleteConsentPreferences",
				_deleteConsentPreferencesParameterTypes0);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, userId, domain);

			try {
				TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static java.util.List
		<com.liferay.cookies.service.model.ConsentPreference>
			getConsentPreferences(
				HttpPrincipal httpPrincipal, long userId, String domain) {

		try {
			MethodKey methodKey = new MethodKey(
				ConsentPreferenceServiceUtil.class, "getConsentPreferences",
				_getConsentPreferencesParameterTypes1);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, userId, domain);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (java.util.List
				<com.liferay.cookies.service.model.ConsentPreference>)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static com.liferay.cookies.service.model.ConsentPreference
		updateConsentPreference(
			HttpPrincipal httpPrincipal,
			com.liferay.cookies.service.model.ConsentPreference
				consentPreference) {

		try {
			MethodKey methodKey = new MethodKey(
				ConsentPreferenceServiceUtil.class, "updateConsentPreference",
				_updateConsentPreferenceParameterTypes2);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, consentPreference);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (com.liferay.cookies.service.model.ConsentPreference)
				returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		ConsentPreferenceServiceHttp.class);

	private static final Class<?>[] _deleteConsentPreferencesParameterTypes0 =
		new Class[] {long.class, String.class};
	private static final Class<?>[] _getConsentPreferencesParameterTypes1 =
		new Class[] {long.class, String.class};
	private static final Class<?>[] _updateConsentPreferenceParameterTypes2 =
		new Class[] {com.liferay.cookies.service.model.ConsentPreference.class};

}