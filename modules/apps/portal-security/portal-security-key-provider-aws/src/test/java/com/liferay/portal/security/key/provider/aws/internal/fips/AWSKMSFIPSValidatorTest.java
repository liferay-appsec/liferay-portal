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
public class AWSKMSFIPSValidatorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsFIPSApprovedKeyOrigin() {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), true);

		Assert.assertTrue(
			awsKMSFIPSValidator.isFIPSApprovedKeyOrigin("AWS_CLOUDHSM"));
		Assert.assertTrue(
			awsKMSFIPSValidator.isFIPSApprovedKeyOrigin("AWS_KMS"));
		Assert.assertFalse(
			awsKMSFIPSValidator.isFIPSApprovedKeyOrigin("EXTERNAL"));
	}

	@Test
	public void testToServiceIndicatorApproved() throws Exception {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), true);

		ServiceIndicator serviceIndicator =
			awsKMSFIPSValidator.toServiceIndicator(true, "AWS.KMS.Encrypt");

		Assert.assertEquals(
			"AWS.KMS.Encrypt", serviceIndicator.getSecurityFunctionName());
		Assert.assertTrue(serviceIndicator.isApproved());
	}

	@Test(expected = CryptoException.class)
	public void testToServiceIndicatorRejectsUnapprovedUnderEnforcement()
		throws Exception {

		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), true);

		awsKMSFIPSValidator.toServiceIndicator(
			false, RandomTestUtil.randomString());
	}

	@Test
	public void testToServiceIndicatorWhenNotEnforced() throws Exception {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), false);

		ServiceIndicator serviceIndicator =
			awsKMSFIPSValidator.toServiceIndicator(
				false, RandomTestUtil.randomString());

		Assert.assertFalse(serviceIndicator.isApproved());
	}

	@Test
	public void testValidateCipherMode() throws Exception {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			"AES_256_GCM", true);

		awsKMSFIPSValidator.validateCipherMode();
	}

	@Test(expected = CryptoException.class)
	public void testValidateCipherModeWithCBC() throws Exception {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			"AES_CBC", true);

		awsKMSFIPSValidator.validateCipherMode();
	}

	@Test
	public void testValidateKeyOriginAllowsCloudHSMUnderEnforcement()
		throws Exception {

		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), true);

		awsKMSFIPSValidator.validateKeyOrigin("AWS_CLOUDHSM");
	}

	@Test(expected = CryptoException.class)
	public void testValidateKeyOriginRejectsExternalUnderEnforcement()
		throws Exception {

		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), true);

		awsKMSFIPSValidator.validateKeyOrigin("EXTERNAL");
	}

	@Test
	public void testValidateKeyOriginWhenNotEnforced() throws Exception {
		AWSKMSFIPSValidator awsKMSFIPSValidator = new AWSKMSFIPSValidator(
			RandomTestUtil.randomString(), false);

		awsKMSFIPSValidator.validateKeyOrigin("EXTERNAL");
	}

}