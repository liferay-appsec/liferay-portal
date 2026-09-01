/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.liferay;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Tracks the scope aliases an OAuth 2 application declared that did not resolve
 * to any scope when its configuration was applied. A configuration factory
 * knows the declared aliases, but an alias whose scope source (such as a custom
 * object or DataSet) registers later resolves to nothing and is never persisted
 * as a scope grant. This registry keeps that declared intent available so a
 * reconciler can bind it once the missing scope sources register.
 *
 * <p>
 * Entries are keyed on the company together with the application because primary
 * keys repeat across virtual instances under database partitioning. A caller
 * must supply the company so a reconciler can select the right schema before it
 * reads the application.
 * </p>
 *
 * @author Allen Ziegenfus
 */
@ProviderType
public interface UnresolvedScopeAliasesRegistry {

	/**
	 * Returns the tracked application IDs grouped by company, so a reconciler
	 * can select each company's schema before it reads the applications.
	 *
	 * @return a snapshot mapping each company ID to the IDs of its applications
	 *         that have unresolved scope aliases
	 */
	public Map<Long, Set<Long>> getOAuth2ApplicationIdsByCompanyId();

	/**
	 * Returns the unresolved scope aliases tracked for an application.
	 *
	 * @param  companyId the company the application belongs to
	 * @param  oAuth2ApplicationId the application's primary key, unique only
	 *         within its company under database partitioning
	 * @return the tracked aliases, or an empty collection if none are tracked
	 */
	public Collection<String> getUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId);

	/**
	 * Returns whether the registry tracks no unresolved scope aliases for any
	 * application.
	 *
	 * @return <code>true</code> if nothing is tracked
	 */
	public boolean isEmpty();

	/**
	 * Stops tracking an application, typically once its aliases are all bound or
	 * its configuration is dropped.
	 *
	 * @param companyId the company the application belongs to
	 * @param oAuth2ApplicationId the application's primary key
	 */
	public void removeUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId);

	/**
	 * Stops tracking the given scope aliases for an application, leaving any
	 * others tracked for it in place. Unlike {@link #setUnresolvedScopeAliases},
	 * which replaces the whole set from the caller's snapshot, this removes only
	 * the named aliases atomically, so a concurrent update that added an alias is
	 * not clobbered. The application stops being tracked once its last alias is
	 * removed.
	 *
	 * @param companyId the company the application belongs to
	 * @param oAuth2ApplicationId the application's primary key
	 * @param scopeAliases the aliases to stop tracking
	 */
	public void removeUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId,
		Collection<String> scopeAliases);

	/**
	 * Records the unresolved scope aliases for an application, replacing any
	 * already tracked for it. An empty or <code>null</code> collection stops
	 * tracking the application.
	 *
	 * @param companyId the company the application belongs to
	 * @param oAuth2ApplicationId the application's primary key
	 * @param scopeAliases the declared aliases that resolved to no scope
	 */
	public void setUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId,
		Collection<String> scopeAliases);

}