/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.context;

import com.liferay.portal.security.audit.context.AuditContext;
import com.liferay.portal.security.audit.context.AuditDeliveryPath;

import java.util.Collections;
import java.util.Set;

/**
 * @author Álvaro Saugar
 */
public class InstanceAuditContext implements AuditContext {

	@Override
	public Set<AuditDeliveryPath> getDefaultAuditDeliveryPaths() {
		return Collections.singleton(AuditDeliveryPath.DATABASE);
	}

	@Override
	public int getMaximumRetentionDays() {
		return 1825;
	}

	@Override
	public int getMinimumRetentionDays() {
		return 30;
	}

	@Override
	public String getOAuth2ScopeName() {
		return "audit.read";
	}

	@Override
	public Set<AuditDeliveryPath> getRequiredAuditDeliveryPaths() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getResourceActions() {
		return Collections.emptySet();
	}

}