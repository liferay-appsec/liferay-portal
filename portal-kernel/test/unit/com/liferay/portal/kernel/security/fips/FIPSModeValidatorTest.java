/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.xml.SecureXMLFactoryProvider;
import com.liferay.portal.kernel.security.xml.SecureXMLFactoryProviderUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.DependenciesTestUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import javax.net.ssl.SSLContext;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Caio Farias
 */
public class FIPSModeValidatorTest {

	@Before
	public void setUp() {
		SecureXMLFactoryProviderUtil secureXMLFactoryProviderUtil =
			new SecureXMLFactoryProviderUtil();

		SecureXMLFactoryProvider secureXMLFactoryProvider = Mockito.mock(
			SecureXMLFactoryProvider.class);

		Mockito.when(
			secureXMLFactoryProvider.newDocumentBuilderFactory()
		).thenAnswer(
			input -> DocumentBuilderFactory.newInstance()
		);

		secureXMLFactoryProviderUtil.setSecureXMLFactoryProvider(
			secureXMLFactoryProvider);
	}

	@Test
	public void testFetchApprovedModeProvider() {
		AtomicReference<Provider> atomicReference =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class,
				"_approvedModeProviderAtomicReference");

		atomicReference.set(_createProvider(RandomTestUtil.randomString()));

		FIPSModeTestUtil.assertSecurityException(
			"The first security provider must be an allowed FIPS provider",
			FIPSModeValidator::validate);

		Assert.assertNull(FIPSModeValidator.fetchApprovedModeProvider());
	}

	@Test
	public void testGetAllowedTLSCipherSuites() {
		Assert.assertArrayEquals(
			new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"},
			FIPSModeValidator.getAllowedTLSCipherSuites(
				new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"}));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertArrayEquals(
				new String[] {"TLS_AES_128_GCM_SHA256"},
				FIPSModeValidator.getAllowedTLSCipherSuites(
					new String[] {
						"TLS_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA"
					}));
		}
	}

	@Test
	public void testGetAllowedTLSProtocols() {
		Assert.assertArrayEquals(
			new String[] {"TLSv1"},
			FIPSModeValidator.getAllowedTLSProtocols(new String[] {"TLSv1"}));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertArrayEquals(
				new String[] {"TLSv1.3"},
				FIPSModeValidator.getAllowedTLSProtocols(
					new String[] {"TLSv1.3", "TLSv1"}));
		}
	}

	@Test
	public void testGetApprovedAlgorithms() {
		Assert.assertArrayEquals(
			new String[] {"BCFKS", "PKCS12"},
			FIPSModeValidator.getApprovedAlgorithms(KeyStore.class));
		Assert.assertArrayEquals(
			new String[0],
			FIPSModeValidator.getApprovedAlgorithms(SSLContext.class));

		String[] approvedAlgorithms = FIPSModeValidator.getApprovedAlgorithms(
			KeyPairGenerator.class);

		Assert.assertArrayEquals(new String[] {"RSA"}, approvedAlgorithms);

		approvedAlgorithms[0] = RandomTestUtil.randomString();

		Assert.assertArrayEquals(
			new String[] {"RSA"},
			FIPSModeValidator.getApprovedAlgorithms(KeyPairGenerator.class));
	}

	@Test
	public void testGetKeySizes() {
		Assert.assertArrayEquals(
			new int[] {128, 192, 256}, FIPSModeValidator.getKeySizes("AES"));
		Assert.assertArrayEquals(
			new int[] {128, 192, 256}, FIPSModeValidator.getKeySizes("aes"));
		Assert.assertArrayEquals(
			new int[] {2048, 3072, 4096}, FIPSModeValidator.getKeySizes("RSA"));
		Assert.assertArrayEquals(
			new int[0],
			FIPSModeValidator.getKeySizes(RandomTestUtil.randomString()));
	}

	@Test
	public void testGetPlaintextSecretProperties() {
		String obfuscatedKey1 = RandomTestUtil.randomString();
		String obfuscatedKey2 = RandomTestUtil.randomString();
		String obfuscatedKey3 = RandomTestUtil.randomString();

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"ADMIN_OBFUSCATED_PROPERTIES",
					new String[] {
						obfuscatedKey1, obfuscatedKey2, obfuscatedKey3
					})) {

			Properties properties = new Properties();

			properties.setProperty(
				obfuscatedKey1, RandomTestUtil.randomString());
			properties.setProperty(
				obfuscatedKey2, "${" + RandomTestUtil.randomString() + "}");
			properties.setProperty(obfuscatedKey3, StringPool.BLANK);
			properties.setProperty(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

			List<String> plaintextSecretProperties = ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_getPlaintextSecretProperties",
				new Class<?>[] {Properties.class}, properties);

			Assert.assertEquals(
				plaintextSecretProperties.toString(), 1,
				plaintextSecretProperties.size());

			Assert.assertTrue(
				plaintextSecretProperties.contains(obfuscatedKey1));
		}
	}

	@Test
	public void testIsAllowedAlgorithm() {
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class,
				"AES/GCM/NoPadding"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class,
				"aes/gcm/nopadding"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class,
				"AES/ECB/PKCS5Padding"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class, null));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyGenerator.class,
				"AES"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyGenerator.class,
				"DESede"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, SSLContext.class,
				"AES/GCM/NoPadding"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyPairGenerator.class,
				"RSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyPairGenerator.class,
				"DSA"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyStore.class, "pkcs12"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyStore.class, "JKS"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, KeyStore.class, "JKS"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Mac.class, "HmacSHA512"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class,
				"SHA512withRSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class,
				"SHA1withRSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Mac.class, "HmacSHA1"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, MessageDigest.class,
				"SHA-256"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, MessageDigest.class,
				"MD5"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, MessageDigest.class,
				"HmacSHA256"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, MessageDigest.class,
				"SHA-512"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, MessageDigest.class,
				"MD5"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, MessageDigest.class,
				"SHA-1"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, MessageDigest.class,
				"SHA-256"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, MessageDigest.class,
				RandomTestUtil.randomString()));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, Cipher.class,
				"AES/ECB/PKCS5Padding"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, Mac.class, "HmacSHA1"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, Mac.class, "HmacMD5"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, MessageDigest.class,
				"SHA-256"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, MessageDigest.class, "MD5"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, KeyStore.class, "JKS"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, MessageDigest.class, "SHA"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.LEGACY, MessageDigest.class,
				"SHA-1"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyFactory.class, "EC"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyFactory.class, "DSA"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class,
				"SHA256withECDSA"));
	}

	@Test
	public void testIsAllowedKeyAlgorithm() {
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class, "AES"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class, "aes"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Cipher.class, "DES"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyGenerator.class,
				"AES"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyPairGenerator.class,
				"RSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyPairGenerator.class,
				"DSA"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Mac.class, "HmacSHA256"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class, "RSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class, "DSA"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Mac.class, "HmacSHA1"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, SecretKeySpec.class,
				"AES"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, SecretKeySpec.class,
				"HmacSHA512"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, SecretKeySpec.class,
				"Blowfish"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, Cipher.class, "AES"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.ALLOWED, Cipher.class, "DES"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.LEGACY, Mac.class, "HmacSHA1"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.LEGACY, SecretKeySpec.class,
				"HmacSHA1"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.LEGACY, Cipher.class, "DES"));

		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyFactory.class, "EC"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, KeyFactory.class,
				"ECDSA"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeyAlgorithm(
				CryptoFunctionApprovalType.APPROVED, Signature.class, "EC"));
	}

	@Test
	public void testIsAllowedKeySize() {
		Assert.assertFalse(FIPSModeValidator.isAllowedKeySize("AES", 64));
		Assert.assertTrue(FIPSModeValidator.isAllowedKeySize("AES", 256));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeySize("HmacSHA256", 64));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeySize("HmacSHA256", 112));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedKeySize("HmacSHA256", 4096));
		Assert.assertFalse(FIPSModeValidator.isAllowedKeySize("RSA", 1024));
		Assert.assertTrue(FIPSModeValidator.isAllowedKeySize("RSA", 2048));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedKeySize(
				RandomTestUtil.randomString(), 256));
	}

	@Test
	public void testIsAllowedSSLContextProtocol() {
		Assert.assertFalse(
			FIPSModeValidator.isAllowedSSLContextProtocol("SSL"));
		Assert.assertTrue(FIPSModeValidator.isAllowedSSLContextProtocol("TLS"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedSSLContextProtocol("TLSv1"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedSSLContextProtocol("TLSv1.1"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedSSLContextProtocol("TLSv1.2"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedSSLContextProtocol("tlsv1.3"));
	}

	@Test
	public void testIsAllowedTLSFactoryAlgorithm() {
		Assert.assertTrue(
			FIPSModeValidator.isAllowedTLSFactoryAlgorithm("PKIX"));
		Assert.assertTrue(
			FIPSModeValidator.isAllowedTLSFactoryAlgorithm("pkix"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedTLSFactoryAlgorithm("SunX509"));
		Assert.assertFalse(
			FIPSModeValidator.isAllowedTLSFactoryAlgorithm(null));
	}

	@Test
	public void testIsHealthyProvider() {
		Assert.assertFalse(
			FIPSModeValidator.isHealthyProvider(
				_createProvider("AmazonCorrettoCryptoProvider")));
		Assert.assertFalse(
			FIPSModeValidator.isHealthyProvider(_createProvider("BCFIPS")));
		Assert.assertFalse(
			FIPSModeValidator.isHealthyProvider(
				_createProvider(RandomTestUtil.randomString())));
	}

	@Test
	public void testReadChannelPropertiesDocument() throws Exception {
		Document document = ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_readChannelPropertiesDocument",
			new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class,
					"cluster-link-channel-properties.xml")));

		NodeList nodeList = document.getElementsByTagName("AUTH");

		Assert.assertEquals(1, nodeList.getLength());

		Element authElement = (Element)nodeList.item(0);

		Assert.assertEquals(
			"org.jgroups.auth.X509Token",
			authElement.getAttribute("auth_class"));

		_assertSecurityException(
			"Unable to parse the cluster link channel properties",
			"_readChannelPropertiesDocument", new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class,
					"cluster-link-channel-properties-malformed.xml")));
		_assertSecurityException(
			"Unable to read the cluster link channel properties",
			"_readChannelPropertiesDocument", new Class<?>[] {String.class},
			RandomTestUtil.randomString());
	}

	@Test
	public void testRequiredProperties() {
		Map<String, String[]> requiredSecurityProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSecurityProperties");

		Assert.assertArrayEquals(
			new String[] {"PKIX"},
			requiredSecurityProperties.get("ssl.KeyManagerFactory.algorithm"));
		Assert.assertArrayEquals(
			new String[] {"PKIX"},
			requiredSecurityProperties.get(
				"ssl.TrustManagerFactory.algorithm"));

		Map<String, String[]> requiredSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSystemProperties");

		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSystemProperties.get(
				"org.bouncycastle.fips.approved_only"));
	}

	@Test
	public void testValidateAlgorithm() {
		FIPSModeValidator.validateAlgorithm("MD5");
		FIPSModeValidator.validateAlgorithm(null);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateAlgorithm("AES");
			FIPSModeValidator.validateAlgorithm("HmacSHA256");
			FIPSModeValidator.validateAlgorithm("PBKDF2WithHmacSHA512");
			FIPSModeValidator.validateAlgorithm("SHA-256");
			FIPSModeValidator.validateAlgorithm("sha-384");

			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm(
					"AES/CBC/PKCS5Padding"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm("HmacSHA1"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm("MD5"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm("SHA-1"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm("SHA-2"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm("SHA-2560"));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateAlgorithm(null));
		}
	}

	@Test
	public void testValidateAllowedPropertyValues() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateAllowedPropertyValues",
			new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.2");
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateAllowedPropertyValues",
			new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.2,");
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateAllowedPropertyValues",
			new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.2,TLSv1.3");
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateAllowedPropertyValues",
			new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.3");

		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> ",TLSv1.2");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "SSLv3,TLSv1.3");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.1");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.1,TLSv1.2");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.11");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "TLSv1.2,SSLv2Hello");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> "tlsv1.2");
		_assertSecurityException(
			"FIPS mode requires the property \"jdk.tls.client.protocols\"",
			"_validateAllowedPropertyValues", new Class<?>[] {Function.class},
			(Function<String, String>)key -> null);
	}

	@Test
	public void testValidateClusterLinkChannelAuthElement() throws Exception {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateClusterLinkChannelAuthElement",
			new Class<?>[] {Element.class, String.class},
			_getElement("cluster-link-channel-properties.xml", "AUTH"),
			RandomTestUtil.randomString());

		_assertSecurityException(
			"must authenticate cluster members with " +
				"\"org.jgroups.auth.X509Token\"",
			"_validateClusterLinkChannelAuthElement",
			new Class<?>[] {Element.class, String.class},
			_getElement(
				"cluster-link-channel-properties-auth-class-missing.xml",
				"AUTH"),
			RandomTestUtil.randomString());
		_assertSecurityException(
			"must authenticate cluster members with " +
				"\"org.jgroups.auth.X509Token\"",
			"_validateClusterLinkChannelAuthElement",
			new Class<?>[] {Element.class, String.class},
			_getElement(
				"cluster-link-channel-properties-auth-class-md5-token.xml",
				"AUTH"),
			RandomTestUtil.randomString());
	}

	@Test
	public void testValidateClusterLinkChannelConfiguration() throws Exception {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateClusterLinkChannelConfiguration",
			new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class,
					"cluster-link-channel-properties.xml")));

		_assertSecurityException(
			"must encrypt intracluster traffic with \"SYM_ENCRYPT\" in FIPS " +
				"mode",
			"_validateClusterLinkChannelConfiguration",
			new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class,
					"cluster-link-channel-properties-asym-encrypt.xml")));
		_assertSecurityException(
			"must encrypt intracluster traffic with \"SYM_ENCRYPT\" in FIPS " +
				"mode",
			"_validateClusterLinkChannelConfiguration",
			new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class,
					"cluster-link-channel-properties-future-encrypt.xml")));
	}

	@Test
	public void testValidateClusterLinkChannelSymEncryptElement()
		throws Exception {

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties.xml", "SYM_ENCRYPT"));
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-provider-bcfips.xml",
					"SYM_ENCRYPT"));

			_assertSecurityException(
				"Initialization vector size 0 is not allowed in FIPS mode",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-sym-iv-length-missing.xml",
					"SYM_ENCRYPT"));
			_assertSecurityException(
				"Initialization vector size 12 is not allowed in FIPS mode",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-sym-iv-length-12.xml",
					"SYM_ENCRYPT"));
			_assertSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-sym-keylength-64.xml",
					"SYM_ENCRYPT"));
			_assertSecurityException(
				"Security provider \"SunJCE\" is not allowed in FIPS mode",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-provider-sun-jce.xml",
					"SYM_ENCRYPT"));
			_assertSecurityException(
				"Transformation \"\" is not allowed",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-sym-algorithm-missing.xml",
					"SYM_ENCRYPT"));
			_assertSecurityException(
				"Transformation \"AES/ECB/NoPadding\" is not allowed",
				"_validateClusterLinkChannelSymEncryptElement",
				new Class<?>[] {Element.class},
				_getElement(
					"cluster-link-channel-properties-sym-algorithm-ecb.xml",
					"SYM_ENCRYPT"));
		}
	}

	@Test
	public void testValidateClusterLinkConfiguration() throws Exception {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_AUTH_KEYSTORE_TYPE", "PKCS12", false);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_CHANNEL_PROPERTIES_CONTROL",
					String.valueOf(
						DependenciesTestUtil.getDependencyAsFile(
							FIPSModeValidatorTest.class,
							"cluster-link-channel-properties.xml")),
					false);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_CHANNEL_PROPERTIES_TRANSPORT",
					String.valueOf(
						DependenciesTestUtil.getDependencyAsFile(
							FIPSModeValidatorTest.class,
							"cluster-link-channel-properties.xml")),
					false);
			SafeCloseable safeCloseable4 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_ENABLED", true);
			SafeCloseable safeCloseable5 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateClusterLinkConfiguration",
				new Class<?>[0]);

			_assertClusterLinkConfigurationSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				"cluster-link-channel-properties-sym-keylength-64.xml",
				PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_TRANSPORT);
			_assertClusterLinkConfigurationSecurityException(
				"must encrypt intracluster traffic with \"SYM_ENCRYPT\" in " +
					"FIPS mode",
				"cluster-link-channel-properties-asym-encrypt.xml",
				PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_TRANSPORT);

			_assertClusterLinkConfigurationSecurityException(
				"must authenticate cluster members with " +
					"\"org.jgroups.auth.X509Token\"",
				"cluster-link-channel-properties-auth-class-md5-token.xml",
				PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_CONTROL);

			try (SafeCloseable safeCloseable6 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"CLUSTER_LINK_AUTH_KEYSTORE_TYPE",
						RandomTestUtil.randomString(), false)) {

				_assertSecurityException(
					"\"" + PropsKeys.CLUSTER_LINK_AUTH_KEYSTORE_TYPE +
						"\" to be set to only",
					"_validateClusterLinkConfiguration", new Class<?>[0]);
			}
		}
	}

	@Test
	public void testValidateFIPSProvider() {
		_assertSecurityException(
			"FIPS provider integrity failed:", "_validateFIPSProvider",
			new Class<?>[] {Provider[].class},
			(Object)new Provider[] {
				_createProvider("AmazonCorrettoCryptoProvider")
			});

		_assertSecurityException(
			"FIPS provider integrity failed:", "_validateFIPSProvider",
			new Class<?>[] {Provider[].class},
			(Object)new Provider[] {_createProvider("BCFIPS")});
		_assertSecurityException(
			"The first security provider must be an allowed FIPS provider",
			"_validateFIPSProvider", new Class<?>[] {Provider[].class},
			(Object)new Provider[] {
				_createProvider(RandomTestUtil.randomString())
			});
		_assertSecurityException(
			"The first security provider must be an allowed FIPS provider",
			"_validateFIPSProvider", new Class<?>[] {Provider[].class},
			(Object)new Provider[] {_createProvider(null)});
		_assertSecurityException(
			"There are no security providers", "_validateFIPSProvider",
			new Class<?>[] {Provider[].class}, (Object)new Provider[0]);
	}

	@Test
	public void testValidateJWSAlgorithm() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			FIPSModeValidator.validateJWSAlgorithm("HS256");
			FIPSModeValidator.validateJWSAlgorithm(null);
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateJWSAlgorithm("ES256");
			FIPSModeValidator.validateJWSAlgorithm("ES384");
			FIPSModeValidator.validateJWSAlgorithm("ES512");
			FIPSModeValidator.validateJWSAlgorithm("EdDSA");
			FIPSModeValidator.validateJWSAlgorithm("PS256");
			FIPSModeValidator.validateJWSAlgorithm("PS384");
			FIPSModeValidator.validateJWSAlgorithm("PS512");
			FIPSModeValidator.validateJWSAlgorithm("RS256");
			FIPSModeValidator.validateJWSAlgorithm("RS384");
			FIPSModeValidator.validateJWSAlgorithm("RS512");

			_assertJWSAlgorithmNotAllowed(StringPool.BLANK);
			_assertJWSAlgorithmNotAllowed("ES256K");
			_assertJWSAlgorithmNotAllowed("HS1");
			_assertJWSAlgorithmNotAllowed("HS256");
			_assertJWSAlgorithmNotAllowed("HS384");
			_assertJWSAlgorithmNotAllowed("HS512");
			_assertJWSAlgorithmNotAllowed("RS1");
			_assertJWSAlgorithmNotAllowed("RSA1_5");
			_assertJWSAlgorithmNotAllowed("garbage");
			_assertJWSAlgorithmNotAllowed("none");
			_assertJWSAlgorithmNotAllowed("rs256");
			_assertJWSAlgorithmNotAllowed(null);
		}
	}

	@Test
	public void testValidateKey() {
		FIPSModeValidator.validateKey("DES", 64);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateKey("AES", 0);
			FIPSModeValidator.validateKey("AES", 128);
			FIPSModeValidator.validateKey("AES", 192);
			FIPSModeValidator.validateKey("AES", 256);

			FIPSModeTestUtil.assertSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("AES", 64));
			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("DES", 128));
		}
	}

	@Test
	public void testValidateKeyAlgorithm() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateKeyAlgorithm",
				new Class<?>[] {String.class, Class.class}, "AES",
				KeyGenerator.class);
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateKeyAlgorithm",
				new Class<?>[] {String.class, Class.class}, "AES",
				Cipher.class);

			_assertSecurityException(
				"is not allowed in FIPS mode", "_validateKeyAlgorithm",
				new Class<?>[] {String.class, Class.class}, "HmacSHA256",
				KeyGenerator.class);
			_assertSecurityException(
				"is not allowed in FIPS mode", "_validateKeyAlgorithm",
				new Class<?>[] {String.class, Class.class}, null, Cipher.class);
		}
	}

	@Test
	public void testValidatePasswordsEncryptionAlgorithm() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256/256/1300000");
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA384/384/1300000");
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA512/512/1300000");

		_assertSecurityException(
			"is not allowed in FIPS mode",
			"_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA1/160/1300000");
		_assertSecurityException(
			"is not allowed in FIPS mode",
			"_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256X/256/1300000");
		_assertSecurityException(
			"is not allowed in FIPS mode",
			"_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "bcrypt/10");
		_assertSecurityException(
			"iteration count", "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256/256/600000");
		_assertSecurityException(
			"output length", "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256/64/1300000");
	}

	@Test
	public void testValidatePortalProperties() {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"COMPANY_ENCRYPTION_ALGORITHM", "AES", false);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"PASSWORDS_ENCRYPTION_ALGORITHM",
					"PBKDF2WithHmacSHA256/256/1300000", false);
			SafeCloseable safeCloseable4 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "AES")) {

			try (SafeCloseable safeCloseable5 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"TUNNEL_UTIL_VERIFY_SSL_HOSTNAME", "true", false)) {

				ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validatePortalProperties",
					new Class<?>[0]);

				try (LogCapture logCapture =
						LoggerTestUtil.configureLog4JLogger(
							FIPSModeValidator.class.getName(),
							LoggerTestUtil.WARN);
					SafeCloseable safeCloseable6 =
						PropsValuesTestUtil.swapWithSafeCloseable(
							"AUTH_MAC_ALGORITHM", "HmacSHA256", false);
					SafeCloseable safeCloseable7 =
						PropsValuesTestUtil.swapWithSafeCloseable(
							"AUTH_MAC_ALLOW", "true", false)) {

					ReflectionTestUtil.invoke(
						FIPSModeValidator.class, "_validatePortalProperties",
						new Class<?>[0]);

					List<LogEntry> logEntries = logCapture.getLogEntries();

					Assert.assertEquals(
						logEntries.toString(), 1, logEntries.size());

					LogEntry logEntry = logEntries.get(0);

					Assert.assertEquals(
						"Auth MAC is disabled in FIPS mode because the " +
							"property \"auth.mac.algorithm\" is set to " +
								"\"HmacSHA256\"",
						logEntry.getMessage());
				}
			}

			try (SafeCloseable safeCloseable5 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"TUNNEL_UTIL_VERIFY_SSL_HOSTNAME", StringPool.BLANK,
						false)) {

				_assertSecurityException(
					"FIPS mode requires the property \"" +
						PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME + "\"",
					"_validatePortalProperties", new Class<?>[0]);
			}

			try (SafeCloseable safeCloseable5 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"TUNNEL_UTIL_VERIFY_SSL_HOSTNAME", "false", false)) {

				_assertSecurityException(
					"FIPS mode requires the property \"" +
						PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME + "\"",
					"_validatePortalProperties", new Class<?>[0]);
			}
		}
	}

	@Test
	public void testValidateProviders() {
		Map<String, List<String>> allowedProviderNames =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedProviderNames");

		for (String allowedProviderName : allowedProviderNames.keySet()) {
			_assertSecurityException(
				"are not allowed in FIPS mode for", "_validateProviders",
				new Class<?>[] {Provider[].class},
				(Object)new Provider[] {
					_createProvider(allowedProviderName),
					_createProvider(RandomTestUtil.randomString())
				});
		}
	}

	@Test
	public void testValidateRequiredPropertyValues() {
		String key = RandomTestUtil.randomString();

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "PKIX",
			Map.of(key, new String[] {"PKIX"}));
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "SSLv3, TLSv1",
			Map.of(key, new String[] {"TLSv1"}));
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "TLSv1.2,TLSv1.3",
			Map.of(key, new String[] {"TLSv1.2"}));
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "TRUE",
			Map.of(key, new String[] {"true"}));

		_assertSecurityException(
			"FIPS mode requires the property \"" + key + "\"",
			"_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "SunPKIXFoo",
			Map.of(key, new String[] {"PKIX"}));
		_assertSecurityException(
			"FIPS mode requires the property \"" + key + "\"",
			"_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "TLSv1.1",
			Map.of(key, new String[] {"TLSv1"}));
		_assertSecurityException(
			"FIPS mode requires the property \"" + key + "\"",
			"_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "untrue",
			Map.of(key, new String[] {"true"}));
		_assertSecurityException(
			"FIPS mode requires the property \"" + key + "\"",
			"_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> null,
			Map.of(key, new String[] {"true"}));
		_assertSecurityException(
			"FIPS mode requires the property \"" + key +
				"\" to include \"TLSv1.3\"",
			"_validateRequiredPropertyValues",
			new Class<?>[] {Function.class, Map.class},
			(Function<String, String>)curKey -> "TLSv1.2",
			Map.of(key, new String[] {"TLSv1.2", "TLSv1.3"}));

		System.setProperty(key, "true");

		try {
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateRequiredPropertyValues",
				new Class<?>[] {Function.class, Map.class},
				(Function<String, String>)System::getProperty,
				Map.of(key, new String[] {"true"}));

			_assertSecurityException(
				"FIPS mode requires the property \"" + key + "\"",
				"_validateRequiredPropertyValues",
				new Class<?>[] {Function.class, Map.class},
				(Function<String, String>)Security::getProperty,
				Map.of(key, new String[] {"true"}));
		}
		finally {
			System.clearProperty(key);
		}
	}

	@Test
	public void testValidateSecureRandomProvider() {
		Object originalSecureRandom = ReflectionTestUtil.getFieldValue(
			SecureRandomUtil.class, "_random");

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.setFieldValue(
				SecureRandomUtil.class, "_random",
				_createSecureRandom("BCFIPS"));

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateSecureRandomProvider",
				new Class<?>[0]);

			String providerName = RandomTestUtil.randomString();

			ReflectionTestUtil.setFieldValue(
				SecureRandomUtil.class, "_random",
				_createSecureRandom(providerName));

			_assertSecurityException(
				"The secure random provider \"" + providerName +
					"\" is not allowed in FIPS mode",
				"_validateSecureRandomProvider", new Class<?>[0]);
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				SecureRandomUtil.class, "_random", originalSecureRandom);
		}
	}

	@Test
	public void testValidateSessionTimeout() {
		FIPSModeValidator.validateSessionTimeout(RandomTestUtil.randomInt());

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateSessionTimeout(720);

			FIPSModeTestUtil.assertSecurityException(
				"Session timeout must not be greater than 12 hours",
				() -> FIPSModeValidator.validateSessionTimeout(721));
		}
	}

	@Test
	public void testValidateURL() {
		FIPSModeValidator.validateURL(
			"ldap://" + RandomTestUtil.randomString());
		FIPSModeValidator.validateURL(
			"ldaps://" + RandomTestUtil.randomString());
		FIPSModeValidator.validateURL(null);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateURL(
				"ldaps://" + RandomTestUtil.randomString());

			FIPSModeTestUtil.assertSecurityException(
				"protocol scheme", () -> FIPSModeValidator.validateURL(""));
			FIPSModeTestUtil.assertSecurityException(
				"protocol scheme",
				() -> FIPSModeValidator.validateURL(
					"ldap://" + RandomTestUtil.randomString()));
			FIPSModeTestUtil.assertSecurityException(
				"protocol scheme", () -> FIPSModeValidator.validateURL(null));
		}
	}

	private void _assertClusterLinkConfigurationSecurityException(
			String expectedMessage, String fileName, String key)
		throws Exception {

		PropsUtil.set(
			key,
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class, fileName)));

		_assertSecurityException(
			expectedMessage, "_validateClusterLinkConfiguration",
			new Class<?>[0]);
	}

	private void _assertJWSAlgorithmNotAllowed(String algorithm) {
		FIPSModeTestUtil.assertSecurityException(
			"is not allowed in FIPS mode",
			() -> FIPSModeValidator.validateJWSAlgorithm(algorithm));
	}

	private void _assertSecurityException(
		String expectedMessage, String methodName, Class<?>[] parameterTypes,
		Object... parameterValues) {

		FIPSModeTestUtil.assertSecurityException(
			expectedMessage,
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, methodName, parameterTypes,
				parameterValues));
	}

	private Provider _createProvider(String name) {
		return new Provider(
			name, RandomTestUtil.randomString(),
			RandomTestUtil.randomString()) {
		};
	}

	private Random _createSecureRandom(String providerName) {
		Provider provider = _createProvider(providerName);

		return new Random() {

			public Provider getProvider() {
				return provider;
			}

		};
	}

	private Element _getElement(String fileName, String tagName)
		throws Exception {

		Document document = ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_readChannelPropertiesDocument",
			new Class<?>[] {String.class},
			String.valueOf(
				DependenciesTestUtil.getDependencyAsFile(
					FIPSModeValidatorTest.class, fileName)));

		NodeList nodeList = document.getElementsByTagName(tagName);

		return (Element)nodeList.item(0);
	}

}