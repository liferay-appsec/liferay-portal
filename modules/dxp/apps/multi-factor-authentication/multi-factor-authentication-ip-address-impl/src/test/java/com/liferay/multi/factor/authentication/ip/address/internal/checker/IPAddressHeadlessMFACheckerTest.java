/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.ip.address.internal.checker;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Regisson Aguiar
 */
public class IPAddressHeadlessMFACheckerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testBuildVerificationFailureAuditMessage() throws Exception {
		String reason = RandomTestUtil.randomString();

		AuditMessage auditMessage = _buildVerificationFailureAuditMessage(
			reason);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			"ip_address", additionalInfoJSONObject.getString("factor"));
		Assert.assertEquals(
			"verification_failure",
			additionalInfoJSONObject.getString("outcome"));
		Assert.assertEquals(
			reason, additionalInfoJSONObject.getString("reason"));

		Assert.assertEquals("verify_failure", auditMessage.getResourceAction());
		Assert.assertEquals("mfa", auditMessage.getResourceType());
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testBuildVerificationFailureAuditMessageWhenFeatureFlagIsDisabled()
		throws Exception {

		String reason = RandomTestUtil.randomString();

		AuditMessage auditMessage = _buildVerificationFailureAuditMessage(
			reason);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertFalse(additionalInfoJSONObject.has("factor"));
		Assert.assertFalse(additionalInfoJSONObject.has("outcome"));
		Assert.assertEquals(
			reason, additionalInfoJSONObject.getString("reason"));

		Assert.assertNull(auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());
	}

	private AuditMessage _buildVerificationFailureAuditMessage(String reason) {
		User user = Mockito.mock(User.class);

		Mockito.when(
			user.getCompanyId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Object mfaipAddressAuditMessageBuilder =
			ReflectionTestUtil.getFieldValue(
				new IPAddressHeadlessMFAChecker(),
				"_mfaipAddressAuditMessageBuilder");

		return ReflectionTestUtil.invoke(
			mfaipAddressAuditMessageBuilder,
			"buildVerificationFailureAuditMessage",
			new Class<?>[] {User.class, String.class, String.class}, user,
			RandomTestUtil.randomString(), reason);
	}

}