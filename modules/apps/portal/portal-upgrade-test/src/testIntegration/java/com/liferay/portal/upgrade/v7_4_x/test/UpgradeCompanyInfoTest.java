/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.v7_4_x.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.cache.CacheRegistryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyInfo;
import com.liferay.portal.kernel.security.keystore.CompanyKeyStoreUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.v7_4_x.UpgradeCompanyInfo;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Pedro Victor Silvestre
 */
@RunWith(Arquillian.class)
public class UpgradeCompanyInfoTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testUpgradeProcess() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", Boolean.TRUE)) {

			UpgradeProcess upgradeProcess = new UpgradeCompanyInfo();

			upgradeProcess.upgrade();

			CacheRegistryUtil.clear();

			company = _companyLocalService.fetchCompany(company.getCompanyId());

			CompanyInfo companyInfo = company.getCompanyInfo();

			Assert.assertEquals(
				companyInfo.getKey(),
				CompanyKeyStoreUtil.generateAlias(company.getCompanyId()));
			Assert.assertNotNull(
				CompanyKeyStoreUtil.getKey(companyInfo.getKey()));
			Assert.assertTrue(
				CompanyKeyStoreUtil.isKeyStoreAlias(companyInfo.getKey()));
		}
		finally {
			_companyLocalService.deleteCompany(company);
		}
	}

	@Inject
	private CompanyLocalService _companyLocalService;

}