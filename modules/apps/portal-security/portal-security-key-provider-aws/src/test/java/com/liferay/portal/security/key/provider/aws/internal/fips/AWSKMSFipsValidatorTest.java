/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.provider.aws.internal.fips;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.ServiceIndicator;
import com.liferay.portal.security.key.crypto.exception.CryptoException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class AWSKMSFipsValidatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAEADCipherModeAllowedUnderFips() throws Exception {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", true);

		awsKMSFipsValidator.validateCipherMode();
	}

	@Test(expected = CryptoException.class)
	public void testCBCCipherModeRejectedUnderFips() throws Exception {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_CBC", true);

		awsKMSFipsValidator.validateCipherMode();
	}

	@Test
	public void testCloudHSMOriginIsFipsApproved() {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", false);

		Assert.assertTrue(
			awsKMSFipsValidator.isFipsApprovedKeyOrigin("AWS_CLOUDHSM"));
	}

	@Test
	public void testFipsEndpointMakesAnyOriginApproved() {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", true);

		Assert.assertTrue(
			awsKMSFipsValidator.isFipsApprovedKeyOrigin("AWS_KMS"));
		Assert.assertTrue(awsKMSFipsValidator.isFipsEnforced());
	}

	@Test
	public void testServiceIndicatorAllowsUnapprovedWhenNotEnforced()
		throws Exception {

		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", false);

		ServiceIndicator serviceIndicator =
			awsKMSFipsValidator.toServiceIndicator(
				false, RandomTestUtil.randomString());

		Assert.assertFalse(serviceIndicator.isApproved());
	}

	@Test
	public void testServiceIndicatorApproved() throws Exception {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", true);

		ServiceIndicator serviceIndicator =
			awsKMSFipsValidator.toServiceIndicator(true, "AWS.KMS.Encrypt");

		Assert.assertEquals(
			"AWS.KMS.Encrypt", serviceIndicator.getSecurityFunctionName());
		Assert.assertTrue(serviceIndicator.isApproved());
	}

	@Test(expected = CryptoException.class)
	public void testServiceIndicatorRejectsUnapprovedUnderFips()
		throws Exception {

		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", true);

		awsKMSFipsValidator.toServiceIndicator(
			false, RandomTestUtil.randomString());
	}

	@Test
	public void testSoftwareOriginNotFipsApprovedWithoutFipsEndpoint() {
		AWSKMSFipsValidator awsKMSFipsValidator = new AWSKMSFipsValidator(
			"AES_256_GCM", false);

		Assert.assertFalse(
			awsKMSFipsValidator.isFipsApprovedKeyOrigin("AWS_KMS"));
	}

}