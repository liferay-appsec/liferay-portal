/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.opensaml.integration.internal.certificate;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.saml.runtime.certificate.CertificateEntityId;

import java.math.BigInteger;

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rafael Praxedes
 */
public class CertificateToolImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() throws Exception {
		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}
	}

	@Test
	public void testGenerateCertificate() throws Exception {
		X509Certificate x509Certificate1 = _generateTestCertificate();
		X509Certificate x509Certificate2 = _generateTestCertificate();

		Assert.assertEquals(3, x509Certificate1.getVersion());
		Assert.assertEquals(-1, x509Certificate1.getBasicConstraints());

		boolean[] keyUsage = x509Certificate1.getKeyUsage();

		Assert.assertNotNull(keyUsage);
		Assert.assertTrue(keyUsage[0]);
		Assert.assertTrue(keyUsage[2]);

		Assert.assertNotEquals(
			x509Certificate1.getSerialNumber(),
			x509Certificate2.getSerialNumber());
	}

	@Test
	public void testGenerateCertificatePreservesSubjectDN() throws Exception {
		CertificateEntityId certificateEntityId = new CertificateEntityId(
			"Test CN", "Test Org", "Test OU", "Test City", "Test State", "US");

		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 2048);

		Calendar startDate = Calendar.getInstance();

		Calendar endDate = (Calendar)startDate.clone();

		endDate.add(Calendar.DAY_OF_YEAR, 365);

		X509Certificate x509Certificate =
			_certificateToolImpl.generateCertificate(
				keyPair, certificateEntityId, certificateEntityId,
				startDate.getTime(), endDate.getTime(), "SHA256withRSA");

		X500Principal subjectX500Principal =
			x509Certificate.getSubjectX500Principal();

		String subjectDN = subjectX500Principal.getName();

		Assert.assertTrue(subjectDN.contains("CN=Test CN"));
		Assert.assertTrue(subjectDN.contains("O=Test Org"));
		Assert.assertTrue(subjectDN.contains("OU=Test OU"));
		Assert.assertTrue(subjectDN.contains("L=Test City"));
		Assert.assertTrue(subjectDN.contains("ST=Test State"));
		Assert.assertTrue(subjectDN.contains("C=US"));
	}

	@Test
	public void testGenerateKeyPairDefaultMode() throws Exception {
		_assertKeyPair(_certificateToolImpl.generateKeyPair("RSA", 512), 512);
		_assertKeyPair(_certificateToolImpl.generateKeyPair("RSA", 2048), 2048);

		KeyPair dsaKeyPair = _certificateToolImpl.generateKeyPair("DSA", 2048);

		Assert.assertNotNull(dsaKeyPair);

		PublicKey dsaPublicKey = dsaKeyPair.getPublic();

		Assert.assertEquals("DSA", dsaPublicKey.getAlgorithm());
	}

	@Test
	public void testGenerateKeyPairFIPSMode() throws Exception {
		_enableFIPSMode();

		_assertRejected("DSA", 2048);
		_assertRejected("RSA", 512);
		_assertRejected("RSA", 1024);

		_assertKeyPair(_certificateToolImpl.generateKeyPair("RSA", 2048), 2048);
		_assertKeyPair(_certificateToolImpl.generateKeyPair("RSA", 3072), 3072);
		_assertKeyPair(_certificateToolImpl.generateKeyPair("RSA", 4096), 4096);
	}

	private void _assertKeyPair(KeyPair keyPair, int expectedBitLength) {
		Assert.assertNotNull(keyPair);

		PublicKey publicKey = keyPair.getPublic();

		Assert.assertEquals("RSA", publicKey.getAlgorithm());

		RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;

		BigInteger modulus = rsaPublicKey.getModulus();

		Assert.assertEquals(expectedBitLength, modulus.bitLength());
	}

	private void _assertRejected(String algorithm, int keySize) {
		try {
			_certificateToolImpl.generateKeyPair(algorithm, keySize);

			Assert.fail(
				"Expected InvalidParameterException for " + algorithm + " " +
					keySize);
		}
		catch (InvalidParameterException invalidParameterException) {
		}
		catch (Exception exception) {
			Assert.fail(
				"Expected InvalidParameterException but got " +
					exception.getClass());
		}
	}

	private void _enableFIPSMode() {
		_autoCloseable = ReflectionTestUtil.setFieldValueWithAutoCloseable(
			PropsValues.class, "PORTAL_SECURITY_FIPS_MODE_ENABLED", true);
	}

	private X509Certificate _generateTestCertificate() throws Exception {
		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 2048);

		CertificateEntityId certificateEntityId = new CertificateEntityId(
			"Test", null, null, null, null, null);

		Date startDate = new Date();

		Date endDate = new Date(
			startDate.getTime() + (365L * 24 * 60 * 60 * 1000));

		return _certificateToolImpl.generateCertificate(
			keyPair, certificateEntityId, certificateEntityId, startDate,
			endDate, "SHA256withRSA");
	}

	private AutoCloseable _autoCloseable;
	private final CertificateToolImpl _certificateToolImpl =
		new CertificateToolImpl();

}