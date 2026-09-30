/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.text.DateFormat;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Regisson Aguiar
 * @author Álvaro Saugar
 */
public class AuditMessageTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() {
		AuditRequestContextThreadLocal.removeAuditRequestContext();
	}

	@Test
	public void testConstructor() throws Exception {
		AuditRequestContext auditRequestContext =
			AuditRequestContextThreadLocal.getAuditRequestContext();

		String correlationId = RandomTestUtil.randomString();

		auditRequestContext.setCorrelationId(correlationId);

		String requestId = RandomTestUtil.randomString();

		auditRequestContext.setRequestId(requestId);

		auditRequestContext.setRequestIdGenerated(true);

		AuditMessage auditMessage = new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
			RandomTestUtil.nextDate(), JSONFactoryUtil.createJSONObject(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		Assert.assertEquals(correlationId, auditMessage.getCorrelationId());
		Assert.assertEquals(requestId, auditMessage.getRequestId());
		Assert.assertTrue(auditMessage.isRequestIdGenerated());
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testConstructorDoesNotResolveRequestIdWhenFeatureFlagIsDisabled()
		throws Exception {

		AuditMessage auditMessage = _createAuditMessage(
			RandomTestUtil.randomLong());

		Assert.assertNull(auditMessage.getRequestId());
		Assert.assertFalse(auditMessage.isRequestIdGenerated());
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testConstructorResolvesOneRequestIdPerScope() {
		long companyId = RandomTestUtil.randomLong();

		AuditMessage auditMessage1 = _createAuditMessage(companyId);

		String requestId = auditMessage1.getRequestId();

		Assert.assertNotNull(requestId);

		AuditMessage auditMessage2 = null;

		try (SafeCloseable safeCloseable =
				AuditRequestContextThreadLocal.
					setAuditRequestContextWithSafeCloseable(
						new AuditRequestContext())) {

			auditMessage2 = _createAuditMessage(companyId);
		}

		Assert.assertNotNull(auditMessage2.getRequestId());
		Assert.assertNotEquals(requestId, auditMessage2.getRequestId());
		Assert.assertTrue(auditMessage2.isRequestIdGenerated());

		AuditMessage auditMessage3 = _createAuditMessage(companyId);

		Assert.assertEquals(requestId, auditMessage3.getRequestId());
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testConstructorResolvesOneRequestIdPerThread()
		throws Exception {

		AuditMessage auditMessage1 = _createAuditMessage(
			RandomTestUtil.randomLong());
		AuditMessage auditMessage2 = _createAuditMessage(
			RandomTestUtil.randomLong());

		Assert.assertTrue(auditMessage1.isRequestIdGenerated());
		Assert.assertTrue(auditMessage2.isRequestIdGenerated());

		String requestId = auditMessage1.getRequestId();

		Assert.assertNotNull(requestId);
		Assert.assertEquals(requestId, auditMessage2.getRequestId());

		AuditRequestContextThreadLocal.removeAuditRequestContext();

		AuditMessage auditMessage3 = _createAuditMessage(
			CompanyConstants.SYSTEM);

		Assert.assertNull(auditMessage3.getRequestId());
		Assert.assertFalse(auditMessage3.isRequestIdGenerated());
	}

	@Test
	public void testToJSONObject() throws Exception {
		long groupId = RandomTestUtil.randomLong();
		Date timestampDate = RandomTestUtil.nextDate();

		AuditMessage auditMessage = new AuditMessage(
			groupId, RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), timestampDate,
			JSONFactoryUtil.createJSONObject(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		JSONObject jsonObject = auditMessage.toJSONObject();

		DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyyMMddkkmmssSSS");

		Assert.assertEquals(groupId, jsonObject.getLong("groupId"));
		Assert.assertEquals(
			dateFormat.format(timestampDate),
			jsonObject.getString("timestamp"));

		auditMessage.setTimestampDate(null);

		jsonObject = auditMessage.toJSONObject();

		Assert.assertNotNull(jsonObject.getString("timestamp"));

		auditMessage = new AuditMessage(jsonObject.toString());

		Assert.assertEquals(groupId, auditMessage.getGroupId());
		Assert.assertNotNull(auditMessage.getTimestampDate());
	}

	private AuditMessage _createAuditMessage(long companyId) {
		return new AuditMessage(
			RandomTestUtil.randomLong(), companyId, RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), RandomTestUtil.nextDate(),
			JSONFactoryUtil.createJSONObject(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString());
	}

}