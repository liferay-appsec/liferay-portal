/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {test} from '@playwright/test';
import performLogin from "../../../utils/performLogin";
import {LoginInstanceSettingsPage} from '../../../pages/login-web/LoginInstanceSettingsPage';

const extendedOpenIdConnectTest = test.extend<{
    loginInstanceSettingsPage: LoginInstanceSettingsPage;
}>({
    //why this fixture is useful? :-)
    loginInstanceSettingsPage: async ({page}, use) => {
        await use(new LoginInstanceSettingsPage(page));
    }
    });

export {extendedOpenIdConnectTest};