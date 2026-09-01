/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.internal.liferay;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
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
	public void testGetOAuth2ApplicationIdsByCompanyIdReturnsSnapshot() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Foo.everything"));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 200, Arrays.asList("C_Bar.everything"));

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertFalse(oAuth2ApplicationIds.contains(200L));
	}

	@Test
	public void testRemoveUnresolvedScopeAliases() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Foo.everything"));

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(_unresolvedScopeAliasesRegistry.isEmpty());
	}

	@Test
	public void testSameApplicationIdIsolatedAcrossCompanies() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Foo.everything"));
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			2, 100, Arrays.asList("C_Bar.everything"));

		Collection<String> company1ScopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);
		Collection<String> company2ScopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(2, 100);

		Assert.assertTrue(company1ScopeAliases.contains("C_Foo.everything"));
		Assert.assertFalse(company1ScopeAliases.contains("C_Bar.everything"));

		Assert.assertTrue(company2ScopeAliases.contains("C_Bar.everything"));
		Assert.assertFalse(company2ScopeAliases.contains("C_Foo.everything"));

		_unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				1, 100
			).isEmpty());
		Assert.assertTrue(
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(
				2, 100
			).contains(
				"C_Bar.everything"
			));
	}

	@Test
	public void testSetOverwritesPreviousScopeAliases() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Foo.everything"));
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Bar.everything"));

		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertFalse(scopeAliases.contains("C_Foo.everything"));
		Assert.assertTrue(scopeAliases.contains("C_Bar.everything"));
	}

	@Test
	public void testSetUnresolvedScopeAliases() {
		_unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
			1, 100, Arrays.asList("C_Foo.everything", "C_Bar.everything"));

		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 100);

		Assert.assertTrue(scopeAliases.contains("C_Foo.everything"));
		Assert.assertTrue(scopeAliases.contains("C_Bar.everything"));

		Map<Long, Set<Long>> oAuth2ApplicationIdsByCompanyId =
			_unresolvedScopeAliasesRegistry.
				getOAuth2ApplicationIdsByCompanyId();

		Set<Long> oAuth2ApplicationIds = oAuth2ApplicationIdsByCompanyId.get(
			1L);

		Assert.assertTrue(oAuth2ApplicationIds.contains(100L));
	}

	@Test
	public void testUnknownOAuth2ApplicationId() {
		Collection<String> scopeAliases =
			_unresolvedScopeAliasesRegistry.getUnresolvedScopeAliases(1, 999);

		Assert.assertTrue(scopeAliases.isEmpty());

		Assert.assertTrue(_unresolvedScopeAliasesRegistry.isEmpty());
	}

	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

}