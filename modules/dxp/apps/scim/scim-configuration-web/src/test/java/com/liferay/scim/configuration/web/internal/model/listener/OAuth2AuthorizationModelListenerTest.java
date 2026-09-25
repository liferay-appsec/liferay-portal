/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.configuration.web.internal.model.listener;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.scim.rest.util.ScimClientUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Caio Farias
 */
public class OAuth2AuthorizationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		String oAuth2ApplicationName = RandomTestUtil.randomString();

		Configuration configuration = Mockito.mock(Configuration.class);

		Mockito.when(
			configuration.getProperties()
		).thenReturn(
			HashMapDictionaryBuilder.<String, Object>put(
				"oAuth2ApplicationName", oAuth2ApplicationName
			).build()
		);

		ConfigurationAdmin configurationAdmin = Mockito.mock(
			ConfigurationAdmin.class);

		Mockito.when(
			configurationAdmin.listConfigurations(Mockito.anyString())
		).thenReturn(
			new Configuration[] {configuration}
		);

		ReflectionTestUtil.setFieldValue(
			_oAuth2AuthorizationModelListener, "_configurationAdmin",
			configurationAdmin);

		Mockito.when(
			_oAuth2Application.getClientId()
		).thenReturn(
			ScimClientUtil.generateScimClientId(oAuth2ApplicationName)
		);

		Mockito.when(
			_oAuth2Application.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		OAuth2ApplicationLocalService oAuth2ApplicationLocalService =
			Mockito.mock(OAuth2ApplicationLocalService.class);

		Mockito.when(
			oAuth2ApplicationLocalService.fetchOAuth2Application(
				_OAUTH2_APPLICATION_ID)
		).thenReturn(
			_oAuth2Application
		);

		ReflectionTestUtil.setFieldValue(
			_oAuth2AuthorizationModelListener, "_oAuth2ApplicationLocalService",
			oAuth2ApplicationLocalService);

		_secretManagerSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			OAuth2AuthorizationModelListener.class, "_secretManagerSnapshot",
			new Snapshot<SecretManager>(
				OAuth2AuthorizationModelListener.class, SecretManager.class) {

				@Override
				public SecretManager get() {
					return _secretManager;
				}

			});
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			OAuth2AuthorizationModelListener.class, "_secretManagerSnapshot",
			_secretManagerSnapshot);
	}

	@Test
	public void testOnAfterRemove() throws Exception {
		KeyReference keyReference = _getKeyReference(
			RandomTestUtil.randomString());

		_oAuth2AuthorizationModelListener.onAfterRemove(
			_getOAuth2Authorization(
				KeyReferenceUtil.toKeyReferenceString(keyReference),
				_OAUTH2_APPLICATION_ID));

		Mockito.verify(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, keyReference
		);
	}

	@Test
	public void testOnBeforeCreate() throws Exception {
		String accessTokenContent = RandomTestUtil.randomString();

		OAuth2Authorization oAuth2Authorization = _getOAuth2Authorization(
			accessTokenContent, _OAUTH2_APPLICATION_ID);

		_oAuth2AuthorizationModelListener.onBeforeCreate(oAuth2Authorization);

		Mockito.verify(
			oAuth2Authorization, Mockito.never()
		).setAccessTokenContent(
			Mockito.anyString()
		);

		Mockito.verifyNoInteractions(_secretManager);

		String uuid = RandomTestUtil.randomString();

		KeyReference keyReference = _getKeyReference(uuid);

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(_COMPANY_ID), Mockito.any(Secret.class))
		).thenReturn(
			keyReference
		);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true);
			MockedStatic<PortalUUIDUtil> portalUUIDUtilMockedStatic =
				Mockito.mockStatic(PortalUUIDUtil.class)) {

			portalUUIDUtilMockedStatic.when(
				PortalUUIDUtil::generate
			).thenReturn(
				uuid
			);

			_oAuth2AuthorizationModelListener.onBeforeCreate(
				oAuth2Authorization);

			Mockito.verify(
				oAuth2Authorization
			).setAccessTokenContent(
				KeyReferenceUtil.toKeyReferenceString(keyReference)
			);

			Mockito.verify(
				oAuth2Authorization
			).setAccessTokenContentHash(
				accessTokenContent.hashCode()
			);

			OAuth2Authorization otherOAuth2Authorization =
				_getOAuth2Authorization(
					RandomTestUtil.randomString(), RandomTestUtil.randomLong());

			_oAuth2AuthorizationModelListener.onBeforeCreate(
				otherOAuth2Authorization);

			Mockito.verify(
				otherOAuth2Authorization, Mockito.never()
			).setAccessTokenContent(
				Mockito.anyString()
			);
		}
	}

	@Test
	public void testOnBeforeUpdate() throws Exception {
		KeyReference originalKeyReference = _getKeyReference(
			RandomTestUtil.randomString());

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(_COMPANY_ID), Mockito.any(Secret.class))
		).thenReturn(
			_getKeyReference(RandomTestUtil.randomString())
		);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			_oAuth2AuthorizationModelListener.onBeforeUpdate(
				_getOAuth2Authorization(
					KeyReferenceUtil.toKeyReferenceString(originalKeyReference),
					_OAUTH2_APPLICATION_ID),
				_getOAuth2Authorization(
					RandomTestUtil.randomString(), _OAUTH2_APPLICATION_ID));
		}

		Mockito.verify(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, originalKeyReference
		);
	}

	private KeyReference _getKeyReference(String uuid) {
		return new KeyReference(
			StringBundler.concat(
				OAuth2Authorization.class.getSimpleName(), StringPool.SLASH,
				_COMPANY_ID, StringPool.SLASH, _OAUTH2_AUTHORIZATION_ID,
				StringPool.SLASH, uuid),
			RandomTestUtil.randomString(), KeyReference.Type.SECRET);
	}

	private OAuth2Authorization _getOAuth2Authorization(
		String accessTokenContent, long oAuth2ApplicationId) {

		OAuth2Authorization oAuth2Authorization = Mockito.mock(
			OAuth2Authorization.class);

		Mockito.when(
			oAuth2Authorization.getAccessTokenContent()
		).thenReturn(
			accessTokenContent
		);

		Mockito.when(
			oAuth2Authorization.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			oAuth2Authorization.getOAuth2ApplicationId()
		).thenReturn(
			oAuth2ApplicationId
		);

		Mockito.when(
			oAuth2Authorization.getOAuth2AuthorizationId()
		).thenReturn(
			_OAUTH2_AUTHORIZATION_ID
		);

		return oAuth2Authorization;
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _OAUTH2_APPLICATION_ID =
		RandomTestUtil.randomLong();

	private static final long _OAUTH2_AUTHORIZATION_ID =
		RandomTestUtil.randomLong();

	private final OAuth2Application _oAuth2Application = Mockito.mock(
		OAuth2Application.class);
	private final OAuth2AuthorizationModelListener
		_oAuth2AuthorizationModelListener =
			new OAuth2AuthorizationModelListener();
	private final SecretManager _secretManager = Mockito.mock(
		SecretManager.class);
	private Snapshot<SecretManager> _secretManagerSnapshot;

}