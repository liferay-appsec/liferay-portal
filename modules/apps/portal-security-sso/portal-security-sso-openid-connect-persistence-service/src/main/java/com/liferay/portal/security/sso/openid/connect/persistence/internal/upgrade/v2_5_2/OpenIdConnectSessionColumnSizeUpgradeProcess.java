/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.persistence.internal.upgrade.v2_5_2;

import com.liferay.portal.kernel.upgrade.BaseIndexedColumnSizeUpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Alvaro Saugar
 */
public class OpenIdConnectSessionColumnSizeUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		for (UpgradeProcess upgradeProcess : _upgradeProcesses) {
			upgradeProcess.upgrade();
		}
	}

	private static final UpgradeProcess[] _upgradeProcesses = {
		new ColumnSizeUpgradeProcess(
			"authServerWellKnownURI", new String[] {"clientId", "userId"}),
		new ColumnSizeUpgradeProcess(
			"clientId", new String[] {"authServerWellKnownURI", "userId"})
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
			return "OpenIdConnectSession";
		}

		private ColumnSizeUpgradeProcess(
			String columnName, String[] groupByColumnNames) {

			_columnName = columnName;
			_groupByColumnNames = groupByColumnNames;
		}

		private final String _columnName;
		private final String[] _groupByColumnNames;

	}

}