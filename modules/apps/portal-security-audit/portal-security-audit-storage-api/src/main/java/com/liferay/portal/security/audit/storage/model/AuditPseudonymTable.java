/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

import java.util.Date;

/**
 * The table class for the &quot;Audit_AuditPseudonym&quot; database table.
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonym
 * @generated
 */
public class AuditPseudonymTable extends BaseTable<AuditPseudonymTable> {

	public static final AuditPseudonymTable INSTANCE =
		new AuditPseudonymTable();

	public final Column<AuditPseudonymTable, Long> auditPseudonymId =
		createColumn(
			"auditPseudonymId", Long.class, Types.BIGINT, Column.FLAG_PRIMARY);
	public final Column<AuditPseudonymTable, Long> companyId = createColumn(
		"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<AuditPseudonymTable, Date> createDate = createColumn(
		"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<AuditPseudonymTable, String> contextName = createColumn(
		"contextName", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<AuditPseudonymTable, String> fieldCategory =
		createColumn(
			"fieldCategory", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<AuditPseudonymTable, String> identityValue =
		createColumn(
			"identityValue", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<AuditPseudonymTable, String> identityValueHash =
		createColumn(
			"identityValueHash", String.class, Types.VARCHAR,
			Column.FLAG_DEFAULT);

	private AuditPseudonymTable() {
		super("Audit_AuditPseudonym", AuditPseudonymTable::new);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-690142401