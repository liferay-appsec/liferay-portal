/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.ldap.persistence.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

/**
 * The table class for the &quot;LDAPServerAttributeRel&quot; database table.
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRel
 * @generated
 */
public class LDAPServerAttributeRelTable
	extends BaseTable<LDAPServerAttributeRelTable> {

	public static final LDAPServerAttributeRelTable INSTANCE =
		new LDAPServerAttributeRelTable();

	public final Column<LDAPServerAttributeRelTable, Long> mvccVersion =
		createColumn(
			"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<LDAPServerAttributeRelTable, Long>
		ldapServerAttributeRelId = createColumn(
			"ldapServerAttributeRelId", Long.class, Types.BIGINT,
			Column.FLAG_PRIMARY);
	public final Column<LDAPServerAttributeRelTable, Long> companyId =
		createColumn(
			"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<LDAPServerAttributeRelTable, Long> ldapServerId =
		createColumn(
			"ldapServerId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<LDAPServerAttributeRelTable, Long> classNameId =
		createColumn(
			"classNameId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<LDAPServerAttributeRelTable, Long> classPK =
		createColumn("classPK", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);

	private LDAPServerAttributeRelTable() {
		super("LDAPServerAttributeRel", LDAPServerAttributeRelTable::new);
	}

}