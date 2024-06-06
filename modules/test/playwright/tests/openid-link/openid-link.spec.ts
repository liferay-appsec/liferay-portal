/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { expect, test } from '@playwright/test';
import performLogin from "../../utils/performLogin";
import { getRandomInt } from "../../utils/getRandomInt";
import getRandomString from "../../utils/getRandomString";

let providerName: string;

test.afterEach(async ({ page }) => {
    await performLogin(page, 'test');
    await page.getByLabel('Open Applications Menu').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'Instance Settings' }).click();
    await page.getByRole('link', { name: 'SSO' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect', exact: true }).click();
    await page.getByText(' Enabled ').uncheck();
    await page.getByRole('button', { name: 'Save' }).click();

    if (providerName) {
        await page.getByRole('menuitem', { name: 'OpenID Connect Provider Connection' }).click();
        await page.waitForTimeout(3000);
        await page.getByRole('row', { name: providerName + ' Actions' }).getByTitle('Actions').click();
        await page.getByText('Delete').click();
        await expect(
            page.getByText('Success:Your request completed successfully.')
        ).toBeVisible();
    }
});

test('openid', async ({ page }) => {
    await performLogin(page, 'test');
    await page.getByLabel('Open Applications Menu').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'Instance Settings' }).click();
    await page.getByRole('link', { name: 'SSO' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect', exact: true }).click();
    await page.getByText(' Enabled ').check();
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect Provider Connection' }).click();
    await page.getByRole('link', { name: 'Add' }).click();
    await page.getByLabel('Provider Name').click();
    providerName = 'mocked' + getRandomInt();
    await page.getByLabel('Provider Name').fill(providerName);
    await page.getByLabel('Discovery Endpoint', { exact: true }).click();
    await page.getByLabel('Discovery Endpoint', { exact: true }).fill('https://accounts.google.com/.well-known/openid-configuration');
    await page.getByLabel('OpenID Connect Client ID').click();
    await page.getByLabel('OpenID Connect Client ID').fill(getRandomString());
    await page.getByLabel('OpenID Connect Client Secret').click();
    await page.getByLabel('OpenID Connect Client Secret').fill(getRandomString());
    await page.getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(3000);
    await page.getByLabel('Test Test User Profile').click();
    await page.getByRole('menuitem', { name: 'Sign Out' }).click();
    await page.getByRole('button', { name: 'Search' }).waitFor({ state: 'visible' });
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page.getByText('OpenId Connect')).toBeVisible();
});