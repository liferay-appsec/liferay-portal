/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.rest.internal.resource.v1_0;

import com.liferay.cookies.rest.dto.v1_0.ConsentPreference;
import com.liferay.cookies.rest.internal.dto.v1_0.util.ConsentPreferenceUtil;
import com.liferay.cookies.rest.resource.v1_0.ConsentPreferenceResource;
import com.liferay.cookies.service.ConsentPreferenceService;
import com.liferay.cookies.service.persistence.ConsentPreferencePersistence;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.vulcan.pagination.Page;

import java.net.URI;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Christopher Kian
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/consent-preference.properties",
	scope = ServiceScope.PROTOTYPE, service = ConsentPreferenceResource.class
)
public class ConsentPreferenceResourceImpl
	extends BaseConsentPreferenceResourceImpl {

	@Override
	public void deleteConsentPreferenceByName(String name) throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-75032")) {

			throw new UnsupportedOperationException();
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		_consentPreferenceService.deleteConsentPreference(
			permissionChecker.getUserId(), _getDomain(), name);
	}

	public void deleteConsentPreferences() throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-75032")) {

			throw new UnsupportedOperationException();
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		_consentPreferenceService.deleteConsentPreferences(
			permissionChecker.getUserId(), _getDomain());
	}

	@Override
	public ConsentPreference getConsentPreferenceByName(String name)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-75032")) {

			throw new UnsupportedOperationException();
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		com.liferay.cookies.model.ConsentPreference portalConsentPreference =
			_consentPreferenceService.getConsentPreference(
				permissionChecker.getUserId(), _getDomain(), name);

		if (portalConsentPreference == null) {
			return null;
		}

		return ConsentPreferenceUtil.toConsentPreference(
			portalConsentPreference);
	}

	public Page<ConsentPreference> getConsentPreferences() throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-75032")) {

			throw new UnsupportedOperationException();
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		return Page.of(
			transform(
				_consentPreferenceService.getConsentPreferences(
					permissionChecker.getUserId(), _getDomain()),
				ConsentPreferenceUtil::toConsentPreference));
	}

	@Override
	public ConsentPreference putConsentPreference(
			ConsentPreference consentPreference)
		throws PortalException {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-75032")) {

			throw new UnsupportedOperationException();
		}

		com.liferay.cookies.model.ConsentPreference portalConsentPreference =
			_consentPreferencePersistence.fetchByU_D_N(
				consentPreference.getUserId(), consentPreference.getDomain(),
				consentPreference.getName());

		if (portalConsentPreference != null) {
			portalConsentPreference.setExpirationDate(
				consentPreference.getExpirationDate());
			portalConsentPreference.setValue(consentPreference.getValue());

			portalConsentPreference = _consentPreferencePersistence.update(
				portalConsentPreference);
		}
		else {
			portalConsentPreference =
				_consentPreferenceService.addConsentPreference(
					consentPreference.getUserId(),
					consentPreference.getDomain(),
					consentPreference.getExpirationDate(),
					consentPreference.getName(), consentPreference.getValue());
		}

		consentPreference.setId(portalConsentPreference::getPrimaryKey);

		return consentPreference;
	}

	private String _getDomain() {
		URI uri = contextUriInfo.getRequestUri();

		StringBuilder sb = new StringBuilder(3);

		sb.append(uri.getScheme());
		sb.append("://");
		sb.append(uri.getAuthority());

		return sb.toString();
	}

	@Reference
	private ConsentPreferencePersistence _consentPreferencePersistence;

	@Reference
	private ConsentPreferenceService _consentPreferenceService;

}