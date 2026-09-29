/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../../fixtures/loginTest';
import {serverAdministrationPageTest} from '../../../fixtures/serverAdministrationPageTest';
import getRandomString from '../../../utils/getRandomString';

export const test = mergeTests(loginTest(), serverAdministrationPageTest);

const AUDIT_PORTLET_ID =
	'com_liferay_portal_security_audit_web_portlet_AuditPortlet';

test(
	'Show unknown additional information keys in the audit event detail',
	{tag: '@LPD-97707'},
	async ({page, serverAdministrationPage}) => {
		const key = getRandomString();
		const value = getRandomString();

		await serverAdministrationPage.goto();

		await serverAdministrationPage.executeScript(`
		import com.liferay.portal.kernel.audit.AuditMessage;
		import com.liferay.portal.kernel.json.JSONUtil;
		import com.liferay.portal.kernel.util.PortalUtil;
		import com.liferay.portal.security.audit.storage.service.AuditEventLocalServiceUtil;

		def auditEvent = AuditEventLocalServiceUtil.addAuditEvent(
			new AuditMessage(
				0, PortalUtil.getDefaultCompanyId(), 0, "${getRandomString()}",
				new Date(), JSONUtil.put("${key}", "${value}"), null, null,
				"${getRandomString().toUpperCase()}", null));

		out.print(auditEvent.getAuditEventId());
	`);

		const auditEventId = (
			await serverAdministrationPage.getScriptOutput()
		).trim();

		try {
			await page.goto(
				`/group/control_panel/manage?p_p_id=${AUDIT_PORTLET_ID}&_${AUDIT_PORTLET_ID}_mvcPath=/view_audit_event.jsp&_${AUDIT_PORTLET_ID}_auditEventId=${auditEventId}`
			);

			const additionalInformation = page
				.locator('.field-wrapper')
				.filter({hasText: 'Additional Information'});

			await expect(additionalInformation).toContainText(key);
			await expect(additionalInformation).toContainText(value);
		}
		finally {
			await serverAdministrationPage.goto();

			await serverAdministrationPage.executeScript(`
			import com.liferay.portal.security.audit.storage.service.AuditEventLocalServiceUtil;

			AuditEventLocalServiceUtil.deleteAuditEvent(${auditEventId});
		`);
		}
	}
);
