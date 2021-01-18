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

package com.liferay.portal.security.auth.verifier.internal;

import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierConfiguration;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.auth.AuthVerifierPipeline;

import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;

/**
 * @author Tomas Polesovsky
 * @author Arthur Chan
 */
public abstract class BaseAuthVerifierPublisher {

	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		Boolean enabled = GetterUtil.getBoolean(properties.get("enabled"));

		if ((enabled == null) || !enabled) {
			return;
		}

		AuthVerifier authVerifier = getAuthVerifierInstance();

		Class<?> clazz = authVerifier.getClass();

		String authVerifierPropertyName =
			AuthVerifierPipeline.getAuthVerifierPropertyName(clazz.getName());

		Properties translatedProperties = new Properties();

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			translatedProperties.setProperty(
				translateKey(authVerifierPropertyName, entry.getKey()),
				String.valueOf(entry.getValue()));
		}

		_authVerifierConfiguration = new AuthVerifierConfiguration();

		_authVerifierConfiguration.setAuthVerifier(authVerifier);
		_authVerifierConfiguration.setAuthVerifierClassName(clazz.getName());
		_authVerifierConfiguration.setProperties(translatedProperties);

		AuthVerifierPipeline.addAuthVerifierConfiguration(
			_authVerifierConfiguration);
	}

	protected void deactivate() {
		AuthVerifierPipeline.removeAuthVerifierConfiguration(
			_authVerifierConfiguration);

		_authVerifierConfiguration = null;
	}

	protected abstract AuthVerifier getAuthVerifierInstance();

	protected void modified(
		BundleContext bundleContext, Map<String, Object> properties) {

		deactivate();

		activate(bundleContext, properties);
	}

	protected String translateKey(String authVerifierPropertyName, String key) {
		if (key.equals("hostsAllowed")) {
			key = "hosts.allowed";
		}
		else if (key.equals("urlsExcludes")) {
			key = "urls.excludes";
		}
		else if (key.equals("urlsIncludes")) {
			key = "urls.includes";
		}

		return key;
	}

	private AuthVerifierConfiguration _authVerifierConfiguration;

}