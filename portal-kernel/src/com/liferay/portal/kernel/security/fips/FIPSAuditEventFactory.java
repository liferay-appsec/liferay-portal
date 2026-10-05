/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Jorge García Jiménez
 */
public class FIPSAuditEventFactory {

	public static FIPSAuditEvent createAlgorithmRejected(
		boolean enforced, String factoryClassName, String providerName,
		String providerVersion, String rejectionReason,
		String requestedAlgorithm) {

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"algorithm-rejected", FIPSAuditEvent.Severity.WARNING);

		fipsAuditEvent.put("enforced", enforced);
		fipsAuditEvent.put(
			"factory-class-name", GetterUtil.getString(factoryClassName));
		fipsAuditEvent.put("provider-name", GetterUtil.getString(providerName));
		fipsAuditEvent.put(
			"provider-version", GetterUtil.getString(providerVersion));
		fipsAuditEvent.put(
			"rejection-reason", GetterUtil.getString(rejectionReason));
		fipsAuditEvent.put(
			"requested-algorithm", GetterUtil.getString(requestedAlgorithm));

		return fipsAuditEvent;
	}

	public static FIPSAuditEvent createAlgorithmServed(
		String factoryClassName, String providerName, String providerVersion,
		String requestedAlgorithm) {

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"algorithm-served", FIPSAuditEvent.Severity.INFO);

		fipsAuditEvent.put(
			"factory-class-name", GetterUtil.getString(factoryClassName));
		fipsAuditEvent.put("provider-name", GetterUtil.getString(providerName));
		fipsAuditEvent.put(
			"provider-version", GetterUtil.getString(providerVersion));
		fipsAuditEvent.put(
			"requested-algorithm", GetterUtil.getString(requestedAlgorithm));

		return fipsAuditEvent;
	}

	public static FIPSAuditEvent createAuthAttemptFailure(
		String attemptedUserId, String authenticationMethod, String clientIP,
		int consecutiveFailureCount, String failureReason) {

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"auth-attempt-failure", FIPSAuditEvent.Severity.WARNING);

		fipsAuditEvent.put(
			"attempted-user-id", GetterUtil.getString(attemptedUserId));
		fipsAuditEvent.put(
			"authentication-method",
			GetterUtil.getString(authenticationMethod));
		fipsAuditEvent.put("client-ip", GetterUtil.getString(clientIP));
		fipsAuditEvent.put(
			"consecutive-failure-count", consecutiveFailureCount);
		fipsAuditEvent.put(
			"failure-reason", GetterUtil.getString(failureReason));

		return fipsAuditEvent;
	}

	public static FIPSAuditEvent createFederationTokenRejected(
		String receivingEndpoint, String rejectedValue, String tokenIssuer,
		String tokenType) {

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"federation-token-rejected", FIPSAuditEvent.Severity.WARNING);

		fipsAuditEvent.put(
			"receiving-endpoint", GetterUtil.getString(receivingEndpoint));
		fipsAuditEvent.put(
			"rejected-value", GetterUtil.getString(rejectedValue));
		fipsAuditEvent.put("token-issuer", GetterUtil.getString(tokenIssuer));
		fipsAuditEvent.put("token-type", GetterUtil.getString(tokenType));

		return fipsAuditEvent;
	}

	public static FIPSAuditEvent createProviderFailure(
		String factoryClassName, String providerErrorMessage,
		String providerName, String providerVersion,
		String requestedAlgorithm) {

		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			"provider-failure", FIPSAuditEvent.Severity.CRITICAL);

		fipsAuditEvent.put(
			"factory-class-name", GetterUtil.getString(factoryClassName));
		fipsAuditEvent.put(
			"provider-error-message",
			GetterUtil.getString(providerErrorMessage));
		fipsAuditEvent.put("provider-name", GetterUtil.getString(providerName));
		fipsAuditEvent.put(
			"provider-version", GetterUtil.getString(providerVersion));
		fipsAuditEvent.put(
			"requested-algorithm", GetterUtil.getString(requestedAlgorithm));

		return fipsAuditEvent;
	}

}