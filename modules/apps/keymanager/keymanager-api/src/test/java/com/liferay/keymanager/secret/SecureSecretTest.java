/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.secret;

import com.liferay.keymanager.KeyReference;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
public class SecureSecretTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDestroyIsIdempotent() {
		SecureSecret secureSecret = new SecureSecret(
			new byte[] {1, 2, 3}, _keyReference());

		secureSecret.destroy();
		secureSecret.destroy();

		Assert.assertTrue(secureSecret.isDestroyed());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetBytesAfterDestroyThrows() {
		SecureSecret secureSecret = new SecureSecret(
			new byte[] {1, 2, 3}, _keyReference());

		secureSecret.close();

		secureSecret.getBytes();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCharsAfterDestroyThrows() {
		SecureSecret secureSecret = new SecureSecret(
			_keyReference(), "password");

		secureSecret.close();

		secureSecret.getChars();
	}

	@Test
	public void testSecureSecretFromChars() {
		char[] data = {'a', 'b', 'c', 'd'};

		SecureSecret secureSecret = new SecureSecret(data, _keyReference());

		Assert.assertArrayEquals(data, secureSecret.getChars());
		Assert.assertTrue(secureSecret.getBytes().length > 0);
	}

	@Test
	public void testSecureSecretFromString() {
		String data = "password";

		SecureSecret secureSecret = new SecureSecret(_keyReference(), data);

		Assert.assertArrayEquals(data.toCharArray(), secureSecret.getChars());
		Assert.assertTrue(secureSecret.getBytes().length > 0);
	}

	@Test
	public void testSecureSecretImmutable() {
		byte[] data = {1, 2, 3, 4};

		SecureSecret secureSecret = new SecureSecret(data, _keyReference());

		// Constructor must copy the input

		data[0] = 9;

		Assert.assertEquals(1, secureSecret.getBytes()[0]);
	}

	@Test
	public void testSecureSecretReturnsSameInstance() {
		byte[] data = {1, 2, 3, 4};

		SecureSecret secureSecret = new SecureSecret(data, _keyReference());

		byte[] internalBytes1 = secureSecret.getBytes();
		byte[] internalBytes2 = secureSecret.getBytes();

		Assert.assertSame(internalBytes1, internalBytes2);
	}

	@Test
	public void testSecureSecretZeroing() {
		byte[] data = {1, 2, 3, 4};

		SecureSecret secureSecret = new SecureSecret(data, _keyReference());

		byte[] internalBytes = secureSecret.getBytes();

		Assert.assertArrayEquals(data, internalBytes);

		secureSecret.close();

		for (byte b : internalBytes) {
			Assert.assertEquals(0, b);
		}
	}

	private KeyReference _keyReference() {
		return KeyReference.fromString("${secretRef:db:alias}");
	}

}