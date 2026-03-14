/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {accountSettingsPagesTest} from '../../../fixtures/accountSettingsPagesTest';
import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {systemSettingsPageTest} from '../../../fixtures/systemSettingsPageTest';
import {usersAndOrganizationsPagesTest} from '../../../fixtures/usersAndOrganizationsPagesTest';
import {getRandomInt} from '../../../utils/getRandomInt';
import performLogin, {
	performLogout,
	userData,
} from '../../../utils/performLogin';
import {
	clearConsentCookies,
	resetConsentManagerConfiguration,
	updateConsentManagerConfiguration,
} from './utils/consentManagerConfigurationHelper';

const cookieKeys = [
	'CONSENT_TYPE_FUNCTIONAL',
	'CONSENT_TYPE_NECESSARY',
	'CONSENT_TYPE_PERFORMANCE',
	'CONSENT_TYPE_PERSONALIZATION',
	'USER_CONSENT_CONFIGURED',
];

export const test = mergeTests(
	accountSettingsPagesTest,
	apiHelpersTest,
	featureFlagsTest({
		'LPD-75032': {enabled: true},
	}),
	loginTest(),
	systemSettingsPageTest,
	usersAndOrganizationsPagesTest
);

let userAccount = undefined;

test.afterEach(async ({apiHelpers, page, systemSettingsPage}) => {
	if (await page.getByRole('button', {name: 'Sign In'}).isVisible()) {
		await performLogin(page, 'test');
	}

	if (userAccount !== undefined) {
		await apiHelpers.headlessAdminUser.deleteUserAccount(userAccount.id);

		userAccount = undefined;
	}

	await test.step('Reset Consent Manager Configuration', async () => {
		await resetConsentManagerConfiguration(systemSettingsPage);
	});

	await test.step('Clear Consent Cookies if present', async () => {
		await clearConsentCookies(systemSettingsPage.page);
	});
});

test.beforeEach(async ({page}) => {
	await test.step('Enable Consent Manager', async () => {
		await updateConsentManagerConfiguration(page, {
			enabled: true,
			forceReload: true,
		});
	});

	await test.step('Verify Cookies Banner appears, then Accept All cookies', async () => {
		const cookiesBanner = await page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);

		await expect(cookiesBanner).toBeVisible();

		await page.getByRole('button', {name: 'Accept All'}).click();
	});
});

test(
	'Store Consent configuration field validation',
	{tag: '@LPD-78076'},
	async ({page}) => {
		const storeConsentField = page.getByLabel('Store Consent');

		await test.step('Validate Store Consent field is not enabled by default', async () => {
			await expect(storeConsentField).not.toBeChecked();
		});

		await test.step('Verify Store Consent field can be saved', async () => {
			await updateConsentManagerConfiguration(page, {
				enabled: true,
				storeConsent: true,
			});

			await expect(storeConsentField).toBeChecked();
		});
	}
);

test(
	'Verify consent preferences set as an authenticated user are removed from the browser after logout AC4/TC1',
	{tag: '@LPD-76011'},
	async ({page}) => {
		await test.step('Verify all consent cookies are set', async () => {
			const cookies = await page.context().cookies();

			for (const cookieKey of cookieKeys) {
				const cookie = await cookies.find(
					(cookie) => cookie.name === cookieKey
				);

				await expect(cookie).toBeDefined();
			}
		});

		await performLogout(page);

		await test.step('Verify no consent cookies are set', async () => {
			await page.reload();

			await page.waitForLoadState();

			const cookies = await page.context().cookies();

			for (const cookieKey of cookieKeys) {
				const cookie = await cookies.find(
					(cookie) => cookie.name === cookieKey
				);

				await expect(cookie).toBeUndefined();
			}
		});
	}
);

test(
	'Verify enabling Store Consent configuration shows Store Consent option across all consent manager pages AC2/TC1',
	{tag: '@LPD-76011'},
	async ({accountSettingsPage, page}) => {
		const storeConsentField = page.getByLabel('Store Consent');

		await test.step('Enabled Store Consent', async () => {
			await updateConsentManagerConfiguration(page, {
				enabled: true,
				storeConsent: true,
			});
		});

		await test.step('Clear consent cookies so banner appears', async () => {
			await clearConsentCookies(page);
		});

		const cookiesBanner = await page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);

		await test.step('Verify Store Consent option is present in cookies banner', async () => {
			await page.goto('/');

			await cookiesBanner.waitFor();

			await expect(storeConsentField).toBeVisible();
		});

		await test.step('Verify Store Consent option is present in cookies banner configuration', async () => {
			await page.getByRole('button', {name: 'Configuration'}).click();

			const cookieConfigurationIFrame = await page.frameLocator(
				'iframe[title="Cookie Configuration"]'
			);

			const storeConsentHeading =
				await cookieConfigurationIFrame.getByRole('heading', {
					name: 'Store Consent',
				});

			await expect(storeConsentHeading).toBeVisible();

			await page.getByRole('button', {name: 'Close'}).click();
		});

		await test.step('Verify Store Consent option is present in Data and Privacy tab', async () => {
			await accountSettingsPage.goToDataAndPrivacy();

			await accountSettingsPage.page
				.getByText('Consent Manager')
				.first()
				.waitFor();

			const storeConsentHeading =
				await accountSettingsPage.page.getByRole('heading', {
					name: 'Store Consent',
				});

			await expect(storeConsentHeading).toBeVisible();
		});
	}
);

test(
	'Verify when the user does not Store Consent, their consent preferences are lost when clearing cookies AC2/TC3',
	{tag: '@LPD-76011'},
	async ({page}) => {
		await test.step('Enabled Store Consent', async () => {
			await updateConsentManagerConfiguration(page, {
				enabled: true,
				storeConsent: true,
			});
		});

		await test.step('Clear consent cookies so banner appears', async () => {
			await clearConsentCookies(page);
		});

		const cookiesBanner = await page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);

		await test.step('Accept all consent preference cookies without storing consent', async () => {
			await page.goto('/');

			await cookiesBanner.waitFor();

			await page.getByRole('button', {name: 'Accept All'}).click();
		});

		await test.step('Verify clearing cookies and refreshing the page makes the cookies banner appear again', async () => {
			await clearConsentCookies(page);

			await page.goto('/');

			await expect(cookiesBanner).toBeVisible();
		});
	}
);

test(
	'Verify when the user does Store Consent, their consent preferences are reloaded after clearing cookies AC1/TC1, AC1/TC2, AC2/TC2',
	{tag: '@LPD-76011'},
	async ({page}) => {
		await test.step('Enabled Store Consent', async () => {
			await updateConsentManagerConfiguration(page, {
				enabled: true,
				storeConsent: true,
			});
		});

		await test.step('Clear consent cookies so banner appears', async () => {
			await clearConsentCookies(page);
		});

		const cookiesBanner = await page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);

		await test.step('Accept all consent preference cookies and store consent', async () => {
			await page.goto('/');

			await cookiesBanner.waitFor();

			await page.getByLabel('Store Consent').setChecked(true);

			await page.getByRole('button', {name: 'Accept All'}).click();
		});

		await test.step('Verify clearing cookies and refreshing the page makes the cookies banner appear again', async () => {
			await clearConsentCookies(page);

			await page.goto('/');

			await expect(cookiesBanner).not.toBeVisible();
		});
	}
);

test(
	'Verify when the user does Store Consent, their consent preferences can be anonymized via Personal Data Erasure AC3/TC1',
	{tag: '@LPD-76011'},
	async ({
		apiHelpers,
		browser,
		page,
		personalDataErasurePage,
		usersAndOrganizationsPage,
	}) => {
		await test.step('Enabled Store Consent', async () => {
			await updateConsentManagerConfiguration(page, {
				enabled: true,
				storeConsent: true,
			});
		});

		const userId = getRandomInt();

		await test.step('Create new user', async () => {
			userAccount = await apiHelpers.headlessAdminUser.postUserAccount(
				undefined,
				userId
			);

			// Add user info to userData const so we can authenticate via performLogin

			userData[userAccount.alternateName] = {
				name: userAccount.givenName,
				password: 'test',
				surname: userAccount.familyName,
			};
		});

		await test.step('Sign in as new user, store and accept consent preferences', async () => {
			const newUserPage = await browser.newPage();

			await performLogin(newUserPage, userAccount.alternateName);

			await newUserPage
				.locator(
					'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
				)
				.waitFor();

			await newUserPage.getByLabel('Store Consent').setChecked(true);

			await newUserPage.getByRole('button', {name: 'Accept All'}).click();
		});

		await test.step('As admin, delete new user personal data', async () => {
			await usersAndOrganizationsPage.goToUsers();

			usersAndOrganizationsPage.page.once('dialog', async (dialog) => {
				dialog.accept();
			});

			await (
				await usersAndOrganizationsPage.usersTableRowActions(
					userAccount.alternateName
				)
			).click();

			await usersAndOrganizationsPage.deletePersonalDataMenuItem.click();
		});

		await test.step('Verify consent preferences appear in personal data erasure page', async () => {
			await expect(
				usersAndOrganizationsPage.page.getByRole('cell', {
					name: 'Consent Preferences',
				})
			).toBeVisible;
		});
	}
);
