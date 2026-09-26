/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rafael Praxedes
 */
public class AuditRequestTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testClone() {
		AuditRequest auditRequest = new AuditRequest();

		auditRequest.setClientHost(RandomTestUtil.randomString());
		auditRequest.setClientIP(RandomTestUtil.randomString());
		auditRequest.setCorrelationId(RandomTestUtil.randomString());
		auditRequest.setQueryString(RandomTestUtil.randomString());
		auditRequest.setRealUserEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		auditRequest.setRealUserId(RandomTestUtil.randomLong());
		auditRequest.setRealUserLogin(RandomTestUtil.randomString());
		auditRequest.setRequestId(RandomTestUtil.randomString());
		auditRequest.setRequestIdGenerated(true);
		auditRequest.setRequestURL(
			"http://" + RandomTestUtil.randomString() + "/path");
		auditRequest.setServerName(RandomTestUtil.randomString());
		auditRequest.setServerPort(RandomTestUtil.randomInt());
		auditRequest.setSessionID(RandomTestUtil.randomString());

		AuditRequest clonedAuditRequest = auditRequest.clone();

		Assert.assertNotSame(auditRequest, clonedAuditRequest);

		_assertEquals(auditRequest, clonedAuditRequest);

		String clientIP = auditRequest.getClientIP();

		clonedAuditRequest.setClientIP(RandomTestUtil.randomString());

		Assert.assertEquals(clientIP, auditRequest.getClientIP());
	}

	private void _assertEquals(
		AuditRequest expectedAuditRequest, AuditRequest actualAuditRequest) {

		Assert.assertEquals(
			expectedAuditRequest.getClientHost(),
			actualAuditRequest.getClientHost());
		Assert.assertEquals(
			expectedAuditRequest.getClientIP(),
			actualAuditRequest.getClientIP());
		Assert.assertEquals(
			expectedAuditRequest.getCorrelationId(),
			actualAuditRequest.getCorrelationId());
		Assert.assertEquals(
			expectedAuditRequest.getQueryString(),
			actualAuditRequest.getQueryString());
		Assert.assertEquals(
			expectedAuditRequest.getRealUserEmailAddress(),
			actualAuditRequest.getRealUserEmailAddress());
		Assert.assertEquals(
			expectedAuditRequest.getRealUserId(),
			actualAuditRequest.getRealUserId());
		Assert.assertEquals(
			expectedAuditRequest.getRealUserLogin(),
			actualAuditRequest.getRealUserLogin());
		Assert.assertEquals(
			expectedAuditRequest.getRequestId(),
			actualAuditRequest.getRequestId());
		Assert.assertEquals(
			expectedAuditRequest.getRequestURL(),
			actualAuditRequest.getRequestURL());
		Assert.assertEquals(
			expectedAuditRequest.getServerName(),
			actualAuditRequest.getServerName());
		Assert.assertEquals(
			expectedAuditRequest.getServerPort(),
			actualAuditRequest.getServerPort());
		Assert.assertEquals(
			expectedAuditRequest.getSessionID(),
			actualAuditRequest.getSessionID());
		Assert.assertEquals(
			expectedAuditRequest.isRequestIdGenerated(),
			actualAuditRequest.isRequestIdGenerated());
	}

}