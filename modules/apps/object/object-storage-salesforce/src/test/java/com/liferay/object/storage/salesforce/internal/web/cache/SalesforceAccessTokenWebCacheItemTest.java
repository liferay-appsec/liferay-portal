/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.salesforce.internal.web.cache;

import com.liferay.object.storage.salesforce.configuration.SalesforceConfiguration;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

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
public class SalesforceAccessTokenWebCacheItemTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGet() throws Exception {
		SalesforceConfiguration salesforceConfiguration = Mockito.mock(
			SalesforceConfiguration.class);

		String consumerSecret = _randomKeyReferenceString();

		Mockito.when(
			salesforceConfiguration.consumerSecret()
		).thenReturn(
			consumerSecret
		);

		String password = _randomKeyReferenceString();

		Mockito.when(
			salesforceConfiguration.password()
		).thenReturn(
			password
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

			try (MockedStatic<WebCachePoolUtil> webCachePoolUtilMockedStatic =
					Mockito.mockStatic(WebCachePoolUtil.class)) {

				JSONObject jsonObject = Mockito.mock(JSONObject.class);

				webCachePoolUtilMockedStatic.when(
					() -> WebCachePoolUtil.get(
						Mockito.anyString(), Mockito.any())
				).thenReturn(
					jsonObject
				);

				long companyId = RandomTestUtil.randomLong();
				SettingsLocator settingsLocator = new SystemSettingsLocator(
					RandomTestUtil.randomString());

				Assert.assertSame(
					jsonObject,
					SalesforceAccessTokenWebCacheItem.get(
						companyId, salesforceConfiguration, settingsLocator));

				_verifyResolve(
					companyId, "consumerSecret", secretResolver,
					settingsLocator, consumerSecret);
				_verifyResolve(
					companyId, "password", secretResolver, settingsLocator,
					password);

				Mockito.when(
					secretResolver.resolve(
						Mockito.eq(companyId), Mockito.any(SecretContext.class),
						Mockito.eq(password))
				).thenAnswer(
					invocationOnMock -> {
						throw new SecretException();
					}
				);

				webCachePoolUtilMockedStatic.clearInvocations();

				Assert.assertThrows(
					SecretException.class,
					() -> SalesforceAccessTokenWebCacheItem.get(
						companyId, salesforceConfiguration, settingsLocator));

				webCachePoolUtilMockedStatic.verifyNoInteractions();
			}
		}
	}

	private String _randomKeyReferenceString() {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	private void _verifyResolve(
		long companyId, String key, SecretResolver secretResolver,
		SettingsLocator settingsLocator, String value) {

		Mockito.verify(
			secretResolver
		).resolve(
			Mockito.eq(companyId),
			Mockito.argThat(
				(SecretContext secretContext) ->
					Objects.equals(secretContext.getKey(), key) &&
					(secretContext.getSettingsLocator() == settingsLocator)),
			Mockito.eq(value)
		);
	}

}