/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../../fixtures/loginTest';
import {systemSettingsPageTest} from '../../../fixtures/systemSettingsPageTest';
import {waitForAlert} from '../../../utils/waitForAlert';
import {
	clearConsentCookies,
	resetCookieManagerConfiguration,
} from './utils/cookieManagerAfterEach';

export const test = mergeTests(loginTest(), systemSettingsPageTest);

test.afterEach(async ({systemSettingsPage}) => {
	await test.step('Reset Cookie Manager Configuration', async () => {
		await resetCookieManagerConfiguration(systemSettingsPage);
	});

	await test.step('Clear Consent Cookies if present', async () => {
		await clearConsentCookies(systemSettingsPage);
	});
});

test.beforeEach(async ({page, systemSettingsPage}) => {
	await test.step('Enable Cookie Manager', async () => {
		await systemSettingsPage.goToSystemSetting('Privacy', 'Cookie Manager');

		await systemSettingsPage.page.waitForTimeout(1000);

		const enabledButton = page.getByLabel('Enabled');

		await enabledButton.waitFor({state: 'visible'});

		await page.waitForLoadState();

		await enabledButton.setChecked(true);

		await page.getByRole('button', {name: 'Save'}).click();

		await waitForAlert(page);
	});

	await test.step('Verify Cookies Banner appears, then Accept All cookies', async () => {
		const cookiesBanner = await page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);

		await expect(cookiesBanner).toBeVisible();

		await page.getByRole('button', {name: 'Accept All'}).click();
	});
});

test(
	'Store Consent configuration field validation',
	{tag: '@LPD-78076'},
	async ({page, systemSettingsPage}) => {
		await systemSettingsPage.goToSystemSetting('Privacy', 'Cookie Manager');

		const storeConsentField = await page.getByLabel('Store Consent');

		await test.step('Validate Store Consent field is not enabled by default', async () => {
			await expect(await storeConsentField).not.toBeChecked();
		});

		await test.step('Verify Store Consent field can be saved', async () => {
			page.once('dialog', async (dialogWindow) => {
				await dialogWindow.dismiss();
			});

			await storeConsentField.dispatchEvent('click');

			await page
				.getByRole('button', {name: 'Update'})
				.dispatchEvent('click');

			await page.waitForLoadState();

			await expect(await storeConsentField).toBeChecked();
		});
	}
);
