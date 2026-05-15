/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Rafael Praxedes
 */
@ExtendedObjectClassDefinition(
	category = "oauth2", scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.oauth2.provider.rest.internal.configuration.DynamicRegistrationConfiguration",
	localization = "content/Language",
	name = "dynamic-registration-configuration-name"
)
public interface DynamicRegistrationConfiguration {

	@Meta.AD(
		deflt = "",
		description = "dynamic-registration-anonymous-allowed-redirect-uri-patterns-description",
		id = "dynamic.registration.anonymous.allowed.redirect.uri.patterns",
		name = "dynamic-registration-anonymous-allowed-redirect-uri-patterns",
		required = false
	)
	public String[] anonymousAllowedRedirectURIPatterns();

	@Meta.AD(
		deflt = "",
		description = "dynamic-registration-anonymous-allowed-scopes-description",
		id = "dynamic.registration.anonymous.allowed.scopes",
		name = "dynamic-registration-anonymous-allowed-scopes", required = false
	)
	public String[] anonymousAllowedScopes();

	@Meta.AD(
		deflt = "true",
		description = "dynamic-registration-require-initial-access-token-description",
		id = "dynamic.registration.require.initial.access.token",
		name = "dynamic-registration-require-initial-access-token",
		required = false
	)
	public boolean requireInitialAccessToken();

}