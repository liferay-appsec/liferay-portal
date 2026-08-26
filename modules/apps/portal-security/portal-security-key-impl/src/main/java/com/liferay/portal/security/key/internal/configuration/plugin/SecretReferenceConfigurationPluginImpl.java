/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.configuration.plugin;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Pedro Victor Silvestre
 */
@Component(
	property = {
		"config.plugin.id=com.liferay.portal.security.key.internal.configuration.plugin.SecretReferenceConfigurationPluginImpl",
		"service.cmRanking:Integer=1000"
	},
	service = ConfigurationPlugin.class
)
public class SecretReferenceConfigurationPluginImpl
	implements ConfigurationPlugin {

	@Override
	public void modifyConfiguration(
		ServiceReference<?> serviceReference,
		Dictionary<String, Object> properties) {

		List<String> keys = Collections.list(properties.keys());

		if (!_hasSecretReference(keys, properties)) {
			return;
		}

		String pid = GetterUtil.getString(
			properties.get(Constants.SERVICE_PID));

		SecretResolver secretResolver = _secretResolverSnapshot.get();

		if (secretResolver == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve the secret references in ",
						"configuration \"", pid,
						"\" because the secret resolver is unavailable"));
			}

			return;
		}

		Long companyId = _getCompanyId(properties);

		if (companyId == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve the secret references in ",
						"configuration \"", pid,
						"\" because its company is unknown"));
			}

			return;
		}

		for (String key : keys) {
			Object value = properties.get(key);

			if (value instanceof String) {
				properties.put(
					key,
					_resolve(
						companyId, key, pid, secretResolver, (String)value));
			}
			else if (value instanceof String[]) {
				String[] values = (String[])value;

				String[] resolvedValues = new String[values.length];

				for (int i = 0; i < values.length; i++) {
					resolvedValues[i] = _resolve(
						companyId, key, pid, secretResolver, values[i]);
				}

				properties.put(key, resolvedValues);
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private Long _getCompanyId(Dictionary<String, Object> properties) {
		long companyId = GetterUtil.getLong(
			properties.get(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey()),
			CompanyConstants.SYSTEM);

		if (companyId != CompanyConstants.SYSTEM) {
			return companyId;
		}

		long groupId = GetterUtil.getLong(
			properties.get(
				ExtendedObjectClassDefinition.Scope.GROUP.getPropertyKey()));

		if (groupId <= 0) {
			return CompanyConstants.SYSTEM;
		}

		ServiceReference<DataSource> dataSourceServiceReference =
			_bundleContext.getServiceReference(DataSource.class);

		if (dataSourceServiceReference == null) {
			_log.error("The data source service is unavailable");

			return null;
		}

		try {
			DataSource dataSource = _bundleContext.getService(
				dataSourceServiceReference);

			DB db = DBManagerUtil.getDB();

			String sql = db.buildSQL(
				"select companyId from Group_ where groupId = ?");

			try (Connection connection = dataSource.getConnection();

				PreparedStatement preparedStatement =
					connection.prepareStatement(sql)) {

				preparedStatement.setLong(1, groupId);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						return resultSet.getLong("companyId");
					}
				}
			}

			_log.error("No company was found for group " + groupId);

			return null;
		}
		catch (Exception exception) {
			_log.error(
				"Unable to get the company for group " + groupId, exception);

			return null;
		}
		finally {
			_bundleContext.ungetService(dataSourceServiceReference);
		}
	}

	private boolean _hasSecretReference(
		List<String> keys, Dictionary<String, Object> properties) {

		for (String key : keys) {
			Object value = properties.get(key);

			if ((value instanceof String) &&
				_isSecretReference((String)value)) {

				return true;
			}
			else if (value instanceof String[]) {
				for (String string : (String[])value) {
					if (_isSecretReference(string)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean _isSecretReference(String value) {
		if (!KeyReferenceUtil.isValidKeyReference(value)) {
			return false;
		}

		KeyReference keyReference = KeyReferenceUtil.toKeyReference(value);

		if (keyReference.getType() == KeyReference.Type.SECRET) {
			return true;
		}

		return false;
	}

	private String _resolve(
		long companyId, String key, String pid, SecretResolver secretResolver,
		String value) {

		if (!_isSecretReference(value)) {
			return value;
		}

		try {
			return secretResolver.resolve(companyId, value);
		}
		catch (SecretException secretException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve the secret reference in property \"",
						key, "\" of configuration \"", pid, "\" for company ",
						companyId),
					secretException);
			}

			return value;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SecretReferenceConfigurationPluginImpl.class);

	private static final Snapshot<SecretResolver> _secretResolverSnapshot =
		new Snapshot<>(
			SecretReferenceConfigurationPluginImpl.class, SecretResolver.class,
			null, true);

	private BundleContext _bundleContext;

}