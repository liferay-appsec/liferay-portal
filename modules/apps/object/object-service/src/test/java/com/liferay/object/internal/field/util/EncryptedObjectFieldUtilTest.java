/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.field.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Manuele Castro
 */
public class EncryptedObjectFieldUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDecrypt() throws Exception {
		Assert.assertEquals(
			StringPool.BLANK,
			EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key, StringPool.BLANK));
		Assert.assertNull(
			EncryptedObjectFieldUtil.decrypt(_encryptor, _key, null));

		Mockito.doThrow(
			new EncryptorException()
		).when(
			_encryptor
		).decryptAuthenticated(
			Mockito.eq(_key), Mockito.anyString()
		);

		String ciphertext = RandomTestUtil.randomString();
		String value = RandomTestUtil.randomString();

		Mockito.doReturn(
			value
		).when(
			_encryptor
		).decryptAuthenticated(
			_key, ciphertext
		);

		String markedCiphertext =
			_MARKER_PREFIX + ciphertext + StringPool.CLOSE_CURLY_BRACE;

		Assert.assertEquals(
			value,
			EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key,
				_encryptECB(value.getBytes(StandardCharsets.UTF_8))));
		Assert.assertEquals(
			value,
			EncryptedObjectFieldUtil.decrypt(_encryptor, _key, ciphertext));
		Assert.assertEquals(
			value,
			EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key, markedCiphertext));

		Assert.assertThrows(
			EncryptorException.class,
			() -> EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key, RandomTestUtil.randomString()));
		Assert.assertThrows(
			EncryptorException.class,
			() -> EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key,
				_MARKER_PREFIX + RandomTestUtil.randomString() +
					StringPool.CLOSE_CURLY_BRACE));
		Assert.assertThrows(
			EncryptorException.class,
			() -> EncryptedObjectFieldUtil.decrypt(
				_encryptor, _key,
				_encryptECB(new byte[] {(byte)0xC3, (byte)0x28})));

		Key blowfishKey = new SecretKeySpec(new byte[16], "Blowfish");

		Mockito.doReturn(
			value
		).when(
			_encryptor
		).decrypt(
			blowfishKey, ciphertext
		);

		Assert.assertEquals(
			value,
			EncryptedObjectFieldUtil.decrypt(
				_encryptor, blowfishKey, ciphertext));
		Assert.assertEquals(
			value,
			EncryptedObjectFieldUtil.decrypt(
				_encryptor, blowfishKey, markedCiphertext));
	}

	@Test
	public void testEncrypt() throws Exception {
		String ciphertext = RandomTestUtil.randomString();
		String value = RandomTestUtil.randomString();

		Mockito.doReturn(
			ciphertext
		).when(
			_encryptor
		).encryptAuthenticated(
			_key, value
		);

		Assert.assertEquals(
			_MARKER_PREFIX + ciphertext + StringPool.CLOSE_CURLY_BRACE,
			EncryptedObjectFieldUtil.encrypt(_encryptor, _key, value));

		Key blowfishKey = new SecretKeySpec(new byte[16], "Blowfish");

		Mockito.doReturn(
			ciphertext
		).when(
			_encryptor
		).encrypt(
			blowfishKey, value
		);

		Assert.assertEquals(
			ciphertext,
			EncryptedObjectFieldUtil.encrypt(_encryptor, blowfishKey, value));
	}

	@Test
	public void testIsAESKey() {
		Assert.assertTrue(
			EncryptedObjectFieldUtil.isAESKey(
				new SecretKeySpec(new byte[16], "AES")));
		Assert.assertFalse(
			EncryptedObjectFieldUtil.isAESKey(
				new SecretKeySpec(new byte[16], "Blowfish")));
	}

	private String _encryptECB(byte[] bytes) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

		cipher.init(Cipher.ENCRYPT_MODE, _key);

		return Base64.encode(cipher.doFinal(bytes));
	}

	private static final String _MARKER_PREFIX = "${aesGcm:v1:";

	private final Encryptor _encryptor = Mockito.mock(Encryptor.class);
	private final Key _key = new SecretKeySpec(
		Arrays.copyOf(RandomTestUtil.randomBytes(), 16), "AES");

}