/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.util;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Regisson Aguiar
 */
public class AuditResourceActionUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testResolveWhenAssignHasNoOwnerKey() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ASSIGN", JSONUtil.put("targetId", 1L));

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.assign", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenAssignHasOwnerId() {
		String[][] ownerAdditionalInfoKeys = {
			{"organizationId", "organization"}, {"userGroupId", "usergroup"}
		};

		for (String[] ownerAdditionalInfoKey : ownerAdditionalInfoKeys) {
			AuditMessage auditMessage = _createAuditMessage(
				User.class.getName(), "UNASSIGN",
				JSONUtil.put(
					ownerAdditionalInfoKey[0], RandomTestUtil.randomLong()));

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				ownerAdditionalInfoKey[0],
				"system." + ownerAdditionalInfoKey[1] + ".unassign",
				auditMessage.getResourceAction());
			Assert.assertEquals(
				ownerAdditionalInfoKey[0], ownerAdditionalInfoKey[1],
				auditMessage.getResourceType());
		}
	}

	@Test
	public void testResolveWhenAssignHasRoleId() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ASSIGN",
			JSONUtil.put(
				"roleId", RandomTestUtil.randomLong()
			).put(
				"roleName", RandomTestUtil.randomString()
			));

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.role.assign", auditMessage.getResourceAction());
		Assert.assertEquals("role", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenClassNameHasMultipleWords() {
		String[][] classNames = {
			{"com.liferay.portal.kernel.model.UserGroup", "usergroup"},
			{"com.liferay.object.model.ObjectDefinition", "objectdefinition"}
		};

		for (String[] className : classNames) {
			AuditMessage auditMessage = _createAuditMessage(
				className[0], "ADD", null);

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				className[0], "system." + className[1] + ".add",
				auditMessage.getResourceAction());
			Assert.assertEquals(
				className[0], className[1], auditMessage.getResourceType());
		}
	}

	@Test
	public void testResolveWhenEventTypeIsCRUDVerb() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ADD", null);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.add", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenEventTypeIsLogin() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "LOGIN", null);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.login", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenEventTypeIsOverridden() {
		String[][] eventTypeOverrides = {
			{"AGREED_TO_TERMS_OF_USE", "system.user.agree_to_terms_of_use"},
			{
				"CT_ON_DEMAND_USER_TICKET_GENERATED",
				"system.ctcollection.grant_on_demand_access"
			},
			{"DYNAMIC_REGISTRATION_ADD", "system.oauth2application.register"},
			{
				"DYNAMIC_REGISTRATION_REJECT",
				"system.oauth2application.register_reject"
			},
			{"MFA_EMAIL_OTP_NOT_VERIFIED", "system.mfa.verify_failure"},
			{"MFA_EMAIL_OTP_VERIFICATION_FAILURE", "system.mfa.verify_failure"},
			{"MFA_EMAIL_OTP_VERIFICATION_SUCCESS", "system.mfa.verify"},
			{"MFA_EMAIL_OTP_VERIFIED", "system.mfa.verify"},
			{"MFA_FIDO2_NOT_VERIFIED", "system.mfa.verify_failure"},
			{"MFA_FIDO2_VERIFICATION_FAILURE", "system.mfa.verify_failure"},
			{"MFA_FIDO2_VERIFICATION_SUCCESS", "system.mfa.verify"},
			{"MFA_FIDO2_VERIFIED", "system.mfa.verify"},
			{"MFA_IP_OTP_VERIFICATION_FAILURE", "system.mfa.verify_failure"},
			{"MFA_IP_OTP_VERIFICATION_SUCCESS", "system.mfa.verify"},
			{"MFA_TIMEBASED_OTP_NOT_VERIFIED", "system.mfa.verify_failure"},
			{
				"MFA_TIMEBASED_OTP_VERIFICATION_FAILURE",
				"system.mfa.verify_failure"
			},
			{"MFA_TIMEBASED_OTP_VERIFICATION_SUCCESS", "system.mfa.verify"},
			{"MFA_TIMEBASED_OTP_VERIFIED", "system.mfa.verify"},
			{
				"ON_DEMAND_ADMIN_TICKET_GENERATED",
				"system.user.grant_on_demand_access"
			},
			{"SCHEDULER", "system.scheduler.update"}
		};

		for (String[] eventTypeOverride : eventTypeOverrides) {
			AuditMessage auditMessage = _createAuditMessage(
				RandomTestUtil.randomString(), eventTypeOverride[0], null);

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				eventTypeOverride[0], eventTypeOverride[1],
				auditMessage.getResourceAction());
		}
	}

	@Test
	public void testResolveWhenResourceActionIsInvalid() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ADD", null);

		auditMessage.setResourceAction("Not-A-Valid-Namespace");

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"Not-A-Valid-Namespace", auditMessage.getResourceAction());
	}

	@Test
	public void testResolveWhenResourceActionIsSet() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ADD", null);

		auditMessage.setResourceAction("custom.feature.action");

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"custom.feature.action", auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenResourceTypeIsSet() {
		AuditMessage auditMessage = _createAuditMessage(
			User.class.getName(), "ADD", null);

		auditMessage.setResourceType("custom-type");

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.add", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	private AuditMessage _createAuditMessage(
		String className, String eventType,
		JSONObject additionalInfoJSONObject) {

		return new AuditMessage(
			0, RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), null, 0, additionalInfoJSONObject,
			className, RandomTestUtil.randomString(), null, eventType, null);
	}

}