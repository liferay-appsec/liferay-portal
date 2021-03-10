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

package com.liferay.app.builder.internal.upgrade.v2_0_0;

import com.liferay.app.builder.internal.upgrade.v2_0_0.util.AppBuilderAppTable;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Rafael Praxedes
 */
public class AppBuilderAppUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		if (!hasColumn("AppBuilderApp", "active_")) {
			alter(
				AppBuilderAppTable.class,
				new AlterTableAddColumn("active_", "BOOLEAN"));

			try (PreparedStatement ps1 = connection.prepareStatement(
					"select appBuilderAppId, status from AppBuilderApp");
				PreparedStatement ps2 =
					AutoBatchPreparedStatementUtil.concurrentAutoBatch(
						connection,
						"update AppBuilderApp set active_ = ? where " +
							"appBuilderAppId = ?");
				ResultSet rs = ps1.executeQuery()) {

				while (rs.next()) {
					ps2.setBoolean(
						1, (rs.getInt("status") == 0) ? true : false);
					ps2.setLong(2, rs.getLong("appBuilderAppId"));

					ps2.addBatch();
				}

				ps2.executeBatch();
			}
		}

		if (hasColumn("AppBuilderApp", "status")) {
			alter(AppBuilderAppTable.class, new AlterTableDropColumn("status"));
		}
	}

}