/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Marcos Martins
 */
public interface OAuth2RESTAuthVerifierErrorResponseHandler {

	public void handle(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse);

}