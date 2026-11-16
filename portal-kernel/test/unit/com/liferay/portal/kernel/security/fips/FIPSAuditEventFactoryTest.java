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
	public void testCreatePasswordRehash() {
		String fromAlgorithm = RandomTestUtil.randomString();
		String toAlgorithm = RandomTestUtil.randomString();
		String userId = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createPasswordRehash(
				fromAlgorithm, toAlgorithm, userId);

		Assert.assertEquals("password-rehash", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.INFO, fipsAuditEvent.getSeverity());

		_assertFields(
			FIPSAuditEventFactory.createPasswordRehash(null, null, null),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK);
		_assertFields(fipsAuditEvent, fromAlgorithm, toAlgorithm, userId);
	}

	@Test
	public void testCreatePasswordResetForced() {
		String userId = RandomTestUtil.randomString();

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createPasswordResetForced(userId);

		Assert.assertEquals(
			"password-reset-forced", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.WARNING, fipsAuditEvent.getSeverity());

		_assertFields(
			FIPSAuditEventFactory.createPasswordResetForced(null),
			StringPool.BLANK);
		_assertFields(fipsAuditEvent, userId);
	}

	private void _assertFields(FIPSAuditEvent fipsAuditEvent, String userId) {
		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"user-id", userId
			).build(),
			fipsAuditEvent.getFields());
	}

	private void _assertFields(
		FIPSAuditEvent fipsAuditEvent, String fromAlgorithm, String toAlgorithm,
		String userId) {

		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"from-algorithm", fromAlgorithm
			).put(
				"to-algorithm", toAlgorithm
			).put(
				"user-id", userId
			).build(),
			fipsAuditEvent.getFields());
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