/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jorge García Jiménez
 */
public class FIPSAuditEventFactoryTest {

	@Test
	public void testCreateAlgorithmRejected() {
		String approvalType = RandomTestUtil.randomString();
		boolean enforced = RandomTestUtil.randomBoolean();
		String factoryClassName = RandomTestUtil.randomString();
		String providerName = RandomTestUtil.randomString();
		String providerVersion = RandomTestUtil.randomString();
		String rejectionReason = RandomTestUtil.randomString();
		String requestedAlgorithm = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createAlgorithmRejected(
				approvalType, enforced, factoryClassName, providerName,
				providerVersion, rejectionReason, requestedAlgorithm);

		Assert.assertEquals(
			"algorithm-rejected", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.WARNING, fipsAuditEvent.getSeverity());

		_assertFields(
			StringPool.BLANK, false, StringPool.BLANK,
			FIPSAuditEventFactory.createAlgorithmRejected(
				null, false, null, null, null, null, null),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			StringPool.BLANK);
		_assertFields(
			approvalType, enforced, factoryClassName, fipsAuditEvent,
			providerName, providerVersion, rejectionReason, requestedAlgorithm);
	}

	@Test
	public void testCreateAlgorithmServed() {
		String approvalType = RandomTestUtil.randomString();
		String factoryClassName = RandomTestUtil.randomString();
		String providerName = RandomTestUtil.randomString();
		String providerVersion = RandomTestUtil.randomString();
		String requestedAlgorithm = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createAlgorithmServed(
				approvalType, factoryClassName, providerName, providerVersion,
				requestedAlgorithm);

		Assert.assertEquals("algorithm-served", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.INFO, fipsAuditEvent.getSeverity());
		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"crypto-function-approval-type", approvalType
			).put(
				"factory-class-name", factoryClassName
			).put(
				"provider-name", providerName
			).put(
				"provider-version", providerVersion
			).put(
				"requested-algorithm", requestedAlgorithm
			).build(),
			fipsAuditEvent.getFields());
	}

	@Test
	public void testCreateAuthAttemptFailure() {
		String attemptedUserId = RandomTestUtil.randomString();
		String authenticationMethod = RandomTestUtil.randomString();
		String clientIP = RandomTestUtil.randomString();
		int consecutiveFailureCount = RandomTestUtil.randomInt();
		String failureReason = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createAuthAttemptFailure(
				attemptedUserId, authenticationMethod, clientIP,
				consecutiveFailureCount, failureReason);

		Assert.assertEquals(
			"auth-attempt-failure", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.WARNING, fipsAuditEvent.getSeverity());

		_assertFields(
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK, 0,
			StringPool.BLANK,
			FIPSAuditEventFactory.createAuthAttemptFailure(
				null, null, null, 0, null));
		_assertFields(
			attemptedUserId, authenticationMethod, clientIP,
			consecutiveFailureCount, failureReason, fipsAuditEvent);
	}

	@Test
	public void testCreateFederationTokenRejected() {
		String receivingEndpoint = RandomTestUtil.randomString();
		String rejectedValue = RandomTestUtil.randomString();
		String tokenIssuer = RandomTestUtil.randomString();
		String tokenType = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createFederationTokenRejected(
				receivingEndpoint, rejectedValue, tokenIssuer, tokenType);

		Assert.assertEquals(
			"federation-token-rejected", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.WARNING, fipsAuditEvent.getSeverity());

		_assertFields(
			FIPSAuditEventFactory.createFederationTokenRejected(
				null, null, null, null),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			StringPool.BLANK);
		_assertFields(
			fipsAuditEvent, receivingEndpoint, rejectedValue, tokenIssuer,
			tokenType);
	}

	@Test
	public void testCreateProviderFailure() {
		String approvalType = RandomTestUtil.randomString();
		String factoryClassName = RandomTestUtil.randomString();
		String providerErrorMessage = RandomTestUtil.randomString();
		String providerName = RandomTestUtil.randomString();
		String providerVersion = RandomTestUtil.randomString();
		String requestedAlgorithm = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createProviderFailure(
				approvalType, factoryClassName, providerErrorMessage,
				providerName, providerVersion, requestedAlgorithm);

		Assert.assertEquals("provider-failure", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.CRITICAL, fipsAuditEvent.getSeverity());

		_assertFields(
			StringPool.BLANK, StringPool.BLANK,
			FIPSAuditEventFactory.createProviderFailure(
				null, null, null, null, null, null),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			StringPool.BLANK);
		_assertFields(
			approvalType, factoryClassName, fipsAuditEvent,
			providerErrorMessage, providerName, providerVersion,
			requestedAlgorithm);
	}

	private void _assertFields(
		FIPSAuditEvent fipsAuditEvent, String receivingEndpoint,
		String rejectedValue, String tokenIssuer, String tokenType) {

		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"receiving-endpoint", receivingEndpoint
			).put(
				"rejected-value", rejectedValue
			).put(
				"token-issuer", tokenIssuer
			).put(
				"token-type", tokenType
			).build(),
			fipsAuditEvent.getFields());
	}

	private void _assertFields(
		String approvalType, boolean enforced, String factoryClassName,
		FIPSAuditEvent fipsAuditEvent, String providerName,
		String providerVersion, String rejectionReason,
		String requestedAlgorithm) {

		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"crypto-function-approval-type", approvalType
			).put(
				"enforced", enforced
			).put(
				"factory-class-name", factoryClassName
			).put(
				"provider-name", providerName
			).put(
				"provider-version", providerVersion
			).put(
				"rejection-reason", rejectionReason
			).put(
				"requested-algorithm", requestedAlgorithm
			).build(),
			fipsAuditEvent.getFields());
	}

	private void _assertFields(
		String approvalType, String factoryClassName,
		FIPSAuditEvent fipsAuditEvent, String providerErrorMessage,
		String providerName, String providerVersion,
		String requestedAlgorithm) {

		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"crypto-function-approval-type", approvalType
			).put(
				"factory-class-name", factoryClassName
			).put(
				"provider-error-message", providerErrorMessage
			).put(
				"provider-name", providerName
			).put(
				"provider-version", providerVersion
			).put(
				"requested-algorithm", requestedAlgorithm
			).build(),
			fipsAuditEvent.getFields());
	}

	private void _assertFields(
		String attemptedUserId, String authenticationMethod, String clientIP,
		int consecutiveFailureCount, String failureReason,
		FIPSAuditEvent fipsAuditEvent) {

		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"attempted-user-id", attemptedUserId
			).put(
				"authentication-method", authenticationMethod
			).put(
				"client-ip", clientIP
			).put(
				"consecutive-failure-count", consecutiveFailureCount
			).put(
				"failure-reason", failureReason
			).build(),
			fipsAuditEvent.getFields());
	}

}