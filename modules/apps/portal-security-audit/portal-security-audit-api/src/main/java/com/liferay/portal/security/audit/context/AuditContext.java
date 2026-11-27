/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.context;

import java.util.Set;

/**
 * @author Álvaro Saugar
 */
public interface AuditContext {

	public Set<AuditDeliveryPath> getDefaultAuditDeliveryPaths();

	public default int getMaximumRetentionDays() {
		return Integer.MAX_VALUE;
	}

	public int getMinimumRetentionDays();

	public String getOAuth2ScopeName();

	public Set<AuditDeliveryPath> getRequiredAuditDeliveryPaths();

	public Set<String> getResourceActions();

}