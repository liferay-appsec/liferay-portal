/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.text.DateFormat;

import java.util.Date;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Regisson Aguiar
 */
public class AuditMessageTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testConstructor() throws Exception {
		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		String correlationId = RandomTestUtil.randomString();

		auditRequestThreadLocal.setCorrelationId(correlationId);

		String requestId = RandomTestUtil.randomString();

		auditRequestThreadLocal.setRequestId(requestId);

		auditRequestThreadLocal.setRequestIdGenerated(true);

		AuditMessage auditMessage = new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
			RandomTestUtil.nextDate(), JSONFactoryUtil.createJSONObject(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		Assert.assertEquals(correlationId, auditMessage.getCorrelationId());
		Assert.assertEquals(requestId, auditMessage.getRequestId());
		Assert.assertTrue(auditMessage.isRequestIdGenerated());

		AuditRequestThreadLocal.removeAuditThreadLocal();
	}

	@Test
	public void testConstructorResourceAction() throws Exception {
		_testConstructorResourceAction(
			"User", "ADD", "system.user.add", "user");
		_testConstructorResourceAction(
			"com.liferay.object.model.ObjectDefinition", "ADD",
			"system.objectdefinition.add", "objectdefinition");
		_testConstructorResourceAction(
			"com.liferay.object.model.ObjectDefinition#CMPProject", "ADD",
			"system.objectdefinition.add", "objectdefinition");
		_testConstructorResourceAction(
			"com.liferay.portal.kernel.model.User", "ADD", "system.user.add",
			"user");
		_testConstructorResourceAction(
			"com.liferay.portal.kernel.model.User", "LOGIN",
			"system.user.login", "user");
		_testConstructorResourceAction(
			"com.liferay.portal.kernel.model.User", null, "system.user.unknown",
			"user");
		_testConstructorResourceAction(
			"com.liferay.portal.kernel.model.User.", "ADD",
			"system.unknown.add", "unknown");
		_testConstructorResourceAction(
			"com.liferay.portal.kernel.model.UserGroup", "ADD",
			"system.usergroup.add", "usergroup");
		_testConstructorResourceAction(
			null, "ADD", "system.unknown.add", "unknown");
	}

	@Test
	public void testSetResourceAction() throws Exception {
		_testSetResourceAction("", "system");
		_testSetResourceAction("AI_HUB", "ai_hub");
		_testSetResourceAction(null, "system");
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

		auditMessage.setResourceAction("verify_pending");
		auditMessage.setResourceType("mfa");

		JSONObject jsonObject = auditMessage.toJSONObject();

		DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyyMMddkkmmssSSS");

		Assert.assertEquals(groupId, jsonObject.getLong("groupId"));
		Assert.assertEquals(
			"system.mfa.verify_pending",
			jsonObject.getString("resourceAction"));
		Assert.assertEquals("mfa", jsonObject.getString("resourceType"));
		Assert.assertEquals(
			dateFormat.format(timestampDate),
			jsonObject.getString("timestamp"));

		auditMessage.setTimestampDate(null);

		jsonObject = auditMessage.toJSONObject();

		Assert.assertNotNull(jsonObject.getString("timestamp"));

		auditMessage = new AuditMessage(jsonObject.toString());

		Assert.assertEquals(groupId, auditMessage.getGroupId());
		Assert.assertEquals(
			"system.mfa.verify_pending", auditMessage.getResourceAction());
		Assert.assertEquals("mfa", auditMessage.getResourceType());
		Assert.assertNotNull(auditMessage.getTimestampDate());
	}

	private void _testConstructorResourceAction(
		String className, String eventType, String expectedResourceAction,
		String expectedResourceType) {

		AuditMessage auditMessage = new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(), null,
			JSONFactoryUtil.createJSONObject(), className,
			RandomTestUtil.randomString(), eventType, null);

		Assert.assertEquals(
			expectedResourceAction, auditMessage.getResourceAction());
		Assert.assertEquals(
			expectedResourceType, auditMessage.getResourceType());
	}

	private void _testSetResourceAction(
		String contextName, String expectedContextName) {

		AuditMessage auditMessage = new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(), null, 0,
			JSONFactoryUtil.createJSONObject(),
			"com.liferay.portal.kernel.model.User",
			RandomTestUtil.randomString(), contextName, "ADD", null);

		Assert.assertEquals(
			expectedContextName + ".user.add",
			auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());

		auditMessage.setResourceAction("verify_pending");
		auditMessage.setResourceType("mfa");

		Assert.assertEquals(
			expectedContextName + ".mfa.verify_pending",
			auditMessage.getResourceAction());
		Assert.assertEquals("mfa", auditMessage.getResourceType());
	}

}