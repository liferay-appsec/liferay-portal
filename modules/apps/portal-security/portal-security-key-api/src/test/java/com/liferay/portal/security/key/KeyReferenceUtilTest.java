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
	public void testToKeyReferenceCrypto() {
		_testRoundTrip("${keyRef:", KeyReference.Type.CRYPTO);
	}

	@Test
	public void testToKeyReferenceSecret() {
		_testRoundTrip("${secretRef:", KeyReference.Type.SECRET);
	}

	private void _testRoundTrip(String prefix, KeyReference.Type type) {
		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), type);

		String referenceString = KeyReferenceUtil.toReferenceString(
			keyReference);

		Assert.assertEquals(
			keyReference, KeyReferenceUtil.toKeyReference(referenceString));
		Assert.assertTrue(referenceString, referenceString.startsWith(prefix));
	}

}