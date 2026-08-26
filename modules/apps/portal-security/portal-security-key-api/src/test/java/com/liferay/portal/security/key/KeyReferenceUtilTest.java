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
	public void testToKeyReference() {
		_assertKeyReference(
			"${keyRef:provider:identifier}", "identifier", "provider",
			KeyReference.Type.CRYPTO);
		_assertKeyReference(
			"${secretRef:provider:identifier}", "identifier", "provider",
			KeyReference.Type.SECRET);

		_assertKeyReference(
			"${secretRef:*:identifier}", "identifier", "*",
			KeyReference.Type.SECRET);
		_assertKeyReference(
			"${secretRef:provider:identi}fier}", "identi}fier", "provider",
			KeyReference.Type.SECRET);
		_assertKeyReference(
			"${secretRef:aws-kms:arn:aws:kms:us-east-1:123:key/abc}",
			"arn:aws:kms:us-east-1:123:key/abc", "aws-kms",
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
		for (String keyReferenceString :
				new String[] {
					"", "${SecretRef:provider:identifier}", "${secretRef:",
					"${secretRef::identifier}", "${secretRef:provider:   }",
					"${secretRef:provider:identifier",
					"${secretRef:provider:identifier}trailing",
					"${secretRef:provider:null}", "${secretRef:provider:}",
					"${secretRef:provider}",
					"${secretRef:pro}vider:identifier}", "${secretRef:}",
					"${secretRef}", "${}", "abc", null
				}) {

			Assert.assertThrows(
				keyReferenceString, IllegalArgumentException.class,
				() -> KeyReferenceUtil.toKeyReference(keyReferenceString));
		}
	}

	private void _assertKeyReference(
		String keyReferenceString, String identifier, String providerId,
		KeyReference.Type type) {

		KeyReference keyReference = KeyReferenceUtil.toKeyReference(
			keyReferenceString);

		Assert.assertEquals(identifier, keyReference.getIdentifier());
		Assert.assertEquals(providerId, keyReference.getProviderId());
		Assert.assertEquals(type, keyReference.getType());
		Assert.assertEquals(
			keyReferenceString,
			KeyReferenceUtil.toKeyReferenceString(keyReference));
	}

}