/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.util;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.scheduler.SchedulerEngine;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
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
			JSONUtil.put(
				RandomTestUtil.randomString(), RandomTestUtil.randomLong()),
			User.class.getName(), EventTypes.ASSIGN);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.assign", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenAssignHasRoleId() {
		AuditMessage auditMessage = _createAuditMessage(
			JSONUtil.put("roleId", RandomTestUtil.randomLong()),
			User.class.getName(), EventTypes.ASSIGN);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.role.assign", auditMessage.getResourceAction());
		Assert.assertEquals("role", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenClassNameHasMultipleWords() {
		String[][] classNameResourceTypes = {
			{"com.liferay.object.model.ObjectDefinition", "objectdefinition"},
			{"com.liferay.portal.kernel.model.UserGroup", "usergroup"}
		};

		for (String[] classNameResourceType : classNameResourceTypes) {
			AuditMessage auditMessage = _createAuditMessage(
				null, classNameResourceType[0], EventTypes.ADD);

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				classNameResourceType[0],
				"system." + classNameResourceType[1] + ".add",
				auditMessage.getResourceAction());
			Assert.assertEquals(
				classNameResourceType[0], classNameResourceType[1],
				auditMessage.getResourceType());
		}
	}

	@Test
	public void testResolveWhenEventTypeIsCRUDVerb() {
		AuditMessage auditMessage = _createAuditMessage(
			null, User.class.getName(), EventTypes.ADD);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.add", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenEventTypeIsLogin() {
		AuditMessage auditMessage = _createAuditMessage(
			null, User.class.getName(), EventTypes.LOGIN);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.login", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenEventTypeIsOverridden() {
		String[][] eventTypeOverrides = {
			{
				EventTypes.AGREED_TO_TERMS_OF_USE,
				"system.user.agree_to_terms_of_use"
			},
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
			{SchedulerEngine.SCHEDULER, "system.scheduler.update"}
		};

		for (String[] eventTypeOverride : eventTypeOverrides) {
			AuditMessage auditMessage = _createAuditMessage(
				null, RandomTestUtil.randomString(), eventTypeOverride[0]);

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				eventTypeOverride[0], eventTypeOverride[1],
				auditMessage.getResourceAction());

			String[] resourceActionParts = StringUtil.split(
				eventTypeOverride[1], CharPool.PERIOD);

			Assert.assertEquals(
				eventTypeOverride[0], resourceActionParts[1],
				auditMessage.getResourceType());
		}
	}

	@Test
	public void testResolveWhenResourceActionIsInvalid() {
		AuditMessage auditMessage = _createAuditMessage(
			null, User.class.getName(), EventTypes.ADD);

		auditMessage.setResourceAction("Not-A-Valid-Namespace");

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"Not-A-Valid-Namespace", auditMessage.getResourceAction());
	}

	@Test
	public void testResolveWhenResourceActionIsSet() {
		AuditMessage auditMessage = _createAuditMessage(
			null, User.class.getName(), EventTypes.ADD);

		auditMessage.setResourceAction("custom.feature.action");

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"custom.feature.action", auditMessage.getResourceAction());
		Assert.assertNull(auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenResourceTypeIsSet() {
		AuditMessage auditMessage = _createAuditMessage(
			null, User.class.getName(), EventTypes.ADD);

		auditMessage.setResourceType(RandomTestUtil.randomString());

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			"system.user.add", auditMessage.getResourceAction());
		Assert.assertEquals("user", auditMessage.getResourceType());
	}

	@Test
	public void testResolveWhenResourceTypeIsUnknown() {
		String[] classNames = {
			"User", "com.liferay.portal.kernel.model.User.", null
		};

		for (String className : classNames) {
			AuditMessage auditMessage = _createAuditMessage(
				null, className, EventTypes.ADD);

			AuditResourceActionUtil.resolve(auditMessage);

			Assert.assertEquals(
				className, "system.unknown.add",
				auditMessage.getResourceAction());
			Assert.assertEquals(
				className, "unknown", auditMessage.getResourceType());
		}
	}

	@Test
	public void testResolveWhenUnassignHasOwnerId() {
		String[][] ownerAdditionalInfoKeys = {
			{"organizationId", "organization"}, {"userGroupId", "usergroup"}
		};

		for (String[] ownerAdditionalInfoKey : ownerAdditionalInfoKeys) {
			AuditMessage auditMessage = _createAuditMessage(
				JSONUtil.put(
					ownerAdditionalInfoKey[0], RandomTestUtil.randomLong()),
				User.class.getName(), EventTypes.UNASSIGN);

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

	private AuditMessage _createAuditMessage(
		JSONObject additionalInfoJSONObject, String className,
		String eventType) {

		return new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomLong(), additionalInfoJSONObject, className,
			RandomTestUtil.randomString(), null, eventType, null);
	}

}