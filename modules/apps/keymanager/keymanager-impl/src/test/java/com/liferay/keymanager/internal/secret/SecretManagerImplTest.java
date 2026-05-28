/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.secret;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.keymanager.spi.secret.SecretVaultReader;
import com.liferay.keymanager.spi.secret.SecretVaultWriter;
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
public class SecretManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		_secretManagerImpl = new SecretManagerImpl();

		_inject("_profileOrchestrator", _profileOrchestrator);
		_inject("_readerServiceTrackerMap", _readerServiceTrackerMap);
		_inject("_writerServiceTrackerMap", _writerServiceTrackerMap);
	}

	@Test(expected = SecretManagerException.class)
	public void testDeleteThrowsWhenNoWriterRegistered() throws Exception {
		String providerId = RandomTestUtil.randomString();

		Mockito.when(
			_writerServiceTrackerMap.getService(providerId)
		).thenReturn(
			null
		);

		_secretManagerImpl.deleteSecret(
			RandomTestUtil.randomLong(), _secretReference(providerId));
	}

	@Test
	public void testGetSecretDelegatesToReader() throws Exception {
		String providerId = RandomTestUtil.randomString();

		Mockito.when(
			_readerServiceTrackerMap.getService(providerId)
		).thenReturn(
			Collections.singletonList(_secretVaultReader)
		);

		long companyId = RandomTestUtil.randomLong();
		String identifier = RandomTestUtil.randomString();

		Mockito.when(
			_secretVaultReader.getSecret(companyId, identifier)
		).thenReturn(
			new SecureSecret(
				RandomTestUtil.randomBytes(),
				_secretReference(providerId, identifier))
		);

		Mockito.when(
			_secretVaultReader.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);

		_secretManagerImpl.getSecret(
			companyId, _secretReference(providerId, identifier));

		Mockito.verify(
			_secretVaultReader
		).getSecret(
			companyId, identifier
		);
	}

	@Test
	public void testGetSecretIdentifiersResolvesWildcardThroughActiveProfile()
		throws Exception {

		String resolvedProviderId = RandomTestUtil.randomString();

		Mockito.when(
			_keyManagerProfile.getCompanySecretProviderId()
		).thenReturn(
			resolvedProviderId
		);

		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			_keyManagerProfile
		);

		Mockito.when(
			_readerServiceTrackerMap.getService(resolvedProviderId)
		).thenReturn(
			Collections.singletonList(_secretVaultReader)
		);

		String identifier = RandomTestUtil.randomString();

		Mockito.when(
			_secretVaultReader.getSecretIdentifiers(Mockito.anyLong())
		).thenReturn(
			Collections.singletonList(identifier)
		);

		Mockito.when(
			_secretVaultReader.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);

		long companyId = RandomTestUtil.randomLong();

		List<KeyReference> keyReferences =
			_secretManagerImpl.getSecretIdentifiers(
				companyId, KeyReference.ANY_PROVIDER);

		Assert.assertEquals(keyReferences.toString(), 1, keyReferences.size());

		KeyReference keyReference = keyReferences.get(0);

		Assert.assertEquals(identifier, keyReference.getIdentifier());
		Assert.assertEquals(resolvedProviderId, keyReference.getProviderId());
		Assert.assertEquals(KeyReference.Type.SECRET, keyReference.getType());
	}

	@Test
	public void testPutSecretRoutesToWriter() throws Exception {
		Mockito.when(
			_secretVaultWriter.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);

		String providerId = RandomTestUtil.randomString();

		Mockito.when(
			_writerServiceTrackerMap.getService(providerId)
		).thenReturn(
			Collections.singletonList(_secretVaultWriter)
		);

		long companyId = RandomTestUtil.randomLong();
		String identifier = RandomTestUtil.randomString();

		try (SecureSecret secureSecret = new SecureSecret(
				_secretReference(providerId, identifier),
				RandomTestUtil.randomString())) {

			_secretManagerImpl.putSecret(companyId, secureSecret);
		}

		Mockito.verify(
			_secretVaultWriter
		).putSecret(
			Mockito.eq(companyId), Mockito.any(SecureSecret.class)
		);
	}

	@Test
	public void testWildcardCompanyRoutesToProfileCompanySecret()
		throws Exception {

		String resolvedProviderId = RandomTestUtil.randomString();

		Mockito.when(
			_keyManagerProfile.getCompanySecretProviderId()
		).thenReturn(
			resolvedProviderId
		);

		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			_keyManagerProfile
		);

		long companyId = RandomTestUtil.randomLong();

		Mockito.when(
			_secretVaultWriter.isAllowedCompany(companyId)
		).thenReturn(
			true
		);

		Mockito.when(
			_writerServiceTrackerMap.getService(resolvedProviderId)
		).thenReturn(
			Collections.singletonList(_secretVaultWriter)
		);

		try (SecureSecret secureSecret = new SecureSecret(
				_secretReference(
					KeyReference.ANY_PROVIDER, RandomTestUtil.randomString()),
				RandomTestUtil.randomString())) {

			_secretManagerImpl.putSecret(companyId, secureSecret);
		}

		Mockito.verify(
			_keyManagerProfile
		).getCompanySecretProviderId();

		Mockito.verify(
			_secretVaultWriter
		).putSecret(
			Mockito.eq(companyId), Mockito.any(SecureSecret.class)
		);
	}

	private void _inject(String fieldName, Object value) {
		try {
			Field field = SecretManagerImpl.class.getDeclaredField(fieldName);

			field.setAccessible(true);
			field.set(_secretManagerImpl, value);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private KeyReference _secretReference(String providerId) {
		return _secretReference(providerId, RandomTestUtil.randomString());
	}

	private KeyReference _secretReference(
		String providerId, String identifier) {

		return KeyReference.fromString(
			StringBundler.concat(
				"${secretRef:", providerId, ":", identifier, "}"));
	}

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private ProfileOrchestrator _profileOrchestrator;

	@Mock
	private ServiceTrackerMap<String, List<SecretVaultReader>>
		_readerServiceTrackerMap;

	private SecretManagerImpl _secretManagerImpl;

	@Mock
	private SecretVaultReader _secretVaultReader;

	@Mock
	private SecretVaultWriter _secretVaultWriter;

	@Mock
	private ServiceTrackerMap<String, List<SecretVaultWriter>>
		_writerServiceTrackerMap;

}