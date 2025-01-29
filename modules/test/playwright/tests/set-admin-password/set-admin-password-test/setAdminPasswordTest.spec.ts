/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, test} from '@playwright/test';

import {liferayConfig} from '../../../liferay.config';

test('LPD-4254 Checking what is the first page load if the property is test', async ({
	page,
}) => {
	await page.goto(liferayConfig.environment.baseUrl);

	await expect(page.getByRole('heading', {name: 'It seems you are using a weak default admin password, to remove this message please delete the default.admin.password property value in portal-ext.properties file. Please also consider to not use default admin password in the future'})).toBeVisible(
		{
			timeout: 10 * 1000,
		}
	);
});
