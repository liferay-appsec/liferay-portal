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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.NamedThreadFactory;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.security.key.spi.secret.SecretProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Pedro Victor Silvestre
 */
public class SecretReferenceConfigurationPluginImpl
	implements ConfigurationPlugin {

	public SecretReferenceConfigurationPluginImpl(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_configurationAdminServiceTracker = new ServiceTracker<>(
			bundleContext, ConfigurationAdmin.class, null);
		_secretProviderServiceTracker = _createServiceTracker(
			SecretProvider.class);
		_secretResolverServiceTracker = _createServiceTracker(
			SecretResolver.class);
	}

	public void close() {
		_secretProviderServiceTracker.close();
		_secretResolverServiceTracker.close();

		_executorService.shutdownNow();

		_configurationAdminServiceTracker.close();
	}

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

		SecretResolver secretResolver =
			_secretResolverServiceTracker.getService();

		if (secretResolver == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to resolve the secret references in ",
						"configuration \"", pid,
						"\" because the secret resolver is unavailable"));
			}

			_record(keys, pid, properties);

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

			_record(keys, pid, properties);

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

		_record(keys, pid, properties);
	}

	public void open() {
		_configurationAdminServiceTracker.open();

		_secretProviderServiceTracker.open();
		_secretResolverServiceTracker.open();
	}

	private <T> ServiceTracker<T, T> _createServiceTracker(Class<T> clazz) {
		return new ServiceTracker<>(
			_bundleContext, clazz,
			new ServiceTrackerCustomizer<T, T>() {

				@Override
				public T addingService(ServiceReference<T> serviceReference) {
					if (!_pids.isEmpty()) {
						_executorService.submit(
							SecretReferenceConfigurationPluginImpl.this::
								_redeliver);
					}

					return _bundleContext.getService(serviceReference);
				}

				@Override
				public void modifiedService(
					ServiceReference<T> serviceReference, T service) {
				}

				@Override
				public void removedService(
					ServiceReference<T> serviceReference, T service) {

					_bundleContext.ungetService(serviceReference);
				}

			});
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

	private void _record(
		List<String> keys, String pid, Dictionary<String, Object> properties) {

		if (Validator.isNull(pid) || !_hasSecretReference(keys, properties)) {
			return;
		}

		_pids.add(pid);
	}

	private void _redeliver() {
		ConfigurationAdmin configurationAdmin =
			_configurationAdminServiceTracker.getService();

		if (configurationAdmin == null) {
			return;
		}

		for (String pid : new ArrayList<>(_pids)) {
			_pids.remove(pid);

			try {
				Configuration[] configurations =
					configurationAdmin.listConfigurations(
						StringBundler.concat(
							"(", Constants.SERVICE_PID, "=", pid, ")"));

				if (configurations == null) {
					continue;
				}

				for (Configuration configuration : configurations) {
					configuration.update(configuration.getProperties());
				}
			}
			catch (Exception exception) {
				_pids.add(pid);

				_log.error(
					"Unable to redeliver configuration \"" + pid + "\"",
					exception);
			}
		}
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

	private final BundleContext _bundleContext;
	private final ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>
		_configurationAdminServiceTracker;
	private final ExecutorService _executorService =
		Executors.newSingleThreadExecutor(
			new NamedThreadFactory(
				SecretReferenceConfigurationPluginImpl.class.getName(),
				Thread.NORM_PRIORITY,
				SecretReferenceConfigurationPluginImpl.class.getClassLoader()));
	private final Set<String> _pids = ConcurrentHashMap.newKeySet();
	private final ServiceTracker<SecretProvider, SecretProvider>
		_secretProviderServiceTracker;
	private final ServiceTracker<SecretResolver, SecretResolver>
		_secretResolverServiceTracker;

}