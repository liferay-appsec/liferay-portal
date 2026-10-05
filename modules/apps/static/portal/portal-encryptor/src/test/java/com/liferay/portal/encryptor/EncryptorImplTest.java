/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.encryptor;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mika Koivisto
 */
public class EncryptorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDecrypt() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Assert.assertEquals(
			_GOLDEN_PLAINTEXT,
			encryptor.decrypt(
				encryptor.deserializeKey(_GOLDEN_KEY_AES_128),
				_GOLDEN_ENCRYPTED_STRING_AES_ECB));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertEquals(
				_GOLDEN_PLAINTEXT,
				encryptor.decrypt(
					encryptor.deserializeKey(_GOLDEN_KEY_AES_256),
					_GOLDEN_ENCRYPTED_STRING_AES_GCM));
		}
	}

	@Test
	public void testDecryptAuthenticated() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Key key = encryptor.deserializeKey(_GOLDEN_KEY_AES_256);

		Assert.assertEquals(
			_GOLDEN_PLAINTEXT,
			encryptor.decryptAuthenticated(
				key, _GOLDEN_ENCRYPTED_STRING_AES_GCM));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertEquals(
				_GOLDEN_PLAINTEXT,
				encryptor.decryptAuthenticated(
					key, _GOLDEN_ENCRYPTED_STRING_AES_GCM));
		}
	}

	@Test
	public void testEncrypt() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Encryptor encryptor = new EncryptorImpl();

			Key key = encryptor.generateKey();

			String encryptedString1 = encryptor.encrypt(key, _PLAINTEXT);
			String encryptedString2 = encryptor.encrypt(key, _PLAINTEXT);

			Assert.assertNotEquals(encryptedString1, encryptedString2);

			Assert.assertEquals(
				_PLAINTEXT, encryptor.decrypt(key, encryptedString1));
			Assert.assertEquals(
				_PLAINTEXT, encryptor.decrypt(key, encryptedString2));

			Assert.assertThrows(
				SecurityException.class,
				() -> encryptor.decryptUnencodedAsBytes(
					new SecretKeySpec(new byte[8], "AES"),
					_PLAINTEXT.getBytes()));
			Assert.assertThrows(
				SecurityException.class,
				() -> encryptor.encryptUnencoded(
					new SecretKeySpec(new byte[8], "AES"),
					_PLAINTEXT.getBytes()));
		}
	}

	@Test
	public void testEncryptAuthenticated() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Key key = encryptor.deserializeKey(_GOLDEN_KEY_AES_256);

		byte[] encryptedBytes = Base64.decode(
			encryptor.encryptAuthenticated(key, _PLAINTEXT));

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		cipher.init(
			Cipher.DECRYPT_MODE, key,
			new GCMParameterSpec(128, encryptedBytes, 0, 12));

		Assert.assertEquals(
			_PLAINTEXT,
			new String(
				cipher.doFinal(encryptedBytes, 12, encryptedBytes.length - 12),
				EncryptorImpl.ENCODING));

		FIPSModeTestUtil.assertAlgorithm(
			"AES/GCM/NoPadding",
			() -> encryptor.encryptAuthenticated(key, _PLAINTEXT));
	}

	@Test
	public void testEncryptAuthenticatedDetectsTampering() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Key key = encryptor.generateKey();

		String encryptedString1 = encryptor.encryptAuthenticated(
			key, _PLAINTEXT);
		String encryptedString2 = encryptor.encryptAuthenticated(
			key, _PLAINTEXT);

		Assert.assertNotEquals(encryptedString1, encryptedString2);

		Assert.assertEquals(
			_PLAINTEXT, encryptor.decryptAuthenticated(key, encryptedString1));

		byte[] encryptedBytes = Base64.decode(encryptedString1);

		encryptedBytes[encryptedBytes.length - 1] ^= 0x01;

		Assert.assertThrows(
			EncryptorException.class,
			() -> encryptor.decryptAuthenticated(
				key, Base64.encode(encryptedBytes)));
	}

	@Test
	public void testEncryptWithKeysOfTheSameHashCode() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Key key1 = encryptor.generateKey();

		byte[] bytes = key1.getEncoded();

		bytes[0] ^= 0x5A;

		Key key2 = new SecretKeySpec(bytes, key1.getAlgorithm());

		Assert.assertEquals(key1.hashCode(), key2.hashCode());

		encryptor.encrypt(key1, _PLAINTEXT);

		Assert.assertEquals(
			_PLAINTEXT,
			encryptor.decrypt(key2, encryptor.encrypt(key2, _PLAINTEXT)));
	}

	@Test
	public void testSerializeKey() throws Exception {
		Encryptor encryptor = new EncryptorImpl();

		Key key = encryptor.generateKey();

		String encryptedString = encryptor.encrypt(key, _PLAINTEXT);

		String serializedKey = encryptor.serializeKey(key);

		key = encryptor.deserializeKey(serializedKey);

		Assert.assertEquals(
			_PLAINTEXT, encryptor.decrypt(key, encryptedString));
	}

	private static final String _GOLDEN_ENCRYPTED_STRING_AES_ECB =
		"c93sY840K8KS+cRbav/aGBR5THb7pFXD4NJkGq5XtY4=";

	private static final String _GOLDEN_ENCRYPTED_STRING_AES_GCM =
		"MDEyMzQ1Njc4OTo7shNVjOciWcNHW7sMm3pih41w/nFhDPZPvawbryCDpIkNgHos" +
			"D4eB9w==";

	private static final String _GOLDEN_KEY_AES_128 =
		"EBESExQVFhcYGRobHB0eHw==";

	private static final String _GOLDEN_KEY_AES_256 =
		"oKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr8=";

	private static final String _GOLDEN_PLAINTEXT = "Liferay golden plaintext";

	private static final String _PLAINTEXT = RandomTestUtil.randomString();

}