/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.opensaml.integration.internal.certificate;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.saml.runtime.certificate.CertificateEntityId;

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import java.util.Calendar;
import java.util.Date;

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
	public void testGenerateCertificateHasBasicConstraints() throws Exception {
		X509Certificate x509Certificate = _generateTestCertificate();

		Assert.assertEquals(-1, x509Certificate.getBasicConstraints());
	}

	@Test
	public void testGenerateCertificateHasKeyUsage() throws Exception {
		X509Certificate x509Certificate = _generateTestCertificate();

		boolean[] keyUsage = x509Certificate.getKeyUsage();

		Assert.assertNotNull(keyUsage);
		Assert.assertTrue(keyUsage[0]);
		Assert.assertTrue(keyUsage[2]);
	}

	@Test
	public void testGenerateCertificateHasRandomSerialNumber()
		throws Exception {

		X509Certificate certificate1 = _generateTestCertificate();
		X509Certificate certificate2 = _generateTestCertificate();

		Assert.assertNotEquals(
			certificate1.getSerialNumber(), certificate2.getSerialNumber());
	}

	@Test
	public void testGenerateCertificateIsX509v3() throws Exception {
		X509Certificate x509Certificate = _generateTestCertificate();

		Assert.assertEquals(3, x509Certificate.getVersion());
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

		String subjectDN = x509Certificate.getSubjectX500Principal().getName();

		Assert.assertTrue(subjectDN.contains("CN=Test CN"));
		Assert.assertTrue(subjectDN.contains("O=Test Org"));
		Assert.assertTrue(subjectDN.contains("OU=Test OU"));
		Assert.assertTrue(subjectDN.contains("L=Test City"));
		Assert.assertTrue(subjectDN.contains("ST=Test State"));
		Assert.assertTrue(subjectDN.contains("C=US"));
	}

	@Test
	public void testGenerateKeyPairDSA2048() throws Exception {
		KeyPair keyPair = _certificateToolImpl.generateKeyPair("DSA", 2048);

		Assert.assertNotNull(keyPair);
		Assert.assertEquals("DSA", keyPair.getPublic().getAlgorithm());
	}

	@Test(expected = InvalidParameterException.class)
	public void testGenerateKeyPairFIPSModeDSA() throws Exception {
		_enableFIPSMode();

		_certificateToolImpl.generateKeyPair("DSA", 2048);
	}

	@Test
	public void testGenerateKeyPairFIPSModeRSA2048() throws Exception {
		_enableFIPSMode();

		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 2048);

		Assert.assertNotNull(keyPair);
	}

	@Test
	public void testGenerateKeyPairFIPSModeRSA3072() throws Exception {
		_enableFIPSMode();

		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 3072);

		Assert.assertNotNull(keyPair);
	}

	@Test
	public void testGenerateKeyPairFIPSModeRSA4096() throws Exception {
		_enableFIPSMode();

		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 4096);

		Assert.assertNotNull(keyPair);
	}

	@Test(expected = InvalidParameterException.class)
	public void testGenerateKeyPairFIPSModeRSA512() throws Exception {
		_enableFIPSMode();

		_certificateToolImpl.generateKeyPair("RSA", 512);
	}

	@Test(expected = InvalidParameterException.class)
	public void testGenerateKeyPairFIPSModeRSA1024() throws Exception {
		_enableFIPSMode();

		_certificateToolImpl.generateKeyPair("RSA", 1024);
	}

	@Test
	public void testGenerateKeyPairRSA2048() throws Exception {
		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 2048);

		Assert.assertNotNull(keyPair);
		Assert.assertEquals("RSA", keyPair.getPublic().getAlgorithm());
		Assert.assertEquals(
			2048,
			((RSAPublicKey)keyPair.getPublic()).getModulus().bitLength());
	}

	@Test
	public void testGenerateKeyPairRSA512() throws Exception {
		KeyPair keyPair = _certificateToolImpl.generateKeyPair("RSA", 512);

		Assert.assertNotNull(keyPair);
	}

	private void _enableFIPSMode() {
		_autoCloseable =
			ReflectionTestUtil.setFieldValueWithAutoCloseable(
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
