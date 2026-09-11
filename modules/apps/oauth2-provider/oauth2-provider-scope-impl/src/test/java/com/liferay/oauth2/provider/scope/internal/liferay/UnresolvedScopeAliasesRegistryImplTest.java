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

	@Test
	public void testGetOAuth2ApplicationIdsByCompanyId() {
		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(RandomTestUtil.randomString()));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			unresolvedScopeAliasesRegistry.getOAuth2ApplicationIdsByCompanyId();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 200, Arrays.asList(RandomTestUtil.randomString()));

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertFalse(oAuth2ApplicationIds.contains(200L));
	}

	@Test
	public void testGetUnresolvedScopeAliases() {
		_testGetUnresolvedScopeAliases();
		_testGetUnresolvedScopeAliasesWithUnknownOAuth2ApplicationId();
	}

	@Test
	public void testIsEmpty() {
		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		Assert.assertTrue(unresolvedScopeAliasesRegistry.isEmpty());

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(RandomTestUtil.randomString()));

		Assert.assertFalse(unresolvedScopeAliasesRegistry.isEmpty());

		unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(unresolvedScopeAliasesRegistry.isEmpty());
	}

	@Test
	public void testRemoveUnresolvedScopeAliases() {
		_testRemoveUnresolvedScopeAliases();
		_testRemoveUnresolvedScopeAliasesKeepsOthers();
		_testRemoveUnresolvedScopeAliasesRemovesEmptyApplication();
	}

	@Test
	public void testSetUnresolvedScopeAliases() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();
		String scopeAlias3 = RandomTestUtil.randomString();

		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		Collection<String> scopeAliases =
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(scopeAliases.contains(scopeAlias1));
		Assert.assertTrue(scopeAliases.contains(scopeAlias2));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			unresolvedScopeAliasesRegistry.getOAuth2ApplicationIdsByCompanyId();

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertTrue(oAuth2ApplicationIds.contains(100L));

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias3));

		scopeAliases = unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
			1, 100);

		Assert.assertFalse(scopeAliases.contains(scopeAlias1));
		Assert.assertFalse(scopeAliases.contains(scopeAlias2));
		Assert.assertTrue(scopeAliases.contains(scopeAlias3));
	}

	private void _testGetUnresolvedScopeAliases() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));
		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			2, 100, Arrays.asList(scopeAlias2));

		Collection<String> company1ScopeAliases =
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);
		Collection<String> company2ScopeAliases =
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(2, 100);

		Assert.assertTrue(company1ScopeAliases.contains(scopeAlias1));
		Assert.assertFalse(company1ScopeAliases.contains(scopeAlias2));
		Assert.assertTrue(company2ScopeAliases.contains(scopeAlias2));
		Assert.assertFalse(company2ScopeAliases.contains(scopeAlias1));
	}

	private void _testGetUnresolvedScopeAliasesWithUnknownOAuth2ApplicationId() {
		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		Assert.assertTrue(
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				1, 999
			).isEmpty());
	}

	private void _testRemoveUnresolvedScopeAliases() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));
		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			2, 100, Arrays.asList(scopeAlias2));

		unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				1, 100
			).isEmpty());
		Assert.assertTrue(
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				2, 100
			).contains(
				scopeAlias2
			));
	}

	private void _testRemoveUnresolvedScopeAliasesKeepsOthers() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();
		String scopeAlias3 = RandomTestUtil.randomString();

		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2, scopeAlias3));

		unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1));

		Collection<String> scopeAliases =
			unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertFalse(scopeAliases.contains(scopeAlias1));
		Assert.assertTrue(scopeAliases.contains(scopeAlias2));
		Assert.assertTrue(scopeAliases.contains(scopeAlias3));
	}

	private void _testRemoveUnresolvedScopeAliasesRemovesEmptyApplication() {
		String scopeAlias1 = RandomTestUtil.randomString();
		String scopeAlias2 = RandomTestUtil.randomString();

		UnresolvedScopeAliasesRegistry unresolvedScopeAliasesRegistry =
			new UnresolvedScopeAliasesRegistryImpl();

		unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
			1, 100, Arrays.asList(scopeAlias1, scopeAlias2));

		Assert.assertTrue(unresolvedScopeAliasesRegistry.isEmpty());
	}

}