/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {ldapConfigurationPagesTest} from '../../../fixtures/ldapConfigurationPagesTest';
import {loginTest} from '../../../fixtures/loginTest';
import getRandomString from '../../../utils/getRandomString';

export const test = mergeTests(loginTest(), ldapConfigurationPagesTest);

const PRESETS = [
	'Apache Directory Server',
	'Fedora Directory Server',
	'Microsoft Active Directory Server',
	'Novell eDirectory',
	'OpenLDAP',
];

test('LPD-86301 FIPS: new LDAP server form defaults base provider URL to ldaps:// scheme and port 636', async ({
	ldapConfigurationPage,
	ldapServerPage,
}) => {
	await test.step('Open the Add LDAP Server form', async () => {
		await ldapConfigurationPage.addLdapServer();
	});

	await test.step('Assert base provider URL defaults to ldaps:// and port 636', async () => {
		await ldapServerPage.serverName.waitFor();

		await expect(ldapServerPage.baseProviderUrl).toHaveValue(
			/^ldaps:\/\/.*:636$/
		);
	});
});

test('LPD-86301 FIPS: selecting a preset applies ldaps:// scheme and port 636 to the base provider URL', async ({
	ldapConfigurationPage,
	ldapServerPage,
}) => {
	await test.step('Open the Add LDAP Server form', async () => {
		await ldapConfigurationPage.addLdapServer();

		await ldapServerPage.serverName.waitFor();
	});

	for (const preset of PRESETS) {
		await test.step(`Assert ${preset} preset uses ldaps:// and port 636`, async () => {
			await ldapServerPage.page
				.getByText(preset)
				.getByRole('radio')
				.check();

			await expect(ldapServerPage.baseProviderUrl).toHaveValue(
				/^ldaps:\/\/.*:636$/
			);
		});
	}
});

test('LPD-86301 FIPS: saving an LDAP server with a non-ldaps:// base provider URL shows the FIPS validation alert and blocks submission', async ({
	ldapConfigurationPage,
	ldapServerPage,
}) => {
	const serverName = `fips-${getRandomString()}`;

	await test.step('Open the Add LDAP Server form', async () => {
		await ldapConfigurationPage.addLdapServer();
	});

	await test.step('Fill the form with a non-ldaps:// URL', async () => {
		await ldapServerPage.serverName.waitFor();

		await ldapServerPage.serverName.fill(serverName);
		await ldapServerPage.baseProviderUrl.fill('ldap://example.com:389');
	});

	const dialogMessagePromise = new Promise<string>((resolve) => {
		ldapServerPage.page.once('dialog', (dialog) => {
			resolve(dialog.message());

			dialog.accept();
		});
	});

	await test.step('Submit the form', async () => {
		await ldapServerPage.saveButton.click();
	});

	await test.step('Assert the FIPS validation alert is shown', async () => {
		expect(await dialogMessagePromise).toBe(
			'FIPS mode requires LDAP connections to use the "ldaps://" scheme. Update the base provider URL to a secure endpoint.'
		);
	});

	await test.step('Assert the form is still visible — submission was blocked', async () => {
		await expect(ldapServerPage.serverName).toBeVisible();
		await expect(ldapServerPage.baseProviderUrl).toBeVisible();
	});
});
