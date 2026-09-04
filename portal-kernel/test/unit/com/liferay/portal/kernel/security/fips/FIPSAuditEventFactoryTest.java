/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jorge García Jiménez
 */
public class FIPSAuditEventFactoryTest {

	@Test
	public void testCreateFederationTokenRejected() {
		String receivingEndpoint = "/o/" + RandomTestUtil.randomString();
		String rejectedValue = "HS256";
		String tokenIssuer = RandomTestUtil.randomString();
		String tokenType = "JWT";

		FIPSAuditEvent fipsAuditEvent =
			FIPSAuditEventFactory.createFederationTokenRejected(
				receivingEndpoint, rejectedValue, tokenIssuer, tokenType);

		Assert.assertEquals(
			"federation-token-rejected", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			FIPSAuditEvent.Severity.WARNING, fipsAuditEvent.getSeverity());

		_assertFields(
			fipsAuditEvent, receivingEndpoint, rejectedValue, tokenIssuer,
			tokenType);

		_assertFields(
			FIPSAuditEventFactory.createFederationTokenRejected(
				null, null, null, null),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			StringPool.BLANK);
	}

	private void _assertFields(
		FIPSAuditEvent fipsAuditEvent, String receivingEndpoint,
		String rejectedValue, String tokenIssuer, String tokenType) {

		Map<String, Object> fields = fipsAuditEvent.getFields();

		Assert.assertEquals(fields.toString(), 4, fields.size());
		Assert.assertEquals(
			receivingEndpoint, fields.get("receiving-endpoint"));
		Assert.assertEquals(rejectedValue, fields.get("rejected-value"));
		Assert.assertEquals(tokenIssuer, fields.get("token-issuer"));
		Assert.assertEquals(tokenType, fields.get("token-type"));
	}

}