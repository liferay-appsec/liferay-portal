/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {captchaConfigPageTest} from '../../fixtures/captchaConfigPageTest';
import {formsPagesTest} from '../../fixtures/formsPagesTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {pageViewModePagesTest} from '../../fixtures/pageViewModePagesTest';
import {getRandomInt} from '../../utils/getRandomInt';
import getRandomString from '../../utils/getRandomString';
import {performLogout} from '../../utils/performLogin';

const test = mergeTests(
	apiHelpersTest,
	captchaConfigPageTest,
	formsPagesTest,
	isolatedSiteTest,
	loginTest(),
	pageViewModePagesTest
);

test('LPD-47067 check that two forms on same page with simplecaptcha could refresh both captchas', async ({
	apiHelpers,
	captchaConfigPage,
	formBuilderPage,
	formBuilderSidePanelPage,
	formSettingsModalPage,
	formWidgetPage,
	page,
	site,
	widgetPagePage,
}) => {
	await captchaConfigPage.goTo();

	await captchaConfigPage.resetCaptchaConfiguration();

	await formBuilderPage.goToNew(site.friendlyUrlPath);

	await expect(formBuilderPage.newFormHeading).toBeVisible();

	const formName = 'Form' + getRandomInt();

	await formBuilderPage.fillFormTitle(formName);

	await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

	await formBuilderPage.formSettingsButton.click();

	await formBuilderPage.requireCaptchaToggle.click();

	await formSettingsModalPage.clickDoneButton();

	await formBuilderPage.publishButton.click();

	await expect(
		page.getByText('Your request completed successfully')
	).toBeVisible();

	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		options: {
			type: 'portlet',
		},
		title: getRandomString(),
	});

	await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyURL}`);

	await addAndConfigureForm(
		formWidgetPage.dropdownButton,
		formName,
		formWidgetPage,
		widgetPagePage
	);

	const dropdownButton = widgetPagePage.page
		.locator(
			'[id^="portlet-topper-toolbar_com_liferay_dynamic_data_mapping_form_web_portlet_DDMFormPortlet_INSTANCE_"]'
		)
		.last();

	await addAndConfigureForm(
		dropdownButton,
		formName,
		formWidgetPage,
		widgetPagePage
	);

	await refreshAndCheckCaptcha(page);

	await refreshAndCheckCaptcha(page);

	await performLogout(page);

	await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyURL}`);

	await refreshAndCheckCaptcha(page);

	await refreshAndCheckCaptcha(page);
});

async function addAndConfigureForm(
	dropdownButton,
	formName,
	formWidgetPage,
	widgetPagePage
) {
	await widgetPagePage.addPortlet('Form');

	await dropdownButton.hover();
	await dropdownButton.click();
	await formWidgetPage.configurationDropdownButton.first().click();

	const formLink = formWidgetPage.getFormLink(formName);

	await formLink.click();

	await formWidgetPage.saveButton.click();

	await formWidgetPage.page.keyboard.press('Escape');

	await widgetPagePage.page.waitForLoadState();

	await widgetPagePage.page.reload();
}

async function refreshAndCheckCaptcha(page) {
	const captchaImgSource1 = await page
		.getByAltText('Text to Identify')
		.first()
		.getAttribute('src');

	await page.getByTitle('Refresh CAPTCHA').first().click();

	const refreshedCaptchaImgSource1 = await page
		.getByAltText('Text to Identify')
		.first()
		.getAttribute('src');

	await expect(captchaImgSource1).not.toEqual(refreshedCaptchaImgSource1);

	const captchaImgSource2 = await page
		.getByAltText('Text to Identify')
		.last()
		.getAttribute('src');

	await page.getByTitle('Refresh CAPTCHA').last().click();

	const refreshedCaptchaImgSource2 = await page
		.getByAltText('Text to Identify')
		.last()
		.getAttribute('src');

	await expect(captchaImgSource2).not.toEqual(refreshedCaptchaImgSource2);
}
