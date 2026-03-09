/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.rest.internal.dto.v1_0.util;

import com.liferay.cookies.rest.dto.v1_0.ConsentPreference;

/**
 * @author Christopher Kian
 */
public class ConsentPreferenceUtil {

	public static ConsentPreference toConsentPreference(
		com.liferay.cookies.model.ConsentPreference consentPreference) {

		return new ConsentPreference() {
			{
				setDomain(consentPreference::getDomain);
				setExpirationDate(consentPreference::getExpirationDate);
				setId(consentPreference::getConsentPreferenceId);
				setName(consentPreference::getName);
				setUserId(consentPreference::getUserId);
				setValue(consentPreference::getValue);
			}
		};
	}

}