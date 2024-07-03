/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {liferayConfig} from '../../liferay.config';
import {samlIdpConfig} from './samlIdp.config';

test('testing', async ({page}) => {
	await page.goto(samlIdpConfig.environment.baseUrl);
	await page.goto(liferayConfig.environment.baseUrl);
});
