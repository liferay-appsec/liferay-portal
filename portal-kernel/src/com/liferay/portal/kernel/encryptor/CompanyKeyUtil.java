/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.encryptor;

import com.liferay.portal.kernel.exception.CompanyKeyResolutionException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

import java.security.Key;

/**
 * @author Christopher Kian
 */
public class CompanyKeyUtil {

	public static final String WRAPPED_KEY_PREFIX = "${wrappedKey:";

	public static final String WRAPPED_KEY_VERSION = "v1";

	public static Key deserializeKey(long companyId, String serializedKey) {
		if (!isWrappedKey(serializedKey)) {
			return EncryptorUtil.deserializeKey(serializedKey);
		}

		CompanyKeyResolver companyKeyResolver = _getCompanyKeyResolver(
			companyId);

		return companyKeyResolver.deserializeKey(companyId, serializedKey);
	}

	public static boolean isWrappedKey(String serializedKey) {
		if ((serializedKey != null) &&
			serializedKey.startsWith(WRAPPED_KEY_PREFIX)) {

			return true;
		}

		return false;
	}

	public static String serializeKey(long companyId, Key key) {
		CompanyKeyResolver companyKeyResolver =
			_companyKeyResolverSnapshot.get();

		if ((companyKeyResolver == null) ||
			!companyKeyResolver.isEnabled(companyId)) {

			return EncryptorUtil.serializeKey(key);
		}

		return companyKeyResolver.serializeKey(companyId, key);
	}

	private static CompanyKeyResolver _getCompanyKeyResolver(long companyId) {
		CompanyKeyResolver companyKeyResolver =
			_companyKeyResolverSnapshot.get();

		if (companyKeyResolver != null) {
			_companyKeyResolverStarted = true;

			return companyKeyResolver;
		}

		long expirationTime = System.currentTimeMillis() + _getTimeout();

		while (System.currentTimeMillis() < expirationTime) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException interruptedException) {
				Thread thread = Thread.currentThread();

				thread.interrupt();

				throw new CompanyKeyResolutionException(
					"Interrupted during key resolution for company " +
						companyId,
					interruptedException);
			}

			companyKeyResolver = _companyKeyResolverSnapshot.get();

			if (companyKeyResolver != null) {
				_companyKeyResolverStarted = true;

				return companyKeyResolver;
			}
		}

		throw new CompanyKeyResolutionException(
			"Key resolver is not available for company " + companyId);
	}

	private static long _getTimeout() {
		if (_companyKeyResolverStarted) {
			return _RESOLVER_RESTART_TIMEOUT;
		}

		return GetterUtil.getLong(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_KEY_RESOLUTION_TIMEOUT),
			30000);
	}

	private static final long _RESOLVER_RESTART_TIMEOUT = 1000;

	private static final Snapshot<CompanyKeyResolver>
		_companyKeyResolverSnapshot = new Snapshot<>(
			CompanyKeyUtil.class, CompanyKeyResolver.class, null, true);
	private static volatile boolean _companyKeyResolverStarted;

}