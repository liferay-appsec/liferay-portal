/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;

import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class OpenIdConnectClientInformationUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_secretResolverSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			new Snapshot<SecretResolver>(
				SecretResolverUtil.class, SecretResolver.class) {

				@Override
				public SecretResolver get() {
					return _secretResolver;
				}

			});
		_userLocalService = ReflectionTestUtil.getAndSetFieldValue(
			UserLocalServiceUtil.class, "_service",
			Mockito.mock(UserLocalService.class));
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			_secretResolverSnapshot);
		ReflectionTestUtil.setFieldValue(
			UserLocalServiceUtil.class, "_service", _userLocalService);
	}

	@Test
	public void testGetOIDCClientInformation() throws Exception {
		String clientSecret = RandomTestUtil.randomString();
		long companyId = RandomTestUtil.randomLong();
		long guestUserId = RandomTestUtil.randomLong();
		long oAuthClientEntryId = RandomTestUtil.randomLong();

		_secretResolver = (secretResolverCompanyId, value) -> {
			Assert.assertEquals(companyId, secretResolverCompanyId);

			return clientSecret;
		};

		UserLocalService userLocalService = ReflectionTestUtil.getFieldValue(
			UserLocalServiceUtil.class, "_service");

		Mockito.when(
			userLocalService.getGuestUserId(companyId)
		).thenReturn(
			guestUserId
		);

		String keyReferenceString = _getKeyReferenceString(
			RandomTestUtil.randomString());

		_assertGetOIDCClientInformation(
			clientSecret, companyId, keyReferenceString, oAuthClientEntryId,
			guestUserId);
		_assertGetOIDCClientInformation(
			keyReferenceString, companyId, keyReferenceString,
			oAuthClientEntryId, guestUserId + 1);

		_assertGetOIDCClientInformation(
			clientSecret, companyId,
			_getKeyReferenceString(
				StringBundler.concat(
					OAuthClientEntry.class.getSimpleName(), StringPool.SLASH,
					companyId, StringPool.SLASH, oAuthClientEntryId)),
			oAuthClientEntryId, guestUserId + 1);
		_assertGetOIDCClientInformation(
			clientSecret, companyId, clientSecret, oAuthClientEntryId,
			guestUserId + 1);
	}

	private void _assertGetOIDCClientInformation(
			String expectedClientSecret, long companyId, String clientSecret,
			long oAuthClientEntryId, long userId)
		throws Exception {

		OAuthClientEntry oAuthClientEntry = Mockito.mock(
			OAuthClientEntry.class);

		Mockito.when(
			oAuthClientEntry.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			oAuthClientEntry.getInfoJSON()
		).thenReturn(
			new JSONObject(
				HashMapBuilder.<String, Object>put(
					"client_id", RandomTestUtil.randomString()
				).put(
					"client_secret", clientSecret
				).build()
			).toJSONString()
		);

		Mockito.when(
			oAuthClientEntry.getOAuthClientEntryId()
		).thenReturn(
			oAuthClientEntryId
		);

		Mockito.when(
			oAuthClientEntry.getUserId()
		).thenReturn(
			userId
		);

		OIDCClientInformation oidcClientInformation =
			OpenIdConnectClientInformationUtil.getOIDCClientInformation(
				oAuthClientEntry);

		Secret secret = oidcClientInformation.getSecret();

		Assert.assertEquals(expectedClientSecret, secret.getValue());
	}

	private String _getKeyReferenceString(String identifier) {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				identifier, RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	private SecretResolver _secretResolver;
	private Snapshot<SecretResolver> _secretResolverSnapshot;
	private UserLocalService _userLocalService;

}