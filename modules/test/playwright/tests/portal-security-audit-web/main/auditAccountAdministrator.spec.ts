/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../helpers/ApiHelpers';
import {performUserSwitch, userData} from '../../../utils/performLogin';
import {initAccountAdministrator} from '../../account-admin-web/main/utils/roles';

export const test = mergeTests(
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-113659': {enabled: true},
	}),
	loginTest()
);

async function addUserAccount(apiHelpers: DataApiHelpers) {
	const userAccount = await apiHelpers.headlessAdminUser.postUserAccount();

	userData[userAccount.alternateName] = {
		name: userAccount.givenName,
		password: 'test',
		surname: userAccount.familyName,
	};

	return userAccount;
}

test(
	'Account administrators see the login events of the users of their accounts',
	{tag: '@LPD-113662'},
	async ({apiHelpers, page}) => {

		// Add an account administrator, a member of the account, and a user outside of it

		const {account, userAccountAdmin} =
			await initAccountAdministrator(apiHelpers);

		const memberUserAccount = await addUserAccount(apiHelpers);

		await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
			account.id,
			[memberUserAccount.emailAddress]
		);

		const outsiderUserAccount = await addUserAccount(apiHelpers);

		// Sign in as the member and as the user outside of the account

		await performUserSwitch(page, memberUserAccount.alternateName);
		await performUserSwitch(page, outsiderUserAccount.alternateName);

		// Check the audit events as the account administrator once they are flushed

		await performUserSwitch(page, userAccountAdmin.alternateName);

		await expect(async () => {
			await page.goto(
				'/group/control_panel/manage?p_p_id=com_liferay_portal_security_audit_web_portlet_AuditPortlet'
			);

			await expect(
				page
					.getByRole('link', {
						exact: true,
						name: `${userAccountAdmin.givenName} ${userAccountAdmin.familyName}`,
					})
					.first()
			).toBeVisible({timeout: 1000});
		}).toPass({intervals: [5000], timeout: 90 * 1000});

		await expect(
			page
				.getByRole('link', {
					exact: true,
					name: `${memberUserAccount.givenName} ${memberUserAccount.familyName}`,
				})
				.first()
		).toBeVisible();
		await expect(
			page.getByRole('link', {
				exact: true,
				name: `${outsiderUserAccount.givenName} ${outsiderUserAccount.familyName}`,
			})
		).toHaveCount(0);
	}
);
