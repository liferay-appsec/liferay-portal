/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.security;

/**
 * @author Rafael Praxedes
 */
public interface JWTTokenUtil {

	public String generateToken(
		long companyId, long expirationTime, String issuer, long userId);

	public long getUserId(long companyId, String token);

}