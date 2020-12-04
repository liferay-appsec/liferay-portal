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

package com.liferay.portal.security.password.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

import java.util.Date;

/**
 * The table class for the &quot;PasswordEntry&quot; database table.
 *
 * @author Arthur Chan
 * @see PasswordEntry
 * @generated
 */
public class PasswordEntryTable extends BaseTable<PasswordEntryTable> {

	public static final PasswordEntryTable INSTANCE = new PasswordEntryTable();

	public final Column<PasswordEntryTable, Long> mvccVersion = createColumn(
		"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<PasswordEntryTable, String> uuid = createColumn(
		"uuid_", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<PasswordEntryTable, Long> passwordEntryId =
		createColumn(
			"passwordEntryId", Long.class, Types.BIGINT, Column.FLAG_PRIMARY);
	public final Column<PasswordEntryTable, Long> userId = createColumn(
		"userId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PasswordEntryTable, Long> companyId = createColumn(
		"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PasswordEntryTable, Date> createDate = createColumn(
		"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<PasswordEntryTable, String> hash = createColumn(
		"hash", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);

	private PasswordEntryTable() {
		super("PasswordEntry", PasswordEntryTable::new);
	}

}