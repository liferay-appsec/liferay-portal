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

package com.liferay.oauth2.provider.rest.internal.spi.bearer.token.provider.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Tomas Polesovsky
 */
@ExtendedObjectClassDefinition(category = "oauth2")
@Meta.OCD(
	id = "com.liferay.oauth2.provider.rest.internal.spi.bearer.token.provider.configuration.JWTDefaultBearerTokenProviderConfiguration",
	localization = "content/Language",
	name = "jwt-bearer-token-provider-configuration-name"
)
public interface JWTDefaultBearerTokenProviderConfiguration {

	@Meta.AD(
		deflt = "600", description = "access-token-expires-in-description",
		id = "access.token.expires.in", name = "access-token-expires-in",
		required = false
	)
	public int accessTokenExpiresIn();

}