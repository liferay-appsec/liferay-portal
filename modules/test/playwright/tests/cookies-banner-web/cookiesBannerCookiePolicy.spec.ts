/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {loginTest} from '../../fixtures/loginTest';
import {cookieBannerPageTest} from './fixtures/cookieBannerPageTest';

export const test = mergeTests(
	featureFlagsTest({
		'LPD-10588': true,
	}),
	loginTest(),
	cookieBannerPageTest
);

test.afterEach(async ({cookieBannerPage}) => {
	await cookieBannerPage.goToSystemSetting();

	await cookieBannerPage.actionsButton.waitFor({state: 'visible'})

	await cookieBannerPage.resetCookiePreferences();
});

test('LPD-30561 Cookie Banner Cookie Policy Page', async ({
	cookieBannerPage,
	page,
}) => {
	await test.step('Enable Explicit Cookie Consent Mode', async () => {
		await cookieBannerPage.goToSystemSetting();

		if (!cookieBannerPage.explicitCookieConsentModeButton.isChecked()) {
			await cookieBannerPage.explicitCookieConsentModeButton.click();
		}

		await expect(
			cookieBannerPage.explicitCookieConsentModeButton
		).toBeChecked();
	});

	await test.step('Enable Preference Handling Cookies and save', async () => {
		await cookieBannerPage.enableThirdPartyCookies();
	});

	await test.step('Go to Cookie Policy page', async () => {
		await page.goto('/');

		await cookieBannerPage.cookieBanner.waitFor({state: 'visible'});

		await expect(cookieBannerPage.cookieBannerParagraph).toBeVisible();

		const cookiePolicyURL =
			cookieBannerPage.cookieBannerParagraph.locator('a');

		await cookiePolicyURL.click();

		await expect(page.getByText('Cookies List')).toBeVisible({
			timeout: 100 * 1000,
		});

		const objectDefinitionPortlets = await page
			.locator(
				'[id^="portlet_com_liferay_object_web_internal_object_definitions_portlet_ObjectDefinitionsPortlet_"]'
			)
			.all();

		expect(objectDefinitionPortlets.length).toBe(4);

		for (const objectDefinitionPortletIndex in objectDefinitionPortlets) {
			const objectDefinitionPortlet =
				objectDefinitionPortlets[objectDefinitionPortletIndex];

			await expect(
				objectDefinitionPortlet.locator('.dnd-thead')
			).toBeVisible({
				timeout: 100 * 1000,
			});

			const tableRows = await objectDefinitionPortlet
				.locator('.dnd-tr')
				.all();

			expect(tableRows.length).toBeGreaterThan(0);
		}
	});
});
