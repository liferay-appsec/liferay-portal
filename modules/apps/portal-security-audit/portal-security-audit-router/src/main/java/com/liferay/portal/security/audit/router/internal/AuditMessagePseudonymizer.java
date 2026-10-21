/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.storage.model.AuditEventPseudonymField;
import com.liferay.portal.security.audit.storage.service.AuditEventPseudonymFieldLocalService;

/**
 * @author Christian Moura
 */
public class AuditMessagePseudonymizer {

	public void pseudonymize(AuditMessage auditMessage) {
		if (auditMessage.isPseudonymized()) {
			return;
		}

		auditMessage.setPseudonymized(true);

		AuditEventPseudonymFieldLocalService
			auditEventPseudonymFieldLocalService =
				_auditEventPseudonymFieldLocalServiceSnapshot.get();

		if (auditEventPseudonymFieldLocalService == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Audit event pseudonym field local service is unavailable");
			}

			_clear(auditMessage);

			return;
		}

		try {
			String clientIP = auditMessage.getClientIP();

			String clientIPPseudonym = _addPseudonym(
				auditEventPseudonymFieldLocalService, auditMessage, "clientIP",
				clientIP);

			String clientHost = auditMessage.getClientHost();

			if (!Validator.isBlank(clientHost)) {
				if (clientHost.equals(clientIP)) {
					auditMessage.setClientHost(clientIPPseudonym);
				}
				else {
					auditMessage.setClientHost(StringPool.BLANK);
				}
			}

			auditMessage.setClientIP(clientIPPseudonym);
			auditMessage.setImpersonatedUserEmailAddress(
				_addPseudonym(
					auditEventPseudonymFieldLocalService, auditMessage,
					"userEmailAddress",
					auditMessage.getImpersonatedUserEmailAddress()));
			auditMessage.setImpersonatedUserName(
				_addPseudonym(
					auditEventPseudonymFieldLocalService, auditMessage,
					"userName", auditMessage.getImpersonatedUserName()));
			auditMessage.setObjectName(
				_addPseudonym(
					auditEventPseudonymFieldLocalService, auditMessage,
					"objectName", auditMessage.getObjectName()));

			String userEmailAddress = auditMessage.getUserEmailAddress();

			String userEmailAddressPseudonym = _addPseudonym(
				auditEventPseudonymFieldLocalService, auditMessage,
				"userEmailAddress", userEmailAddress);

			auditMessage.setUserEmailAddress(userEmailAddressPseudonym);

			String userLogin = auditMessage.getUserLogin();

			if (!Validator.isBlank(userLogin)) {
				if (userLogin.equals(userEmailAddress)) {
					auditMessage.setUserLogin(userEmailAddressPseudonym);
				}
				else if (!userLogin.equals(
							String.valueOf(auditMessage.getUserId()))) {

					auditMessage.setUserLogin(StringPool.BLANK);
				}
			}

			auditMessage.setUserName(
				_addPseudonym(
					auditEventPseudonymFieldLocalService, auditMessage,
					"userName", auditMessage.getUserName()));
		}
		catch (Exception exception) {
			_log.error("Unable to pseudonymize the audit message", exception);

			_clear(auditMessage);
		}
	}

	private String _addPseudonym(
			AuditEventPseudonymFieldLocalService
				auditEventPseudonymFieldLocalService,
			AuditMessage auditMessage, String name, String value)
		throws PortalException {

		if (Validator.isBlank(value)) {
			return value;
		}

		AuditEventPseudonymField auditEventPseudonymField =
			auditEventPseudonymFieldLocalService.addAuditEventPseudonymField(
				auditMessage.getCompanyId(), auditMessage.getContextName(),
				name, value);

		return String.valueOf(
			auditEventPseudonymField.getAuditEventPseudonymFieldId());
	}

	private void _clear(AuditMessage auditMessage) {
		auditMessage.setClientHost(StringPool.BLANK);
		auditMessage.setClientIP(StringPool.BLANK);
		auditMessage.setImpersonatedUserEmailAddress(StringPool.BLANK);
		auditMessage.setImpersonatedUserName(StringPool.BLANK);
		auditMessage.setObjectName(StringPool.BLANK);
		auditMessage.setUserEmailAddress(StringPool.BLANK);
		auditMessage.setUserLogin(StringPool.BLANK);
		auditMessage.setUserName(StringPool.BLANK);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuditMessagePseudonymizer.class);

	private static final Snapshot<AuditEventPseudonymFieldLocalService>
		_auditEventPseudonymFieldLocalServiceSnapshot = new Snapshot<>(
			AuditMessagePseudonymizer.class,
			AuditEventPseudonymFieldLocalService.class, null, true);

}