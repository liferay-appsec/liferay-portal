/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../fixtures/loginTest';
import {samlAdminPagesTest} from '../../fixtures/samlAdminPagesTest';
import {virtualInstancesPagesTest} from '../../fixtures/virtualInstancesPagesTest';
import {liferayConfig} from '../../liferay.config';
import getRandomString from '../../utils/getRandomString';
import {samlIdpConfig} from './samlIdp.config';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	loginTest(),
	samlAdminPagesTest,
	virtualInstancesPagesTest
);

test('Create, edit, and delete a new virtual instance', async ({
	editVirtualInstancePage,
	virtualInstancesPage,
}) => {
	const name = getRandomString();

	await virtualInstancesPage.addNewVirtualInstance(
		undefined,
		undefined,
		name,
		undefined
	);

	const newName = getRandomString();

	await editVirtualInstancePage.editVirtualInstance(
		false,
		name,
		newName + '.com',
		'100',
		newName
	);

	await expect(
		await virtualInstancesPage.page
			.getByRole('row')
			.getByText(name + ' ' + newName + ' ' + newName + '.com 0 100 No')
	).toBeVisible();

	await virtualInstancesPage.deleteVirtualInstance(name);
});

test('Create a new virtual instance, and configure it for SAML IdP', async ({
	editVirtualInstancePage,
	samlAdminPage,
	virtualInstancesPage,
}) => {
	// const name = getRandomString();
	//
	// await virtualInstancesPage.addNewVirtualInstance(
	// 	undefined,
	// 	undefined,
	// 	name,
	// 	undefined
	// );

	await samlAdminPage.configureSAML(false, 'testEntityId', 'Identity Provider');
	await samlAdminPage.configureSAML(false, 'testEntityId2', 'Service Provider');


	// await virtualInstancesPage.deleteVirtualInstance(name);
});

test('testing', async ({page}) => {
	await page.goto(samlIdpConfig.environment.baseUrl);
	await page.goto(liferayConfig.environment.baseUrl);
});
