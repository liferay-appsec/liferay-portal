/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.consent;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Christian Moura
 */
public interface CookiesConsentResolver {

	public boolean hasConsent(
		int consentType, HttpServletRequest httpServletRequest);

}