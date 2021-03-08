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

package com.liferay.dynamic.data.mapping.internal.upgrade.v1_1_1;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.metadata.RawMetadataProcessor;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Lino Alves
 */
public class DDMStructureIndexTypeUpgradeProcess extends UpgradeProcess {

	public DDMStructureIndexTypeUpgradeProcess(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeDDMStructureDefinition();
	}

	private void _upgradeDDMStructureDefinition() throws Exception {
		StringBundler sb = new StringBundler(2);

		sb.append("select DDMStructure.definition, DDMStructure.structureId ");
		sb.append("from DDMStructure where structureKey = ? ");

		try (PreparedStatement ps1 = connection.prepareStatement(sb.toString());
			PreparedStatement ps2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update DDMStructure set definition = ? where " +
						"structureId = ?");
			PreparedStatement ps3 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update DDMStructureVersion set definition = ? where " +
						"structureId = ?")) {

			ps1.setString(1, RawMetadataProcessor.TIKA_RAW_METADATA);

			try (ResultSet rs = ps1.executeQuery()) {
				while (rs.next()) {
					String newDefinition = _upgradeIndexType(rs.getString(1));

					ps2.setString(1, newDefinition);

					ps2.setLong(2, rs.getLong(2));

					ps2.addBatch();

					ps3.setString(1, newDefinition);

					ps3.setLong(2, rs.getLong(2));

					ps3.addBatch();
				}

				ps2.executeBatch();
				ps3.executeBatch();
			}
		}
	}

	private String _upgradeIndexType(String definition) throws PortalException {
		try {
			JSONObject definitionJSONObject = _jsonFactory.createJSONObject(
				definition);

			JSONArray fieldsJSONArray = definitionJSONObject.getJSONArray(
				"fields");

			for (int i = 0; i < fieldsJSONArray.length(); i++) {
				JSONObject jsonObject = fieldsJSONArray.getJSONObject(i);

				if (!jsonObject.has("indexType")) {
					jsonObject.put("indexType", "text");
				}
			}

			return definitionJSONObject.toString();
		}
		catch (JSONException jsonException) {
			return definition;
		}
	}

	private final JSONFactory _jsonFactory;

}