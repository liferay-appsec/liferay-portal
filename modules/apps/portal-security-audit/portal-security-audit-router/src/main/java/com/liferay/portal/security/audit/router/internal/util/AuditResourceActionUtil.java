/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.util;

import com.liferay.change.tracking.constants.CTOnDemandUserConstants;
import com.liferay.on.demand.admin.constants.OnDemandAdminConstants;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.scheduler.SchedulerEngine;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Regisson Aguiar
 */
public class AuditResourceActionUtil {

	public static void resolve(AuditMessage auditMessage) {
		String resourceAction = auditMessage.getResourceAction();

		if (Validator.isNotNull(resourceAction)) {
			Matcher matcher = _resourceActionPattern.matcher(resourceAction);

			if (!matcher.matches() && _log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"An audit message producer supplied an invalid ",
						"resourceAction \"", resourceAction,
						"\" for event type ", auditMessage.getEventType()));
			}

			return;
		}

		String eventType = auditMessage.getEventType();

		String[] eventTypeOverride = _eventTypeOverrides.get(eventType);

		String resourceType;
		String action;

		if (eventTypeOverride != null) {
			resourceType = eventTypeOverride[0];
			action = eventTypeOverride[1];
		}
		else {
			resourceType = _getResourceType(auditMessage);
			action = StringUtil.toLowerCase(eventType);
		}

		auditMessage.setResourceAction(
			StringBundler.concat(
				"system.", resourceType, StringPool.PERIOD, action));
		auditMessage.setResourceType(resourceType);
	}

	private static String _getOwnerResourceType(
		JSONObject additionalInfoJSONObject) {

		if (additionalInfoJSONObject == null) {
			return null;
		}

		for (String[] ownerAdditionalInfoKey : _OWNER_ADDITIONAL_INFO_KEYS) {
			if (additionalInfoJSONObject.has(ownerAdditionalInfoKey[0])) {
				return ownerAdditionalInfoKey[1];
			}
		}

		return null;
	}

	private static String _getResourceType(AuditMessage auditMessage) {
		String eventType = auditMessage.getEventType();

		if (StringUtil.equals(eventType, EventTypes.ASSIGN) ||
			StringUtil.equals(eventType, EventTypes.UNASSIGN)) {

			String ownerResourceType = _getOwnerResourceType(
				auditMessage.getAdditionalInfo());

			if (ownerResourceType != null) {
				return ownerResourceType;
			}
		}

		return _getSimpleClassNameResourceType(auditMessage.getClassName());
	}

	private static String _getSimpleClassNameResourceType(String className) {
		if (Validator.isNull(className)) {
			return _UNKNOWN_RESOURCE_TYPE;
		}

		String simpleClassName = StringUtil.extractLast(
			className, CharPool.PERIOD);

		if (Validator.isNull(simpleClassName)) {
			return _UNKNOWN_RESOURCE_TYPE;
		}

		return StringUtil.toLowerCase(simpleClassName);
	}

	private static final String[][] _OWNER_ADDITIONAL_INFO_KEYS = {
		{"organizationId", "organization"}, {"roleId", "role"},
		{"userGroupId", "usergroup"}
	};

	private static final String _UNKNOWN_RESOURCE_TYPE = "unknown";

	private static final Log _log = LogFactoryUtil.getLog(
		AuditResourceActionUtil.class);

	private static final Map<String, String[]> _eventTypeOverrides =
		HashMapBuilder.put(
			CTOnDemandUserConstants.
				AUDIT_EVENT_TYPE_CT_ON_DEMAND_USER_TICKET_GENERATED,
			new String[] {"ctcollection", "grant_on_demand_access"}
		).put(
			EventTypes.AGREED_TO_TERMS_OF_USE,
			new String[] {"user", "agree_to_terms_of_use"}
		).put(
			OnDemandAdminConstants.
				AUDIT_EVENT_TYPE_ON_DEMAND_ADMIN_TICKET_GENERATED,
			new String[] {"user", "grant_on_demand_access"}
		).put(
			SchedulerEngine.SCHEDULER, new String[] {"scheduler", "update"}
		).put(
			"DYNAMIC_REGISTRATION_ADD",
			new String[] {"oauth2application", "register"}
		).put(
			"DYNAMIC_REGISTRATION_REJECT",
			new String[] {"oauth2application", "register_reject"}
		).put(
			"MFA_EMAIL_OTP_NOT_VERIFIED", new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_EMAIL_OTP_VERIFICATION_FAILURE",
			new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_EMAIL_OTP_VERIFICATION_SUCCESS", new String[] {"mfa", "verify"}
		).put(
			"MFA_EMAIL_OTP_VERIFIED", new String[] {"mfa", "verify"}
		).put(
			"MFA_FIDO2_NOT_VERIFIED", new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_FIDO2_VERIFICATION_FAILURE",
			new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_FIDO2_VERIFICATION_SUCCESS", new String[] {"mfa", "verify"}
		).put(
			"MFA_FIDO2_VERIFIED", new String[] {"mfa", "verify"}
		).put(
			"MFA_IP_OTP_VERIFICATION_FAILURE",
			new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_IP_OTP_VERIFICATION_SUCCESS", new String[] {"mfa", "verify"}
		).put(
			"MFA_TIMEBASED_OTP_NOT_VERIFIED",
			new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_TIMEBASED_OTP_VERIFICATION_FAILURE",
			new String[] {"mfa", "verify_failure"}
		).put(
			"MFA_TIMEBASED_OTP_VERIFICATION_SUCCESS",
			new String[] {"mfa", "verify"}
		).put(
			"MFA_TIMEBASED_OTP_VERIFIED", new String[] {"mfa", "verify"}
		).build();
	private static final Pattern _resourceActionPattern = Pattern.compile(
		"[a-z0-9_]+\\.[a-z0-9_]+\\.[a-z0-9_]+");

}