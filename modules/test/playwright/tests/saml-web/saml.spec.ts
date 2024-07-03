/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../fixtures/loginTest';
import {virtualInstancesPagesTest} from '../../fixtures/virtualInstancesPagesTest';
import {liferayConfig} from '../../liferay.config';
import getRandomString from '../../utils/getRandomString';
import {samlIdpConfig} from './samlIdp.config';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	loginTest(),
	virtualInstancesPagesTest
);

test('Create and delete a new virtual instance', async ({
	virtualInstancesPage,
}) => {
	const name = getRandomString();

	await virtualInstancesPage.addNewVirtualInstance(
		undefined,
		undefined,
		name,
		undefined
	);

	await virtualInstancesPage.deleteVirtualInstance(name);
});

test('testing', async ({page}) => {
	await page.goto(samlIdpConfig.environment.baseUrl);
	await page.goto(liferayConfig.environment.baseUrl);
});
