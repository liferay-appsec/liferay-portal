/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApiHelpers, DataApiHelpers} from './ApiHelpers';

export class CookiesApiHelper {
	readonly apiHelpers: ApiHelpers | DataApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'cookies/v1.0/';
	}

	async deleteConsentPreferences() {
		return this.apiHelpers.delete(
			`${this.apiHelpers.baseUrl}${this.basePath}consent-preferences`
		);
	}

	async deleteConsentPreferencesByName(name: string) {
		return this.apiHelpers.delete(
			`${this.apiHelpers.baseUrl}${this.basePath}consent-preference/by-name/{name}`
		);
	}

	async getConsentPreferencesByName(name: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}consent-preference/by-name/{name}`
		);
	}

	async putConsentPreference(consentPreference) {
		return this.apiHelpers.put(
			`${this.apiHelpers.baseUrl}${this.basePath}consent-preferences`,
			{data: consentPreference}
		);
	}
}
