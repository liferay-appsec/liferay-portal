/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.context.test;

import com.liferay.portal.security.audit.context.AuditContext;
import com.liferay.portal.security.audit.context.AuditDeliveryPath;

import java.util.Set;

/**
 * @author Álvaro Saugar
 */
public class TestAuditContext implements AuditContext {

	public TestAuditContext(
		Set<AuditDeliveryPath> defaultAuditDeliveryPaths,
		int maximumRetentionDays, int minimumRetentionDays,
		String oAuth2ScopeName,
		Set<AuditDeliveryPath> requiredAuditDeliveryPaths,
		Set<String> resourceActions) {

		_defaultAuditDeliveryPaths = defaultAuditDeliveryPaths;
		_maximumRetentionDays = maximumRetentionDays;
		_minimumRetentionDays = minimumRetentionDays;
		_oAuth2ScopeName = oAuth2ScopeName;
		_requiredAuditDeliveryPaths = requiredAuditDeliveryPaths;
		_resourceActions = resourceActions;
	}

	@Override
	public Set<AuditDeliveryPath> getDefaultAuditDeliveryPaths() {
		return _defaultAuditDeliveryPaths;
	}

	@Override
	public int getMaximumRetentionDays() {
		return _maximumRetentionDays;
	}

	@Override
	public int getMinimumRetentionDays() {
		return _minimumRetentionDays;
	}

	@Override
	public String getOAuth2ScopeName() {
		return _oAuth2ScopeName;
	}

	@Override
	public Set<AuditDeliveryPath> getRequiredAuditDeliveryPaths() {
		return _requiredAuditDeliveryPaths;
	}

	@Override
	public Set<String> getResourceActions() {
		return _resourceActions;
	}

	private final Set<AuditDeliveryPath> _defaultAuditDeliveryPaths;
	private final int _maximumRetentionDays;
	private final int _minimumRetentionDays;
	private final String _oAuth2ScopeName;
	private final Set<AuditDeliveryPath> _requiredAuditDeliveryPaths;
	private final Set<String> _resourceActions;

}