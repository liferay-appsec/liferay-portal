/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class AWSArnResolverTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testResolveCompanyTemplate() {
		String resolved = AWSArnResolver.resolve(
			_COMPANY_TEMPLATE, "us-east-1", "111122223333", 42, "master");

		Assert.assertEquals(
			"arn:aws:kms:us-east-1:111122223333:alias/liferay-company-42-" +
				"master",
			resolved);
	}

	@Test
	public void testResolveNullTemplateFallsBackToIdentifier() {
		String resolved = AWSArnResolver.resolve(
			null, "us-east-1", "111122223333", 0, "master");

		Assert.assertEquals("master", resolved);
	}

	@Test
	public void testResolvePassesThroughAliasIdentifier() {
		String resolved = AWSArnResolver.resolve(
			_SYSTEM_TEMPLATE, "us-east-1", "111122223333", 0,
			"alias/already-qualified");

		Assert.assertEquals("alias/already-qualified", resolved);
	}

	@Test
	public void testResolvePassesThroughArnIdentifier() {
		String resolved = AWSArnResolver.resolve(
			_SYSTEM_TEMPLATE, "us-east-1", "111122223333", 0,
			"arn:aws:kms:eu-west-1:222244448888:alias/foo");

		Assert.assertEquals(
			"arn:aws:kms:eu-west-1:222244448888:alias/foo", resolved);
	}

	@Test
	public void testResolveSystemTemplate() {
		String resolved = AWSArnResolver.resolve(
			_SYSTEM_TEMPLATE, "us-east-1", "111122223333", 0, "master");

		Assert.assertEquals(
			"arn:aws:kms:us-east-1:111122223333:alias/liferay-system-master",
			resolved);
	}

	@Test
	public void testResolveTolerantOfMissingAccountId() {
		String resolved = AWSArnResolver.resolve(
			_SYSTEM_TEMPLATE, "us-east-1", null, 0, "master");

		Assert.assertEquals(
			"arn:aws:kms:us-east-1:{accountId}:alias/liferay-system-master",
			resolved);
	}

	private static final String _COMPANY_TEMPLATE =
		"arn:aws:kms:{region}:{accountId}:" +
			"alias/liferay-company-{companyId}-{identifier}";

	private static final String _SYSTEM_TEMPLATE =
		"arn:aws:kms:{region}:{accountId}:alias/liferay-system-{identifier}";

}