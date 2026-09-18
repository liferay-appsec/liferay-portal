/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth.client.persistence.internal.upgrade.v1_6_3;

import com.liferay.portal.kernel.upgrade.BaseIndexedColumnSizeUpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Alvaro Saugar
 */
public class OAuthClientColumnSizeUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		for (UpgradeProcess upgradeProcess : _upgradeProcesses) {
			upgradeProcess.upgrade();
		}
	}

	private static final UpgradeProcess[] _upgradeProcesses = {
		new ColumnSizeUpgradeProcess(
			"issuer", new String[] {"companyId"}, "OAuthClientASLocalMetadata"),
		new ColumnSizeUpgradeProcess(
			"localWellKnownURI", new String[] {"companyId"},
			"OAuthClientASLocalMetadata"),
		new ColumnSizeUpgradeProcess(
			"oAuthASLocalWellKnownURI", new String[] {"companyId"},
			"OAuthClientASLocalMetadata"),
		new ColumnSizeUpgradeProcess(
			"authServerWellKnownURI", new String[] {"clientId", "companyId"},
			"OAuthClientEntry"),
		new ColumnSizeUpgradeProcess(
			"clientId", new String[] {"authServerWellKnownURI", "companyId"},
			"OAuthClientEntry"),
		new ColumnSizeUpgradeProcess(
			"localWellKnownURI", new String[] {"companyId"},
			"OAuthClientPRLocalMetadata"),
		new ColumnSizeUpgradeProcess(
			"protectedResourceURI", new String[] {"companyId"},
			"OAuthClientPRLocalMetadata")
	};

	private static class ColumnSizeUpgradeProcess
		extends BaseIndexedColumnSizeUpgradeProcess {

		@Override
		protected String getColumnName() {
			return _columnName;
		}

		@Override
		protected String[] getGroupByColumnNames() {
			return _groupByColumnNames;
		}

		@Override
		protected int getMaxColumnLength() {
			return 255;
		}

		@Override
		protected String getTableName() {
			return _tableName;
		}

		private ColumnSizeUpgradeProcess(
			String columnName, String[] groupByColumnNames, String tableName) {

			_columnName = columnName;
			_groupByColumnNames = groupByColumnNames;
			_tableName = tableName;
		}

		private final String _columnName;
		private final String[] _groupByColumnNames;
		private final String _tableName;

	}

}