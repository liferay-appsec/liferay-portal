/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rafael Praxedes
 * @author Álvaro Saugar
 */
public class AuditRequestContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testClone() {
		AuditRequestContext auditRequestContext = new AuditRequestContext();

		auditRequestContext.setClientHost(RandomTestUtil.randomString());
		auditRequestContext.setClientIP(RandomTestUtil.randomString());
		auditRequestContext.setCorrelationId(RandomTestUtil.randomString());
		auditRequestContext.setQueryString(RandomTestUtil.randomString());
		auditRequestContext.setRealUserEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		auditRequestContext.setRealUserId(RandomTestUtil.randomLong());
		auditRequestContext.setRealUserLogin(RandomTestUtil.randomString());
		auditRequestContext.setRequestId(RandomTestUtil.randomString());
		auditRequestContext.setRequestIdGenerated(true);
		auditRequestContext.setRequestURL(
			"http://" + RandomTestUtil.randomString() + "/path");
		auditRequestContext.setServerName(RandomTestUtil.randomString());
		auditRequestContext.setServerPort(RandomTestUtil.randomInt());
		auditRequestContext.setSessionID(RandomTestUtil.randomString());

		AuditRequestContext clonedAuditRequestContext =
			auditRequestContext.clone();

		Assert.assertNotSame(auditRequestContext, clonedAuditRequestContext);

		_assertEquals(auditRequestContext, clonedAuditRequestContext);

		String clientIP = auditRequestContext.getClientIP();

		clonedAuditRequestContext.setClientIP(RandomTestUtil.randomString());

		Assert.assertEquals(clientIP, auditRequestContext.getClientIP());
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testResolveRequestId() {
		AuditRequestContext auditRequestContext = new AuditRequestContext();

		Assert.assertNull(auditRequestContext.getRequestId());
		Assert.assertFalse(auditRequestContext.isRequestIdGenerated());

		String requestId1 = auditRequestContext.resolveRequestId(
			RandomTestUtil.randomLong());

		Assert.assertNotNull(requestId1);

		Assert.assertEquals(requestId1, auditRequestContext.getRequestId());
		Assert.assertEquals(
			requestId1,
			auditRequestContext.resolveRequestId(RandomTestUtil.randomLong()));

		Assert.assertTrue(auditRequestContext.isRequestIdGenerated());

		auditRequestContext = new AuditRequestContext();

		String requestId2 = RandomTestUtil.randomString();

		auditRequestContext.setRequestId(requestId2);

		Assert.assertEquals(
			requestId2,
			auditRequestContext.resolveRequestId(RandomTestUtil.randomLong()));

		Assert.assertFalse(auditRequestContext.isRequestIdGenerated());

		auditRequestContext = new AuditRequestContext();

		Assert.assertNull(
			auditRequestContext.resolveRequestId(CompanyConstants.SYSTEM));
	}

	private void _assertEquals(
		AuditRequestContext expectedAuditRequestContext,
		AuditRequestContext actualAuditRequestContext) {

		Assert.assertEquals(
			expectedAuditRequestContext.getClientHost(),
			actualAuditRequestContext.getClientHost());
		Assert.assertEquals(
			expectedAuditRequestContext.getClientIP(),
			actualAuditRequestContext.getClientIP());
		Assert.assertEquals(
			expectedAuditRequestContext.getCorrelationId(),
			actualAuditRequestContext.getCorrelationId());
		Assert.assertEquals(
			expectedAuditRequestContext.getQueryString(),
			actualAuditRequestContext.getQueryString());
		Assert.assertEquals(
			expectedAuditRequestContext.getRealUserEmailAddress(),
			actualAuditRequestContext.getRealUserEmailAddress());
		Assert.assertEquals(
			expectedAuditRequestContext.getRealUserId(),
			actualAuditRequestContext.getRealUserId());
		Assert.assertEquals(
			expectedAuditRequestContext.getRealUserLogin(),
			actualAuditRequestContext.getRealUserLogin());
		Assert.assertEquals(
			expectedAuditRequestContext.getRequestId(),
			actualAuditRequestContext.getRequestId());
		Assert.assertEquals(
			expectedAuditRequestContext.getRequestURL(),
			actualAuditRequestContext.getRequestURL());
		Assert.assertEquals(
			expectedAuditRequestContext.getServerName(),
			actualAuditRequestContext.getServerName());
		Assert.assertEquals(
			expectedAuditRequestContext.getServerPort(),
			actualAuditRequestContext.getServerPort());
		Assert.assertEquals(
			expectedAuditRequestContext.getSessionID(),
			actualAuditRequestContext.getSessionID());
		Assert.assertEquals(
			expectedAuditRequestContext.isRequestIdGenerated(),
			actualAuditRequestContext.isRequestIdGenerated());
	}

}