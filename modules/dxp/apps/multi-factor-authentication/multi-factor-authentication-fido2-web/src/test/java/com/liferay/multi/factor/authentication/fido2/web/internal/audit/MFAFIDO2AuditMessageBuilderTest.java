/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.fido2.web.internal.audit;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
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
public class MFAFIDO2AuditMessageBuilderTest {

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
	public void testBuildNotVerifiedAuditMessage() throws Exception {
		String reason = RandomTestUtil.randomString();

		AuditMessage auditMessage = _buildNotVerifiedAuditMessage(reason);

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			"fido2", additionalInfoJSONObject.getString("factor"));
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

	private AuditMessage _buildNotVerifiedAuditMessage(String reason) {
		User user = Mockito.mock(User.class);

		Mockito.when(
			user.getCompanyId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		MFAFIDO2AuditMessageBuilder mfaFIDO2AuditMessageBuilder =
			new MFAFIDO2AuditMessageBuilder();

		return mfaFIDO2AuditMessageBuilder.buildNotVerifiedAuditMessage(
			user, RandomTestUtil.randomString(), reason);
	}

}