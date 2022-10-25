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

package com.liferay.portal.security.auth.verifier.internal.request.parameter.configuration.definition;

import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;
import com.liferay.portal.security.auth.verifier.internal.constants.AuthVerifierConstants;
import com.liferay.portal.security.auth.verifier.internal.request.parameter.configuration.RequestParameterAuthVerifierCompanyConfiguration;

import org.osgi.service.component.annotations.Component;

/**
 * @author Avaro Saugar
 */
@Component(service = ConfigurationPidMapping.class)
public class RequestParameterAuthVerifierCompanyServiceConfigurationPidMapping
	implements ConfigurationPidMapping {

	@Override
	public Class<?> getConfigurationBeanClass() {
		return RequestParameterAuthVerifierCompanyConfiguration.class;
	}

	@Override
	public String getConfigurationPid() {
		return AuthVerifierConstants.REQUEST_PARAMETER_SERVICE_NAME;
	}

}