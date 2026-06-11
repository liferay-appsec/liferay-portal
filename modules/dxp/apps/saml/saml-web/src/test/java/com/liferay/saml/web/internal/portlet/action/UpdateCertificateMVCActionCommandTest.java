/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.web.internal.portlet.action;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.math.BigInteger;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class UpdateCertificateMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_actionCommand = new UpdateCertificateMVCActionCommand();
	}

	@Test
	public void testIsFIPSCompliant() {
		Assert.assertFalse(_isFIPSCompliant(_mockCertificateWithDSAKey()));
		Assert.assertFalse(_isFIPSCompliant(_mockCertificateWithRSAKey(1024)));
		Assert.assertTrue(_isFIPSCompliant(_mockCertificateWithRSAKey(2048)));
		Assert.assertTrue(_isFIPSCompliant(_mockCertificateWithRSAKey(4096)));

		X509Certificate certificate = Mockito.mock(X509Certificate.class);

		PublicKey publicKey = Mockito.mock(PublicKey.class);

		Mockito.when(
			publicKey.getAlgorithm()
		).thenReturn(
			"UNKNOWN"
		);

		Mockito.when(
			certificate.getPublicKey()
		).thenReturn(
			publicKey
		);

		Assert.assertFalse(_isFIPSCompliant(certificate));
	}

	private boolean _isFIPSCompliant(X509Certificate certificate) {
		return ReflectionTestUtil.invoke(
			_actionCommand, "_isFIPSCompliant",
			new Class<?>[] {X509Certificate.class}, certificate);
	}

	private X509Certificate _mockCertificateWithDSAKey() {
		X509Certificate certificate = Mockito.mock(X509Certificate.class);

		DSAPublicKey publicKey = Mockito.mock(DSAPublicKey.class);

		Mockito.when(
			certificate.getPublicKey()
		).thenReturn(
			publicKey
		);

		return certificate;
	}

	private X509Certificate _mockCertificateWithRSAKey(int bitLength) {
		X509Certificate certificate = Mockito.mock(X509Certificate.class);

		RSAPublicKey publicKey = Mockito.mock(RSAPublicKey.class);

		Mockito.when(
			publicKey.getModulus()
		).thenReturn(
			BigInteger.ONE.shiftLeft(bitLength - 1)
		);

		Mockito.when(
			certificate.getPublicKey()
		).thenReturn(
			publicKey
		);

		return certificate;
	}

	private UpdateCertificateMVCActionCommand _actionCommand;

}