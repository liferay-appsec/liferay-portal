/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.key.KeyReferenceUtil;

import jakarta.portlet.PortletPreferences;
import jakarta.portlet.ReadOnlyException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Pedro Victor Silvestre
 */
public class SecretResolverUtil {

	public static void copy(
			long companyId, PortletPreferences portletPreferences,
			Function<String, SecretContext> sourceSecretContextFunction,
			Function<String, SecretContext> targetSecretContextFunction)
		throws ReadOnlyException {

		Map<String, String[]> preferencesMap = portletPreferences.getMap();

		for (Map.Entry<String, String[]> entry : preferencesMap.entrySet()) {
			String[] values = entry.getValue();

			String[] copiedValues = Arrays.copyOf(values, values.length);

			for (int i = 0; i < copiedValues.length; i++) {
				if (!KeyReferenceUtil.isKeyReference(copiedValues[i])) {
					continue;
				}

				SecretResolver secretResolver = _getSecretResolver();

				copiedValues[i] = secretResolver.copy(
					companyId,
					sourceSecretContextFunction.apply(entry.getKey()),
					targetSecretContextFunction.apply(entry.getKey()),
					copiedValues[i]);
			}

			if (Arrays.equals(values, copiedValues)) {
				continue;
			}

			portletPreferences.setValues(entry.getKey(), copiedValues);
		}
	}

	public static String resolve(
		long companyId, SecretContext secretContext, String value) {

		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		SecretResolver secretResolver = _getSecretResolver();

		return secretResolver.resolve(companyId, secretContext, value);
	}

	public static String resolveProperty(String key) {
		String value = PropsUtil.get(key);

		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		SecretResolver secretResolver = _getSecretResolver();

		return secretResolver.resolveProperty(key);
	}

	private static SecretResolver _getSecretResolver() {
		SecretResolver secretResolver = _secretResolverSnapshot.get();

		if (secretResolver == null) {
			throw new IllegalStateException("Secret resolver is unavailable");
		}

		return secretResolver;
	}

	private static final Snapshot<SecretResolver> _secretResolverSnapshot =
		new Snapshot<>(
			SecretResolverUtil.class, SecretResolver.class, null, true);

}