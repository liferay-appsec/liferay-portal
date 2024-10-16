/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {isolatedLayoutTest} from '../../fixtures/isolatedLayoutTest';
import {loginTest} from '../../fixtures/loginTest';
import {waitForAlert} from '../../utils/waitForAlert';
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

test('@LPD-25701 Cookie Banner Script', async ({
	cookieBannerPage,
	layout,
	page,
}) => {
	await test.step('Edit page and add html component', async () => {
		await cookieBannerPage.addHTMLComponent({layout});
	});

	await test.step('Add script to html and save page', async () => {
		const textarea = page.locator('textarea');

		await textarea.press('Control+a');
		await textarea.press('Backspace');
		await textarea.fill(
			'<h1 id="test">HTML Example</h1>\n' +
				'\n' +
				'<script type="text/plain" data-third-party-cookie="CONSENT_TYPE_FUNCTIONAL">\n' +
				'      document.getElementById(\'test\').style.backgroundColor = "#ff0000"\n' +
				'</script>'
		);

		await page
			.getByRole('button', {
				name: 'Save',
			})
			.click();

		await page.getByLabel('Publish', {exact: true}).click();

		await waitForAlert(
			page,
			`Success:The page was published successfully.`
		);
	});

	await test.step('Enable Third Party Cookies', async () => {
		await cookieBannerPage.enableThirdPartyCookies();
	});

	await test.step('Accept Cookies', async () => {
		await page.goto(layout.friendlyURL);

		await page.getByRole('heading', {name: 'HTML Example'}).waitFor();

		await cookieBannerPage.acceptAllCookies();
	});

	await test.step('Check if script changed html', async () => {
		await page.reload();

		const htmlFragment = page.getByRole('heading', {
			name: 'HTML Example',
		});

		await htmlFragment.waitFor();

		await expect(htmlFragment).toHaveCSS(
			'background-color',
			'rgb(255, 0, 0)'
		);
	});
});
