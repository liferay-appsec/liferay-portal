/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {SamlAdminPage} from '../pages/saml-web/SamlAdminPage';
import {ServiceProviderConnectionPage} from '../pages/saml-web/ServiceProviderConnectionPage';

const samlAdminPagesTest = test.extend<{
    samlAdminPage: SamlAdminPage;
    serviceProviderConnectionPage: ServiceProviderConnectionPage;
}>({
    samlAdminPage: async ({page}, use) => {
        await use(new SamlAdminPage(page));
    },
    serviceProviderConnectionPage: async ({page}, use) => {
        await use(new ServiceProviderConnectionPage(page));
    },
});

export {samlAdminPagesTest};
