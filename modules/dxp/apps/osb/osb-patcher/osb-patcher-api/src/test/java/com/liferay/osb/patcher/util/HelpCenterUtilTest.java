/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.osb.patcher.util;

import com.liferay.osb.patcher.configuration.PatcherConfiguration;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.net.HttpURLConnection;

import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class HelpCenterUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetAuthenticationToken() throws Exception {
		PatcherConfiguration patcherConfiguration = Mockito.mock(
			PatcherConfiguration.class);

		String supportLiferayAPIClientSecret =
			KeyReferenceUtil.toKeyReferenceString(
				new KeyReference(
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(), KeyReference.Type.SECRET));

		Mockito.when(
			patcherConfiguration.supportLiferayAPIClientSecret()
		).thenReturn(
			supportLiferayAPIClientSecret
		);

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					SecretResolverUtil.class, "_secretResolverSnapshot",
					new Snapshot<SecretResolver>(
						SecretResolverUtil.class, SecretResolver.class) {

						@Override
						public SecretResolver get() {
							return secretResolver;
						}

					})) {

			long companyId = RandomTestUtil.randomLong();

			Mockito.when(
				secretResolver.resolve(
					Mockito.eq(companyId), Mockito.any(SecretContext.class),
					Mockito.eq(supportLiferayAPIClientSecret))
			).thenReturn(
				RandomTestUtil.randomString()
			);

			String accessToken = RandomTestUtil.randomString();

			try (MockedStatic<HttpUtil> httpUtilMockedStatic =
					Mockito.mockStatic(HttpUtil.class)) {

				httpUtilMockedStatic.when(
					() -> HttpUtil.URLtoString(Mockito.any(Http.Options.class))
				).thenAnswer(
					invocationOnMock -> {
						Http.Options options = invocationOnMock.getArgument(0);

						Http.Response response = options.getResponse();

						response.setResponseCode(HttpURLConnection.HTTP_OK);

						return JSONUtil.put(
							"access_token", accessToken
						).put(
							"expires_in", RandomTestUtil.randomInt()
						).toString();
					}
				);

				Assert.assertEquals(
					accessToken,
					HelpCenterUtil.getAuthenticationToken(
						companyId, patcherConfiguration));
			}

			Mockito.verify(
				secretResolver
			).resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(
							secretContext.getKey(),
							"supportLiferayAPIClientSecret") &&
						(secretContext.getSettingsLocator() instanceof
							CompanyServiceSettingsLocator
								companyServiceSettingsLocator) &&
						(companyServiceSettingsLocator.getCompanyId() ==
							companyId)),
				Mockito.eq(supportLiferayAPIClientSecret)
			);
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				HelpCenterUtil.class, "_accessToken", null);
			ReflectionTestUtil.setFieldValue(
				HelpCenterUtil.class, "_tokenExpirationTime", 0L);
		}
	}

}