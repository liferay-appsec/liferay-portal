/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.bootstrap.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsValues;

import java.security.Provider;
import java.security.Security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Caio Farias
 */
public class FIPSComplianceChecker {

	public static void run() {
		_checkFIPSProvider();

		_checkApprovedProviders();
	}

	private static void _checkApprovedProviders() {
		Set<String> approvedProviderNames = new LinkedHashSet<>(
			Arrays.asList(PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_APPROVED));

		List<String> unapprovedProviderNames = new ArrayList<>();

		for (Provider provider : Security.getProviders()) {
			String providerName = provider.getName();

			if (!approvedProviderNames.contains(providerName)) {
				unapprovedProviderNames.add(providerName);
			}
		}

		if (unapprovedProviderNames.isEmpty()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					StringBundler.concat(
						"All registered JCE providers are approved for FIPS ",
						"mode ", _collectProviderNames()));
			}

			return;
		}

		String message = StringBundler.concat(
			"Unapproved JCE providers registered in FIPS mode ",
			unapprovedProviderNames, "; approved providers ",
			approvedProviderNames);

		if (PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_STRICT) {
			throw new SecurityException(message);
		}

		if (_log.isWarnEnabled()) {
			_log.warn(message);
		}
	}

	private static void _checkFIPSProvider() {
		String fipsProviderName =
			PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_NAME;

		Provider[] providers = Security.getProviders();

		if ((providers.length > 0) &&
			fipsProviderName.equals(providers[0].getName())) {

			if (_log.isInfoEnabled()) {
				_log.info(
					StringBundler.concat(
						"FIPS provider \"", fipsProviderName,
						"\" is the first registered JCE provider"));
			}

			return;
		}

		String message = StringBundler.concat(
			"FIPS provider \"", fipsProviderName,
			"\" must be the first registered JCE provider; registered ",
			_collectProviderNames());

		throw new SecurityException(message);
	}

	private static List<String> _collectProviderNames() {
		Provider[] providers = Security.getProviders();

		List<String> providerNames = new ArrayList<>(providers.length);

		for (Provider provider : providers) {
			providerNames.add(provider.getName());
		}

		return providerNames;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSComplianceChecker.class);

}