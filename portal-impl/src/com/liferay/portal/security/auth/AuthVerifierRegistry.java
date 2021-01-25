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

package com.liferay.portal.security.auth;

import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierConfiguration;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.ServiceReference;
import com.liferay.registry.ServiceTracker;
import com.liferay.registry.ServiceTrackerCustomizer;
import com.liferay.registry.collections.ServiceReferenceMapper;
import com.liferay.registry.collections.ServiceReferenceMapperFactory;
import com.liferay.registry.collections.ServiceTrackerMap;
import com.liferay.registry.collections.ServiceTrackerMapFactory;
import com.liferay.registry.collections.ServiceTrackerMapFactoryUtil;

import java.util.Properties;

/**
 * @author Carlos Sierra Andrés
 */
public class AuthVerifierRegistry {

	public static AuthVerifier getAuthVerifier(String simpleClassName) {
		return _serviceTrackerMap.getService(simpleClassName);
	}

	private static AuthVerifierConfiguration _buildAuthVerifierConfiguration(
		ServiceReference<AuthVerifier> serviceReference,
		AuthVerifier authVerifier) {

		Class<?> clazz = authVerifier.getClass();

		String authVerifierPropertyName =
			AuthVerifierPipeline.getAuthVerifierPropertyName(clazz.getName());

		Properties properties = new Properties();

		for (String propertyKey : serviceReference.getPropertyKeys()) {
			if (!propertyKey.startsWith(authVerifierPropertyName)) {
				continue;
			}

			properties.put(
				propertyKey.substring(authVerifierPropertyName.length()),
				serviceReference.getProperty(propertyKey));
		}

		if (properties.isEmpty()) {
			return null;
		}

		AuthVerifierConfiguration authVerifierConfiguration =
			new AuthVerifierConfiguration();

		authVerifierConfiguration.setAuthVerifierClassName(clazz.getName());
		authVerifierConfiguration.setProperties(properties);

		return authVerifierConfiguration;
	}

	private static final ServiceReferenceMapper<String, AuthVerifier>
		_authVerifierServiceReferenceMapper;
	private static final ServiceTracker<AuthVerifier, Tracked> _serviceTracker;
	private static final ServiceTrackerMap<String, AuthVerifier>
		_serviceTrackerMap;

	private static class Tracked {

		public Tracked(
			AuthVerifier authVerifier,
			AuthVerifierConfiguration authVerifierConfiguration) {

			_authVerifier = authVerifier;
			_authVerifierConfiguration = authVerifierConfiguration;
		}

		public AuthVerifier getAuthVerifier() {
			return _authVerifier;
		}

		public AuthVerifierConfiguration getAuthVerifierConfiguration() {
			return _authVerifierConfiguration;
		}

		public void setAuthVerifier(AuthVerifier authVerifier) {
			_authVerifier = authVerifier;
		}

		public void setAuthVerifierConfiguration(
			AuthVerifierConfiguration authVerifierConfiguration) {

			_authVerifierConfiguration = authVerifierConfiguration;
		}

		private AuthVerifier _authVerifier;
		private AuthVerifierConfiguration _authVerifierConfiguration;

	}

	static {
		ServiceTrackerMapFactory serviceTrackerMapFactory =
			ServiceTrackerMapFactoryUtil.getServiceTrackerMapFactory();

		_authVerifierServiceReferenceMapper =
			ServiceReferenceMapperFactory.create(
				(authVerifier, emitter) -> {
					Class<? extends AuthVerifier> clazz =
						authVerifier.getClass();

					emitter.emit(clazz.getSimpleName());
				});

		_serviceTrackerMap = serviceTrackerMapFactory.openSingleValueMap(
			AuthVerifier.class, null,
			(serviceReference, emitter) -> {
				String authVerifierClassName = GetterUtil.getString(
					serviceReference.getProperty("auth.verifier.class.name"));

				if (Validator.isNotNull(authVerifierClassName)) {
					emitter.emit(authVerifierClassName);
				}
				else {
					_authVerifierServiceReferenceMapper.map(
						serviceReference, emitter);
				}
			});

		Registry registry = RegistryUtil.getRegistry();

		_serviceTracker = registry.trackServices(
			AuthVerifier.class,
			new ServiceTrackerCustomizer<AuthVerifier, Tracked>() {

				@Override
				public Tracked addingService(
					ServiceReference<AuthVerifier> serviceReference) {

					Registry registry = RegistryUtil.getRegistry();

					AuthVerifier authVerifier = registry.getService(
						serviceReference);

					AuthVerifierConfiguration authVerifierConfiguration =
						_buildAuthVerifierConfiguration(
							serviceReference, authVerifier);

					if (authVerifierConfiguration != null) {
						AuthVerifierPipeline.PORTAL_AUTH_VERIFIER_PIPELINE.
							addAuthVerifierConfiguration(
								authVerifierConfiguration);
					}

					return new Tracked(authVerifier, authVerifierConfiguration);
				}

				@Override
				public void modifiedService(
					ServiceReference<AuthVerifier> serviceReference,
					Tracked tracked) {

					AuthVerifierConfiguration authVerifierConfiguration =
						tracked.getAuthVerifierConfiguration();

					if (authVerifierConfiguration != null) {
						AuthVerifierPipeline.PORTAL_AUTH_VERIFIER_PIPELINE.
							removeAuthVerifierConfiguration(
								authVerifierConfiguration);
					}

					authVerifierConfiguration = _buildAuthVerifierConfiguration(
						serviceReference, tracked.getAuthVerifier());

					if (authVerifierConfiguration != null) {
						AuthVerifierPipeline.PORTAL_AUTH_VERIFIER_PIPELINE.
							addAuthVerifierConfiguration(
								authVerifierConfiguration);
					}

					tracked.setAuthVerifierConfiguration(
						authVerifierConfiguration);
				}

				@Override
				public void removedService(
					ServiceReference<AuthVerifier> serviceReference,
					Tracked tracked) {

					AuthVerifierConfiguration authVerifierConfiguration =
						tracked.getAuthVerifierConfiguration();

					if (authVerifierConfiguration != null) {
						AuthVerifierPipeline.PORTAL_AUTH_VERIFIER_PIPELINE.
							removeAuthVerifierConfiguration(
								authVerifierConfiguration);
					}

					Registry registry = RegistryUtil.getRegistry();

					registry.ungetService(serviceReference);
				}

			});

		_serviceTracker.open();
	}

}