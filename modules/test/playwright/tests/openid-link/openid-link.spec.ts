/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { test, expect } from '@playwright/test';
import performLogin, {performLogout} from "../../utils/performLogin";
import {getRandomInt} from "../../utils/getRandomInt";
import {randomFill} from "crypto";
import getRandomString from "../../utils/getRandomString";

test('openid', async ({ page }) => {
    await performLogin(page, 'test');
    await page.getByLabel('Open Applications Menu').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'Instance Settings' }).click();
    //await page.goto('http://localhost:8080/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet&p_p_lifecycle=0&p_p_state=maximized&p_v_l_s_g_id=20117');
    await page.getByRole('link', { name: 'SSO' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect', exact: true }).click();
    await page.getByText(' Enabled ').check();
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect Provider Connection' }).click();
    await page.getByRole('link', { name: 'Add' }).click();
    await page.getByLabel('Provider Name').click();
    const providerName ='mocked' + getRandomInt();
    await page.getByLabel('Provider Name').fill(providerName);
    await page.getByLabel('Discovery Endpoint', { exact: true }).click();
    await page.getByLabel('Discovery Endpoint', { exact: true }).fill('https://accounts.google.com/.well-known/openid-configuration');
    await page.getByLabel('OpenID Connect Client ID').click();
    await page.getByLabel('OpenID Connect Client ID').fill(getRandomString());
    await page.getByLabel('OpenID Connect Client Secret').click();
    await page.getByLabel('OpenID Connect Client Secret').fill(getRandomString());
    await page.getByRole('button', { name: 'Save' }).click();

    await page.getByLabel('Test Test User Profile').click();
    await page.getByRole('menuitem', { name: 'Sign Out' }).click();
    await page.getByRole('button', { name: 'Sign In' }).click();
    const linkName = page.getByText('OpenId Connect');
    await expect(linkName).toBeVisible();

    await performLogin(page, 'test');
    await page.getByLabel('Open Applications Menu').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'Instance Settings' }).click();
    await page.getByRole('link', { name: 'SSO' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect Provider Connection' }).click();

    await page.getByText(providerName).click();
    await page.getByRole('button', { name: 'Actions' }).click();
    await page.getByRole('link', { name: 'Delete' }).click();

    await page.getByRole('menuitem', { name: 'OpenID Connect', exact: true }).click();
    await page.getByText(' Enabled ').uncheck();
    await page.getByRole('button', { name: 'Save' }).click();
    await performLogout(page);
});
