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

package com.liferay.oauth.client.persistence.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Clob;
import java.sql.Types;

import java.util.Date;

/**
 * The table class for the &quot;OAuthClientASMetadata&quot; database table.
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadata
 * @generated
 */
public class OAuthClientASMetadataTable
	extends BaseTable<OAuthClientASMetadataTable> {

	public static final OAuthClientASMetadataTable INSTANCE =
		new OAuthClientASMetadataTable();

	public final Column<OAuthClientASMetadataTable, Long> mvccVersion =
		createColumn(
			"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<OAuthClientASMetadataTable, Long>
		oAuthClientASMetadataId = createColumn(
			"oAuthClientASMetadataId", Long.class, Types.BIGINT,
			Column.FLAG_PRIMARY);
	public final Column<OAuthClientASMetadataTable, Long> companyId =
		createColumn(
			"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, Long> userId = createColumn(
		"userId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, Date> createDate =
		createColumn(
			"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, Date> modifiedDate =
		createColumn(
			"modifiedDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, String> discoveryEndpoint =
		createColumn(
			"discoveryEndpoint", String.class, Types.VARCHAR,
			Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, String> issuer =
		createColumn(
			"issuer", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<OAuthClientASMetadataTable, Clob> metadataJSON =
		createColumn(
			"metadataJSON", Clob.class, Types.CLOB, Column.FLAG_DEFAULT);

	private OAuthClientASMetadataTable() {
		super("OAuthClientASMetadata", OAuthClientASMetadataTable::new);
	}

}