/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.internal.liferay;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Allen Ziegenfus
 */
public class UnresolvedScopeAliasesRegistryImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();
	}

	@Test
	public void testGetOAuth2ApplicationIdsByCompanyId() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(RandomTestUtil.randomString()));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 200, Arrays.asList(RandomTestUtil.randomString()));

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertFalse(oAuth2ApplicationIds.contains(200L));
	}

	@Test
	public void testGetUnresolvedScopeAliasesAcrossCompanies() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			2, 100, Arrays.asList(scopeAlias2));

		Collection<String> company1ScopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);
		Collection<String> company2ScopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(2, 100);

		Assert.assertTrue(company1ScopeAliases.contains(scopeAlias1));
		Assert.assertFalse(company1ScopeAliases.contains(scopeAlias2));

		Assert.assertTrue(company2ScopeAliases.contains(scopeAlias2));
		Assert.assertFalse(company2ScopeAliases.contains(scopeAlias1));

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				1, 100
			).isEmpty());
		Assert.assertTrue(
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				2, 100
			).contains(
				scopeAlias2
			));
	}

	@Test
	public void testGetUnresolvedScopeAliasesWithUnknownOAuth2ApplicationId() {
		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 999);

		Assert.assertTrue(scopeAliases.isEmpty());

		Assert.assertTrue(_unresolvedScopeAliasesRegistry.isEmpty());
	}

	@Test
	public void testRemoveUnresolvedScopeAliases() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(RandomTestUtil.randomString()));

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(_unresolvedScopeAliasesRegistry.isEmpty());
	}

	@Test
	public void testRemoveUnresolvedScopeAliasesKeepsOthers() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();
		String scopeAlias3 = RandomTestUtil.randomString();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		// A configuration update records another alias while a reconcile pass,
		// holding an earlier snapshot, is still running

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2, scopeAlias3));

		// The pass removes only what it bound; the newly recorded alias must
		// survive rather than being overwritten from the stale snapshot

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));

		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertFalse(scopeAliases.contains(scopeAlias1));
		Assert.assertTrue(scopeAliases.contains(scopeAlias2));
		Assert.assertTrue(scopeAliases.contains(scopeAlias3));
	}

	@Test
	public void testRemoveUnresolvedScopeAliasesRemovesEmptyApplication() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		Assert.assertTrue(_unresolvedScopeAliasesRegistry.isEmpty());
	}

	@Test
	public void testSetUnresolvedScopeAliases() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(scopeAliases.contains(scopeAlias1));
		Assert.assertTrue(scopeAliases.contains(scopeAlias2));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertTrue(oAuth2ApplicationIds.contains(100L));
	}

	@Test
	public void testSetUnresolvedScopeAliasesReplacesPrevious() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias2));

		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertFalse(scopeAliases.contains(scopeAlias1));
		Assert.assertTrue(scopeAliases.contains(scopeAlias2));
	}

	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

}