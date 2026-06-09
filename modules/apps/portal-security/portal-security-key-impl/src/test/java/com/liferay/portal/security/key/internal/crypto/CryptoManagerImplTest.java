/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.crypto;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.crypto.CryptoManagerException;
import com.liferay.portal.security.key.spi.crypto.CryptoVaultProvider;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileOrchestrator;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Christopher Kian
 */
public class CryptoManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		_cryptoManagerImpl = new CryptoManagerImpl();

		_inject(
			"_keyManagerProfileOrchestrator", _keyManagerProfileOrchestrator);
		_inject("_serviceTrackerMap", _serviceTrackerMap);
	}

	@Test(expected = CryptoManagerException.class)
	public void testEncryptThrowsWhenNoProviderRegistered() throws Exception {
		String providerId = RandomTestUtil.randomString();

		Mockito.when(
			_serviceTrackerMap.getService(providerId)
		).thenReturn(
			null
		);

		_cryptoManagerImpl.encrypt(
			RandomTestUtil.randomLong(), _keyReference(providerId),
			RandomTestUtil.randomBytes());
	}

	@Test
	public void testImportSecretKeyZerosRawKeyMaterialEvenOnFailure()
		throws Exception {

		Mockito.when(
			_cryptoVaultProvider.importSecretKey(
				Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(byte[].class))
		).thenThrow(
			new CryptoManagerException("provider boom")
		);

		Mockito.when(
			_cryptoVaultProvider.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);

		String providerId = RandomTestUtil.randomString();

		Mockito.when(
			_serviceTrackerMap.getService(providerId)
		).thenReturn(
			Collections.singletonList(_cryptoVaultProvider)
		);

		byte[] rawKeyMaterial = RandomTestUtil.randomBytes();

		try {
			_cryptoManagerImpl.importSecretKey(
				RandomTestUtil.randomString(), RandomTestUtil.randomLong(),
				RandomTestUtil.randomString(), providerId, rawKeyMaterial);

			Assert.fail();
		}
		catch (CryptoManagerException cryptoManagerException) {
			for (byte b : rawKeyMaterial) {
				Assert.assertEquals(0, b);
			}
		}
	}

	@Test
	public void testWildcardSystemRoutesToProfileSystemDEK() throws Exception {
		String identifier = RandomTestUtil.randomString();

		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.eq(0L), Mockito.eq(identifier),
				Mockito.any(byte[].class))
		).thenReturn(
			new byte[0]
		);

		Mockito.when(
			_cryptoVaultProvider.isAllowedCompany(0L)
		).thenReturn(
			true
		);

		String resolvedProviderId = RandomTestUtil.randomString();

		Mockito.when(
			_keyManagerProfile.getSystemDEKProviderId()
		).thenReturn(
			resolvedProviderId
		);

		Mockito.when(
			_keyManagerProfileOrchestrator.getActiveKeyManagerProfile()
		).thenReturn(
			_keyManagerProfile
		);

		Mockito.when(
			_serviceTrackerMap.getService(resolvedProviderId)
		).thenReturn(
			Collections.singletonList(_cryptoVaultProvider)
		);

		_cryptoManagerImpl.encrypt(
			0L, _keyReference(StringPool.STAR, identifier),
			RandomTestUtil.randomBytes());

		Mockito.verify(
			_keyManagerProfile, Mockito.atLeastOnce()
		).getSystemDEKProviderId();
	}

	@Test(expected = CryptoManagerException.class)
	public void testWildcardThrowsWhenNoProfileActive() throws Exception {
		Mockito.when(
			_keyManagerProfileOrchestrator.getActiveKeyManagerProfile()
		).thenReturn(
			null
		);

		_cryptoManagerImpl.encrypt(
			RandomTestUtil.randomLong(), _keyReference(StringPool.STAR),
			RandomTestUtil.randomBytes());
	}

	private void _inject(String fieldName, Object value) {
		try {
			Field field = CryptoManagerImpl.class.getDeclaredField(fieldName);

			field.setAccessible(true);
			field.set(_cryptoManagerImpl, value);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private KeyReference _keyReference(String providerId) {
		return _keyReference(providerId, RandomTestUtil.randomString());
	}

	private KeyReference _keyReference(String providerId, String identifier) {
		return new KeyReference(
			identifier, providerId, KeyReference.Type.CRYPTO);
	}

	private CryptoManagerImpl _cryptoManagerImpl;

	@Mock
	private CryptoVaultProvider _cryptoVaultProvider;

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private KeyManagerProfileOrchestrator _keyManagerProfileOrchestrator;

	@Mock
	private ServiceTrackerMap<String, List<CryptoVaultProvider>>
		_serviceTrackerMap;

}