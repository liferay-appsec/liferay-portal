/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.agent;

import com.liferay.portal.kernel.exception.PortalException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Christopher Kian
 */
public interface OnBehalfOfTokenValidator {

	public static final String ON_BEHALF_OF_HEADER =
		"Liferay-AI-Hub-Cell-On-Behalf-Of";

	public String validate(HttpServletRequest httpServletRequest)
		throws PortalException;

}