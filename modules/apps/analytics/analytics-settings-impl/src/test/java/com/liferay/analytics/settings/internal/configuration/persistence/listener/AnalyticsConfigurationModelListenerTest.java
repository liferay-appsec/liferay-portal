/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.analytics.settings.internal.configuration.persistence.listener;

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.analytics.settings.configuration.AnalyticsConfigurationRegistry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
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

import java.util.Dictionary;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class AnalyticsConfigurationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_analyticsConfigurationModelListener,
			"_analyticsConfigurationRegistry", _analyticsConfigurationRegistry);

		Mockito.when(
			_analyticsConfigurationRegistry.getAnalyticsConfiguration(
				Mockito.anyString())
		).thenReturn(
			_analyticsConfiguration
		);

		_secretManagerSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			AnalyticsConfigurationModelListener.class, "_secretManagerSnapshot",
			new Snapshot<SecretManager>(
				AnalyticsConfigurationModelListener.class,
				SecretManager.class) {

				@Override
				public SecretManager get() {
					return _secretManager;
				}

			});
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			AnalyticsConfigurationModelListener.class, "_secretManagerSnapshot",
			_secretManagerSnapshot);
	}

	@Test
	public void testOnBeforeDelete() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = _getKeyReference(companyId, "token");

		Mockito.when(
			_analyticsConfiguration.token()
		).thenReturn(
			KeyReferenceUtil.toKeyReferenceString(keyReference)
		);

		Mockito.when(
			_analyticsConfiguration.
				liferayAnalyticsFaroBackendSecuritySignature()
		).thenReturn(
			KeyReferenceUtil.toKeyReferenceString(
				new KeyReference(
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(), KeyReference.Type.SECRET))
		);

		_analyticsConfigurationModelListener.onBeforeDelete(
			RandomTestUtil.randomString());

		Mockito.verify(
			_secretManager
		).deleteSecret(
			companyId, keyReference
		);

		Mockito.verifyNoMoreInteractions(_secretManager);
	}

	@Test
	public void testOnBeforeSave() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String providerId = RandomTestUtil.randomString();
		String signature = RandomTestUtil.randomString();
		String token = RandomTestUtil.randomString();
		String uuid = RandomTestUtil.randomString();

		Dictionary<String, Object> properties = _onBeforeSave(
			companyId, signature, token);

		Assert.assertEquals(
			signature,
			properties.get("liferayAnalyticsFaroBackendSecuritySignature"));
		Assert.assertEquals(token, properties.get("token"));

		Mockito.verifyNoInteractions(_secretManager);

		KeyReference previousKeyReference = _getKeyReference(
			companyId, "token");

		Mockito.when(
			_analyticsConfiguration.token()
		).thenReturn(
			KeyReferenceUtil.toKeyReferenceString(previousKeyReference)
		);

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(companyId), Mockito.any(Secret.class))
		).thenAnswer(
			invocationOnMock -> {
				Secret secret = invocationOnMock.getArgument(1);

				KeyReference keyReference = secret.getKeyReference();

				return new KeyReference(
					keyReference.getIdentifier(), providerId,
					KeyReference.Type.SECRET);
			}
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

			properties = _onBeforeSave(companyId, signature, token);

			Assert.assertEquals(
				_getKeyReferenceString(
					companyId, "liferayAnalyticsFaroBackendSecuritySignature",
					providerId, uuid),
				properties.get("liferayAnalyticsFaroBackendSecuritySignature"));
			Assert.assertEquals(
				_getKeyReferenceString(companyId, "token", providerId, uuid),
				properties.get("token"));

			Mockito.verify(
				_secretManager
			).deleteSecret(
				companyId, previousKeyReference
			);

			String keyReferenceString = _getKeyReferenceString(
				companyId, "token", providerId, RandomTestUtil.randomString());

			properties = _onBeforeSave(
				companyId, signature, keyReferenceString);

			Assert.assertEquals(keyReferenceString, properties.get("token"));
		}

		Assert.assertThrows(
			ConfigurationModelListenerException.class,
			() -> _onBeforeSave(
				companyId, signature,
				_getKeyReferenceString(
					companyId + 1, "token", providerId,
					RandomTestUtil.randomString())));
	}

	private KeyReference _getKeyReference(long companyId, String name) {
		return new KeyReference(
			StringBundler.concat(
				AnalyticsConfiguration.class.getSimpleName(), StringPool.SLASH,
				companyId, StringPool.SLASH, name, StringPool.SLASH,
				RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), KeyReference.Type.SECRET);
	}

	private String _getKeyReferenceString(
		long companyId, String name, String providerId, String uuid) {

		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				StringBundler.concat(
					AnalyticsConfiguration.class.getSimpleName(),
					StringPool.SLASH, companyId, StringPool.SLASH, name,
					StringPool.SLASH, uuid),
				providerId, KeyReference.Type.SECRET));
	}

	private Dictionary<String, Object> _onBeforeSave(
			long companyId, String signature, String token)
		throws Exception {

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", companyId
			).put(
				"liferayAnalyticsFaroBackendSecuritySignature", signature
			).put(
				"token", token
			).build();

		_analyticsConfigurationModelListener.onBeforeSave(
			RandomTestUtil.randomString(), properties);

		return properties;
	}

	private final AnalyticsConfiguration _analyticsConfiguration = Mockito.mock(
		AnalyticsConfiguration.class);
	private final AnalyticsConfigurationModelListener
		_analyticsConfigurationModelListener =
			new AnalyticsConfigurationModelListener();
	private final AnalyticsConfigurationRegistry
		_analyticsConfigurationRegistry = Mockito.mock(
			AnalyticsConfigurationRegistry.class);
	private final SecretManager _secretManager = Mockito.mock(
		SecretManager.class);
	private Snapshot<SecretManager> _secretManagerSnapshot;

}