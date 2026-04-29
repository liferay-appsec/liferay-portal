/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class RetrievalAugmentorUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testCreateRetrievalAugmentorDecryptsTokens() throws Exception {
		String encryptedAccessToken = RandomTestUtil.randomString();
		String encryptedUserToken = RandomTestUtil.randomString();

		long companyId = RandomTestUtil.randomLong();

		Company company = Mockito.mock(Company.class);

		CompanyLocalService companyLocalService = _mockCompanyLocalService(
			company, companyId);

		Encryptor encryptor = _mockEncryptor(
			company, encryptedAccessToken, encryptedUserToken);

		RetrievalAugmentorUtil.createRetrievalAugmentor(
			companyId, companyLocalService, null, encryptor, null, null,
			HashMapBuilder.put(
				"rag",
				"{\"contentRetriever\":{\"blueprintExternalReferenceCode\":" +
					"\"test-bp\",\"key\":\"liferay\"}}"
			).build(),
			null, null, null, 0L,
			HashMapBuilder.<String, Serializable>put(
				"accessToken", encryptedAccessToken
			).put(
				"userToken", encryptedUserToken
			).build());

		Mockito.verify(
			encryptor
		).decrypt(
			company.getKeyObj(), encryptedAccessToken
		);

		Mockito.verify(
			encryptor
		).decrypt(
			company.getKeyObj(), encryptedUserToken
		);
	}

	private CompanyLocalService _mockCompanyLocalService(
			Company company, long companyId)
		throws Exception {

		CompanyLocalService companyLocalService = Mockito.mock(
			CompanyLocalService.class);

		Mockito.when(
			companyLocalService.getCompany(companyId)
		).thenReturn(
			company
		);

		return companyLocalService;
	}

	private Encryptor _mockEncryptor(
			Company company, String encryptedAccessToken,
			String encryptedUserToken)
		throws Exception {

		Encryptor encryptor = Mockito.mock(Encryptor.class);

		Mockito.when(
			encryptor.decrypt(company.getKeyObj(), encryptedAccessToken)
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			encryptor.decrypt(company.getKeyObj(), encryptedUserToken)
		).thenReturn(
			RandomTestUtil.randomString()
		);

		return encryptor;
	}

}