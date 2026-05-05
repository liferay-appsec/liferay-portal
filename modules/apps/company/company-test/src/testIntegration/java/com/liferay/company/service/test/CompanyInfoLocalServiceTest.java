/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.company.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyInfo;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.keystore.CompanyKeyStoreUtil;
import com.liferay.portal.kernel.service.CompanyInfoLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alberto Chaparro
 */
@RunWith(Arquillian.class)
public class CompanyInfoLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_safeCloseable = CompanyThreadLocal.setCompanyIdWithSafeCloseable(
			_company.getCompanyId());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_safeCloseable.close();

		_companyLocalService.deleteCompany(_company);
	}

	@Test
	public void testDeleteCompanyInfo() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		long companyId = company.getCompanyId();

		_companyLocalService.deleteCompany(companyId);

		Assert.assertNull(_companyInfoLocalService.fetchCompany(companyId));
	}

	@Test
	public void testDeleteCompanyInfoFIPSEnabled() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", Boolean.TRUE)) {

			Company company = CompanyTestUtil.addCompany();

			long companyId = company.getCompanyId();

			CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
				companyId);

			String alias = companyInfo.getKey();

			_companyLocalService.deleteCompany(companyId);

			Assert.assertTrue(CompanyKeyStoreUtil.isKeyStoreAlias(alias));
			Assert.assertNull(_companyInfoLocalService.fetchCompany(companyId));
			Assert.assertNull(CompanyKeyStoreUtil.getKey(alias));
		}
	}

	@Test
	public void testGetCompanyInfoKey() {
		CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
			_company.getCompanyId());

		Assert.assertEquals(companyInfo.getKey(), _company.getKey());
	}

	@Test
	public void testGetCompanyInfoKeyObj() {
		CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
			_company.getCompanyId());

		Assert.assertEquals(
			_encryptor.deserializeKey(companyInfo.getKey()),
			_company.getKeyObj());
	}

	@Test
	public void testGetCompanyInfoKeyObjFIPSEnabled() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", Boolean.TRUE)) {

			Company company = CompanyTestUtil.addCompany();

			try {
				CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
					company.getCompanyId());

				Assert.assertTrue(
					CompanyKeyStoreUtil.isKeyStoreAlias(companyInfo.getKey()));
				Assert.assertNotNull(
					CompanyKeyStoreUtil.getKey(companyInfo.getKey()));
				Assert.assertEquals(
					CompanyKeyStoreUtil.getKey(companyInfo.getKey()),
					company.getKeyObj());
			}
			finally {
				_companyLocalService.deleteCompany(company.getCompanyId());
			}
		}
	}

	@Test
	public void testUpdateCompanyInfoKey() {
		_company.setKey(RandomTestUtil.randomString());

		_company = _companyLocalService.updateCompany(_company);

		CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
			_company.getCompanyId());

		Assert.assertEquals(companyInfo.getKey(), _company.getKey());
	}

	@Test
	public void testUpdateCompanyInfoKeyObj() {
		_company.setKey(RandomTestUtil.randomString());

		_company = _companyLocalService.updateCompany(_company);

		CompanyInfo companyInfo = _companyInfoLocalService.fetchCompany(
			_company.getCompanyId());

		Assert.assertEquals(
			_encryptor.deserializeKey(companyInfo.getKey()),
			_company.getKeyObj());
	}

	private static Company _company;

	@Inject
	private static CompanyInfoLocalService _companyInfoLocalService;

	@Inject
	private static CompanyLocalService _companyLocalService;

	@Inject
	private static CounterLocalService _counterLocalService;

	@Inject
	private static Encryptor _encryptor;

	private static SafeCloseable _safeCloseable;

}