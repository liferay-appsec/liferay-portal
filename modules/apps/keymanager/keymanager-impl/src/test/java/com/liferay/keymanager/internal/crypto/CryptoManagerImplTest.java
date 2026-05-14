/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.crypto;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.crypto.CryptoManagerException;
import com.liferay.keymanager.spi.crypto.CryptoVaultProvider;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
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

		_inject("_profileOrchestrator", _profileOrchestrator);
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
	public void testWildcardSystemRoutesToProfileSystemDek() throws Exception {
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
			_keyManagerProfile.getSystemDekProviderId()
		).thenReturn(
			resolvedProviderId
		);

		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			_keyManagerProfile
		);

		Mockito.when(
			_serviceTrackerMap.getService(resolvedProviderId)
		).thenReturn(
			Collections.singletonList(_cryptoVaultProvider)
		);

		_cryptoManagerImpl.encrypt(
			0L, _keyReference(KeyReference.ANY_PROVIDER, identifier),
			RandomTestUtil.randomBytes());

		Mockito.verify(
			_keyManagerProfile, Mockito.atLeastOnce()
		).getSystemDekProviderId();
	}

	@Test(expected = CryptoManagerException.class)
	public void testWildcardThrowsWhenNoProfileActive() throws Exception {
		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			null
		);

		_cryptoManagerImpl.encrypt(
			RandomTestUtil.randomLong(),
			_keyReference(KeyReference.ANY_PROVIDER),
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
		return KeyReference.fromString(
			StringBundler.concat(
				"${keyRef:", providerId, ":", identifier, "}"));
	}

	private CryptoManagerImpl _cryptoManagerImpl;

	@Mock
	private CryptoVaultProvider _cryptoVaultProvider;

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private ProfileOrchestrator _profileOrchestrator;

	@Mock
	private ServiceTrackerMap<String, List<CryptoVaultProvider>>
		_serviceTrackerMap;

}