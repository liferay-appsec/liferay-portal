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

package com.liferay.portal.kernel.security.auth.verifier;

import com.liferay.registry.collections.ServiceReferenceMapperFactory;
import com.liferay.registry.collections.ServiceTrackerMap;
import com.liferay.registry.collections.ServiceTrackerMapFactory;
import com.liferay.registry.collections.ServiceTrackerMapFactoryUtil;

/**
 * @author Carlos Sierra Andrés
 */
public class AuthVerifierRegistry {

	public static AuthVerifier getAuthVerifier(String simpleClassName) {
		return _serviceTrackerMap.getService(simpleClassName);
	}

	private static final ServiceTrackerMap<String, AuthVerifier>
		_serviceTrackerMap;

	static {
		ServiceTrackerMapFactory serviceTrackerMapFactory =
			ServiceTrackerMapFactoryUtil.getServiceTrackerMapFactory();

		_serviceTrackerMap = serviceTrackerMapFactory.openSingleValueMap(
			AuthVerifier.class, null,
			ServiceReferenceMapperFactory.create(
				(authVerifier, emitter) -> {
					Class<? extends AuthVerifier> clazz =
						authVerifier.getClass();

					emitter.emit(clazz.getSimpleName());
				}));
	}

}