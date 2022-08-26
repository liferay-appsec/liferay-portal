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

package com.liferay.oauth.resource.server.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Arthur Chan
 */
@ExtendedObjectClassDefinition(
	category = "oauth2", scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	id = "com.liferay.oauth.resource.server.internal.configuration.OAuthResourceServerConfiguration",
	localization = "content/Language",
	name = "oauth-resource-server-configuration-name"
)
public interface OAuthResourceServerConfiguration {

	@Meta.AD(
		deflt = "false",
		description = "oauth-resource-server-accept-jwt-access-token-description",
		id = "oauth.resource.server.accept.jwt.access.token",
		name = "oauth-resource-server-accept-jwt-access-token", required = false
	)
	public boolean acceptJWTAccessToken();

	@Meta.AD(
		deflt = "",
		description = "oauth-resource-server-authorization-server-well-known-uri-description",
		id = "oauth.resource.server.authorization.server.well.known.uri",
		name = "oauth-resource-server-authorization-server-well-known-uri",
		required = false
	)
	public String authorizationServerWellKnownURI();

	@Meta.AD(
		deflt = "",
		description = "oauth-resource-server-authorization-server-issuer-description",
		id = "oauth.resource.server.authorization.server.issuer",
		name = "oauth-resource-server-authorization-server-issuer",
		required = false
	)
	public String authorizationServerIssuer();

	@Meta.AD(
		deflt = "{\"keys\":[{},{}]}",
		description = "oauth-resource-server-authorization-server-json-web-key-set-description",
		id = "oauth.resource.server.authorization.server.json.web.key.set",
		name = "oauth-resource-server-authorization-server-json-web-key-set",
		required = false
	)
	public String authorizationServerJSONWebKeySet();

}