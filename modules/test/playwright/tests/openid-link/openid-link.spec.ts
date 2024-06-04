/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { test, expect } from '@playwright/test';
import performLogin from "../../utils/performLogin";
import {getRandomInt} from "../../utils/getRandomInt";

test('when openId connection is enabled and set on NOT Utility page, then the openId connection link is visible during sign in', async ({ page }) => {
    await performLogin(page, 'test');
    await page.getByLabel('Open Applications Menu').click();
    await page.getByRole('tab', { name: 'Control Panel' }).click();
    await page.getByRole('menuitem', { name: 'Instance Settings' }).click();
    await page.goto('http://localhost:8080/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet&p_p_lifecycle=0&p_p_state=maximized&p_v_l_s_g_id=20117');
    await page.getByRole('link', { name: 'SSO' }).click();
    await page.getByRole('link', { name: 'Add' }).click();
    await page.getByLabel('Provider Name').click();
    await page.getByLabel('Provider Name').fill('mocked2');
    await page.getByLabel('Discovery Endpoint', { exact: true }).click();
    await page.getByLabel('Discovery Endpoint', { exact: true }).fill('https://accounts.google.com/.well-known/openid-configuration');
    await page.getByLabel('OpenID Connect Client ID').click();
    await page.getByLabel('OpenID Connect Client ID').fill('mocked_client');
    await page.getByLabel('OpenID Connect Client Secret').click();
    await page.getByLabel('OpenID Connect Client Secret').fill('mocked_secret');
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByRole('menuitem', { name: 'OpenID Connect', exact: true }).click();
    await page.getByLabel('Enabled').check();
    await page.getByRole('button', { name: 'Save' }).click();
    await page.getByLabel('Test Test User Profile').click();
    await page.getByRole('menuitem', { name: 'Sign Out' }).click();
    await page.getByRole('button', { name: 'Sign In' }).click();
    const linkName = page.getByText( 'OpenId Connect' );
    await expect(linkName).toBeVisible();
});
