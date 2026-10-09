/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet.filters.secure;

import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class NonceUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGenerate() throws Exception {
		try (MockedStatic<CompanyLocalServiceUtil>
				companyLocalServiceUtilMockedStatic = Mockito.mockStatic(
					CompanyLocalServiceUtil.class)) {

			Company company = Mockito.mock(Company.class);

			Mockito.when(
				company.getKey()
			).thenReturn(
				RandomTestUtil.randomString()
			);

			companyLocalServiceUtilMockedStatic.when(
				() -> CompanyLocalServiceUtil.getCompanyById(Mockito.anyLong())
			).thenReturn(
				company
			);

			FIPSModeTestUtil.assertAlgorithmSwitch(
				DigesterUtil.MD5, CryptoUtil.class, DigesterUtil.SHA_256,
				algorithm -> CryptoUtil.createMessageDigest(
					algorithm, CryptoFunctionApprovalType.APPROVED),
				() -> NonceUtil.generate(
					RandomTestUtil.randomLong(),
					RandomTestUtil.randomString()));
		}
	}

}