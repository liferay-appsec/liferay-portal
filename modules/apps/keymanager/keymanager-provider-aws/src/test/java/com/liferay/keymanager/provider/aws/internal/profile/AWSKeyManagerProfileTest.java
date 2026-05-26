/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.profile;

import com.liferay.keymanager.provider.aws.internal.profile.configuration.AWSKeyManagerProfileConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class AWSKeyManagerProfileTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_profile = new AWSKeyManagerProfile();

		_setConfiguration(
			_profile,
			_buildConfiguration("us-east-1", "111122223333", "aws", false));
	}

	@Test
	public void testGetCompanyProviderIdsWithAwsSecretLayer() throws Exception {
		_setConfiguration(
			_profile,
			_buildConfiguration("us-east-1", "111122223333", "aws", false));

		Assert.assertEquals(
			"aws-company-crypto", _profile.getCompanyDekProviderId());
		Assert.assertEquals(
			"aws-company-crypto", _profile.getCompanyKekProviderId());
		Assert.assertEquals(
			"aws-company-secret", _profile.getCompanySecretProviderId());
	}

	@Test
	public void testGetCompanySecretProviderIdWithDbSecretLayer()
		throws Exception {

		_setConfiguration(
			_profile,
			_buildConfiguration("us-east-1", "111122223333", "db", false));

		Assert.assertEquals(
			"db-company-secret", _profile.getCompanySecretProviderId());
	}

	@Test
	public void testGetProfileId() {
		Assert.assertEquals("aws", _profile.getProfileId());
	}

	@Test
	public void testGetSystemProviderIdsWithAwsSecretLayer() throws Exception {
		Assert.assertEquals(
			"aws-system-crypto", _profile.getSystemDekProviderId());
		Assert.assertEquals(
			"aws-system-crypto", _profile.getSystemKekProviderId());
		Assert.assertEquals(
			"aws-system-secret", _profile.getSystemSecretProviderId());
	}

	@Test
	public void testGetSystemSecretProviderIdWithDbSecretLayer()
		throws Exception {

		_setConfiguration(
			_profile,
			_buildConfiguration("us-east-1", "111122223333", "db", false));

		Assert.assertEquals(
			"db-system-secret", _profile.getSystemSecretProviderId());
	}

	private AWSKeyManagerProfileConfiguration _buildConfiguration(
		String region, String accountId, String secretLayer,
		boolean strictMode) {

		return new AWSKeyManagerProfileConfiguration() {

			@Override
			public String awsAccountId() {
				return accountId;
			}

			@Override
			public String awsRegion() {
				return region;
			}

			@Override
			public String secretLayer() {
				return secretLayer;
			}

			@Override
			public boolean strictMode() {
				return strictMode;
			}

		};
	}

	private void _setConfiguration(
			AWSKeyManagerProfile profile,
			AWSKeyManagerProfileConfiguration awsKeyManagerProfileConfiguration)
		throws Exception {

		Field field = AWSKeyManagerProfile.class.getDeclaredField(
			"_awsKeyManagerProfileConfiguration");

		field.setAccessible(true);

		field.set(profile, awsKeyManagerProfileConfiguration);
	}

	private AWSKeyManagerProfile _profile;

}