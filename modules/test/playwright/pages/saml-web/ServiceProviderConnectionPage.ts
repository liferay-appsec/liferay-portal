/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export class ServiceProviderConnectionPage {
    readonly applicationsMenuPage;
    readonly page: Page;
    readonly nameField: Locator;
    readonly entityIdField: Locator;
    readonly enabledField: Locator;
    readonly assertionLifetimeField: Locator;
    readonly forceEncryptionToggle: Locator;
    readonly metadataUrlField: Locator;
    readonly nameIdentifierFormatField: Locator;
    readonly nameIdentifierAttributeNameField: Locator;
    readonly attributesEnabledToggle: Locator;
    readonly attributesNamespaceEnabledToggle: Locator;
    readonly attributesField: Locator;
    readonly keepAliveUrlField: Locator;
    readonly saveButton: Locator;
    readonly successMessage: Locator;

    constructor(page: Page) {
        this.page = page;
        this.applicationsMenuPage = new ApplicationsMenuPage(page);

        //This is returning a new entries, maybe do .first()?
        this.nameField = page.getByLabel('Name Required', { exact: true });
        this.entityIdField = page.getByLabel('Entity ID');
        this.enabledField = page.getByText('Enabled', { exact: true });
        this.assertionLifetimeField = page.getByLabel('Assertion Lifetime');
        this.forceEncryptionToggle = page.getByText('Force Encryption');
        this.metadataUrlField = page.getByLabel('Metadata URL', { exact: true });
        this.nameIdentifierFormatField = page.getByLabel('Name Identifier Format');
        this.nameIdentifierAttributeNameField = page.getByLabel('Name Identifier Attribute Name');
        this.attributesEnabledToggle = page.getByText('Attributes Enabled');
        this.attributesNamespaceEnabledToggle = page.getByText('Attributes Namespace Enabled');
        this.attributesField = page.getByLabel('Attributes');
        this.keepAliveUrlField = page.getByLabel('Keep Alive URL');

        this.saveButton = page.getByRole('button', {name: 'Save'});

        this.successMessage = page.getByText(
            'Your request completed successfully'
        );
    }

    async goToServiceProviderConnectionsTab() {
        await this.applicationsMenuPage.goToSamlAdmin();
        await this.page.getByRole('tab', {name: 'Service Provider Connections'}).click();
        await expect(
            await this.page.getByRole('button', {name: 'Add Service Provider'})
        ).toBeVisible();
    }

    async addNewServiceProviderConnection(
        name: string,
        entityId: string,
        enabled = true,
        assertionLifetime = '1800',
        forceEncrytion = false,
        metadataURL: string,
        nameIdentifierFormat = 'Email Address',
        nameIdentifierAttributeName = 'emailAddress',
        attributesEnabled = false,
        attributesNamespaceEnabled = false,
        attributes?: string,
        keepAliveUrl?: string,
    ) {
        await this.goToServiceProviderConnectionsTab();

        await this.page.getByRole('button', {name: 'Add Service Provider'}).click();

        await this.populateAndSaveServiceProviderConnectionDetails(
            name, entityId, enabled, assertionLifetime, forceEncrytion,
            metadataURL, nameIdentifierFormat, nameIdentifierAttributeName,
            attributesEnabled, attributesNamespaceEnabled, attributes,
            keepAliveUrl);
    }

    async editServiceProviderConnection(
        name: string,
        entityId?: string,
        enabled?: boolean,
        assertionLifetime?: string,
        forceEncrytion?: boolean,
        metadataURL?: string,
        nameIdentifierFormat?: string,
        nameIdentifierAttributeName?: string,
        attributesEnabled?: boolean,
        attributesNamespaceEnabled?: boolean,
        attributes?: string,
        keepAliveUrl?: string,
    ) {
        await this.goToServiceProviderConnectionsTab();

      //needs to find and edit the correct entry

        await this.populateAndSaveServiceProviderConnectionDetails(
            name, entityId, enabled, assertionLifetime, forceEncrytion,
            metadataURL, nameIdentifierFormat, nameIdentifierAttributeName,
            attributesEnabled, attributesNamespaceEnabled, attributes,
            keepAliveUrl);
    }

    private async populateAndSaveServiceProviderConnectionDetails(
        name: string,
        entityId: string,
        enabled = true,
        assertionLifetime = '1800',
        forceEncrytion = false,
        metadataURL: string,
        nameIdentifierFormat = 'Email Address',
        nameIdentifierAttributeName = 'emailAddress',
        attributesEnabled = false,
        attributesNamespaceEnabled = false,
        attributes?: string,
        keepAliveUrl?: string,
    ) {
        await this.nameField.fill(name);
        await this.entityIdField.fill(entityId);
        await this.enabledField.setChecked(enabled);
        await this.assertionLifetimeField.fill(assertionLifetime);
        await this.forceEncryptionToggle.setChecked(forceEncrytion);
        await this.metadataUrlField.fill(metadataURL);
        await this.nameIdentifierFormatField.selectOption(nameIdentifierFormat);
        await this.nameIdentifierAttributeNameField.fill(nameIdentifierAttributeName);
        await this.attributesEnabledToggle.setChecked(attributesEnabled);
        await this.attributesNamespaceEnabledToggle.setChecked(attributesNamespaceEnabled);
        
        if(attributes!== undefined){
            await this.attributesField.fill(attributes);
        }
        if(keepAliveUrl !== undefined){
            await this.keepAliveUrlField.fill(keepAliveUrl);
        }
        
        await this.saveButton.click();

        await expect(await this.successMessage).toBeVisible();
    }
}
