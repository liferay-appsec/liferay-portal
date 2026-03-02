/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.service.service.impl;

import com.liferay.cookies.service.model.ConsentPreference;
import com.liferay.cookies.service.service.base.ConsentPreferenceServiceBaseImpl;
import com.liferay.portal.aop.AopService;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brian Wing Shun Chan
 * @author Christopher Kian
 */
@Component(
	property = {
		"json.web.service.context.name=cookies",
		"json.web.service.context.path=ConsentPreference"
	},
	service = AopService.class
)
public class ConsentPreferenceServiceImpl
	extends ConsentPreferenceServiceBaseImpl {

	public void deleteConsentPreferences(long userId, String domain) {
		consentPreferencePersistence.removeByU_D(userId, domain);
	}

	public List<ConsentPreference> getConsentPreferences(
		long userId, String domain) {

		return consentPreferencePersistence.findByU_D(userId, domain);
	}

	public ConsentPreference updateConsentPreference(
		ConsentPreference consentPreference) {

		return consentPreferencePersistence.updateImpl(consentPreference);
	}

}