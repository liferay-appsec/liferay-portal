/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.audit;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christian Moura
 */
public class AuditRequestThreadLocalTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() {
		AuditRequestThreadLocal.removeAuditThreadLocal();
	}

	@Test
	public void testGetCorrelationId() {
		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(auditRequestThreadLocal.getCorrelationId());

		String correlationId = RandomTestUtil.randomString();

		auditRequestThreadLocal.setCorrelationId(correlationId);

		Assert.assertEquals(
			correlationId, auditRequestThreadLocal.getCorrelationId());

		String newCorrelationId = RandomTestUtil.randomString();

		auditRequestThreadLocal.setCorrelationId(newCorrelationId);

		Assert.assertEquals(
			newCorrelationId, auditRequestThreadLocal.getCorrelationId());
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testResolveRequestId() {
		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(auditRequestThreadLocal.getRequestId());
		Assert.assertFalse(auditRequestThreadLocal.isRequestIdGenerated());

		String requestId1 = auditRequestThreadLocal.resolveRequestId(
			RandomTestUtil.randomLong());

		Assert.assertNotNull(requestId1);
		Assert.assertEquals(requestId1, auditRequestThreadLocal.getRequestId());
		Assert.assertEquals(
			requestId1,
			auditRequestThreadLocal.resolveRequestId(
				RandomTestUtil.randomLong()));

		Assert.assertTrue(auditRequestThreadLocal.isRequestIdGenerated());

		AuditRequestThreadLocal.removeAuditThreadLocal();

		auditRequestThreadLocal = AuditRequestThreadLocal.getAuditThreadLocal();

		String requestId2 = RandomTestUtil.randomString();

		auditRequestThreadLocal.setRequestId(requestId2);

		Assert.assertEquals(
			requestId2,
			auditRequestThreadLocal.resolveRequestId(
				RandomTestUtil.randomLong()));

		Assert.assertFalse(auditRequestThreadLocal.isRequestIdGenerated());

		AuditRequestThreadLocal.removeAuditThreadLocal();

		auditRequestThreadLocal = AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(
			auditRequestThreadLocal.resolveRequestId(CompanyConstants.SYSTEM));
	}

	@Test
	public void testSetNewAuditThreadLocalWithSafeCloseable() {
		AuditRequestThreadLocal auditRequestThreadLocal1 =
			AuditRequestThreadLocal.getAuditThreadLocal();

		String correlationId = RandomTestUtil.randomString();
		String requestId = RandomTestUtil.randomString();
		String sessionID = RandomTestUtil.randomString();

		auditRequestThreadLocal1.setCorrelationId(correlationId);
		auditRequestThreadLocal1.setRequestId(requestId);
		auditRequestThreadLocal1.setSessionID(sessionID);

		try (SafeCloseable safeCloseable =
				AuditRequestThreadLocal.
					setNewAuditThreadLocalWithSafeCloseable()) {

			AuditRequestThreadLocal auditRequestThreadLocal2 =
				AuditRequestThreadLocal.getAuditThreadLocal();

			Assert.assertNull(auditRequestThreadLocal2.getCorrelationId());
			Assert.assertNull(auditRequestThreadLocal2.getRequestId());
			Assert.assertNull(auditRequestThreadLocal2.getSessionID());
		}

		AuditRequestThreadLocal auditRequestThreadLocal3 =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertEquals(
			correlationId, auditRequestThreadLocal3.getCorrelationId());
		Assert.assertEquals(requestId, auditRequestThreadLocal3.getRequestId());
		Assert.assertEquals(sessionID, auditRequestThreadLocal3.getSessionID());

		AuditRequestThreadLocal.removeAuditThreadLocal();

		try (SafeCloseable safeCloseable =
				AuditRequestThreadLocal.
					setNewAuditThreadLocalWithSafeCloseable()) {

			AuditRequestThreadLocal auditRequestThreadLocal4 =
				AuditRequestThreadLocal.getAuditThreadLocal();

			auditRequestThreadLocal4.setRequestId(
				RandomTestUtil.randomString());
		}

		AuditRequestThreadLocal auditRequestThreadLocal5 =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(auditRequestThreadLocal5.getRequestId());
	}

}