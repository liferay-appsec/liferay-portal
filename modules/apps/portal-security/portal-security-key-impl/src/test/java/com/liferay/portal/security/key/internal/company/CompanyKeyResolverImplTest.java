/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.company;

import com.liferay.portal.kernel.encryptor.CompanyKeyUtil;
import com.liferay.portal.kernel.exception.CompanyKeyResolutionException;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.ServiceIndicator;
import com.liferay.portal.security.key.crypto.CryptoManager;
import com.liferay.portal.security.key.crypto.CryptoServiceResult;
import com.liferay.portal.security.key.crypto.exception.CryptoException;
import com.liferay.portal.security.key.internal.profile.configuration.KeyManagerConfiguration;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileRegistry;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Key;

import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * @author Christopher Kian
 */
public class CompanyKeyResolverImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDeactivate() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		String serializedKey = _serialize(_CIPHERTEXT);

		_mockDecrypt(_CIPHERTEXT);

		companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);

		Map<Long, CompanyKeyCacheEntry> companyKeyCacheEntries =
			_getCompanyKeyCacheEntries(companyKeyResolverImpl);

		CompanyKeyCacheEntry companyKeyCacheEntry = companyKeyCacheEntries.get(
			_COMPANY_ID);

		companyKeyResolverImpl.deactivate();

		Assert.assertTrue(companyKeyCacheEntries.isEmpty());
		Assert.assertTrue(companyKeyCacheEntry.isDestroyed());
	}

	@Test
	public void testDeserializeKey() throws Exception {
		_testDeserializeKey();
		_testDeserializeKeyWithChangedSerializedKey();
		_testDeserializeKeyWithDecryptFailure();
		_testDeserializeKeyWithExpiredCacheEntry();
		_testDeserializeKeyWithMalformedSerializedKey();
		_testDeserializeKeyWithoutCache();
	}

	@Test
	public void testIsEnabled() {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		Assert.assertTrue(companyKeyResolverImpl.isEnabled());

		Mockito.when(
			_keyManagerConfiguration.companyKEKIdentifier()
		).thenReturn(
			""
		);

		Assert.assertFalse(companyKeyResolverImpl.isEnabled());

		Mockito.when(
			_keyManagerConfiguration.companyKEKIdentifier()
		).thenReturn(
			_KEK_IDENTIFIER
		);

		Mockito.when(
			_keyManagerProfileRegistry.getActiveKeyManagerProfile()
		).thenReturn(
			null
		);

		Assert.assertFalse(companyKeyResolverImpl.isEnabled());
	}

	@Test
	public void testSerializeKey() throws Exception {
		_testSerializeKey();
		_testSerializeKeyWithEncryptFailure();
		_testSerializeKeyWithoutKEKIdentifier();
		_testSerializeKeyWithoutKEKProvider();
	}

	private void _assertDeserializeKeyFails(
		CompanyKeyResolverImpl companyKeyResolverImpl, String serializedKey) {

		try {
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);

			Assert.fail();
		}
		catch (CompanyKeyResolutionException companyKeyResolutionException) {
		}
	}

	private void _assertSerializeKeyFails(
		CompanyKeyResolverImpl companyKeyResolverImpl) {

		try {
			companyKeyResolverImpl.serializeKey(_COMPANY_ID, _key);

			Assert.fail();
		}
		catch (CompanyKeyResolutionException companyKeyResolutionException) {
		}
	}

	private CompanyKeyResolverImpl _createCompanyKeyResolverImpl(
		int companyKeyCacheTTL) {

		CompanyKeyResolverImpl companyKeyResolverImpl =
			new CompanyKeyResolverImpl();

		_keyManagerConfiguration = Mockito.mock(KeyManagerConfiguration.class);

		Mockito.when(
			_keyManagerConfiguration.companyKEKIdentifier()
		).thenReturn(
			_KEK_IDENTIFIER
		);

		Mockito.when(
			_keyManagerConfiguration.companyKeyCacheTTL()
		).thenReturn(
			companyKeyCacheTTL
		);

		KeyManagerProfile keyManagerProfile = Mockito.mock(
			KeyManagerProfile.class);

		Mockito.when(
			keyManagerProfile.getCompanyKEKProviderId()
		).thenReturn(
			_KEK_PROVIDER_ID
		);

		_keyManagerProfileRegistry = Mockito.mock(
			KeyManagerProfileRegistry.class);

		Mockito.when(
			_keyManagerProfileRegistry.getActiveKeyManagerProfile()
		).thenReturn(
			keyManagerProfile
		);

		_cryptoManager = Mockito.mock(CryptoManager.class);

		ReflectionTestUtil.setFieldValue(
			companyKeyResolverImpl, "_cryptoManager", _cryptoManager);

		ReflectionTestUtil.setFieldValue(
			companyKeyResolverImpl, "_keyAlgorithm", _KEY_ALGORITHM);
		ReflectionTestUtil.setFieldValue(
			companyKeyResolverImpl, "_keyManagerConfiguration",
			_keyManagerConfiguration);
		ReflectionTestUtil.setFieldValue(
			companyKeyResolverImpl, "_keyManagerProfileRegistry",
			_keyManagerProfileRegistry);

		return companyKeyResolverImpl;
	}

	private Map<Long, CompanyKeyCacheEntry> _getCompanyKeyCacheEntries(
		CompanyKeyResolverImpl companyKeyResolverImpl) {

		return ReflectionTestUtil.getFieldValue(
			companyKeyResolverImpl, "_companyKeyCacheEntries");
	}

	private void _mockDecrypt(byte[] ciphertext) throws Exception {
		Mockito.when(
			_cryptoManager.decrypt(
				AdditionalMatchers.aryEq(ciphertext),
				ArgumentMatchers.eq(_COMPANY_ID),
				ArgumentMatchers.eq(
					new KeyReference(
						_KEK_IDENTIFIER, _KEK_PROVIDER_ID,
						KeyReference.Type.CRYPTO)))
		).thenAnswer(
			invocationOnMock -> new CryptoServiceResult<>(
				_serviceIndicator, _KEY_BYTES.clone())
		);
	}

	private void _mockDecryptFailure() throws Exception {
		Mockito.when(
			_cryptoManager.decrypt(
				ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
				ArgumentMatchers.any())
		).thenThrow(
			new CryptoException(RandomTestUtil.randomString())
		);
	}

	private String _serialize(byte[] ciphertext) {
		WrappedCompanyKey wrappedCompanyKey = new WrappedCompanyKey(
			ciphertext, _KEK_IDENTIFIER, _KEK_PROVIDER_ID);

		return wrappedCompanyKey.serialize();
	}

	private void _testDeserializeKey() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		String serializedKey = _serialize(_CIPHERTEXT);

		_mockDecrypt(_CIPHERTEXT);

		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey));
		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey));

		Mockito.verify(
			_cryptoManager, Mockito.times(1)
		).decrypt(
			ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
			ArgumentMatchers.any()
		);
	}

	private void _testDeserializeKeyWithChangedSerializedKey()
		throws Exception {

		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		_mockDecrypt(_CIPHERTEXT);

		companyKeyResolverImpl.deserializeKey(
			_COMPANY_ID, _serialize(_CIPHERTEXT));

		Map<Long, CompanyKeyCacheEntry> companyKeyCacheEntries =
			_getCompanyKeyCacheEntries(companyKeyResolverImpl);

		CompanyKeyCacheEntry companyKeyCacheEntry = companyKeyCacheEntries.get(
			_COMPANY_ID);

		byte[] changedCiphertext = _CIPHERTEXT.clone();

		changedCiphertext[0] = (byte)(changedCiphertext[0] + 1);

		_mockDecrypt(changedCiphertext);

		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(
				_COMPANY_ID, _serialize(changedCiphertext)));

		Mockito.verify(
			_cryptoManager, Mockito.times(2)
		).decrypt(
			ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
			ArgumentMatchers.any()
		);

		Assert.assertTrue(companyKeyCacheEntry.isDestroyed());
	}

	private void _testDeserializeKeyWithDecryptFailure() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		String serializedKey = _serialize(_CIPHERTEXT);

		_mockDecrypt(_CIPHERTEXT);

		companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);

		_mockDecryptFailure();

		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey));

		companyKeyResolverImpl = _createCompanyKeyResolverImpl(
			RandomTestUtil.randomInt(1, 1000));

		_mockDecryptFailure();

		_assertDeserializeKeyFails(companyKeyResolverImpl, serializedKey);
	}

	private void _testDeserializeKeyWithExpiredCacheEntry() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		String serializedKey = _serialize(_CIPHERTEXT);

		_mockDecrypt(_CIPHERTEXT);

		companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);

		Map<Long, CompanyKeyCacheEntry> companyKeyCacheEntries =
			_getCompanyKeyCacheEntries(companyKeyResolverImpl);

		CompanyKeyCacheEntry expiredCompanyKeyCacheEntry =
			new CompanyKeyCacheEntry(
				System.currentTimeMillis() - 1, _KEY_BYTES, serializedKey);

		companyKeyCacheEntries.put(_COMPANY_ID, expiredCompanyKeyCacheEntry);

		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey));

		Mockito.verify(
			_cryptoManager, Mockito.times(2)
		).decrypt(
			ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
			ArgumentMatchers.any()
		);

		Assert.assertTrue(expiredCompanyKeyCacheEntry.isDestroyed());
	}

	private void _testDeserializeKeyWithMalformedSerializedKey() {
		_assertDeserializeKeyFails(
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000)),
			RandomTestUtil.randomString());
	}

	private void _testDeserializeKeyWithoutCache() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(0);

		String serializedKey = _serialize(_CIPHERTEXT);

		_mockDecrypt(_CIPHERTEXT);

		companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);
		companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey);

		Mockito.verify(
			_cryptoManager, Mockito.times(2)
		).decrypt(
			ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
			ArgumentMatchers.any()
		);

		Map<Long, CompanyKeyCacheEntry> companyKeyCacheEntries =
			_getCompanyKeyCacheEntries(companyKeyResolverImpl);

		Assert.assertTrue(companyKeyCacheEntries.isEmpty());
	}

	private void _testSerializeKey() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		byte[][] plaintexts = new byte[2][];

		Mockito.when(
			_cryptoManager.encrypt(
				ArgumentMatchers.eq(_COMPANY_ID),
				ArgumentMatchers.eq(
					new KeyReference(
						_KEK_IDENTIFIER, _KEK_PROVIDER_ID,
						KeyReference.Type.CRYPTO)),
				ArgumentMatchers.any(byte[].class))
		).thenAnswer(
			invocationOnMock -> {
				byte[] plaintext = invocationOnMock.getArgument(2);

				plaintexts[0] = plaintext;
				plaintexts[1] = plaintext.clone();

				return new CryptoServiceResult<>(
					_serviceIndicator, _CIPHERTEXT.clone());
			}
		);

		String serializedKey = companyKeyResolverImpl.serializeKey(
			_COMPANY_ID, _key);

		WrappedCompanyKey wrappedCompanyKey = WrappedCompanyKey.parse(
			_COMPANY_ID, serializedKey);

		Assert.assertArrayEquals(
			_CIPHERTEXT, wrappedCompanyKey.getCiphertext());
		Assert.assertEquals(_KEK_IDENTIFIER, wrappedCompanyKey.getIdentifier());
		Assert.assertEquals(
			_KEK_PROVIDER_ID, wrappedCompanyKey.getProviderId());

		Assert.assertArrayEquals(_KEY_BYTES, plaintexts[1]);
		Assert.assertArrayEquals(new byte[_KEY_BYTES.length], plaintexts[0]);

		Assert.assertEquals(
			_key,
			companyKeyResolverImpl.deserializeKey(_COMPANY_ID, serializedKey));
		Assert.assertTrue(CompanyKeyUtil.isWrappedKey(serializedKey));

		Mockito.verify(
			_cryptoManager, Mockito.never()
		).decrypt(
			ArgumentMatchers.any(), ArgumentMatchers.anyLong(),
			ArgumentMatchers.any()
		);
	}

	private void _testSerializeKeyWithEncryptFailure() throws Exception {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		Mockito.when(
			_cryptoManager.encrypt(
				ArgumentMatchers.anyLong(), ArgumentMatchers.any(),
				ArgumentMatchers.any())
		).thenThrow(
			new CryptoException(RandomTestUtil.randomString())
		);

		_assertSerializeKeyFails(companyKeyResolverImpl);
	}

	private void _testSerializeKeyWithoutKEKIdentifier() {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		Mockito.when(
			_keyManagerConfiguration.companyKEKIdentifier()
		).thenReturn(
			""
		);

		_assertSerializeKeyFails(companyKeyResolverImpl);
	}

	private void _testSerializeKeyWithoutKEKProvider() {
		CompanyKeyResolverImpl companyKeyResolverImpl =
			_createCompanyKeyResolverImpl(RandomTestUtil.randomInt(1, 1000));

		Mockito.when(
			_keyManagerProfileRegistry.getActiveKeyManagerProfile()
		).thenReturn(
			null
		);

		_assertSerializeKeyFails(companyKeyResolverImpl);
	}

	private static final byte[] _CIPHERTEXT = RandomTestUtil.randomBytes();

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final String _KEK_IDENTIFIER = RandomTestUtil.randomString();

	private static final String _KEK_PROVIDER_ID =
		RandomTestUtil.randomString();

	private static final String _KEY_ALGORITHM = "AES";

	private static final byte[] _KEY_BYTES = RandomTestUtil.randomBytes();

	private CryptoManager _cryptoManager;
	private final Key _key = new SecretKeySpec(_KEY_BYTES, _KEY_ALGORITHM);
	private KeyManagerConfiguration _keyManagerConfiguration;
	private KeyManagerProfileRegistry _keyManagerProfileRegistry;
	private final ServiceIndicator _serviceIndicator = new ServiceIndicator(
		true, RandomTestUtil.randomString());

}