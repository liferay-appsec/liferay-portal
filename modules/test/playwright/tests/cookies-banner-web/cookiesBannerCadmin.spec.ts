/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {isolatedLayoutTest} from '../../fixtures/isolatedLayoutTest';
import {loginTest} from '../../fixtures/loginTest';
import {cookieBannerPageTest} from './fixtures/cookieBannerPageTest';

export const test = mergeTests(
	isolatedLayoutTest(),
	loginTest(),
	cookieBannerPageTest
);

test.afterEach(async ({cookieBannerPage}) => {
	await cookieBannerPage.goToSystemSetting();

	await cookieBannerPage.actionsButton.waitFor({state: 'visible'})

	await cookieBannerPage.resetCookiePreferences();
});

test('LPD-25440 Cookie Banner Cadmin', async ({cookieBannerPage, page}) => {
	await test.step('Enable Third Party Cookies', async () => {
		await cookieBannerPage.enableThirdPartyCookies();
	});

	await test.step('Open Configuration', async () => {
		await page.goto('/');

		await cookieBannerPage.cookieBanner.waitFor({state: 'visible'});

		await cookieBannerPage.cookieBannerConfigurationButton.click();
	});

	await test.step('Check cadmin is not applied', async () => {
		const modalBody = page
			.frameLocator('#cookiesBannerConfiguration_iframe_')
			.locator('.dialog-iframe-popup');

		await expect(modalBody).not.toHaveClass(/cadmin/);
	});
});
