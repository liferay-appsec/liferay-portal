/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';

export const test = mergeTests(
	featureFlagsTest({
		'LPD-6378': true,
	}),
	loginTest()
);


test('Forgot Pasword Utility Page created and set as default', async ({
						   page,
					   }) => {
	await page.goto('/');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.getByRole('link', {name: 'Utility Pages'}).click();
	await page.waitForTimeout(3000);
	await page.getByRole('button', { name: 'New' }).click();
	await page.getByRole('menuitem', { name: 'Forgot Password' }).click();
	await page.getByRole('button', { name: 'Blank' }).click();
	await page.getByPlaceholder('Name').click();
	await page.getByPlaceholder('Name').fill('fp-test');
	await page.getByRole('button', { name: 'Save' }).click();
	await page.waitForTimeout(3000);
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).click();
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).press('Enter');

	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).locator('div').nth(1).click();
	await page.getByLabel('Desktop', { exact: true }).press('Enter');
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).click();
	await page.getByLabel('Desktop', { exact: true }).press('Enter');
	await page.getByLabel('Publish', { exact: true }).click();

	page.on('dialog', async (dialogWindow) => {
		await dialogWindow.accept();
	});

	await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_5"]').getByLabel('More actions').click();
	await page.getByRole('menuitem', { name: 'Mark as Default' }).click();
	await page.waitForTimeout(3000);




	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByRole('link', { name: 'Forgot Password' }).click();


	expect(await page.getByRole('link', { name: 'Go Somewhere' }));
})
test('Forgot Pasword Utility Page created and set as non-default 3', async ({
																			  page,
																		  }) => {
	await page.goto('/');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.getByRole('link', {name: 'Utility Pages'}).click();
	await page.waitForTimeout(3000);




	await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_5"]').getByLabel('More actions').click();
	await page.getByRole('menuitem', { name: 'Delete' }).click();
	await page.waitForTimeout(3000);
	await page.getByRole('button', { name: 'Delete' }).click();

	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByRole('menuitem', { name: 'Forgot Password' }).click();


	expect(await page.getByRole('heading', { name: 'Forgot Password' }));
})


test('Create Account Utility Page created and set as default', async ({
																		  page,
																	  }) => {
	await page.goto('/');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.getByRole('link', {name: 'Utility Pages'}).click();
	await page.waitForTimeout(3000);
	await page.getByRole('button', { name: 'New' }).click();
	await page.getByRole('menuitem', { name: 'Create Account' }).click();
	await page.getByRole('button', { name: 'Blank' }).click();
	await page.getByPlaceholder('Name').click();
	await page.getByPlaceholder('Name').fill('ca-test');
	await page.getByRole('button', { name: 'Save' }).click();
	await page.waitForTimeout(3000);
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).click();
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).press('Enter');

	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).locator('div').nth(1).click();
	await page.getByLabel('Desktop', { exact: true }).press('Enter');
	await page.getByRole('menuitem', { name: 'Button Add Button Mark Button as Favorite' }).click();
	await page.getByLabel('Desktop', { exact: true }).press('Enter');
	await page.getByLabel('Publish', { exact: true }).click();

	page.on('dialog', async (dialogWindow) => {
		await dialogWindow.accept();
	});

	await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_3"]').getByLabel('More actions').click();
	await page.getByRole('menuitem', { name: 'Mark as Default' }).click();
	await page.waitForTimeout(3000);




	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByRole('link', { name: 'Create Account' }).click();


	expect(await page.getByRole('link', { name: 'Go Somewhere' }));
})

test('Create Account Utility Page created and set as non-default 3', async ({
																				page,
																			}) => {
	await page.goto('/');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.getByRole('link', {name: 'Utility Pages'}).click();
	await page.waitForTimeout(3000);

	await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_3"]').getByLabel('More actions').click();
	await page.getByRole('menuitem', { name: 'Delete' }).click();
	await page.waitForTimeout(3000);
	await page.getByRole('button', { name: 'Delete' }).click();
	await page.waitForTimeout(3000);

	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.waitForTimeout(3000);
	await page.getByRole('menuitem', { name: 'Create Account' }).click();

	expect(await page.getByRole('heading', { name: 'Create Account' }));
})
