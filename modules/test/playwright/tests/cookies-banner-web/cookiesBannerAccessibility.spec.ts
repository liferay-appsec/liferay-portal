/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {cookieBannerPageTest} from './fixtures/cookieBannerPageTest';

export const test = mergeTests(loginTest(), cookieBannerPageTest);

test.afterEach(async ({cookieBannerPage}) => {
	await cookieBannerPage.goToSystemSetting();

	await cookieBannerPage.actionsButton.waitFor({state: 'visible'})

	await cookieBannerPage.resetCookiePreferences();
});

test('LPD-30822 Cookie Banner Accessibility', async ({
	cookieBannerPage,
	page,
}) => {
	await test.step('Enable Third Party Cookies', async () => {
		await cookieBannerPage.enableThirdPartyCookies();
	});

	await test.step('Check aria-label, role, and paragraph', async () => {
		await page.goto('/');

		await cookieBannerPage.cookieBanner.waitFor({state: 'visible'});

		await expect(cookieBannerPage.cookieBannerParagraph).toBeVisible();
	});
});
