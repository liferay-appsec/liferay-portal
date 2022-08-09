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

package com.liferay.oauth2.provider.scope.liferay;

import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.BundleContext;

/**
 * @author Carlos Sierra Andrés
 * @author Stian Sigvartsen
 */
@ProviderType
public interface ScopedServiceTrackerMapFactory {

	public <T> ScopedServiceTrackerMap<T> create(
		BundleContext bundleContext, Class<T> clazz, String property,
		String filter, Supplier<T> defaultServiceSupplier,
		Runnable onChangeRunnable);

	public default <T> ScopedServiceTrackerMap<T> create(
		BundleContext bundleContext, Class<T> clazz, String property,
		Supplier<T> defaultServiceSupplier) {

		return create(
			bundleContext, clazz, property, defaultServiceSupplier,
			() -> {
			});
	}

	public default <T> ScopedServiceTrackerMap<T> create(
		BundleContext bundleContext, Class<T> clazz, String property,
		Supplier<T> defaultServiceSupplier, Runnable onChangeRunnable) {

		return create(
			bundleContext, clazz, property, null, defaultServiceSupplier,
			onChangeRunnable);
	}

}