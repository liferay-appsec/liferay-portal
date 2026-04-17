/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.v7_4_x;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.keystore.CompanyKeyStoreUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author Pedro Victor Silvestre
 */
public class UpgradeCompanyInfo extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		String keyAlgorithm = StringUtil.toUpperCase(
			GetterUtil.getString(
				PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM)));

		if (_log.isInfoEnabled()) {
			_log.info(
				"Migrating company encryption keys from database to KeyStore");
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
			"SELECT companyInfoId, companyId, key_ FROM CompanyInfo");

			 ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				String keyValue = resultSet.getString("key_");

				if ((keyValue == null) || keyValue.isEmpty()) {
					continue;
				}

				long companyId = resultSet.getLong("companyId");

				if (CompanyKeyStoreUtil.isKeyStoreAlias(keyValue)) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							StringBundler.concat(
								"Company ", companyId,
								" key is already a KeyStore alias: ",
								keyValue));
					}

					continue;
				}

				try {
					byte[] keyBytes = Base64.decode(keyValue);

					SecretKeySpec secretKeySpec = new SecretKeySpec(
						keyBytes, keyAlgorithm);

					Arrays.fill(keyBytes, (byte)0);

					String alias = CompanyKeyStoreUtil.generateAlias(companyId);

					CompanyKeyStoreUtil.setKey(alias, secretKeySpec);

					try (PreparedStatement updatePreparedStatement =
							connection.prepareStatement(
								"UPDATE CompanyInfo SET key_ = ? WHERE " +
									"companyInfoId = ?")) {

						updatePreparedStatement.setString(1, alias);
						updatePreparedStatement.setLong(
							2, resultSet.getLong("companyInfoId"));

						updatePreparedStatement.executeUpdate();
					}

					if (_log.isInfoEnabled()) {
						_log.info(
							StringBundler.concat(
								"Migrated company ", companyId,
								" key to KeyStore alias: ", alias));
					}
				}
				catch (Exception exception) {
					_log.error(
						"Unable to migrate company " + companyId +
							" key to KeyStore",
						exception);
				}
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeCompanyInfo.class);

}