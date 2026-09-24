/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.storage.constants.AuditPseudonymConstants;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Christian Moura
 */
public class AuditMessagePseudonymizer {

	public void pseudonymize(AuditMessage auditMessage) {
		if (auditMessage.isPseudonymized()) {
			return;
		}

		auditMessage.setPseudonymized(true);

		AuditPseudonymLocalService auditPseudonymLocalService =
			_auditPseudonymLocalServiceSnapshot.get();

		if (auditPseudonymLocalService == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Audit pseudonym local service is unavailable");
			}

			_mask(auditMessage);

			return;
		}

		try {
			long companyId = auditMessage.getCompanyId();
			String contextName = auditMessage.getContextName();

			String clientIP = auditMessage.getClientIP();

			String clientIPReference = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_CLIENT_IP, clientIP);

			String impersonatedUserEmailAddressToken = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_EMAIL_ADDRESS,
				auditMessage.getImpersonatedUserEmailAddress());

			long impersonatedUserId = auditMessage.getImpersonatedUserId();

			long impersonatedUserAuditPseudonymId = _getAuditPseudonymId(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_ID,
				impersonatedUserId);

			String impersonatedUserNameToken = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_NAME,
				auditMessage.getImpersonatedUserName());
			String objectNameToken = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_OBJECT_NAME,
				auditMessage.getObjectName());

			String userEmailAddress = auditMessage.getUserEmailAddress();

			String userEmailAddressToken = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_EMAIL_ADDRESS,
				userEmailAddress);

			long userId = auditMessage.getUserId();

			long userAuditPseudonymId = _getAuditPseudonymId(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_ID, userId);

			String userNameToken = _getToken(
				auditPseudonymLocalService, companyId, contextName,
				AuditPseudonymConstants.FIELD_CATEGORY_USER_NAME,
				auditMessage.getUserName());

			String reducedClientIP = _reduceClientIP(clientIP);

			auditMessage.setClassPK(
				_getClassPK(
					auditMessage.getClassPK(), impersonatedUserAuditPseudonymId,
					impersonatedUserId, userAuditPseudonymId, userId));
			auditMessage.setClientHost(
				_getClientHost(
					auditMessage.getClientHost(), clientIP, reducedClientIP));
			auditMessage.setClientIP(reducedClientIP);
			auditMessage.setClientIPReference(clientIPReference);
			auditMessage.setImpersonatedUserEmailAddress(
				impersonatedUserEmailAddressToken);
			auditMessage.setImpersonatedUserId(
				impersonatedUserAuditPseudonymId);
			auditMessage.setImpersonatedUserName(impersonatedUserNameToken);
			auditMessage.setObjectName(objectNameToken);
			auditMessage.setUserEmailAddress(userEmailAddressToken);
			auditMessage.setUserId(userAuditPseudonymId);
			auditMessage.setUserLogin(
				_getUserLogin(
					userAuditPseudonymId, userEmailAddress,
					userEmailAddressToken, userId,
					auditMessage.getUserLogin()));
			auditMessage.setUserName(userNameToken);
		}
		catch (Exception exception) {
			_log.error("Unable to pseudonymize the audit message", exception);

			_mask(auditMessage);
		}
	}

	private long _getAuditPseudonymId(
		AuditPseudonymLocalService auditPseudonymLocalService, long companyId,
		String contextName, String fieldCategory, long value) {

		if (value <= 0) {
			return value;
		}

		AuditPseudonym auditPseudonym =
			auditPseudonymLocalService.getOrAddAuditPseudonym(
				companyId, contextName, fieldCategory, String.valueOf(value));

		return auditPseudonym.getAuditPseudonymId();
	}

	private String _getClassPK(
		String classPK, long impersonatedUserAuditPseudonymId,
		long impersonatedUserId, long userAuditPseudonymId, long userId) {

		if (_isUserId(classPK, userId)) {
			return String.valueOf(userAuditPseudonymId);
		}

		if (_isUserId(classPK, impersonatedUserId)) {
			return String.valueOf(impersonatedUserAuditPseudonymId);
		}

		return classPK;
	}

	private String _getClientHost(
		String clientHost, String clientIP, String reducedClientIP) {

		if (Validator.isBlank(clientHost)) {
			return clientHost;
		}

		if (clientHost.equals(clientIP)) {
			return reducedClientIP;
		}

		return StringPool.BLANK;
	}

	private String _getToken(
		AuditPseudonymLocalService auditPseudonymLocalService, long companyId,
		String contextName, String fieldCategory, String value) {

		if (Validator.isBlank(value)) {
			return value;
		}

		AuditPseudonym auditPseudonym =
			auditPseudonymLocalService.getOrAddAuditPseudonym(
				companyId, contextName, fieldCategory, value);

		return String.valueOf(auditPseudonym.getAuditPseudonymId());
	}

	private String _getUserLogin(
		long userAuditPseudonymId, String userEmailAddress,
		String userEmailAddressToken, long userId, String userLogin) {

		if (Validator.isBlank(userLogin)) {
			return userLogin;
		}

		if (userLogin.equals(userEmailAddress)) {
			return userEmailAddressToken;
		}

		if (_isUserId(userLogin, userId)) {
			return String.valueOf(userAuditPseudonymId);
		}

		return StringPool.BLANK;
	}

	private boolean _isUserId(String value, long userId) {
		if ((userId > 0) && Objects.equals(value, String.valueOf(userId))) {
			return true;
		}

		return false;
	}

	private void _mask(AuditMessage auditMessage) {
		String classPK = auditMessage.getClassPK();

		if (_isUserId(classPK, auditMessage.getImpersonatedUserId()) ||
			_isUserId(classPK, auditMessage.getUserId())) {

			auditMessage.setClassPK(StringPool.BLANK);
		}

		auditMessage.setClientHost(StringPool.BLANK);
		auditMessage.setClientIP(StringPool.BLANK);
		auditMessage.setClientIPReference(StringPool.BLANK);
		auditMessage.setImpersonatedUserEmailAddress(StringPool.BLANK);
		auditMessage.setImpersonatedUserId(0);
		auditMessage.setImpersonatedUserName(StringPool.BLANK);
		auditMessage.setObjectName(StringPool.BLANK);
		auditMessage.setPseudonymizationFailed(true);
		auditMessage.setUserEmailAddress(StringPool.BLANK);
		auditMessage.setUserId(0);
		auditMessage.setUserLogin(StringPool.BLANK);
		auditMessage.setUserName(StringPool.BLANK);
	}

	private String _reduceClientIP(String clientIP) {
		if (Validator.isBlank(clientIP)) {
			return clientIP;
		}

		if (!Validator.isIPAddress(clientIP)) {
			return StringPool.BLANK;
		}

		String hostAddress = clientIP;

		if (hostAddress.startsWith(StringPool.OPEN_BRACKET) &&
			hostAddress.endsWith(StringPool.CLOSE_BRACKET)) {

			hostAddress = hostAddress.substring(1, hostAddress.length() - 1);
		}

		int index = hostAddress.indexOf(CharPool.PERCENT);

		if (index != -1) {
			hostAddress = hostAddress.substring(0, index);
		}

		hostAddress = hostAddress.trim();

		try {
			InetAddress inetAddress = InetAddress.getByName(hostAddress);

			int prefixLength = 24;

			if (inetAddress instanceof Inet6Address) {
				prefixLength = 64;
			}

			byte[] bytes = inetAddress.getAddress();

			Arrays.fill(bytes, prefixLength / 8, bytes.length, (byte)0);

			InetAddress reducedInetAddress = InetAddress.getByAddress(bytes);

			return reducedInetAddress.getHostAddress() + StringPool.SLASH +
				prefixLength;
		}
		catch (UnknownHostException unknownHostException) {
			if (_log.isDebugEnabled()) {
				_log.debug(unknownHostException);
			}

			return StringPool.BLANK;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuditMessagePseudonymizer.class);

	private static final Snapshot<AuditPseudonymLocalService>
		_auditPseudonymLocalServiceSnapshot = new Snapshot<>(
			AuditMessagePseudonymizer.class, AuditPseudonymLocalService.class,
			null, true);

}