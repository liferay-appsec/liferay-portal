/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.mcp.tool.provider;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Lucas Miranda
 */
public class MCPToolProviderUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCreateMcpTransportBlocksLocalNetworkURLs() {
		_assertSSRFBlocked("http://127.0.0.1/mcp");
		_assertSSRFBlocked("http://169.254.169.254/mcp");
		_assertSSRFBlocked("http://192.168.1.1/mcp");
	}

	private void _assertSSRFBlocked(String url) {
		try {
			ReflectionTestUtil.invoke(
				MCPToolProviderUtil.class, "_createMcpTransport",
				new Class<?>[] {Map.class},
				Map.of("url", url, "authArguments", ""));

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertTrue(
				illegalArgumentException.getMessage(),
				illegalArgumentException.getMessage(
				).contains(
					url
				));
		}
	}

}