/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.web.internal.portlet.action;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.math.BigInteger;

import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;

import org.junit.After;
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

	@After
	public void tearDown() throws Exception {
		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}
	}

	@Test
	public void testIsFIPSCompliantCertificateWithDSAKey() {
		X509Certificate certificate = _mockCertificateWithDSAKey();

		Assert.assertFalse(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithECP192Key() {
		X509Certificate certificate = _mockCertificateWithECKey(192);

		Assert.assertFalse(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithECP256Key() {
		X509Certificate certificate = _mockCertificateWithECKey(256);

		Assert.assertTrue(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithRSA1024Key() {
		X509Certificate certificate = _mockCertificateWithRSAKey(1024);

		Assert.assertFalse(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithRSA2048Key() {
		X509Certificate certificate = _mockCertificateWithRSAKey(2048);

		Assert.assertTrue(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithRSA4096Key() {
		X509Certificate certificate = _mockCertificateWithRSAKey(4096);

		Assert.assertTrue(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSCompliantCertificateWithUnknownAlgorithm() {
		X509Certificate certificate = Mockito.mock(X509Certificate.class);

		java.security.PublicKey publicKey = Mockito.mock(
			java.security.PublicKey.class);

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

		Assert.assertFalse(_invokeFIPSCompliantCheck(certificate));
	}

	@Test
	public void testIsFIPSModeDisabled() {
		boolean result = ReflectionTestUtil.invoke(
			_actionCommand, "_isFIPSModeEnabled", new Class<?>[0]);

		Assert.assertFalse(result);
	}

	@Test
	public void testIsFIPSModeEnabled() {
		_enableFIPSMode();

		boolean result = ReflectionTestUtil.invoke(
			_actionCommand, "_isFIPSModeEnabled", new Class<?>[0]);

		Assert.assertTrue(result);
	}

	private void _enableFIPSMode() {
		_autoCloseable =
			ReflectionTestUtil.setFieldValueWithAutoCloseable(
				PropsValues.class, "PORTAL_SECURITY_FIPS_MODE_ENABLED", true);
	}

	private boolean _invokeFIPSCompliantCheck(X509Certificate certificate) {
		return ReflectionTestUtil.invoke(
			_actionCommand, "_isFIPSCompliantCertificate",
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

	private X509Certificate _mockCertificateWithECKey(int orderBitLength) {
		X509Certificate certificate = Mockito.mock(X509Certificate.class);

		ECPublicKey publicKey = Mockito.mock(ECPublicKey.class);

		ECParameterSpec parameterSpec = Mockito.mock(ECParameterSpec.class);

		Mockito.when(
			parameterSpec.getOrder()
		).thenReturn(
			BigInteger.ONE.shiftLeft(orderBitLength - 1)
		);

		Mockito.when(
			publicKey.getParams()
		).thenReturn(
			parameterSpec
		);

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
	private AutoCloseable _autoCloseable;

}
