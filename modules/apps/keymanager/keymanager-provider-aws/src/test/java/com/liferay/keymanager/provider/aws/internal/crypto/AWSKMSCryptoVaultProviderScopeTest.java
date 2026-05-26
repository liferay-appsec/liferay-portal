/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.crypto;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class AWSKMSCryptoVaultProviderScopeTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCompanyProviderAllowsOnlyMatchingCompany()
		throws Exception {

		AWSKMSCompanyCryptoVaultProvider provider =
			new AWSKMSCompanyCryptoVaultProvider();

		_setCompanyId(provider, 42L);

		Assert.assertTrue(provider.isAllowedCompany(42L));
		Assert.assertFalse(provider.isAllowedCompany(43L));
	}

	@Test
	public void testCompanyProviderRejectsSystemCompany() {
		AWSKMSCompanyCryptoVaultProvider provider =
			new AWSKMSCompanyCryptoVaultProvider();

		Assert.assertFalse(provider.isAllowedCompany(0L));
	}

	@Test
	public void testCompanyProviderRejectsUnconfiguredInstance() {
		AWSKMSCompanyCryptoVaultProvider provider =
			new AWSKMSCompanyCryptoVaultProvider();

		Assert.assertFalse(provider.isAllowedCompany(1L));
		Assert.assertFalse(provider.isAllowedCompany(123_456L));
	}

	@Test
	public void testSystemProviderRejectsTenantCompanies() {
		AWSKMSSystemCryptoVaultProvider provider =
			new AWSKMSSystemCryptoVaultProvider();

		Assert.assertFalse(provider.isAllowedCompany(1L));
	}

	@Test
	public void testSystemProviderTracksOnlySystem() {
		AWSKMSSystemCryptoVaultProvider provider =
			new AWSKMSSystemCryptoVaultProvider();

		Assert.assertTrue(provider.isAllowedCompany(0L));
	}

	private void _setCompanyId(
			AWSKMSCompanyCryptoVaultProvider provider, long companyId)
		throws Exception {

		Field field = BaseAWSKMSCryptoVaultProvider.class.getDeclaredField(
			"_companyId");

		field.setAccessible(true);

		field.set(provider, companyId);
	}

}