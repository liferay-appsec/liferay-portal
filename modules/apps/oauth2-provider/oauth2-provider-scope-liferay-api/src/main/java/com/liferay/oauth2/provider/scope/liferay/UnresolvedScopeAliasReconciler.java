/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.liferay;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Reconciles the scope aliases held in the
 * {@link UnresolvedScopeAliasesRegistry} against the scopes that are currently
 * registered. When a scope source (such as a custom object or DataSet)
 * registers after an OAuth 2 application's configuration was applied, an alias
 * that previously resolved to nothing becomes resolvable; reconciling binds it
 * and persists the scope grant. A scheduled job invokes this periodically as a
 * fallback, and a trigger invokes it as soon as a scope source registers.
 *
 * @author Allen Ziegenfus
 */
@ProviderType
public interface UnresolvedScopeAliasReconciler {

	/**
	 * @return <code>true</code> if reconciling bound at least one previously
	 *         unresolved scope alias, so a caller retrying against a lagging
	 *         scope source can stop once progress is made
	 */
	public boolean reconcile() throws Exception;

}