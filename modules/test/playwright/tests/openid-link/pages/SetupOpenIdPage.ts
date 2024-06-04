/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {Page} from '@playwright/test';

export class SetupOpenIdPage {
    someLocalVar;
    readonly otherLocalVar: Page;

    constructor(page: Page) {
        this.someLocalVar = "trial";
        this.otherLocalVar = page;
    }

    //go to instance setting
    //setup openId
    //Remove everything after

    async enableOpenIdConnection (){
        this.someLocalVar = "test";
    }

    async setupMockedOpenIdData (title: String){

    }

    async removeOpenIdConnection (title: String){

    }
}