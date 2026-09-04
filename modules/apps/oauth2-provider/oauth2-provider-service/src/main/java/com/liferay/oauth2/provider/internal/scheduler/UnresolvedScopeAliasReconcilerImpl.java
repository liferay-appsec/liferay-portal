/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import com.liferay.oauth2.provider.scope.liferay.LiferayOAuth2Scope;
import com.liferay.oauth2.provider.scope.liferay.ScopeLocator;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.oauth2.provider.service.OAuth2ScopeGrantLocalService;
import com.liferay.osgi.util.configuration.ConfigurationFactoryUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Binds the scope aliases tracked in the {@link UnresolvedScopeAliasesRegistry}
 * once the scope sources they name become resolvable.
 *
 * <p>
 * Binding is additive: {@link #_addScopeAliases} copies every existing grant and
 * adds the grants for the aliases that resolve now, so an already-granted alias
 * is never revoked and a redundant pass writes nothing. The new snapshot and the
 * application repoint share the single transaction Service Builder wraps around
 * {@code addOAuth2ApplicationScopeAliasesAndUpdateApplication}, so a failure
 * leaves no orphan snapshot. Reconciling is master-only, matching the clustered
 * scheduler that owns the fallback pass.
 * </p>
 *
 * <p>
 * {@link #reconcile()} serializes on a lock and runs one pass per call, so a
 * caller returns only after its own pass has completed.
 * </p>
 *
 * @author Allen Ziegenfus
 */
@Component(service = UnresolvedScopeAliasReconciler.class)
public class UnresolvedScopeAliasReconcilerImpl
	implements UnresolvedScopeAliasReconciler {

	@Override
	public boolean reconcile() throws Exception {
		_reconcileLock.lock();

		try {
			return _reconcileOnce();
		}
		finally {
			_reconcileLock.unlock();
		}
	}

	private Set<String> _addScopeAliases(
			long companyId, long oAuth2ApplicationId,
			Map<String, String> resolvedScopeAliases)
		throws Exception {

		Set<String> persistedScopeAliases = new HashSet<>();

		// Resolve each alias once, here, and reuse the result when building the
		// snapshot. Resolving again inside the builder let a scope source that
		// deregistered between the two calls leave a no-op snapshot behind
		// (copied grants, nothing new), orphaning the prior snapshot on every
		// pass. Guarding on a nonempty resolution keeps the write additive.

		Map<String, Collection<LiferayOAuth2Scope>>
			resolvedLiferayOAuth2Scopes = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry :
				resolvedScopeAliases.entrySet()) {

			Collection<LiferayOAuth2Scope> liferayOAuth2Scopes =
				_scopeLocator.getLiferayOAuth2Scopes(
					companyId, entry.getValue());

			if (!liferayOAuth2Scopes.isEmpty()) {
				resolvedLiferayOAuth2Scopes.put(
					entry.getKey(), liferayOAuth2Scopes);
			}
		}

		if (resolvedLiferayOAuth2Scopes.isEmpty()) {
			return persistedScopeAliases;
		}

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.getOAuth2Application(
				oAuth2ApplicationId);

		long oAuth2ApplicationScopeAliasesId =
			oAuth2Application.getOAuth2ApplicationScopeAliasesId();

		_oAuth2ApplicationScopeAliasesLocalService.
			addOAuth2ApplicationScopeAliasesAndUpdateApplication(
				companyId, oAuth2Application.getUserId(),
				oAuth2Application.getUserName(), oAuth2ApplicationId,
				oAuth2ScopeBuilder -> {
					for (OAuth2ScopeGrant oAuth2ScopeGrant :
							_oAuth2ScopeGrantLocalService.getOAuth2ScopeGrants(
								oAuth2ApplicationScopeAliasesId,
								QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

						oAuth2ScopeBuilder.forApplication(
							oAuth2ScopeGrant.getApplicationName(),
							oAuth2ScopeGrant.getBundleSymbolicName(),
							applicationScopeAssigner ->
								applicationScopeAssigner.assignScope(
									oAuth2ScopeGrant.getScope()
								).mapToScopeAlias(
									oAuth2ScopeGrant.getScopeAliasesList()
								));
					}

					for (Map.Entry<String, Collection<LiferayOAuth2Scope>>
							entry : resolvedLiferayOAuth2Scopes.entrySet()) {

						String declaredScopeAlias = entry.getKey();

						for (LiferayOAuth2Scope liferayOAuth2Scope :
								entry.getValue()) {

							Bundle bundle = liferayOAuth2Scope.getBundle();

							oAuth2ScopeBuilder.forApplication(
								liferayOAuth2Scope.getApplicationName(),
								bundle.getSymbolicName(),
								applicationScopeAssigner ->
									applicationScopeAssigner.assignScope(
										liferayOAuth2Scope.getScope()
									).mapToScopeAlias(
										declaredScopeAlias
									));

							persistedScopeAliases.add(declaredScopeAlias);
						}
					}
				});

		return persistedScopeAliases;
	}

	private String _normalizeScopeAlias(
		Collection<String> registeredScopeAliases, String scopeAlias) {

		if (registeredScopeAliases.contains(scopeAlias)) {
			return scopeAlias;
		}

		for (String registeredScopeAlias : registeredScopeAliases) {
			if (StringUtil.equalsIgnoreCase(registeredScopeAlias, scopeAlias)) {
				return registeredScopeAlias;
			}
		}

		return scopeAlias;
	}

	private boolean _reconcile(long companyId, Set<Long> oAuth2ApplicationIds)
		throws Exception {

		Collection<String> registeredScopeAliases =
			_scopeLocator.getScopeAliases(companyId);

		boolean bound = false;

		for (long oAuth2ApplicationId : oAuth2ApplicationIds) {
			try {
				OAuth2Application oAuth2Application =
					_oAuth2ApplicationLocalService.fetchOAuth2Application(
						oAuth2ApplicationId);

				if (oAuth2Application == null) {
					_unresolvedScopeAliasesRegistry.
						removeUnresolvedScopeAliases(
							companyId, oAuth2ApplicationId);

					continue;
				}

				if (_reconcile(oAuth2Application, registeredScopeAliases)) {
					bound = true;
				}
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to reconcile OAuth 2 application " +
							oAuth2ApplicationId,
						exception);
				}
			}
		}

		return bound;
	}

	private boolean _reconcile(
			OAuth2Application oAuth2Application,
			Collection<String> registeredScopeAliases)
		throws Exception {

		long companyId = oAuth2Application.getCompanyId();
		long oAuth2ApplicationId = oAuth2Application.getOAuth2ApplicationId();

		Collection<String> unresolvedScopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId);

		if (unresolvedScopeAliases.isEmpty()) {
			return false;
		}

		List<String> grantedScopeAliasesList =
			_oAuth2ApplicationScopeAliasesLocalService.getScopeAliasesList(
				oAuth2Application.getOAuth2ApplicationScopeAliasesId());

		List<String> alreadyGrantedScopeAliasesList = new ArrayList<>();
		Map<String, String> resolvedScopeAliases = new LinkedHashMap<>();

		for (String scopeAlias : unresolvedScopeAliases) {
			if (grantedScopeAliasesList.contains(scopeAlias)) {
				alreadyGrantedScopeAliasesList.add(scopeAlias);

				continue;
			}

			String normalizedScopeAlias = _normalizeScopeAlias(
				registeredScopeAliases, scopeAlias);

			if (_scopeLocator.getLiferayOAuth2Scopes(
					companyId, normalizedScopeAlias
				).isEmpty()) {

				continue;
			}

			resolvedScopeAliases.put(scopeAlias, normalizedScopeAlias);
		}

		Set<String> persistedScopeAliases = Collections.emptySet();

		if (!resolvedScopeAliases.isEmpty()) {
			persistedScopeAliases = _addScopeAliases(
				companyId, oAuth2ApplicationId, resolvedScopeAliases);
		}

		List<String> boundScopeAliasesList = new ArrayList<>(
			alreadyGrantedScopeAliasesList);

		boundScopeAliasesList.addAll(persistedScopeAliases);

		if (boundScopeAliasesList.isEmpty()) {
			return false;
		}

		List<String> remainingScopeAliasesList = new ArrayList<>(
			unresolvedScopeAliases);

		remainingScopeAliasesList.removeAll(boundScopeAliasesList);

		// Remove only the aliases this pass actually bound, atomically, rather
		// than overwriting the whole entry from the snapshot read at the top of
		// the pass. A configuration update that recorded a new alias while the
		// pass ran is then preserved instead of being clobbered.

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			companyId, oAuth2ApplicationId, boundScopeAliasesList);

		if (!persistedScopeAliases.isEmpty() && _log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"Bound previously unresolved scope aliases ",
					persistedScopeAliases, " for OAuth 2 application ",
					oAuth2ApplicationId, " (", oAuth2Application.getName(),
					")"));

			if (remainingScopeAliasesList.isEmpty()) {
				_log.info(
					StringBundler.concat(
						"OAuth 2 application ", oAuth2ApplicationId, " (",
						oAuth2Application.getName(),
						") resolved all previously unresolved scope aliases"));
			}
		}

		return !persistedScopeAliases.isEmpty();
	}

	private boolean _reconcileOnce() throws Exception {
		if (_unresolvedScopeAliasesRegistry.isEmpty()) {
			return false;
		}

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Reconciling unresolved scope aliases for OAuth 2 " +
					"applications " + oAuth2ApplicationIdsByCompanyId);
		}

		boolean[] bound = {false};

		for (Map.Entry<Long, Set<Long>> entry :
				oAuth2ApplicationIdsByCompanyId.entrySet()) {

			long companyId = entry.getKey();
			Set<Long> oAuth2ApplicationIds = entry.getValue();

			try {
				ConfigurationFactoryUtil.executeAsCompany(
					_companyLocalService,
					HashMapBuilder.<String, Object>put(
						"companyId", companyId
					).build(),
					curCompanyId -> {
						if (_reconcile(curCompanyId, oAuth2ApplicationIds)) {
							bound[0] = true;
						}
					});
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to reconcile OAuth 2 applications for " +
							"company " + companyId,
						exception);
				}
			}
		}

		return bound[0];
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UnresolvedScopeAliasReconcilerImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference
	private OAuth2ApplicationScopeAliasesLocalService
		_oAuth2ApplicationScopeAliasesLocalService;

	@Reference
	private OAuth2ScopeGrantLocalService _oAuth2ScopeGrantLocalService;

	private final Lock _reconcileLock = new ReentrantLock();

	@Reference
	private ScopeLocator _scopeLocator;

	@Reference
	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

}