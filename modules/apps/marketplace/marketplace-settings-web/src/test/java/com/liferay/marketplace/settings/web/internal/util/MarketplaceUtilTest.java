/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.marketplace.settings.web.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PrefsProps;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

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
public class MarketplaceUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_prefsProps = ReflectionTestUtil.getAndSetFieldValue(
			PrefsPropsUtil.class, "_prefsProps", _mockPrefsProps);
		_secretManagerSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			MarketplaceUtil.class, "_secretManagerSnapshot",
			new Snapshot<SecretManager>(
				MarketplaceUtil.class, SecretManager.class) {

				@Override
				public SecretManager get() {
					return _secretManager;
				}

			});
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			MarketplaceUtil.class, "_secretManagerSnapshot",
			_secretManagerSnapshot);
		ReflectionTestUtil.setFieldValue(
			PrefsPropsUtil.class, "_prefsProps", _prefsProps);
	}

	@Test
	public void testGetStoredToken() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String providerId = RandomTestUtil.randomString();
		String token = RandomTestUtil.randomString();
		String uuid = RandomTestUtil.randomString();

		Assert.assertEquals(
			token,
			_getStoredToken(companyId, "marketplaceRefreshToken", token));

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(companyId), Mockito.any(Secret.class))
		).thenAnswer(
			invocationOnMock -> {
				Secret secret = invocationOnMock.getArgument(1);

				Assert.assertEquals(token, new String(secret.getChars()));

				KeyReference keyReference = secret.getKeyReference();

				Assert.assertEquals(
					StringPool.STAR, keyReference.getProviderId());

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

			String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
				new KeyReference(
					_getIdentifier(companyId, "marketplaceRefreshToken", uuid),
					providerId, KeyReference.Type.SECRET));

			Assert.assertEquals(
				keyReferenceString,
				_getStoredToken(companyId, "marketplaceRefreshToken", token));

			String identifier = _getIdentifier(
				companyId, "marketplaceRefreshToken",
				RandomTestUtil.randomString());

			Mockito.when(
				_mockPrefsProps.getString(companyId, "marketplaceRefreshToken")
			).thenReturn(
				KeyReferenceUtil.toKeyReferenceString(
					new KeyReference(
						identifier, providerId, KeyReference.Type.SECRET))
			);

			Assert.assertEquals(
				KeyReferenceUtil.toKeyReferenceString(
					new KeyReference(
						identifier, providerId, KeyReference.Type.SECRET)),
				_getStoredToken(companyId, "marketplaceRefreshToken", token));
		}

		PortalException portalException = Assert.assertThrows(
			PortalException.class,
			() -> _getStoredToken(
				companyId, "marketplaceRefreshToken",
				KeyReferenceUtil.toKeyReferenceString(
					new KeyReference(
						RandomTestUtil.randomString(), StringPool.STAR,
						KeyReference.Type.SECRET))));

		Assert.assertEquals(
			"Token cannot begin with a reserved key reference prefix",
			portalException.getMessage());
	}

	@Test
	public void testGetToken() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String token = RandomTestUtil.randomString();

		KeyReference keyReference = new KeyReference(
			_getIdentifier(
				companyId, "marketplaceAccessToken",
				RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), KeyReference.Type.SECRET);

		Mockito.when(
			_secretManager.getSecret(companyId, keyReference)
		).thenReturn(
			new Secret(keyReference, token)
		);

		_assertGetToken(
			companyId, token,
			KeyReferenceUtil.toKeyReferenceString(keyReference));

		_assertGetToken(companyId, token, token);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				_getIdentifier(
					companyId, "marketplaceRefreshToken",
					RandomTestUtil.randomString()),
				RandomTestUtil.randomString(), KeyReference.Type.SECRET));

		_assertGetToken(companyId, keyReferenceString, keyReferenceString);

		keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				_getIdentifier(
					companyId + 1, "marketplaceAccessToken",
					RandomTestUtil.randomString()),
				RandomTestUtil.randomString(), KeyReference.Type.SECRET));

		_assertGetToken(companyId, keyReferenceString, keyReferenceString);
	}

	private void _assertGetToken(
			long companyId, String expectedToken, String storedToken)
		throws Exception {

		Mockito.when(
			_mockPrefsProps.getString(companyId, "marketplaceAccessToken")
		).thenReturn(
			storedToken
		);

		Assert.assertEquals(
			expectedToken,
			MarketplaceUtil.getToken(companyId, "marketplaceAccessToken"));
	}

	private String _getIdentifier(long companyId, String name, String uuid) {
		return StringBundler.concat(
			"Marketplace/", companyId, StringPool.SLASH, name, StringPool.SLASH,
			uuid);
	}

	private String _getStoredToken(long companyId, String name, String token)
		throws Exception {

		return ReflectionTestUtil.invoke(
			MarketplaceUtil.class, "_getStoredToken",
			new Class<?>[] {long.class, String.class, String.class}, companyId,
			name, token);
	}

	private final PrefsProps _mockPrefsProps = Mockito.mock(PrefsProps.class);
	private PrefsProps _prefsProps;
	private final SecretManager _secretManager = Mockito.mock(
		SecretManager.class);
	private Snapshot<SecretManager> _secretManagerSnapshot;

}