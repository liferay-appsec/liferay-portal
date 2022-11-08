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

package com.liferay.oauth2.provider.internal.upgrade.v4_3_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Bowerman
 */
public class OAuth2ScopeGrantUpgradeProcess extends UpgradeProcess {

	public OAuth2ScopeGrantUpgradeProcess(
		CompanyLocalService companyLocalService) {

		_companyLocalService = companyLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(
			companyId -> _upgradeCompany(companyId));
	}

	private boolean _hasObjectDefinition(long companyId, String name)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select * from ObjectDefinition where companyId = ? and name " +
					"= ?")) {

			preparedStatement.setLong(1, companyId);
			preparedStatement.setString(2, name);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private void _upgradeCompany(long companyId) throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select oAuth2ScopeGrantId, applicationName, scopeAliases ",
					"from OAuth2ScopeGrant where companyId = ? and ",
					"bundleSymbolicName = ?"))) {

			preparedStatement.setLong(1, companyId);
			preparedStatement.setString(2, _BUNDLE_SYMBOLIC_NAME);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				String companyIdString = String.valueOf(companyId);

				while (resultSet.next()) {
					String applicationName = resultSet.getString(
						"applicationName");

					if (Validator.isNull(applicationName) ||
						applicationName.endsWith(companyIdString)) {

						continue;
					}

					if (!_hasObjectDefinition(companyId, applicationName)) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								StringBundler.concat(
									"Detected OAuth2 scope grant for ",
									"object-related scope named '",
									applicationName, "' in company ", companyId,
									", but no object definition with that ",
									"name exists in that company"));
						}

						continue;
					}

					_upgradeOAuth2ScopeGrant(
						resultSet.getLong("oAuth2ScopeGrantId"),
						companyIdString, applicationName,
						resultSet.getString("scopeAliases"));
				}
			}
		}
	}

	private void _upgradeOAuth2ScopeGrant(
			long oAuth2ScopeGrantId, String companyId, String applicationName,
			String scopeAliases)
		throws Exception {

		String upgradedApplicationName = applicationName + companyId;

		List<String> scopeAliasesList = Arrays.asList(
			StringUtil.split(scopeAliases, StringPool.SPACE));

		for (int i = 0; i < scopeAliasesList.size(); i++) {
			String scopeAlias = scopeAliasesList.get(i);

			if (!scopeAlias.startsWith(applicationName) ||
				scopeAlias.startsWith(upgradedApplicationName)) {

				continue;
			}

			scopeAliasesList.set(
				i,
				StringUtil.replaceFirst(
					scopeAlias, applicationName, upgradedApplicationName));
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"update OAuth2ScopeGrant set applicationName = ?, " +
					"scopeAliases = ? where oAuth2ScopeGrantId = ?")) {

			preparedStatement.setString(1, upgradedApplicationName);
			preparedStatement.setString(
				2,
				StringUtil.merge(
					ListUtil.sort(scopeAliasesList), StringPool.SPACE));
			preparedStatement.setLong(3, oAuth2ScopeGrantId);

			preparedStatement.execute();
		}
	}

	private static final String _BUNDLE_SYMBOLIC_NAME =
		"com.liferay.object.rest.impl";

	private static final Log _log = LogFactoryUtil.getLog(
		OAuth2ScopeGrantUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;

}