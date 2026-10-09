/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.util;

import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class DigesterUtilTest {

	@Test
	public void testDigest() throws Exception {
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.SHA_1,
			() -> Assert.assertEquals(
				"b250e2cb60bf01591b8fd6ce9568e02c255378da",
				DigesterUtil.digest(
					CryptoFunctionApprovalType.ALLOWED, DigesterUtil.SHA_1,
					"Liferay", "Portal")));
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.SHA_256,
			() -> Assert.assertEquals(
				"15cce9decbf65e7962be6cf0b6a2b19d" +
					"46756ac87c5cc47d55dde1f7a5a652ee",
				DigesterUtil.digest(
					DigesterUtil.SHA_256, "Liferay", "Portal")));
	}

	@Test
	public void testDigestBase64() throws Exception {
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.SHA_1,
			() -> Assert.assertEquals(
				"SiIOm0MgopeZQ458YI23LpBn76E=",
				DigesterUtil.digestBase64(
					CryptoFunctionApprovalType.ALLOWED, DigesterUtil.SHA_1,
					new UnsyncByteArrayInputStream(_LIFERAY_BYTES))));
	}

	@Test
	public void testDigestHex() throws Exception {
		FIPSModeTestUtil.assertAlgorithmSwitch(
			DigesterUtil.SHA, CryptoUtil.class, DigesterUtil.SHA_256,
			algorithm -> CryptoUtil.createMessageDigest(
				algorithm, CryptoFunctionApprovalType.APPROVED),
			() -> DigesterUtil.digestHex(RandomTestUtil.randomString()));
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.SHA_256,
			() -> DigesterUtil.digestHex(
				DigesterUtil.SHA_256, RandomTestUtil.randomString()));

		Assert.assertEquals(
			"c3b54c22a323527935b3fec34608a2e0",
			DigesterUtil.digestHex(DigesterUtil.MD5, "Liferay"));

		try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_assertSystemException(
				NoSuchAlgorithmException.class,
				() -> DigesterUtil.digestHex(DigesterUtil.MD5, "Liferay"));
			_assertSystemException(
				NoSuchAlgorithmException.class,
				() -> DigesterUtil.digestHex(DigesterUtil.SHA_1, "Liferay"));

			mockedStatic.verify(
				() -> FIPSAuditUtil.write(Mockito.any()), Mockito.times(2));
		}

		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.MD5,
			() -> Assert.assertEquals(
				"c3b54c22a323527935b3fec34608a2e0",
				DigesterUtil.digestHex(
					CryptoFunctionApprovalType.ALLOWED, DigesterUtil.MD5,
					new UnsyncByteArrayInputStream(_LIFERAY_BYTES))));
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.MD5,
			() -> Assert.assertEquals(
				"c3b54c22a323527935b3fec34608a2e0",
				DigesterUtil.digestHex(
					CryptoFunctionApprovalType.ALLOWED, DigesterUtil.MD5,
					"Liferay")));
	}

	@Test
	public void testDigestRaw() throws Exception {
		FIPSModeTestUtil.assertAlgorithm(
			DigesterUtil.MD5,
			() -> Assert.assertEquals(
				"w7VMIqMjUnk1s/7DRgii4A==",
				Base64.encode(
					DigesterUtil.digestRaw(
						CryptoFunctionApprovalType.ALLOWED, DigesterUtil.MD5,
						ByteBuffer.wrap(_LIFERAY_BYTES)))));

		_assertSystemException(
			IOException.class,
			() -> DigesterUtil.digestRaw(
				DigesterUtil.SHA_256,
				new InputStream() {

					@Override
					public int read() throws IOException {
						throw new IOException();
					}

				}));
		_assertSystemException(
			NoSuchAlgorithmException.class,
			() -> DigesterUtil.digestRaw(
				RandomTestUtil.randomString(),
				ByteBuffer.wrap(RandomTestUtil.randomBytes())));
		_assertSystemException(
			NoSuchAlgorithmException.class,
			() -> DigesterUtil.digestRaw(
				RandomTestUtil.randomString(), RandomTestUtil.randomString()));
	}

	private void _assertSystemException(
		Class<? extends Exception> expectedExceptionClass,
		ThrowingRunnable throwingRunnable) {

		SystemException systemException = Assert.assertThrows(
			SystemException.class, throwingRunnable);

		Assert.assertTrue(
			expectedExceptionClass.isInstance(systemException.getCause()));
	}

	private static final byte[] _LIFERAY_BYTES = {
		'L', 'i', 'f', 'e', 'r', 'a', 'y'
	};

}