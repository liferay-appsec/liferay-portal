/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {SystemSettingsPage} from '../../../pages/configuration-admin-web/SystemSettingsPage';
import {waitForAlert} from '../../../utils/waitForAlert';

export class CookieBannerPage {
	readonly systemSettingsPage: SystemSettingsPage;
	readonly enabledButton: Locator;
	readonly explicitCookieConsentModeButton: Locator;
	readonly actionsButton: Locator;
	readonly saveButton: Locator;
	readonly updateButton: Locator;
	readonly page: Page;
	readonly cookieBanner: Locator;
	readonly cookieBannerConfigurationButton: Locator;
	readonly cookieBannerParagraph: Locator;
	readonly cookieBannerAcceptAllButton: Locator;
	readonly cookieBannerDeclineAllButton: Locator;
	readonly fragmentWidgetSearchInput: Locator;

	constructor(page: Page) {
		this.systemSettingsPage = new SystemSettingsPage(page);
		this.enabledButton = page.getByLabel('Enabled');
		this.explicitCookieConsentModeButton = page.getByLabel(
			'Explicit Cookie Consent Mode'
		);
		this.actionsButton = page.getByRole('button', {name: 'Actions'});
		this.saveButton = page.getByRole('button', {
			name: 'Save',
		});
		this.updateButton = page.getByRole('button', {
			name: 'Update',
		});
		this.page = page;
		this.cookieBanner = page.locator(
			'#p_p_id_com_liferay_cookies_banner_web_portlet_CookiesBannerPortlet_'
		);
		this.cookieBannerConfigurationButton = page.getByRole('button', {
			name: 'Configuration',
		});
		this.cookieBannerParagraph = page
			.locator('//div[@role="dialog"][@aria-label="banner cookies"]')
			.locator('p.mb-0');
		this.cookieBannerAcceptAllButton = page.getByRole('button', {
			name: 'Accept All',
		});
		this.cookieBannerDeclineAllButton = page.getByRole('button', {
			name: 'Decline All',
		});
		this.fragmentWidgetSearchInput = page.getByLabel(
			'Search Fragments and Widgets'
		);
	}

	async goToSystemSetting() {
		await this.systemSettingsPage.goToSystemSetting(
			'Cookies',
			'Preference Handling'
		);
	}

	async editPage({layout}: {layout: Layout}) {
		await this.page.goto(`${layout.friendlyURL}?p_l_mode=edit`);
	}

	async searchFragmentOrWidget(itemName: string) {
		await this.fragmentWidgetSearchInput.fill(itemName);
	}

	async addHTMLComponent({layout}: {layout: Layout}) {
		await this.editPage({layout});

		await this.searchFragmentOrWidget('html');

		const htmlItem = this.page.getByRole('menuitem', {
			name: 'HTML Add HTML Mark HTML as Favorite',
		});

		await htmlItem.dragTo(
			this.page.getByText('Drag and drop fragments or widgets here.')
		);

		const htmlExample = this.page.getByText('HTML Example');

		await htmlExample.click();
		await htmlExample.click();
	}

	async enableThirdPartyCookies() {
		await this.goToSystemSetting();

		if (!await this.enabledButton.isChecked()) {
			await this.enabledButton.click();
		}

		await expect(this.enabledButton).toBeChecked();

		if (await this.saveButton.isVisible()) {
			await this.saveButton.click();
		}
		else if (await this.updateButton.isVisible()) {
			await this.updateButton.click()
		}

		await waitForAlert(this.page);
	}

	async acceptAllCookies() {
		await this.cookieBanner.waitFor({state: 'visible'});

		await this.cookieBannerAcceptAllButton.click();
	}

	async resetCookiePreferences() {
		await this.actionsButton.click();

		await this.page
			.getByRole('link', {name: 'Reset Default Values'})
			.click();

		await waitForAlert(this.page);
	}
}
