/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

/**
 * @author Regisson Aguiar
 */
public class AuditMessageConstants {

	public static final String RESOURCE_ACTION_GRANT_ON_DEMAND_ACCESS =
		"grant_on_demand_access";

	public static final String RESOURCE_ACTION_REGISTER = "register";

	public static final String RESOURCE_ACTION_REGISTER_REJECT =
		"register_reject";

	public static final String RESOURCE_ACTION_VERIFY = "verify";

	public static final String RESOURCE_ACTION_VERIFY_FAILURE =
		"verify_failure";

	public static final String RESOURCE_ACTION_VERIFY_PENDING =
		"verify_pending";

	public static final String RESOURCE_TYPE_MFA = "mfa";

	public static final String RESOURCE_TYPE_ORGANIZATION = "organization";

	public static final String RESOURCE_TYPE_ROLE = "role";

	public static final String RESOURCE_TYPE_SCHEDULER = "scheduler";

	public static final String RESOURCE_TYPE_USER_GROUP = "usergroup";

}