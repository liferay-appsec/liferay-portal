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

package com.liferay.oauth2.provider.rest.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Arthur Chan
 */
@ExtendedObjectClassDefinition(
	category = "oauth2", scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	id = "com.liferay.oauth2.provider.rest.internal.configuration.DefaultWebKeyProviderConfiguration",
	localization = "content/Language",
	name = "default-web-key-provider-configuration-name"
)
public interface DefaultWebKeyProviderConfiguration {

	@Meta.AD(
		deflt = "{}",
		description = "oauth2-authorization-server-jwt-access-token-signing-json-web-key-description",
		id = "oauth2.authorization.server.jwt.access.token.signing.json.web.key",
		name = "oauth2-authorization-server-jwt-access-token-signing-json-web-key",
		required = false
	)
	public String jwtAccessTokenSigningJSONWebKey();

}