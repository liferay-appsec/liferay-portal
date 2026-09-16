/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

/**
 * @author Michael C. Han
 */
public class AuditRequestThreadLocal {

	public static AuditRequestThreadLocal getAuditThreadLocal() {
		AuditRequestThreadLocal auditRequestThreadLocal = _auditRequest.get();

		if (auditRequestThreadLocal == null) {
			auditRequestThreadLocal = new AuditRequestThreadLocal();

			_auditRequest.set(auditRequestThreadLocal);
		}

		return auditRequestThreadLocal;
	}

	public static void removeAuditThreadLocal() {
		_auditRequest.remove();
	}

	public static SafeCloseable setNewAuditThreadLocalWithSafeCloseable() {
		return _auditRequest.setWithSafeCloseable(
			new AuditRequestThreadLocal());
	}

	public String getClientHost() {
		return _clientHost;
	}

	public String getClientIP() {
		return _clientIP;
	}

	public String getCorrelationId() {
		return _correlationId;
	}

	public String getQueryString() {
		return _queryString;
	}

	public String getRealUserEmailAddress() {
		return _realUserEmailAddress;
	}

	public long getRealUserId() {
		return _realUserId;
	}

	public String getRealUserLogin() {
		return _realUserLogin;
	}

	public String getRequestId() {
		return _requestId;
	}

	public String getRequestURL() {
		return _requestURL;
	}

	public String getServerName() {
		return _serverName;
	}

	public int getServerPort() {
		return _serverPort;
	}

	public String getSessionID() {
		return _sessionID;
	}

	public boolean isRequestIdGenerated() {
		return _requestIdGenerated;
	}

	public String resolveRequestId(long companyId) {
		if ((_requestId == null) && (companyId > CompanyConstants.SYSTEM) &&
			FeatureFlagManagerUtil.isEnabled(companyId, "LPD-6417")) {

			_requestId = PortalUUIDUtil.generate();
			_requestIdGenerated = true;
		}

		return _requestId;
	}

	public void setClientHost(String clientHost) {
		_clientHost = clientHost;
	}

	public void setClientIP(String clientIP) {
		_clientIP = clientIP;
	}

	public void setCorrelationId(String correlationId) {
		_correlationId = correlationId;
	}

	public void setQueryString(String queryString) {
		_queryString = queryString;
	}

	public void setRealUserEmailAddress(String realUserEmailAddress) {
		_realUserEmailAddress = realUserEmailAddress;
	}

	public void setRealUserId(long realUserId) {
		_realUserId = realUserId;
	}

	public void setRealUserLogin(String realUserLogin) {
		_realUserLogin = realUserLogin;
	}

	public void setRequestId(String requestId) {
		_requestId = requestId;
	}

	public void setRequestIdGenerated(boolean requestIdGenerated) {
		_requestIdGenerated = requestIdGenerated;
	}

	public void setRequestURL(String requestURL) {
		_requestURL = requestURL;
	}

	public void setServerName(String serverName) {
		_serverName = serverName;
	}

	public void setServerPort(int serverPort) {
		_serverPort = serverPort;
	}

	public void setSessionID(String sessionID) {
		_sessionID = sessionID;
	}

	private static final CentralizedThreadLocal<AuditRequestThreadLocal>
		_auditRequest = new CentralizedThreadLocal<>(
			AuditRequestThreadLocal.class + "._auditRequest");

	private String _clientHost;
	private String _clientIP;
	private String _correlationId;
	private String _queryString;
	private String _realUserEmailAddress;
	private long _realUserId;
	private String _realUserLogin;
	private String _requestId;
	private boolean _requestIdGenerated;
	private String _requestURL;
	private String _serverName;
	private int _serverPort;
	private String _sessionID;

}