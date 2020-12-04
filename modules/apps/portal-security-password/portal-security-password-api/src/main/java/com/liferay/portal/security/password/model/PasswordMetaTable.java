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
 * The table class for the &quot;PasswordMeta&quot; database table.
 *
 * @author Arthur Chan
 * @see PasswordMeta
 * @generated
 */
public class PasswordMetaTable extends BaseTable<PasswordMetaTable> {

	public static final PasswordMetaTable INSTANCE = new PasswordMetaTable();

	public final Column<PasswordMetaTable, Long> mvccVersion = createColumn(
		"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<PasswordMetaTable, String> uuid = createColumn(
		"uuid_", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<PasswordMetaTable, Long> passwordMetaId = createColumn(
		"passwordMetaId", Long.class, Types.BIGINT, Column.FLAG_PRIMARY);
	public final Column<PasswordMetaTable, Long> companyId = createColumn(
		"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PasswordMetaTable, Date> createDate = createColumn(
		"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<PasswordMetaTable, Long> passwordEntryId = createColumn(
		"passwordEntryId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PasswordMetaTable, String> salt = createColumn(
		"salt", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<PasswordMetaTable, String> pepperId = createColumn(
		"pepperId", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);

	private PasswordMetaTable() {
		super("PasswordMeta", PasswordMetaTable::new);
	}

}