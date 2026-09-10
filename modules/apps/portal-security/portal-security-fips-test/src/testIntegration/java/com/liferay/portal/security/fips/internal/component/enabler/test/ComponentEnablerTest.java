/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.internal.component.enabler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.fips.test.util.FIPSTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lucas Miranda
 */
@RunWith(Arquillian.class)
public class ComponentEnablerTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testActivate() throws Exception {
		Assume.assumeFalse(PropsValues.FIPS_ENABLED);

		_assertGatedComponentsEnabled(false);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSTestUtil.restartBundles();

			_assertGatedComponentsEnabled(true);
		}

		FIPSTestUtil.restartBundles();

		_assertGatedComponentsEnabled(false);
	}

	private void _assertGatedComponentsEnabled(boolean expected) {
		Map<String, Boolean> gatedComponentsEnabled =
			FIPSTestUtil.getGatedComponentsEnabled();

		for (Map.Entry<String, Boolean> entry :
				gatedComponentsEnabled.entrySet()) {

			String name = entry.getKey();
			Boolean value = entry.getValue();

			Assert.assertEquals(name, expected, value.booleanValue());
		}
	}

}