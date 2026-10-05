/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.email.otp.web.internal.checker;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Stian Sigvartsen
 */
public class EmailOTPBrowserMFACheckerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		Mockito.when(
			FrameworkUtil.getBundle(Mockito.any())
		).thenReturn(
			bundleContext.getBundle()
		);

		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@AfterClass
	public static void tearDownClass() {
		_frameworkUtilMockedStatic.close();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testBuildNotVerifiedAuditMessage() throws Exception {
		String reason = RandomTestUtil.randomString();

		AuditMessage auditMessage = _buildNotVerifiedAuditMessage(reason);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			"email_otp", additionalInfoJSONObject.getString("factor"));
		Assert.assertEquals(
			"not_verified", additionalInfoJSONObject.getString("outcome"));
		Assert.assertEquals(
			reason, additionalInfoJSONObject.getString("reason"));

		Assert.assertEquals("verify_failure", auditMessage.getResourceAction());
		Assert.assertEquals("mfa", auditMessage.getResourceType());
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testBuildNotVerifiedAuditMessageWhenFeatureFlagIsDisabled()
		throws Exception {

		String reason = RandomTestUtil.randomString();

		AuditMessage auditMessage = _buildNotVerifiedAuditMessage(reason);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertFalse(additionalInfoJSONObject.has("factor"));
		Assert.assertFalse(additionalInfoJSONObject.has("outcome"));
		Assert.assertEquals(
			reason, additionalInfoJSONObject.getString("reason"));

		Assert.assertNull(auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());
	}

	@Test
	public void testObfuscateEmailAddress() throws Exception {
		Assert.assertEquals(
			"*@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress("t@liferay.com"));
		Assert.assertEquals(
			"**@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress("te@liferay.com"));
		Assert.assertEquals(
			"***@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress("tes@liferay.com"));
		Assert.assertEquals(
			"t***@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress(
				"test@liferay.com"));
		Assert.assertEquals(
			"t***1@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress(
				"test1@liferay.com"));
		Assert.assertEquals(
			"te***1@liferay.com",
			EmailOTPBrowserMFAChecker.obfuscateEmailAddress(
				"test11@liferay.com"));
	}

	private AuditMessage _buildNotVerifiedAuditMessage(String reason) {
		User user = Mockito.mock(User.class);

		Mockito.when(
			user.getCompanyId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Object mfaEmailOTPAuditMessageBuilder =
			ReflectionTestUtil.getFieldValue(
				new EmailOTPBrowserMFAChecker(),
				"_mfaEmailOTPAuditMessageBuilder");

		return ReflectionTestUtil.invoke(
			mfaEmailOTPAuditMessageBuilder, "buildNotVerifiedAuditMessage",
			new Class<?>[] {User.class, String.class, String.class}, user,
			RandomTestUtil.randomString(), reason);
	}

	private static final MockedStatic<FrameworkUtil>
		_frameworkUtilMockedStatic = Mockito.mockStatic(FrameworkUtil.class);

}