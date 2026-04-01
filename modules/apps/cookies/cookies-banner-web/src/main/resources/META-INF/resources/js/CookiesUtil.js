/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	COOKIE_TYPES,
	getCookie as getCookieUtil,
	getOpener,
	removeCookie as removeCookieUtil,
	setCookie as setCookieUtil,
} from 'frontend-js-web';

export const userConfigCookieName = 'USER_CONSENT_CONFIGURED';
export const userConfigDateCookieName = 'USER_CONSENT_CONFIGURED_DATE';

export function acceptAllCookies(
	consentRenewalPeriod,
	optionalConsentCookieTypeNames,
	requiredConsentCookieTypeNames,
	timeUnit = 'months'
) {
	optionalConsentCookieTypeNames.forEach((optionalConsentCookieTypeName) => {
		setCookie(
			consentRenewalPeriod,
			optionalConsentCookieTypeName,
			timeUnit,
			'true'
		);
	});

	requiredConsentCookieTypeNames.forEach((requiredConsentCookieTypeName) => {
		setCookie(
			consentRenewalPeriod,
			requiredConsentCookieTypeName,
			timeUnit,
			'true'
		);
	});
}

export function declineAllCookies(
	consentRenewalPeriod,
	optionalConsentCookieTypeNames,
	requiredConsentCookieTypeNames,
	timeUnit = 'months'
) {
	optionalConsentCookieTypeNames.forEach((optionalConsentCookieTypeName) => {
		setCookie(
			consentRenewalPeriod,
			optionalConsentCookieTypeName,
			timeUnit,
			'false'
		);
	});

	requiredConsentCookieTypeNames.forEach((requiredConsentCookieTypeName) => {
		setCookie(
			consentRenewalPeriod,
			requiredConsentCookieTypeName,
			timeUnit,
			'true'
		);
	});
}

export function getCookie(name) {
	return getCookieUtil(name, COOKIE_TYPES.NECESSARY);
}

export function removeAllCookies(
	optionalConsentCookieTypeNames,
	requiredConsentCookieTypeNames
) {
	optionalConsentCookieTypeNames.forEach((optionalConsentCookieTypeName) => {
		removeCookieUtil(optionalConsentCookieTypeName);
	});

	requiredConsentCookieTypeNames.forEach((requiredConsentCookieTypeName) => {
		removeCookieUtil(requiredConsentCookieTypeName);
	});

	removeCookieUtil(userConfigDateCookieName);
}

export function setCookie(consentRenewalPeriod, name, timeUnit, value) {
	const secondsInDay = 60 * 60 * 24;
	let maxAge = 0;

	const timeUnitLowerCase = (timeUnit || 'months').toLowerCase();

	if (timeUnitLowerCase === 'days') {
		maxAge = secondsInDay * consentRenewalPeriod;
	}
	else if (timeUnitLowerCase === 'weeks') {
		maxAge = secondsInDay * 7 * consentRenewalPeriod;
	}
	else {
		maxAge = secondsInDay * 365 * (consentRenewalPeriod / 12);
	}

	setCookieUtil(name, value, COOKIE_TYPES.NECESSARY, {
		'max-age': Math.floor(maxAge),
		'path': themeDisplay.getPathContext() || '/',
	});
}

export function setUserConfigCookie(consentRenewalPeriod, timeUnit = 'months') {
	setCookie(consentRenewalPeriod, userConfigCookieName, timeUnit, 'true');

	setCookie(
		consentRenewalPeriod,
		userConfigDateCookieName,
		timeUnit,
		new Date().getTime()
	);

	getOpener()?.Liferay.fire('cookieBannerSetCookie');
}
