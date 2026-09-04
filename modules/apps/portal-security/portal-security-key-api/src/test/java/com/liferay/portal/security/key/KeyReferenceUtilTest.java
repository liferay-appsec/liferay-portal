/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 * @author Pedro Victor Silvestre
 */
public class KeyReferenceUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsKeyReference() {
		Assert.assertFalse(KeyReferenceUtil.isKeyReference(null));
		Assert.assertFalse(
			KeyReferenceUtil.isKeyReference(RandomTestUtil.randomString()));
		Assert.assertTrue(
			KeyReferenceUtil.isKeyReference("${keyRef:provider:identifier}"));
		Assert.assertTrue(
			KeyReferenceUtil.isKeyReference(
				"${secretRef:provider:identifier}"));
	}

	@Test
	public void testIsValidKeyReferenceWithInvalidKeyReference() {
		for (String keyReferenceString : _KEY_REFERENCE_STRINGS_INVALID) {
			Assert.assertFalse(
				keyReferenceString,
				KeyReferenceUtil.isValidKeyReference(keyReferenceString));
		}
	}

	@Test
	public void testToKeyReference() {
		_assertKeyReference(
			"identifier", "${keyRef:provider:identifier}", "provider",
			KeyReference.Type.CRYPTO);
		_assertKeyReference(
			"identifier", "${secretRef:*:identifier}", "*",
			KeyReference.Type.SECRET);
		_assertKeyReference(
			"arn:aws:kms:us-east-1:123:key/abc",
			"${secretRef:aws-kms:arn:aws:kms:us-east-1:123:key/abc}", "aws-kms",
			KeyReference.Type.SECRET);
		_assertKeyReference(
			"identifier", "${secretRef:provider:identifier}", "provider",
			KeyReference.Type.SECRET);
		_assertKeyReference(
			"identi}fier", "${secretRef:provider:identi}fier}", "provider",
			KeyReference.Type.SECRET);

		for (KeyReference.Type type : KeyReference.Type.values()) {
			KeyReference keyReference = new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				type);

			Assert.assertEquals(
				keyReference,
				KeyReferenceUtil.toKeyReference(
					KeyReferenceUtil.toKeyReferenceString(keyReference)));
		}
	}

	@Test
	public void testToKeyReferenceWithInvalidKeyReference() {
		for (String keyReferenceString : _KEY_REFERENCE_STRINGS_INVALID) {
			Assert.assertThrows(
				keyReferenceString, IllegalArgumentException.class,
				() -> KeyReferenceUtil.toKeyReference(keyReferenceString));
		}
	}

	private void _assertKeyReference(
		String identifier, String keyReferenceString, String providerId,
		KeyReference.Type type) {

		Assert.assertTrue(
			keyReferenceString,
			KeyReferenceUtil.isValidKeyReference(keyReferenceString));

		KeyReference keyReference = KeyReferenceUtil.toKeyReference(
			keyReferenceString);

		Assert.assertEquals(identifier, keyReference.getIdentifier());
		Assert.assertEquals(
			keyReferenceString,
			KeyReferenceUtil.toKeyReferenceString(keyReference));
		Assert.assertEquals(providerId, keyReference.getProviderId());
		Assert.assertEquals(type, keyReference.getType());
	}

	private static final String[] _KEY_REFERENCE_STRINGS_INVALID = {
		"", "${SecretRef:provider:identifier}", "${secretRef:",
		"${secretRef::identifier}", "${secretRef:provider:   }",
		"${secretRef:provider:identifier",
		"${secretRef:provider:identifier}trailing",
		"${secretRef:provider:null}", "${secretRef:provider:}",
		"${secretRef:provider}", "${secretRef:pro}vider:identifier}",
		"${secretRef:}", "${secretRef}", "${}", "abc", null
	};

}