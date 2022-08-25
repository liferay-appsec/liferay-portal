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

package com.liferay.oauth.resource.server.internal.jsonws.auth.verifier;

import com.liferay.oauth.resource.server.internal.auth.verifier.BaseAuthVerifier;
import com.liferay.oauth.resource.server.internal.jsonws.policy.scope.SAPEntryScope;
import com.liferay.oauth.resource.server.internal.jsonws.policy.scope.SAPEntryScopeDescriptorFinderRegistrator;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import com.liferay.oauth2.provider.rest.spi.bearer.token.provider.BearerTokenProvider;
import com.liferay.oauth2.provider.scope.liferay.constants.OAuth2ProviderScopeLiferayConstants;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.oauth2.provider.service.OAuth2ScopeGrantLocalService;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.security.service.access.policy.ServiceAccessPolicy;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Tomas Polesovsky
 */
@Component(
	property = "auth.verifier.OAuth2JSONWSAuthVerifier.urls.includes=/api/jsonws/*",
	service = AuthVerifier.class
)
public class OAuth2JSONWSAuthVerifier extends BaseAuthVerifier {

	@Override
	public String getAuthType() {
		return OAuth2ProviderScopeLiferayConstants.AUTH_VERIFIER_OAUTH2_TYPE;
	}

	@Override
	public void postProcess(
		BearerTokenProvider.AccessToken accessToken,
		AuthVerifierResult authVerifierResult) {

		OAuth2Authorization oAuth2Authorization =
			oAuth2AuthorizationLocalService.
				fetchOAuth2AuthorizationByAccessTokenContent(
					accessToken.getTokenKey());

		List<OAuth2ScopeGrant> oAuth2AuthorizationOAuth2ScopeGrants =
			_oAuth2ScopeGrantLocalService.
				getOAuth2AuthorizationOAuth2ScopeGrants(
					oAuth2Authorization.getOAuth2AuthorizationId());

		Stream<OAuth2ScopeGrant> stream =
			oAuth2AuthorizationOAuth2ScopeGrants.stream();

		List<String> scopes = stream.filter(
			oAuth2ScopeGrant -> _jaxRsApplicationNames.contains(
				oAuth2ScopeGrant.getApplicationName())
		).map(
			OAuth2ScopeGrant::getScope
		).collect(
			Collectors.toList()
		);

		List<SAPEntryScope> sapEntryScopes =
			_sapEntryScopeDescriptorFinderRegistrator.
				getRegisteredSAPEntryScopes(oAuth2Authorization.getCompanyId());

		List<String> serviceAccessPolicyNames = new ArrayList<>(
			sapEntryScopes.size());

		for (SAPEntryScope sapEntryScope : sapEntryScopes) {
			if (scopes.contains(sapEntryScope.getScope())) {
				serviceAccessPolicyNames.add(sapEntryScope.getSAPEntryName());
			}
		}

		Map<String, Object> settings = authVerifierResult.getSettings();

		settings.put(
			BearerTokenProvider.AccessToken.class.getName(), accessToken);
		settings.put(
			ServiceAccessPolicy.SERVICE_ACCESS_POLICY_NAMES,
			serviceAccessPolicyNames);

		authVerifierResult.setUserId(accessToken.getUserId());
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(&(osgi.jaxrs.name=*)(sap.scope.finder=true))"
	)
	protected void addJaxRsApplicationName(
		ServiceReference<ScopeFinder> serviceReference) {

		_jaxRsApplicationNames.add(
			GetterUtil.getString(
				serviceReference.getProperty("osgi.jaxrs.name")));
	}

	protected void removeJaxRsApplicationName(
		ServiceReference<ScopeFinder> serviceReference) {

		_jaxRsApplicationNames.remove(
			GetterUtil.getString(
				serviceReference.getProperty("osgi.jaxrs.name")));
	}

	private final Set<String> _jaxRsApplicationNames =
		Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Reference
	private OAuth2ScopeGrantLocalService _oAuth2ScopeGrantLocalService;

	@Reference
	private SAPEntryScopeDescriptorFinderRegistrator
		_sapEntryScopeDescriptorFinderRegistrator;

}