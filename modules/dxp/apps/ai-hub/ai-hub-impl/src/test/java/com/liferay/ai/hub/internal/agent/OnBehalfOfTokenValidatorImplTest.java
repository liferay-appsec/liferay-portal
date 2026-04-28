/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.OnBehalfOfTokenValidator;
import com.liferay.ai.hub.cell.security.JWTTokenUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Christopher Kian
 */
public class OnBehalfOfTokenValidatorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testValidateWithBlankHeader() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader(
			OnBehalfOfTokenValidator.ON_BEHALF_OF_HEADER, StringPool.BLANK);

		Assert.assertNull(
			_onBehalfOfTokenValidatorImpl.validate(mockHttpServletRequest));
	}

	@Test(expected = PrincipalException.class)
	public void testValidateWithExpiredToken() throws Exception {
		String token = JWTTokenUtil.generateToken(
			-1L, RandomTestUtil.randomString(), RandomTestUtil.randomLong());

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader(
			OnBehalfOfTokenValidator.ON_BEHALF_OF_HEADER, token);

		_onBehalfOfTokenValidatorImpl.validate(mockHttpServletRequest);
	}

	@Test(expected = PrincipalException.class)
	public void testValidateWithMalformedToken() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader(
			OnBehalfOfTokenValidator.ON_BEHALF_OF_HEADER,
			RandomTestUtil.randomString());

		_onBehalfOfTokenValidatorImpl.validate(mockHttpServletRequest);
	}

	@Test
	public void testValidateWithMissingHeader() throws Exception {
		Assert.assertNull(
			_onBehalfOfTokenValidatorImpl.validate(
				new MockHttpServletRequest()));
	}

	@Test
	public void testValidateWithValidToken() throws Exception {
		String token = JWTTokenUtil.generateToken(
			60000L, RandomTestUtil.randomString(), RandomTestUtil.randomLong());

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader(
			OnBehalfOfTokenValidator.ON_BEHALF_OF_HEADER, token);

		Assert.assertEquals(
			token,
			_onBehalfOfTokenValidatorImpl.validate(mockHttpServletRequest));
	}

	private final OnBehalfOfTokenValidatorImpl _onBehalfOfTokenValidatorImpl =
		new OnBehalfOfTokenValidatorImpl();

}