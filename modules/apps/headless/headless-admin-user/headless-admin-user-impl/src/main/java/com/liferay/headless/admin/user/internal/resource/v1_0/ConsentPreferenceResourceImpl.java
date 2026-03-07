/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.user.internal.resource.v1_0;

import com.liferay.cookies.service.service.ConsentPreferenceService;
import com.liferay.headless.admin.user.dto.v1_0.ConsentPreference;
import com.liferay.headless.admin.user.internal.dto.v1_0.util.ConsentPreferenceUtil;
import com.liferay.headless.admin.user.resource.v1_0.ConsentPreferenceResource;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.vulcan.pagination.Page;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 * @author Christopher Kian
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/consent-preference.properties",
	scope = ServiceScope.PROTOTYPE, service = ConsentPreferenceResource.class
)
public class ConsentPreferenceResourceImpl
	extends BaseConsentPreferenceResourceImpl {

	public void deleteConsentPreferences() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		_consentPreferenceService.deleteConsentPreferences(
			permissionChecker.getUserId(), contextUriInfo.getBaseUri().toString());
	}

	public Page<ConsentPreference> getConsentPreferences() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		return Page.of(
			transform(
				_consentPreferenceService.getConsentPreferences(
					permissionChecker.getUserId(), contextUriInfo.getBaseUri().toString()),
				ConsentPreferenceUtil::toConsentPreference));
	}

	public ConsentPreference putConsentPreference(
		ConsentPreference consentPreference) {

		return null;
//		return _consentPreferenceService.addOrUpdateConsentPreference(
//				transform(consentPreference,
//					ConsentPreferenceUtil::toConsentPreference));
	}

	@Reference
	private ConsentPreferenceService _consentPreferenceService;

}