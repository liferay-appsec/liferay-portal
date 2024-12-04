/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {loginTest} from '../../fixtures/loginTest';
import { getTempDir } from '../../utils/temp';
import { readFileSync } from 'fs';
import { reloadUntilVisible } from '../../utils/reloadUntilVisible';

export const test = mergeTests(loginTest(), applicationsMenuPageTest, apiHelpersTest);

const AUDIT_PORTLET_NAMESPACE =
	'_com_liferay_portal_security_audit_web_portlet_AuditPortlet_';

const dateFields = [
	'endDateAmPm',
	'endDateDay',
	'endDateHour',
	'endDateMinute',
	'endDateMonth',
	'endDateYear',
	'startDateAmPm',
	'startDateDay',
	'startDateHour',
	'startDateMinute',
	'startDateMonth',
	'startDateYear',
];

const fields = [
	'className',
	'classPK',
	'clientHost',
	'clientIP',
	'eventType',
	'serverName',
	'userName',
	'groupId',
	'serverPort',
	'userId',
];

test('LPD-40224: Check if the export audit events .csv is being filtered by the search fields', async ({
	apiHelpers,
	applicationsMenuPage,
	page,
}) => {
	page.on('dialog', (dialog) => dialog.accept());

	//Post a new user to create some User related audit events
	
	const user = await apiHelpers.headlessAdminUser.postUserAccount();

	try {
		await applicationsMenuPage.goToAudit();

		await page.locator('#toggle_id_audit_event_searchtoggleAdvanced').click();

		await page.locator(`#${AUDIT_PORTLET_NAMESPACE}className:visible`)
		.fill('com.liferay.portal.kernel.model.User');

		await page.locator('.lexicon-icon-search').click();

		await page.waitForTimeout(500);

		const locator = page.getByRole('cell', {name: "UPDATE"})

		await reloadUntilVisible({
			myLocator: locator,
			page: page
		})

		// Populate map with all the date parameters

		const dateValues = {};

		for (const field of dateFields) {
			const inputElement = page.locator(`#${field}`);
			const inputValue = await inputElement.inputValue();

			dateValues[field] = inputValue;
		}

		// On the export request, check if the body has all parameters

		page.on('request', async (request) => {
			if (request.url().includes('export_audit_events')) {
				const requestBody = request.postData();

				for (const field of dateFields) {
					expect(requestBody).toContain(
						`${AUDIT_PORTLET_NAMESPACE + field}=${dateValues[field]}`
					);
				}

				for (const field of fields) {
					expect(requestBody).toContain(AUDIT_PORTLET_NAMESPACE + field);
				}
			}
		});

		const options = page.getByLabel('Options');

		await options.click();

		const menuItem = page.getByRole('menuitem', {
			name: 'Export Audit Events',
		});

		await menuItem.click();

		// With just one user added, there should only be three events excluding LOGIN in the .csv

		const downloadPromise = page.waitForEvent('download');

		const download = await downloadPromise;

		const filePath = getTempDir() + download.suggestedFilename();

		await download.saveAs(filePath);

		const content = readFileSync(filePath, 'utf8');

		const regex = '/\b(ADD|ASSIGN|UPDATE)\b/g';

		const matches = content.match(regex);

		expect(matches).toHaveLength(3);
	}
	finally {
		await apiHelpers.headlessAdminUser.deleteUserAccount(Number(user.id));
	}
});

test("LPS-192555: Assert that the page's URL with advanced search doesn't get over 2048 characters", async ({
	applicationsMenuPage,
	page,
}) => {
	await applicationsMenuPage.goToAudit();

	await page.locator('#toggle_id_audit_event_searchtoggleAdvanced').click();

	await page
		.locator(`#${AUDIT_PORTLET_NAMESPACE}userName:visible`)
		.fill('test test');

	await page
		.locator(`#${AUDIT_PORTLET_NAMESPACE}eventType:visible`)
		.fill('LOGIN');

	await page.locator('.lexicon-icon-search').click();

	await page.waitForTimeout(500);

	const pageURL = page.url();

	expect(pageURL.length).toBeLessThan(2048);
});
