/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal;

import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.sso.openid.connect.internal.util.OpenIdConnectRequestParametersUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.langtag.LangTag;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Tamas Biro
 */
public class OpenIdConnectAuthenticationHandlerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetUserInfoClaims() throws Exception {
		Map<String, Object> claims = _getUserInfoClaims(
			JSONUtil.put(
				"email", "exists@test.com"
			).put(
				"name", "test_account"
			).put(
				"sub", "subject"
			).toString());

		Assert.assertEquals("exists@test.com", claims.get("email"));

		claims = _getUserInfoClaims(
			JSONUtil.put(
				"name", "test_account"
			).put(
				"sub", "subject"
			).toString());

		Assert.assertNull(claims.get("email"));
	}

	@Test
	public void testUILocaleLangTag() {
		List<LangTag> langTags = _getLangTags(
			OpenIdConnectRequestParametersUtil.UILocaleMode.DISABLED);

		Assert.assertNull(langTags);

		langTags = _getLangTags(
			OpenIdConnectRequestParametersUtil.UILocaleMode.
				LOWERCASE_BCP47_LANGUAGE_CODE);

		Assert.assertEquals(
			"en-us",
			langTags.get(
				0
			).toString());

		langTags = _getLangTags(
			OpenIdConnectRequestParametersUtil.UILocaleMode.DEFAULT);

		Assert.assertEquals(
			"en-US",
			langTags.get(
				0
			).toString());
	}

	protected Language language = Mockito.mock(Language.class);
	protected Portal portal = Mockito.mock(Portal.class);

	private List<LangTag> _getLangTags(
		OpenIdConnectRequestParametersUtil.UILocaleMode uiLocaleMode) {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		Mockito.doReturn(
			LocaleUtil.US
		).when(
			portal
		).getLocale(
			Mockito.any(HttpServletRequest.class)
		);

		Mockito.doReturn(
			"en-US"
		).when(
			language
		).getBCP47LangTag(
			Mockito.any(Locale.class)
		);

		OpenIdConnectAuthenticationHandlerImpl
			openIdConnectAuthenticationHandlerImpl =
				new OpenIdConnectAuthenticationHandlerImpl();

		ReflectionTestUtil.setFieldValue(
			openIdConnectAuthenticationHandlerImpl, "_language", language);

		ReflectionTestUtil.setFieldValue(
			openIdConnectAuthenticationHandlerImpl, "_portal", portal);

		return openIdConnectAuthenticationHandlerImpl.getLangTags(
			mockHttpServletRequest, uiLocaleMode);
	}

	private Map<String, Object> _getUserInfoClaims(String claimSetJSON)
		throws Exception {

		OpenIdConnectAuthenticationHandlerImpl
			openIdConnectAuthenticationHandlerImpl =
				new OpenIdConnectAuthenticationHandlerImpl();

		JWT mockJWT = Mockito.mock(JWT.class);

		Mockito.when(
			mockJWT.getJWTClaimsSet()
		).thenReturn(
			JWTClaimsSet.parse(claimSetJSON)
		);

		return openIdConnectAuthenticationHandlerImpl.getUserInfoClaims(
			mockJWT);
	}

}