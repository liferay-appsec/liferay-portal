/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.internal.osgi.commands;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.LiferayOAuth2Scope;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	property = {
		"osgi.command.function=listScopes",
		"osgi.command.function=listUnresolvedScopes",
		"osgi.command.function=reconcile", "osgi.command.scope=oauth2"
	},
	service = OSGiCommands.class
)
public class OAuth2OSGiCommands implements OSGiCommands {

	public void listScopes() {
		listScopes(_portal.getDefaultCompanyId());
	}

	public void listScopes(long companyId) {
		Collection<String> scopeAliases = ListUtil.sort(
			new ArrayList<>(_scopeLocator.getScopeAliases(companyId)));

		for (String scopeAlias : scopeAliases) {
			System.out.println();
			System.out.println(scopeAlias);

			Collection<LiferayOAuth2Scope> liferayOAuth2Scopes = ListUtil.sort(
				new ArrayList<>(
					_scopeLocator.getLiferayOAuth2Scopes(
						companyId, scopeAlias)),
				Comparator.comparing(LiferayOAuth2Scope::getScope));

			for (LiferayOAuth2Scope liferayOAuth2Scope : liferayOAuth2Scopes) {
				Bundle bundle = liferayOAuth2Scope.getBundle();

				System.out.println(
					StringBundler.concat(
						"    ", liferayOAuth2Scope.getScope(), " (",
						liferayOAuth2Scope.getApplicationName(), " [",
						bundle.getBundleId(), "])"));
			}
		}

		System.out.println();
	}

	public void listUnresolvedScopes() {
		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		if (oAuth2ApplicationIdsByCompanyId.isEmpty()) {
			System.out.println("No unresolved scope aliases are tracked");

			return;
		}

		for (Map.Entry<Long, Set<Long>> entry :
				oAuth2ApplicationIdsByCompanyId.entrySet()) {

			long companyId = entry.getKey();

			for (long oAuth2ApplicationId : entry.getValue()) {
				OAuth2Application oAuth2Application =
					_oAuth2ApplicationLocalService.fetchOAuth2Application(
						oAuth2ApplicationId);

				System.out.println(
					StringBundler.concat(
						"company ", companyId, " application ",
						oAuth2ApplicationId, " (",
						(oAuth2Application == null) ? "" :
							oAuth2Application.getName(),
						"): ",
						ListUtil.sort(
							new ArrayList<>(
								_unresolvedScopeAliasesRegistry.
									getUnresolvedScopeAliases(
										companyId, oAuth2ApplicationId)))));
			}
		}
	}

	public void reconcile() throws Exception {
		if (_unresolvedScopeAliasReconciler.reconcile()) {
			System.out.println("Bound previously unresolved scope aliases");
		}
		else {
			System.out.println("No unresolved scope aliases were bound");
		}
	}

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private ScopeLocator _scopeLocator;

	@Reference
	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

	@Reference
	private UnresolvedScopeAliasReconciler _unresolvedScopeAliasReconciler;

}