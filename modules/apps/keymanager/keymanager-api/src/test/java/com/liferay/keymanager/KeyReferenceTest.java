/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
public class KeyReferenceTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testEqualsAndHashCode() {
		KeyReference keyReference1 = KeyReference.fromString("${keyRef:p1:i1}");
		KeyReference keyReference2 = KeyReference.fromString("${keyRef:p1:i1}");
		KeyReference keyReference3 = KeyReference.fromString(
			"${secretRef:p1:i1}");
		KeyReference keyReference4 = KeyReference.fromString("${keyRef:p2:i1}");

		Assert.assertEquals(keyReference1, keyReference2);
		Assert.assertEquals(keyReference1.hashCode(), keyReference2.hashCode());

		Assert.assertNotEquals(keyReference1, keyReference3);
		Assert.assertNotEquals(keyReference1, keyReference4);
		Assert.assertNotEquals(keyReference1, null);
		Assert.assertNotEquals(keyReference1, "string");
	}

	@Test
	public void testFromStringAnyProvider() {
		String raw = "${secretRef:*:my-secret}";

		KeyReference keyReference = KeyReference.fromString(raw);

		Assert.assertNotNull(keyReference);
		Assert.assertEquals(
			KeyReference.ANY_PROVIDER, keyReference.getProviderId());
		Assert.assertEquals("my-secret", keyReference.getIdentifier());
		Assert.assertEquals(raw, keyReference.toString());
	}

	@Test
	public void testFromStringCrypto() {
		String raw = "${keyRef:keystore:master-key}";

		KeyReference keyReference = KeyReference.fromString(raw);

		Assert.assertNotNull(keyReference);
		Assert.assertEquals(KeyReference.Type.CRYPTO, keyReference.getType());
		Assert.assertEquals("keystore", keyReference.getProviderId());
		Assert.assertEquals("master-key", keyReference.getIdentifier());
		Assert.assertEquals(raw, keyReference.toString());

		// Identifier may contain colons — the third capture group is greedy

		raw = "${keyRef:gcp:my.key:v1}";

		keyReference = KeyReference.fromString(raw);

		Assert.assertEquals("gcp", keyReference.getProviderId());
		Assert.assertEquals("my.key:v1", keyReference.getIdentifier());
	}

	@Test
	public void testFromStringInvalid() {
		Assert.assertNull(KeyReference.fromString(null));
		Assert.assertNull(KeyReference.fromString("invalid"));
		Assert.assertNull(KeyReference.fromString("${unknownRef:provider:id}"));
		Assert.assertNull(KeyReference.fromString("${keyRef:onlyone}"));
	}

	@Test
	public void testFromStringSecret() {
		String raw = "${secretRef:db:jdbc-password}";

		KeyReference keyReference = KeyReference.fromString(raw);

		Assert.assertNotNull(keyReference);
		Assert.assertEquals(KeyReference.Type.SECRET, keyReference.getType());
		Assert.assertEquals("db", keyReference.getProviderId());
		Assert.assertEquals("jdbc-password", keyReference.getIdentifier());
		Assert.assertEquals(raw, keyReference.toString());
	}

	@Test
	public void testIsKeyReference() {
		Assert.assertTrue(KeyReference.isKeyReference("${keyRef:p:i}"));
		Assert.assertTrue(KeyReference.isKeyReference("${secretRef:*:i}"));
		Assert.assertFalse(KeyReference.isKeyReference("not a ref"));
		Assert.assertFalse(KeyReference.isKeyReference(null));
	}

}