/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.OnBehalfOfTokenValidator;
import com.liferay.ai.hub.cell.security.JWTTokenUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Christopher Kian
 */
@Component(service = OnBehalfOfTokenValidator.class)
public class OnBehalfOfTokenValidatorImpl implements OnBehalfOfTokenValidator {

	@Override
	public String validate(HttpServletRequest httpServletRequest)
		throws PortalException {

		String token = httpServletRequest.getHeader(ON_BEHALF_OF_HEADER);

		if (Validator.isNull(token)) {
			return null;
		}

		if (JWTTokenUtil.getUserId(token) == 0) {
			throw new PrincipalException(
				"Invalid " + ON_BEHALF_OF_HEADER + " token");
		}

		return token;
	}

}