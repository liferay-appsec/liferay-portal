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

package com.liferay.portal.security.auth.verifier.internal.request.parameter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.AuthException;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.AutoLoginException;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.auth.verifier.internal.constants.AuthVerifierConstants;
import com.liferay.portal.security.auth.verifier.internal.request.parameter.configuration.RequestParameterAuthVerifierCompanyConfiguration;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tomas Polesovsky
 */
@Component(service = AuthVerifier.class)
public class RequestParameterAuthVerifier implements AuthVerifier {

	@Override
	public String getAuthType() {
		Class<?> clazz = getClass();

		return clazz.getSimpleName();
	}

	@Override
	public AuthVerifierResult verify(
			AccessControlContext accessControlContext, Properties properties)
		throws AuthException {

		try {
			AuthVerifierResult authVerifierResult = new AuthVerifierResult();

			HttpServletRequest httpServletRequest =
				accessControlContext.getRequest();

			if (!isEnabled(_portal.getCompanyId(httpServletRequest))) {
				return authVerifierResult;
			}

			String[] credentials = _autoLogin.login(
				httpServletRequest, accessControlContext.getResponse());

			if (credentials != null) {
				authVerifierResult.setPassword(credentials[1]);
				authVerifierResult.setPasswordBasedAuthentication(true);
				authVerifierResult.setState(AuthVerifierResult.State.SUCCESS);
				authVerifierResult.setUserId(Long.valueOf(credentials[0]));
			}

			return authVerifierResult;
		}
		catch (AutoLoginException autoLoginException) {
			throw new AuthException(autoLoginException);
		}
	}

	protected boolean isEnabled(long companyId) {
		RequestParameterAuthVerifierCompanyConfiguration
			requestParameterAuthVerifierCompanyConfiguration =
				_getRequestParameterAuthVerifierCompanyConfiguration(companyId);

		if (requestParameterAuthVerifierCompanyConfiguration == null) {
			return false;
		}

		return requestParameterAuthVerifierCompanyConfiguration.enabled();
	}

	private RequestParameterAuthVerifierCompanyConfiguration
		_getRequestParameterAuthVerifierCompanyConfiguration(long companyId) {

		try {
			return _configurationProvider.getConfiguration(
				RequestParameterAuthVerifierCompanyConfiguration.class,
				new CompanyServiceSettingsLocator(
					companyId,
					AuthVerifierConstants.REQUEST_PARAMETER_SERVICE_NAME,
					RequestParameterAuthVerifierCompanyConfiguration.class.
						getName()));
		}
		catch (ConfigurationException configurationException) {
			_log.error(
				"Unable to get basic auth header configuration",
				configurationException);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RequestParameterAuthVerifier.class);

	@Reference(target = "(&(private.auto.login=true)(type=request.parameter))")
	private AutoLogin _autoLogin;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private Portal _portal;

}