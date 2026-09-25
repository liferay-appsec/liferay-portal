/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.avalara.connector.internal;

import com.liferay.commerce.avalara.connector.configuration.CommerceAvalaraConnectorConfiguration;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Caio Farias
 */
public class CommerceAvalaraConnectorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetLicenseKey() {
		long companyId = RandomTestUtil.randomLong();
		String licenseKey = RandomTestUtil.randomString();

		CommerceAvalaraConnectorImpl commerceAvalaraConnectorImpl =
			new CommerceAvalaraConnectorImpl();

		ReflectionTestUtil.setFieldValue(
			commerceAvalaraConnectorImpl, "_secretResolver",
			(SecretResolver)(secretResolverCompanyId, value) -> {
				Assert.assertEquals(companyId, secretResolverCompanyId);

				return licenseKey;
			});

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			String keyReferenceString = _getKeyReferenceString(
				_getIdentifier(companyId));

			Assert.assertEquals(
				licenseKey,
				_getLicenseKey(
					commerceAvalaraConnectorImpl, keyReferenceString));

			Assert.assertEquals(
				licenseKey,
				_getLicenseKey(commerceAvalaraConnectorImpl, licenseKey));

			keyReferenceString = _getKeyReferenceString(
				_getIdentifier(companyId + 1));

			Assert.assertEquals(
				keyReferenceString,
				_getLicenseKey(
					commerceAvalaraConnectorImpl, keyReferenceString));

			keyReferenceString = _getKeyReferenceString(
				RandomTestUtil.randomString());

			Assert.assertEquals(
				keyReferenceString,
				_getLicenseKey(
					commerceAvalaraConnectorImpl, keyReferenceString));
		}
	}

	private String _getIdentifier(long companyId) {
		return StringBundler.concat(
			CommerceAvalaraConnectorConfiguration.class.getSimpleName(),
			StringPool.SLASH, companyId, "/licenseKey");
	}

	private String _getKeyReferenceString(String identifier) {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				identifier, RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	private String _getLicenseKey(
		CommerceAvalaraConnectorImpl commerceAvalaraConnectorImpl,
		String licenseKey) {

		return ReflectionTestUtil.invoke(
			commerceAvalaraConnectorImpl, "_getLicenseKey",
			new Class<?>[] {String.class}, licenseKey);
	}

}