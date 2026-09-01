/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.internal.liferay;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

/**
 * In-memory {@link UnresolvedScopeAliasesRegistry} backed by a nested map of
 * company ID to application ID to aliases.
 *
 * <p>
 * The state is node-local and rebuilt as configurations are applied, so it is
 * not persisted. Reads and writes are concurrent: the outer map is a {@link
 * ConcurrentHashMap}, and {@link #setUnresolvedScopeAliases} and {@link
 * #removeUnresolvedScopeAliases} mutate a company's inner map under a single
 * {@code compute} on the outer key so an entry is never left behind after its
 * last application is removed, nor lost to a concurrent removal.
 * </p>
 *
 * @author Allen Ziegenfus
 */
@Component(service = UnresolvedScopeAliasesRegistry.class)
public class UnresolvedScopeAliasesRegistryImpl
	implements UnresolvedScopeAliasesRegistry {

	@Override
	public Map<Long, Set<Long>> getOAuth2ApplicationIdsByCompanyId() {
		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId = new HashMap<>();

		for (Map.Entry<Long, Map<Long, Set<String>>> entry :
				_scopeAliasesMap.entrySet()) {

			Map<Long, Set<String>> value = entry.getValue();

			oAuth2ApplicationIdsByCompanyId.put(
				entry.getKey(), new HashSet<>(value.keySet()));
		}

		return oAuth2ApplicationIdsByCompanyId;
	}

	@Override
	public Collection<String> getUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId) {

		Map<Long, Set<String>> scopeAliasesMap = _scopeAliasesMap.get(
			companyId);

		if (scopeAliasesMap == null) {
			return Collections.emptySet();
		}

		return scopeAliasesMap.getOrDefault(
			oAuth2ApplicationId, Collections.emptySet());
	}

	@Override
	public boolean isEmpty() {
		return _scopeAliasesMap.isEmpty();
	}

	@Override
	public void removeUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId) {

		_scopeAliasesMap.computeIfPresent(
			companyId,
			(key, scopeAliasesMap) -> {
				scopeAliasesMap.remove(oAuth2ApplicationId);

				if (scopeAliasesMap.isEmpty()) {
					return null;
				}

				return scopeAliasesMap;
			});
	}

	@Override
	public void setUnresolvedScopeAliases(
		long companyId, long oAuth2ApplicationId,
		Collection<String> scopeAliases) {

		if ((scopeAliases == null) || scopeAliases.isEmpty()) {
			removeUnresolvedScopeAliases(companyId, oAuth2ApplicationId);

			return;
		}

		_scopeAliasesMap.compute(
			companyId,
			(key, scopeAliasesMap) -> {
				if (scopeAliasesMap == null) {
					scopeAliasesMap = new ConcurrentHashMap<>();
				}

				scopeAliasesMap.put(
					oAuth2ApplicationId,
					Collections.unmodifiableSet(
						new LinkedHashSet<>(scopeAliases)));

				return scopeAliasesMap;
			});
	}

	private final Map<Long, Map<Long, Set<String>>> _scopeAliasesMap =
		new ConcurrentHashMap<>();

}