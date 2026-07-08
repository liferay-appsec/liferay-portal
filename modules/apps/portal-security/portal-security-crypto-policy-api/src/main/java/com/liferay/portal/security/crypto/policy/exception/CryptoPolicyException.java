/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.exception;

/**
 * @author Manuele Castro
 */
public class CryptoPolicyException extends RuntimeException {

	public CryptoPolicyException(String message) {
		super(message);
	}

}