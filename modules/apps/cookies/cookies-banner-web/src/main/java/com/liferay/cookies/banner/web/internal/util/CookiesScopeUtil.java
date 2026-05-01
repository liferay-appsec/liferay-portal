/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.banner.web.internal.util;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.theme.ThemeDisplay;

/**
 * @author Alvaro Saugar
 */
public class CookiesScopeUtil {

	public static ExtendedObjectClassDefinition.Scope getScope(
		String portletId) {

		if (ConfigurationAdminPortletKeys.INSTANCE_SETTINGS.equals(portletId)) {
			return ExtendedObjectClassDefinition.Scope.COMPANY;
		}

		if (ConfigurationAdminPortletKeys.SITE_SETTINGS.equals(portletId)) {
			return ExtendedObjectClassDefinition.Scope.GROUP;
		}

		return ExtendedObjectClassDefinition.Scope.SYSTEM;
	}

	public static ExtendedObjectClassDefinition.Scope getScopeFromName(
		String scopeName) {

		if ("COMPANY".equals(scopeName)) {
			return ExtendedObjectClassDefinition.Scope.COMPANY;
		}

		if ("GROUP".equals(scopeName)) {
			return ExtendedObjectClassDefinition.Scope.GROUP;
		}

		return ExtendedObjectClassDefinition.Scope.SYSTEM;
	}

	public static long getScopePK(
		ExtendedObjectClassDefinition.Scope scope, ThemeDisplay themeDisplay) {

		if (scope == ExtendedObjectClassDefinition.Scope.COMPANY) {
			return themeDisplay.getCompanyId();
		}

		if (scope == ExtendedObjectClassDefinition.Scope.GROUP) {
			return themeDisplay.getScopeGroupId();
		}

		return 0L;
	}

}
