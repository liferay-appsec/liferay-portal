/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.internal.upgrade.v2_5_0;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;

/**
 * @author Manuele Castro
 */
public class AuditEventContextNameUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			processConcurrently(
				"select auditEventId from Audit_AuditEvent where contextName " +
					"is null or contextName = ''",
				"update Audit_AuditEvent set contextName = 'INSTANCE' where " +
					"auditEventId = ?",
				resultSet -> new Object[] {resultSet.getLong("auditEventId")},
				(values, preparedStatement) -> {
					preparedStatement.setLong(1, (Long)values[0]);

					preparedStatement.addBatch();
				},
				"Unable to set the default context name of audit events");
		}
	}

}