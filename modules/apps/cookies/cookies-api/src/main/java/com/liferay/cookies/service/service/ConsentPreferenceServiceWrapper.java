/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.service.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link ConsentPreferenceService}.
 *
 * @author Brian Wing Shun Chan
 * @see ConsentPreferenceService
 * @generated
 */
public class ConsentPreferenceServiceWrapper
	implements ConsentPreferenceService,
			   ServiceWrapper<ConsentPreferenceService> {

	public ConsentPreferenceServiceWrapper() {
		this(null);
	}

	public ConsentPreferenceServiceWrapper(
		ConsentPreferenceService consentPreferenceService) {

		_consentPreferenceService = consentPreferenceService;
	}

	@Override
	public com.liferay.cookies.service.model.ConsentPreference
		addOrUpdateConsentPreference(
			com.liferay.cookies.service.model.ConsentPreference
				consentPreference) {

		return _consentPreferenceService.addOrUpdateConsentPreference(
			consentPreference);
	}

	@Override
	public void deleteConsentPreferences(long userId, String domain) {
		_consentPreferenceService.deleteConsentPreferences(userId, domain);
	}

	@Override
	public java.util.List<com.liferay.cookies.service.model.ConsentPreference>
		getConsentPreferences(long userId, String domain) {

		return _consentPreferenceService.getConsentPreferences(userId, domain);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _consentPreferenceService.getOSGiServiceIdentifier();
	}

	@Override
	public com.liferay.cookies.service.model.ConsentPreference
		updateConsentPreference(
			com.liferay.cookies.service.model.ConsentPreference
				consentPreference) {

		return _consentPreferenceService.updateConsentPreference(
			consentPreference);
	}

	@Override
	public ConsentPreferenceService getWrappedService() {
		return _consentPreferenceService;
	}

	@Override
	public void setWrappedService(
		ConsentPreferenceService consentPreferenceService) {

		_consentPreferenceService = consentPreferenceService;
	}

	private ConsentPreferenceService _consentPreferenceService;

}