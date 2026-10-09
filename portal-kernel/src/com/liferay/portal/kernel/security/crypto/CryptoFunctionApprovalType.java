/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.crypto;

/**
 * @author Rafael Praxedes
 */
public enum CryptoFunctionApprovalType {

	/**
	 * Functions allowed with no security claimed: the approved and legacy
	 * functions plus a short list of non-approved ones, served by any
	 * provider and recorded.
	 */
	ALLOWED, APPROVED,

	/**
	 * Approved functions kept for legacy use only: verifying or decrypting
	 * values that already exist.
	 */
	LEGACY

}