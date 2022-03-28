/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.oauth.client.admin.web.internal.display.context;

import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;

/**
 * @author Arthur Chan
 */
public abstract class BaseOAuthClientAdminPortletDisplayContext {

	protected static boolean hasPermission(
		long companyId, String modelClassName, long modelId, long modelUserId,
		String actionId) {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (permissionChecker.hasOwnerPermission(
			companyId,
			modelClassName,
			modelId,
			modelUserId, actionId)) {

			return true;
		}

		return permissionChecker.hasPermission(
			0, modelClassName,
			modelId, actionId);
	}
}