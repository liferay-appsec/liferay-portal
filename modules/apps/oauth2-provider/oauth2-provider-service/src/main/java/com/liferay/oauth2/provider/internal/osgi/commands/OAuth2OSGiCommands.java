/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.osgi.commands;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.osgi.util.configuration.ConfigurationFactoryUtil;
import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Diagnostic Gogo commands for the unresolved-scope-alias reconcile feature.
 * They live here, alongside the reconciler, rather than in the
 * <code>oauth2-provider-scope-impl</code> command component, so their mandatory
 * references to this bundle's services do not take down the pre-existing
 * <code>oauth2:listScopes</code> command when this bundle is unsatisfied.
 *
 * @author Allen Ziegenfus
 */
@Component(
	property = {
		"osgi.command.function=listUnresolvedScopes",
		"osgi.command.function=reconcile", "osgi.command.scope=oauth2"
	},
	service = OSGiCommands.class
)
public class OAuth2OSGiCommands implements OSGiCommands {

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

			ConfigurationFactoryUtil.executeAsCompany(
				_companyLocalService,
				HashMapBuilder.<String, Object>put(
					"companyId", companyId
				).build(),
				curCompanyId -> {
					for (long oAuth2ApplicationId : entry.getValue()) {
						OAuth2Application oAuth2Application =
							_oAuth2ApplicationLocalService.
								fetchOAuth2Application(oAuth2ApplicationId);

						String name = "";

						if (oAuth2Application != null) {
							name = oAuth2Application.getName();
						}

						System.out.println(
							StringBundler.concat(
								"company ", companyId, " application ",
								oAuth2ApplicationId, " (", name, "): ",
								ListUtil.sort(
									new ArrayList<>(
										_unresolvedScopeAliasesRegistry.
											getUnresolvedScopeAliases(
												companyId,
												oAuth2ApplicationId)))));
					}
				});
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
	private CompanyLocalService _companyLocalService;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

	@Reference
	private UnresolvedScopeAliasReconciler _unresolvedScopeAliasReconciler;

}