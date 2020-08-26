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

package com.liferay.portal.remote.cors.internal;

import com.liferay.portal.kernel.util.Validator;

/**
 * @author Carlos Sierra Andrés
 */
public abstract class BaseFastURLToCORSSupportMapper
	extends BaseURLToCORSSupportMapper {

	@Override
	public CORSSupport get(String urlPath) {
		try {
			CORSSupport corsSupport = getWildcardCORSSupport(urlPath);

			if (corsSupport != null) {
				return corsSupport;
			}

			return getExtensionCORSSupport(urlPath);
		}
		catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			throw new IllegalArgumentException(
				"urlPath contains invalid characters",
				indexOutOfBoundsException);
		}
	}

	protected abstract void fastPut(
		CORSSupport corsSupport, String urlPattern, boolean wildcard);

	protected abstract CORSSupport getExtensionCORSSupport(String urlPath);

	protected abstract CORSSupport getWildcardCORSSupport(String urlPath);

	@Override
	protected void put(CORSSupport corsSupport, String urlPattern)
		throws IllegalArgumentException {

		if (corsSupport == null) {
			throw new IllegalArgumentException("CORS support is null");
		}

		if (Validator.isBlank(urlPattern)) {
			throw new IllegalArgumentException("urlPattern is empty");
		}

		try {
			if (isWildcardURLPattern(urlPattern)) {
				fastPut(corsSupport, urlPattern, true);

				return;
			}

			if (isExtensionURLPattern(urlPattern)) {
				fastPut(corsSupport, urlPattern, false);

				return;
			}

			fastPut(corsSupport, urlPattern, true);
		}
		catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			throw new IllegalArgumentException(
				"urlPattern contains invalid characters",
				indexOutOfBoundsException);
		}
	}

	protected static final byte ASCII_CHARACTER_RANGE = 96;

	protected static final byte ASCII_PRINTABLE_OFFSET = 32;

}