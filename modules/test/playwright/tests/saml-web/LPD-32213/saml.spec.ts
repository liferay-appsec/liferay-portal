/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../../fixtures/loginTest';
import {searchAdminPageTest} from '../../../fixtures/searchAdminPageTest';
import {serverAdministrationPageTest} from '../../../fixtures/serverAdministrationPageTest';
import {usersAndOrganizationsPagesTest} from '../../../fixtures/usersAndOrganizationsPagesTest';
import {virtualInstancesPagesTest} from '../../../fixtures/virtualInstancesPagesTest';
import {liferayConfig} from '../../../liferay.config';
import {InstanceSettingsPage} from '../../../pages/configuration-admin-web/InstanceSettingsPage';
import {GeneralPage} from '../../../pages/instance-configuration-web/GeneralPage';
import {PagesAdminPage} from '../../../pages/layout-admin-web/PagesAdminPage';
import {IdentityProviderConnectionsPage} from '../../../pages/saml-web/IdentityProviderConnectionsPage';
import {SamlAdminPage} from '../../../pages/saml-web/SamlAdminPage';
import {ServiceProviderConnectionsPage} from '../../../pages/saml-web/ServiceProviderConnectionsPage';
import getRandomString from '../../../utils/getRandomString';
import performLogin from '../../../utils/performLogin';
import {configureIdentityProvider} from '.././utils/IdentityProviderUtil';
import {configureServiceProvider} from '.././utils/ServiceProviderUtil';
import {performSpInitiatedSSO} from '.././utils/samlAuthUtil';
import {connectSpAndIdp} from '.././utils/samlProviderConnectionUtil';
import {
	DEFAULT_IDP_NAME,
	DEFAULT_SP_NAME,
	DEFAULT_SP_URL,
	configureVirtualInstanceForSaml,
	createUser,
	deleteAfterTestProviderConnections,
	deleteAfterTestVirtualInstances,
	deleteVirtualInstance,
	performSamlSafeLogin,
	resetSamlConfiguration,
	resetSamlKeystoreManagerTarget,
	setupSamlInstances,
	updateRuntimeMetadataRefreshInterval,
	updateSamlKeystoreManagerTarget,
} from '.././utils/samlVirtualInstanceUtil';

export const test = mergeTests(
	applicationsMenuPageTest,
	loginTest(),
	searchAdminPageTest,
	usersAndOrganizationsPagesTest,
	serverAdministrationPageTest,
	virtualInstancesPagesTest
);

const resetAfterTestGeneralPage = new Set<string>();

test.afterAll(async ({browser}) => {

	// Remove virtual instances

	const newPage = await browser.newPage();

	await performLogin(newPage, 'test');

	for (const virtualInstanceName of deleteAfterTestVirtualInstances) {
		await deleteVirtualInstance(virtualInstanceName, newPage);
	}

	await newPage.waitForTimeout(60 * 1000);

	// Reset saml configuration, in cases where test failed before doing so

	await resetSamlConfiguration(newPage);

	// Reset saml keystore

	await resetSamlKeystoreManagerTarget(newPage);
});

test.afterEach(async ({browser}) => {
	const defaultBaseUrl = liferayConfig.environment.baseUrl;

	for (const instanceName of resetAfterTestGeneralPage) {
		liferayConfig.environment.baseUrl = `http://${instanceName}:8080`;

		// Reset general tab

		const newPage = await performSamlSafeLogin(browser, instanceName);

		const instanceSettingsPage = new InstanceSettingsPage(newPage);

		await instanceSettingsPage.goToInstanceSetting(
			'Instance Configuration',
			'General',
			false
		);

		const generalPage = new GeneralPage(instanceSettingsPage.page);

		await generalPage.resetNavigationFields();

		await newPage.close();
	}

	for (const instanceName of deleteAfterTestProviderConnections) {
		liferayConfig.environment.baseUrl = `http://${instanceName}:8080`;

		// Reset general tab

		const newPage = await performSamlSafeLogin(browser, instanceName);

		const samlAdminPage = new SamlAdminPage(newPage);

		await samlAdminPage.configureSAML(false);

		// Delete all connections

		if ((await samlAdminPage.samlRoleField.inputValue()) === 'idp') {
			const serviceProviderConnectionsPage =
				new ServiceProviderConnectionsPage(samlAdminPage.page);

			await serviceProviderConnectionsPage.goTo();

			await serviceProviderConnectionsPage.deleteServiceProviderConnections();

			await configureIdentityProvider(newPage);
		}
		else {
			const identityProviderConnectionsPage =
				new IdentityProviderConnectionsPage(samlAdminPage.page);

			await identityProviderConnectionsPage.goTo();

			await identityProviderConnectionsPage.deleteIdentityProviderConnections();

			await configureServiceProvider(newPage);
		}

		await newPage.close();
	}

	liferayConfig.environment.baseUrl = defaultBaseUrl;
});

test.beforeAll(async ({browser}) => {

	// Set saml keystore

	const newPage = await browser.newPage();

	await performLogin(newPage, 'test');

	await updateSamlKeystoreManagerTarget(
		newPage,
		'Document Library Keystore Manager'
	);

	// Update Runtime Metadata Refresh Interval value to a low value, otherwise
	// the tests may update faster than the interval, causing errors.

	await updateRuntimeMetadataRefreshInterval(newPage, '4');

	// Create virtual instances

	await setupSamlInstances(browser, newPage);

	await newPage.close();
});

test('LPD-32213 AC1 TC3: Verify SP initiated SSO with a Default Landing Page and Home Url configured on SP instance redirects user back to Default Landing Page.', async ({
	browser,
}) => {
	const idpAdminPage = await configureVirtualInstanceForSaml(
		browser,
		DEFAULT_IDP_NAME,
		'Identity Provider'
	);

	const spAdminPage = await configureVirtualInstanceForSaml(
		browser,
		DEFAULT_SP_NAME,
		'Service Provider'
	);

	await connectSpAndIdp(
		idpAdminPage,
		DEFAULT_IDP_NAME,
		spAdminPage,
		DEFAULT_SP_NAME
	);

	// Create a user on the IdP instance

	const userAccount = await createUser(idpAdminPage, DEFAULT_IDP_NAME);

	// Configure Default Landing Page on SP instance

	const pagesAdminPage = new PagesAdminPage(spAdminPage);

	await pagesAdminPage.goto();

	const defaultLandingPageTitle = getRandomString();

	await pagesAdminPage.createNewPage({
		name: defaultLandingPageTitle,
	});

	const defaultLandingPagePath = '/web/guest/' + defaultLandingPageTitle;

	// Configure Home Url on SP instance

	await pagesAdminPage.goto();

	const homeUrlPageTitle = getRandomString();

	await pagesAdminPage.createNewPage({
		name: homeUrlPageTitle,
	});

	const homeUrlPagePath = '/web/guest/' + homeUrlPageTitle;

	// Configure Default Landing Page and Home Url

	const instanceSettingsPage = new InstanceSettingsPage(spAdminPage);

	await instanceSettingsPage.goToInstanceSetting(
		'Instance Configuration',
		'General',
		false
	);

	const generalPage = new GeneralPage(instanceSettingsPage.page);

	await generalPage.editDefaultLandingPage(defaultLandingPagePath);
	await generalPage.editHomeUrl(homeUrlPagePath);

	resetAfterTestGeneralPage.add(DEFAULT_SP_NAME);

	// SP initiated SSO

	const newPage = await performSpInitiatedSSO(
		browser,
		userAccount.emailAddress,
		DEFAULT_SP_URL
	);

	// Expect to be redirected back to Default Landing Page configuration value

	expect(await newPage.url()).toContain(
		DEFAULT_SP_URL + defaultLandingPagePath
	);
});
