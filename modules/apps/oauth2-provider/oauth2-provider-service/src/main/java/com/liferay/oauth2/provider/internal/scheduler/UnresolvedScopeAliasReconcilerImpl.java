/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
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
 * Binding is additive. Rather than passing the full alias list back through
 * {@code updateScopeAliases}, which rebuilds the whole scope-aliases snapshot
 * and re-resolves every alias, {@link #_addScopeAliases} feeds every existing
 * grant plus the grants for the aliases that resolve now to
 * {@code OAuth2ApplicationScopeAliasesLocalService.addOAuth2ApplicationScopeAliasesAndUpdateApplication}.
 * That service method persists the new snapshot and its grant rows and repoints
 * the application at it, both writes running inside the single transaction
 * Service Builder wraps around the method, against an application re-fetched in
 * that transaction, so a failure leaves no orphan snapshot and the window for
 * losing a concurrent edit of the application shrinks to that transaction. The
 * application has no
 * optimistic-lock column, so a configuration redeploy or scope update
 * interleaving on the same node can still drop one write's aliases; that is a
 * known limitation. Existing grants are never re-resolved, so
 * an already-granted alias whose source is momentarily unavailable can never be
 * revoked, and there is no need to guard against transient churn. A recorded
 * alias is looked up under its registered casing, matching what the
 * headless-server configuration factory does when the scope source is present,
 * and its grant is persisted under the declared alias the client holds. Tokens
 * issued against the old snapshot keep referencing it and are unaffected. An
 * alias that
 * already resolves and is already granted is skipped, so a redundant reconcile
 * writes nothing; because the registry is node-local while reconciling is
 * master-only, this keeps a new master from rewriting an already-bound alias
 * after a cluster failover.
 * </p>
 *
 * <p>
 * Reconciling may be requested from several threads at once (the periodic
 * scheduler and the scope finder trigger). {@link #reconcile()} serializes on a
 * lock and runs one pass per call, so a caller only returns after its own pass
 * has completed and its return value reflects that pass. Concurrent callers run
 * their passes in turn rather than sharing one; a pass that finds nothing to bind
 * is cheap, so the redundancy is immaterial.
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

		Map<String, String> stillResolvingScopeAliases = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry :
				resolvedScopeAliases.entrySet()) {

			if (!_scopeLocator.getLiferayOAuth2Scopes(
					companyId, entry.getValue()
				).isEmpty()) {

				stillResolvingScopeAliases.put(
					entry.getKey(), entry.getValue());
			}
		}

		if (stillResolvingScopeAliases.isEmpty()) {
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

					for (Map.Entry<String, String> entry :
							stillResolvingScopeAliases.entrySet()) {

						String declaredScopeAlias = entry.getKey();

						for (LiferayOAuth2Scope liferayOAuth2Scope :
								_scopeLocator.getLiferayOAuth2Scopes(
									companyId, entry.getValue())) {

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

		if (remainingScopeAliasesList.isEmpty()) {
			_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId);
		}
		else {
			_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId, remainingScopeAliasesList);
		}

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