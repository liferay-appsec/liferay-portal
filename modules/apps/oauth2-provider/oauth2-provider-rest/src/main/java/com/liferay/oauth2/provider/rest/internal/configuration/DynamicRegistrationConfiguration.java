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
	category = "oauth2", featureFlagKey = "LPD-63416",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.oauth2.provider.rest.internal.configuration.DynamicRegistrationConfiguration",
	localization = "content/Language",
	name = "dynamic-client-registration-configuration-name"
)
public interface DynamicRegistrationConfiguration {

	@Meta.AD(
		deflt = "",
		description = "dynamic-client-registration-allowed-grant-types-description",
		id = "dynamic.client.registration.allowed.grant.types",
		name = "dynamic-client-registration-allowed-grant-types",
		required = false
	)
	public String[] allowedGrantTypes();

	@Meta.AD(
		deflt = "",
		description = "dynamic-client-registration-allowed-hosts-description",
		id = "dynamic.client.registration.allowed.hosts",
		name = "dynamic-client-registration-allowed-hosts", required = false
	)
	public String[] allowedHosts();

	@Meta.AD(
		deflt = "",
		description = "dynamic-client-registration-allowed-redirect-uri-patterns-description",
		id = "dynamic.client.registration.allowed.redirect.uri.patterns",
		name = "dynamic-client-registration-allowed-redirect-uri-patterns",
		required = false
	)
	public String[] allowedRedirectURIPatterns();

	@Meta.AD(
		deflt = "",
		description = "dynamic-client-registration-allowed-scopes-description",
		id = "dynamic.client.registration.allowed.scopes",
		name = "dynamic-client-registration-allowed-scopes", required = false
	)
	public String[] allowedScopes();

	@Meta.AD(
		deflt = "true",
		description = "dynamic-client-registration-require-initial-access-token-description",
		id = "dynamic.client.registration.require.initial.access.token",
		name = "dynamic-client-registration-require-initial-access-token",
		required = false
	)
	public boolean requireInitialAccessToken();

	@Meta.AD(
		deflt = "false",
		description = "dynamic-client-registration-trust-proxy-headers-description",
		id = "dynamic.client.registration.trust.proxy.headers",
		name = "dynamic-client-registration-trust-proxy-headers",
		required = false
	)
	public boolean trustProxyHeaders();

}