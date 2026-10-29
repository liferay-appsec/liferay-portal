/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Provider;

import org.cyclonedx.model.component.crypto.AlgorithmProperties;
import org.cyclonedx.model.component.crypto.enums.Mode;
import org.cyclonedx.model.component.crypto.enums.Padding;
import org.cyclonedx.model.component.crypto.enums.Primitive;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testFetchJCAClassName() {
		Assert.assertNull(CBOMUtil.fetchJCAClassName("Cipher"));
		Assert.assertNull(
			CBOMUtil.fetchJCAClassName("com/liferay/test/Cipher"));
		Assert.assertEquals(
			"MessageDigest",
			CBOMUtil.fetchJCAClassName("java/security/MessageDigest"));
		Assert.assertEquals(
			"Cipher", CBOMUtil.fetchJCAClassName("javax/crypto/Cipher"));
		Assert.assertNull(CBOMUtil.fetchJCAClassName("javax/crypto/SecretKey"));
		Assert.assertEquals(
			"SSLContext",
			CBOMUtil.fetchJCAClassName("javax/net/ssl/SSLContext"));
	}

	@Test
	public void testFetchProvider() {
		_testFetchProvider("AES", "SunJCE", "KeyGenerator");
		_testFetchProvider("AES/GCM/NoPadding", "SunJCE", "Cipher");
		_testFetchProvider("EC", "SunEC", "KeyPairGenerator");
		_testFetchProvider("ECDH", "SunEC", "KeyAgreement");
		_testFetchProvider("HmacSHA256", "SunJCE", "Mac");
		_testFetchProvider(
			"PBKDF2WithHmacSHA256", "SunJCE", "SecretKeyFactory");
		_testFetchProvider("PKCS12", "SUN", "KeyStore");
		_testFetchProvider("PKIX", "SunJSSE", "KeyManagerFactory");
		_testFetchProvider("PKIX", "SunJSSE", "TrustManagerFactory");
		_testFetchProvider("RSA", "SunRsaSign", "KeyFactory");
		_testFetchProvider("SHA-256", "SUN", "MessageDigest");
		_testFetchProvider("SHA1PRNG", "SUN", "SecureRandom");
		_testFetchProvider("SHA256withRSA", "SunRsaSign", "Signature");
		_testFetchProvider("TLSv1.3", "SunJSSE", "SSLContext");

		Assert.assertNull(CBOMUtil.fetchProvider("AES", "SecretKeySpec"));
		Assert.assertNull(
			CBOMUtil.fetchProvider(RandomTestUtil.randomString(), "BCrypt"));
		Assert.assertNull(
			CBOMUtil.fetchProvider(
				RandomTestUtil.randomString(), "MessageDigest"));
	}

	@Test
	public void testGetCycloneDXName() {
		_testGetCycloneDXName("AES", "AES", "Cipher", 0);
		_testGetCycloneDXName("AES", "AES-256", "KeyGenerator", 256);
		_testGetCycloneDXName("AES/CBC/PKCS5Padding", "AES-CBC", "Cipher", 0);
		_testGetCycloneDXName(
			"AES/GCM/NoPadding", "AES-128-GCM", "Cipher", 128);
		_testGetCycloneDXName("AES/GCM/NoPadding", "AES-GCM", "Cipher", 0);
		_testGetCycloneDXName(
			"ChaCha20-Poly1305/None/NoPadding", "ChaCha20-Poly1305", "Cipher",
			0);
		_testGetCycloneDXName(
			"DESede/CBC/PKCS5Padding", "3DES-CBC", "Cipher", 0);
		_testGetCycloneDXName("DH", "FFDH", "KeyAgreement", 0);
		_testGetCycloneDXName("Ed25519", "Ed25519", "Signature", 0);
		_testGetCycloneDXName("HmacSHA1", "HMAC-SHA-1", "Mac", 0);
		_testGetCycloneDXName("HmacSHA256", "HMAC-SHA-256", "KeyGenerator", 0);
		_testGetCycloneDXName("HmacSHA512", "HMAC-SHA-512", "Mac", 0);
		_testGetCycloneDXName("HmacSHA512", "HMAC-SHA-512", "SecretKeySpec", 0);
		_testGetCycloneDXName("MD5", "MD5", "MessageDigest", 0);
		_testGetCycloneDXName("PKCS12", "PKCS12", "KeyStore", 0);
		_testGetCycloneDXName("RAW", "RAW", "SecretKeySpec", 0);
		_testGetCycloneDXName("RSA", "RSA-2048", "KeyPairGenerator", 2048);
		_testGetCycloneDXName(
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "RSA-OAEP-SHA-256",
			"Cipher", 0);
		_testGetCycloneDXName(
			"RSA/ECB/PKCS1Padding", "RSA-PKCS1-1.5", "Cipher", 0);
		_testGetCycloneDXName("SHA", "SHA-1", "MessageDigest", 0);
		_testGetCycloneDXName("SHA-512/256", "SHA-512/256", "MessageDigest", 0);
		_testGetCycloneDXName("SHA1withDSA", "DSA-SHA-1", "Signature", 0);
		_testGetCycloneDXName(
			"SHA256withRSA", "RSA-PKCS1-1.5-SHA-256", "Signature", 0);
		_testGetCycloneDXName(
			"SHA256withRSA/PSS", "RSA-PSS-SHA-256", "Signature", 0);
		_testGetCycloneDXName("SHA3-256", "SHA3-256", "MessageDigest", 0);
		_testGetCycloneDXName(
			"SHA384withECDSA", "ECDSA-SHA-384", "Signature", 0);
		_testGetCycloneDXName("TLSv1.3", "TLSv1.3", "SSLContext", 0);
		_testGetCycloneDXName("X25519", "x25519", "KeyAgreement", 0);

		String algorithm = RandomTestUtil.randomString();

		_testGetCycloneDXName(algorithm, algorithm, "Cipher", 0);
		_testGetCycloneDXName(algorithm, algorithm, "MessageDigest", 0);
		_testGetCycloneDXName(algorithm, algorithm, "Signature", 0);
	}

	@Test
	public void testGetPrimitive() {
		_testGetPrimitive("AES", Primitive.BLOCK_CIPHER, "SecretKeySpec");
		_testGetPrimitive(
			"AES/CBC/PKCS5Padding", Primitive.BLOCK_CIPHER, "Cipher");
		_testGetPrimitive("AES/GCM/NoPadding", Primitive.AE, "Cipher");
		_testGetPrimitive("ChaCha20", Primitive.STREAM_CIPHER, "Cipher");
		_testGetPrimitive("Ed25519", Primitive.SIGNATURE, "KeyPairGenerator");
		_testGetPrimitive("HmacSHA256", Primitive.MAC, "KeyGenerator");
		_testGetPrimitive(
			"PBKDF2WithHmacSHA256", Primitive.KDF, "SecretKeyFactory");
		_testGetPrimitive("PKCS12", Primitive.OTHER, "KeyStore");
		_testGetPrimitive("RSA", Primitive.UNKNOWN, "KeyPairGenerator");
		_testGetPrimitive(
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding", Primitive.PKE, "Cipher");
		_testGetPrimitive("SHA256withRSA", Primitive.SIGNATURE, "Signature");
		_testGetPrimitive("X25519", Primitive.KEY_AGREE, "KeyPairGenerator");
		_testGetPrimitive(null, Primitive.HASH, "MessageDigest");
		_testGetPrimitive(null, Primitive.UNKNOWN, "Cipher");
	}

	@Test
	public void testGetStandardAlgorithmName() {
		Assert.assertEquals(
			"SHA1withDSA",
			CBOMUtil.getStandardAlgorithmName("DSA", "Signature"));
		Assert.assertEquals(
			"SHA-1", CBOMUtil.getStandardAlgorithmName("SHA", "MessageDigest"));
		Assert.assertEquals(
			"TLS", CBOMUtil.getStandardAlgorithmName("TLS", "SSLContext"));
		Assert.assertEquals(
			"AES/GCM/NoPadding",
			CBOMUtil.getStandardAlgorithmName("aes/GCM/NoPadding", "Cipher"));

		String algorithm = RandomTestUtil.randomString();

		Assert.assertEquals(
			algorithm,
			CBOMUtil.getStandardAlgorithmName(algorithm, "MessageDigest"));
		Assert.assertEquals(
			algorithm,
			CBOMUtil.getStandardAlgorithmName(algorithm, "SecretKeySpec"));
	}

	@Test
	public void testIsKeyAlgorithm() {
		Assert.assertTrue(
			CBOMUtil.isKeyAlgorithm("AES/GCM/NoPadding", "Cipher", "AES"));
		Assert.assertFalse(
			CBOMUtil.isKeyAlgorithm("AES/GCM/NoPadding", "Cipher", "DESede"));
		Assert.assertTrue(
			CBOMUtil.isKeyAlgorithm("ECDH", "KeyAgreement", "EC"));
		Assert.assertTrue(
			CBOMUtil.isKeyAlgorithm("HmacSHA1", "Mac", "hmacsha1"));
		Assert.assertTrue(
			CBOMUtil.isKeyAlgorithm("SHA1withDSA", "Signature", "DSA"));
		Assert.assertFalse(
			CBOMUtil.isKeyAlgorithm("SHA256withRSA", "Signature", "EC"));
		Assert.assertTrue(
			CBOMUtil.isKeyAlgorithm("SHA384withECDSA", "Signature", "EC"));
	}

	@Test
	public void testSetCipherProperties() {
		_testSetCipherProperties(
			Mode.CBC, Padding.PKCS5, "AES/CBC/PKCS5Padding");
		_testSetCipherProperties(
			Mode.ECB, Padding.OAEP, "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		_testSetCipherProperties(
			Mode.ECB, Padding.PKCS1V15, "RSA/ECB/PKCS1Padding");
		_testSetCipherProperties(Mode.GCM, null, "AES/GCM/NoPadding");
		_testSetCipherProperties(null, null, "AES");
	}

	private void _testFetchProvider(
		String algorithm, String expectedProviderName, String jcaClassName) {

		Provider provider = CBOMUtil.fetchProvider(algorithm, jcaClassName);

		Assert.assertEquals(expectedProviderName, provider.getName());
	}

	private void _testGetCycloneDXName(
		String algorithm, String expectedName, String jcaClassName,
		int keySize) {

		Assert.assertEquals(
			expectedName,
			CBOMUtil.getCycloneDXName(algorithm, jcaClassName, keySize));
	}

	private void _testGetPrimitive(
		String algorithm, Primitive expectedPrimitive, String jcaClassName) {

		Assert.assertEquals(
			expectedPrimitive, CBOMUtil.getPrimitive(algorithm, jcaClassName));
	}

	private void _testSetCipherProperties(
		Mode expectedMode, Padding expectedPadding, String transformation) {

		AlgorithmProperties algorithmProperties = new AlgorithmProperties();

		CBOMUtil.setCipherProperties(algorithmProperties, transformation);

		Assert.assertEquals(expectedMode, algorithmProperties.getMode());
		Assert.assertEquals(expectedPadding, algorithmProperties.getPadding());
	}

}