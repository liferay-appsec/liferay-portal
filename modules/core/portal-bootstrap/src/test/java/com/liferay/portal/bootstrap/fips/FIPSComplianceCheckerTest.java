/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.bootstrap.fips;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Provider;
import java.security.Security;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Caio Farias
 */
public class FIPSComplianceCheckerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_originalProviderApproved =
			PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_APPROVED;
		_originalProviderName = PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_NAME;
		_originalProviderStrict =
			PropsValues.PORTAL_SECURITY_FIPS_PROVIDER_STRICT;

		Provider[] providers = Security.getProviders();

		_firstProviderName = providers[0].getName();
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_APPROVED",
			_originalProviderApproved);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_NAME",
			_originalProviderName);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_STRICT",
			_originalProviderStrict);
	}

	@Test
	public void testValidateOnlyWarnsWhenNonstrictAndUnapprovedProvider() {
		_setFIPSProviderProps(_firstProviderName, new String[0], false);

		FIPSComplianceChecker.run();
	}

	@Test
	public void testValidatePassesWhenFIPSProviderFirstAndAllApproved() {
		Provider[] providers = Security.getProviders();

		String[] allProviderNames = new String[providers.length];

		for (int i = 0; i < providers.length; i++) {
			allProviderNames[i] = providers[i].getName();
		}

		_setFIPSProviderProps(_firstProviderName, allProviderNames, true);

		FIPSComplianceChecker.run();
	}

	@Test
	public void testValidateThrowsWhenFIPSProviderNotFirst() {
		_setFIPSProviderProps("NotARegisteredProvider", new String[0], true);

		try {
			FIPSComplianceChecker.run();

			Assert.fail("SecurityException expected");
		}
		catch (SecurityException securityException) {
			String message = securityException.getMessage();

			Assert.assertTrue(
				message,
				message.contains("must be the first registered JCE provider"));
		}
	}

	@Test
	public void testValidateThrowsWhenStrictAndUnapprovedProvider() {
		_setFIPSProviderProps(_firstProviderName, new String[0], true);

		try {
			FIPSComplianceChecker.run();

			Assert.fail("SecurityException expected");
		}
		catch (SecurityException securityException) {
			String message = securityException.getMessage();

			Assert.assertTrue(
				message,
				message.contains(
					"Unapproved JCE providers registered in FIPS mode"));
		}
	}

	private void _setFIPSProviderProps(
		String name, String[] approved, boolean strict) {

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_APPROVED",
			approved);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_NAME", name);
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "PORTAL_SECURITY_FIPS_PROVIDER_STRICT", strict);
	}

	private String _firstProviderName;
	private String[] _originalProviderApproved;
	private String _originalProviderName;
	private boolean _originalProviderStrict;

}
