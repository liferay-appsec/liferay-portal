/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.util;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Regisson Aguiar
 */
public class AuditResourceActionUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testResolve() {
		_testResolve(
			"Not-A-Valid-Namespace", "custom", "Not-A-Valid-Namespace",
			"custom");
		_testResolve(
			"Not-A-Valid-Namespace", null, "Not-A-Valid-Namespace", null);
		_testResolve("system.user.add", "custom", "system.user.add", "custom");
		_testResolve("system.user.add", "user", "system.user.add", null);
		_testResolve(null, "user", null, "user");
		_testResolve(null, null, null, null);
	}

	private void _testResolve(
		String expectedResourceAction, String expectedResourceType,
		String initialResourceAction, String initialResourceType) {

		AuditMessage auditMessage = new AuditMessage(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomLong(), null, RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), null, RandomTestUtil.randomString(),
			null);

		auditMessage.setResourceAction(initialResourceAction);
		auditMessage.setResourceType(initialResourceType);

		AuditResourceActionUtil.resolve(auditMessage);

		Assert.assertEquals(
			expectedResourceAction, auditMessage.getResourceAction());
		Assert.assertEquals(
			expectedResourceType, auditMessage.getResourceType());
	}

}