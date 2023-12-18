/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.osb.faro.web.internal.handler;

import com.liferay.oauth2.provider.handler.OAuth2RESTAuthVerifierErrorResponseHandler;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcos Martins
 */
@Component(service = OAuth2RESTAuthVerifierErrorResponseHandler.class)
public class FaroOAuth2RESTAuthVerifierErrorResponseHandler
	implements OAuth2RESTAuthVerifierErrorResponseHandler {

	@Override
	public void handle(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		try {
			ServletResponseUtil.write(
				httpServletResponse,
				JSONUtil.put(
					"message",
					_language.get(
						httpServletRequest,
						"your-access-token-has-expired.-please-generate-a-" +
							"new-one-and-try-again")
				).put(
					"status", "ERROR"
				).toString());
		}
		catch (IOException ioException) {
			_log.error(ioException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FaroOAuth2RESTAuthVerifierErrorResponseHandler.class.getName());

	@Reference
	private Language _language;

}