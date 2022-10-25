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

package com.liferay.portal.security.auth.verifier.internal.digest.authentication.configuration.definition;

import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;
import com.liferay.portal.security.auth.verifier.internal.constants.AuthVerifierConstants;
import com.liferay.portal.security.auth.verifier.internal.digest.authentication.configuration.DigestAuthenticationAuthVerifierCompanyConfiguration;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alvaro Saugar
 * @deprecated As of Cavanaugh (7.4.x), with no direct replacement
 */
@Component(service = ConfigurationPidMapping.class)
@Deprecated
public class
	DigestAuthenticationAuthVerifierCompanyServiceConfigurationPidMapping
		implements ConfigurationPidMapping {

	@Override
	public Class<?> getConfigurationBeanClass() {
		return DigestAuthenticationAuthVerifierCompanyConfiguration.class;
	}

	@Override
	public String getConfigurationPid() {
		return AuthVerifierConstants.DIGEST_AUTHENTICATION_SERVICE_NAME;
	}

}