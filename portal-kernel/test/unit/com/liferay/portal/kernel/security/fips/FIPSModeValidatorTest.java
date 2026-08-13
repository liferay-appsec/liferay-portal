/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.security.Provider;
import java.security.Security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Caio Farias
 */
public class FIPSModeValidatorTest {

	@Test
	public void testAllowedAndRequiredProperties() {
		Map<String, String[]> allowedSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedSystemProperties");

		Assert.assertArrayEquals(
			new String[] {"TLSv1.2", "TLSv1.3"},
			allowedSystemProperties.get("jdk.tls.client.protocols"));
		Assert.assertEquals(
			allowedSystemProperties.toString(), 1,
			allowedSystemProperties.size());

		Map<String, String[]> requiredSecurityProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSecurityProperties");

		Assert.assertArrayEquals(
			new String[] {"SSLv3", "TLS_RSA_*", "TLSv1", "TLSv1.1"},
			requiredSecurityProperties.get("jdk.tls.disabledAlgorithms"));
		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSecurityProperties.get("ocsp.enable"));
		Assert.assertArrayEquals(
			new String[] {"PKIX"},
			requiredSecurityProperties.get(
				"ssl.TrustManagerFactory.algorithm"));
		Assert.assertEquals(
			requiredSecurityProperties.toString(), 3,
			requiredSecurityProperties.size());

		Map<String, String[]> requiredSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSystemProperties");

		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSystemProperties.get("com.sun.net.ssl.checkRevocation"));
		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSystemProperties.get("com.sun.security.enableCRLDP"));
		Assert.assertEquals(
			requiredSystemProperties.toString(), 2,
			requiredSystemProperties.size());
	}

	@Test
	public void testGetAllowedTLSCipherSuites() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			Assert.assertArrayEquals(
				new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"},
				FIPSModeValidator.getAllowedTLSCipherSuites(
					new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"}));
		}

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
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			Assert.assertArrayEquals(
				new String[] {"TLSv1"},
				FIPSModeValidator.getAllowedTLSProtocols(
					new String[] {"TLSv1"}));
		}

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
	public void testIsNotAllowedProviderName() {
		Assert.assertFalse(
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_isNotAllowedProviderName",
				new Class<?>[] {String.class}, "AmazonCorrettoCryptoProvider"));
		Assert.assertFalse(
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_isNotAllowedProviderName",
				new Class<?>[] {String.class}, "BCFIPS"));

		Assert.assertTrue(
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_isNotAllowedProviderName",
				new Class<?>[] {String.class}, RandomTestUtil.randomString()));
		Assert.assertTrue(
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_isNotAllowedProviderName",
				new Class<?>[] {String.class}, (Object)null));
	}

	@Test
	public void testValidateAlgorithm() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			for (String algorithm : new String[] {"MD5", null}) {
				FIPSModeValidator.validateAlgorithm(algorithm);
			}
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			for (String algorithm : new String[] {"AES", "SHA-256"}) {
				FIPSModeValidator.validateAlgorithm(algorithm);
			}

			for (String algorithm : new String[] {"MD5", "SHA-2", null}) {
				FIPSModeTestUtil.assertSecurityException(
					"is not allowed in FIPS mode",
					() -> FIPSModeValidator.validateAlgorithm(algorithm));
			}
		}
	}

	@Test
	public void testValidateAllowedValues() {
		String key = RandomTestUtil.randomString();

		for (String value :
				new String[] {
					"TLSv1.2", "TLSv1.2,", "TLSv1.2,TLSv1.3", "TLSv1.3"
				}) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateAllowedValues",
				new Class<?>[] {Function.class, Map.class},
				(Function<String, String>)curKey -> value,
				Map.of(key, new String[] {"TLSv1.2", "TLSv1.3"}));
		}

		for (String value :
				new String[] {
					"", ",TLSv1.2", "SSLv3,TLSv1.3", "TLSv1", "TLSv1.1",
					"TLSv1.1,TLSv1.2", "TLSv1.11", "TLSv1.2,SSLv2Hello",
					"tlsv1.2", null
				}) {

			FIPSModeTestUtil.assertSecurityException(
				"FIPS mode requires the property \"" + key + "\"",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateAllowedValues",
					new Class<?>[] {Function.class, Map.class},
					(Function<String, String>)curKey -> value,
					Map.of(key, new String[] {"TLSv1.2", "TLSv1.3"})));
		}
	}

	@Test
	public void testValidateClusterProperties() throws Exception {
		Path controlPath = Files.createTempFile(null, ".xml");
		Path transportPath = Files.createTempFile(null, ".xml");

		String transportKey =
			PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_TRANSPORT + ".0";

		String transportValue = PropsUtil.get(transportKey);

		PropsUtil.set(transportKey, String.valueOf(transportPath));

		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_ENABLED", true);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_AUTH_KEYSTORE_TYPE", "PKCS12", false);
			SafeCloseable safeCloseable4 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"CLUSTER_LINK_CHANNEL_PROPERTIES_CONTROL",
					String.valueOf(controlPath), false)) {

			String channelPropertiesXML = StringBundler.concat(
				"<config>", FIPSModeTestUtil.XML_AUTH,
				FIPSModeTestUtil.XML_SYM_ENCRYPT, "</config>");

			Files.write(
				controlPath,
				channelPropertiesXML.getBytes(StandardCharsets.UTF_8));
			Files.write(
				transportPath,
				channelPropertiesXML.getBytes(StandardCharsets.UTF_8));

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateClusterProperties",
				new Class<?>[0]);

			_assertClusterPropertiesSecurityException(
				StringUtil.replace(
					channelPropertiesXML, "sym_keylength=\"128\"",
					"sym_keylength=\"64\""),
				transportPath,
				StringBundler.concat(
					"\"", transportPath,
					"\" are not allowed: Key size 64 is not allowed in FIPS ",
					"mode"));

			_assertClusterPropertiesSecurityException(
				StringBundler.concat(
					"<config>", FIPSModeTestUtil.XML_AUTH,
					FIPSModeTestUtil.XML_ASYM_ENCRYPT, "</config>"),
				transportPath,
				"must encrypt intracluster traffic with [SYM_ENCRYPT] in " +
					"FIPS mode, not [ASYM_ENCRYPT]");

			Files.write(
				transportPath,
				channelPropertiesXML.getBytes(StandardCharsets.UTF_8));

			_assertClusterPropertiesSecurityException(
				StringUtil.replace(
					channelPropertiesXML, FIPSModeTestUtil.AUTH_CLASS_NAME,
					RandomTestUtil.randomString()),
				controlPath, String.valueOf(controlPath));

			PropsUtil.set(
				PropsKeys.CLUSTER_LINK_AUTH_KEYSTORE_TYPE,
				RandomTestUtil.randomString());

			FIPSModeTestUtil.assertSecurityException(
				"\"" + PropsKeys.CLUSTER_LINK_AUTH_KEYSTORE_TYPE +
					"\" to be set to only",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateClusterProperties",
					new Class<?>[0]));
		}
		finally {
			Files.delete(controlPath);
			Files.delete(transportPath);

			PropsUtil.set(transportKey, transportValue);
		}
	}

	@Test
	public void testValidateFIPSProvider() {
		FIPSModeTestUtil.assertSecurityException(
			"FIPS provider integrity failed:",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class},
				(Object)new Provider[] {
					_createProvider("AmazonCorrettoCryptoProvider")
				}));

		FIPSModeTestUtil.assertSecurityException(
			"FIPS provider integrity failed:",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class},
				(Object)new Provider[] {_createProvider("BCFIPS")}));

		FIPSModeTestUtil.assertSecurityException(
			"The first security provider must be an allowed FIPS provider",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class},
				(Object)new Provider[] {
					_createProvider(RandomTestUtil.randomString())
				}));

		FIPSModeTestUtil.assertSecurityException(
			"There are no security providers",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class}, (Object)new Provider[0]));
	}

	@Test
	public void testValidateIVSize() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateIVSize",
				new Class<?>[] {int.class}, 16);

			for (int ivSize : List.of(0, 12, 32)) {
				FIPSModeTestUtil.assertSecurityException(
					"Initialization vector size " + ivSize +
						" is not allowed in FIPS mode",
					() -> ReflectionTestUtil.invoke(
						FIPSModeValidator.class, "_validateIVSize",
						new Class<?>[] {int.class}, ivSize));
			}
		}
	}

	@Test
	public void testValidateJGroupsProfileAuthElement() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_invokeValidateJGroupsProfileAuthElement(FIPSModeTestUtil.XML_AUTH);

			for (String channelPropertiesXML :
					new String[] {"<AUTH />", "<config />"}) {

				FIPSModeTestUtil.assertSecurityException(
					"must authenticate cluster members with \"" +
						FIPSModeTestUtil.AUTH_CLASS_NAME + "\"",
					() -> _invokeValidateJGroupsProfileAuthElement(
						channelPropertiesXML));
			}

			FIPSModeTestUtil.assertSecurityException(
				"must authenticate cluster members with \"" +
					FIPSModeTestUtil.AUTH_CLASS_NAME + "\"",
				() -> _invokeValidateJGroupsProfileAuthElement(
					StringUtil.replace(
						FIPSModeTestUtil.XML_AUTH,
						FIPSModeTestUtil.AUTH_CLASS_NAME,
						RandomTestUtil.randomString())));
		}
	}

	@Test
	public void testValidateJGroupsProfileEncryptElementNames() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validateJGroupsProfileEncryptElementNames",
				new Class<?>[] {String.class, List.class},
				RandomTestUtil.randomString(), List.of("SYM_ENCRYPT"));

			FIPSModeTestUtil.assertSecurityException(
				"must encrypt intracluster traffic with [SYM_ENCRYPT] in " +
					"FIPS mode, not []",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class,
					"_validateJGroupsProfileEncryptElementNames",
					new Class<?>[] {String.class, List.class},
					RandomTestUtil.randomString(), Collections.emptyList()));

			FIPSModeTestUtil.assertSecurityException(
				"must encrypt intracluster traffic with [SYM_ENCRYPT] in " +
					"FIPS mode, not [ASYM_ENCRYPT]",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class,
					"_validateJGroupsProfileEncryptElementNames",
					new Class<?>[] {String.class, List.class},
					RandomTestUtil.randomString(), List.of("ASYM_ENCRYPT")));

			FIPSModeTestUtil.assertSecurityException(
				"must encrypt intracluster traffic with [SYM_ENCRYPT] in " +
					"FIPS mode, not [FUTURE_ENCRYPT]",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class,
					"_validateJGroupsProfileEncryptElementNames",
					new Class<?>[] {String.class, List.class},
					RandomTestUtil.randomString(), List.of("FUTURE_ENCRYPT")));

			FIPSModeTestUtil.assertSecurityException(
				"must encrypt intracluster traffic with [SYM_ENCRYPT] in " +
					"FIPS mode, not [SYM_ENCRYPT, ASYM_ENCRYPT]",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class,
					"_validateJGroupsProfileEncryptElementNames",
					new Class<?>[] {String.class, List.class},
					RandomTestUtil.randomString(),
					List.of("SYM_ENCRYPT", "ASYM_ENCRYPT")));
		}
	}

	@Test
	public void testValidateJGroupsProfileSymEncryptElement() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_invokeValidateJGroupsProfileSymEncryptElement(
				FIPSModeTestUtil.XML_SYM_ENCRYPT);
			_invokeValidateJGroupsProfileSymEncryptElement("<config />");

			FIPSModeTestUtil.assertSecurityException(
				"Transformation \"\" is not allowed",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.removeSubstring(
						FIPSModeTestUtil.XML_SYM_ENCRYPT,
						"sym_algorithm=\"" +
							FIPSModeTestUtil.TRANSFORMATION_SYM + "\" ")));

			FIPSModeTestUtil.assertSecurityException(
				"Transformation \"AES/ECB/NoPadding\" is not allowed",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.replace(
						FIPSModeTestUtil.XML_SYM_ENCRYPT,
						"sym_algorithm=\"" +
							FIPSModeTestUtil.TRANSFORMATION_SYM + "\"",
						"sym_algorithm=\"AES/ECB/NoPadding\"")));

			FIPSModeTestUtil.assertSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.replace(
						FIPSModeTestUtil.XML_SYM_ENCRYPT,
						"sym_keylength=\"128\"", "sym_keylength=\"64\"")));

			FIPSModeTestUtil.assertSecurityException(
				"Initialization vector size 0 is not allowed in FIPS mode",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.removeSubstring(
						FIPSModeTestUtil.XML_SYM_ENCRYPT,
						"sym_iv_length=\"16\" ")));

			FIPSModeTestUtil.assertSecurityException(
				"Initialization vector size 12 is not allowed in FIPS mode",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.replace(
						FIPSModeTestUtil.XML_SYM_ENCRYPT,
						"sym_iv_length=\"16\"", "sym_iv_length=\"12\"")));

			String providerName = RandomTestUtil.randomString();

			FIPSModeTestUtil.assertSecurityException(
				"Security provider \"" + providerName +
					"\" is not allowed in FIPS mode",
				() -> _invokeValidateJGroupsProfileSymEncryptElement(
					StringUtil.replace(
						FIPSModeTestUtil.XML_SYM_ENCRYPT, "<SYM_ENCRYPT ",
						"<SYM_ENCRYPT provider=\"" + providerName + "\" ")));
		}
	}

	@Test
	public void testValidateKey() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			FIPSModeValidator.validateKey("DES", 64);
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			for (int keySize : List.of(0, 128, 192, 256)) {
				FIPSModeValidator.validateKey("AES", keySize);
			}

			FIPSModeTestUtil.assertSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("AES", 64));

			FIPSModeTestUtil.assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("DES", 128));
		}
	}

	@Test
	public void testValidatePasswordsEncryptionAlgorithm() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256/256/1300000");

		FIPSModeTestUtil.assertSecurityException(
			"is not allowed in FIPS mode",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA1/160/1300000"));

		FIPSModeTestUtil.assertSecurityException(
			"is not allowed in FIPS mode",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class}, "bcrypt/10"));

		FIPSModeTestUtil.assertSecurityException(
			"iteration count",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA256/256/600000"));

		FIPSModeTestUtil.assertSecurityException(
			"output length",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA256/64/1300000"));
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

			String tunnelVerifySSLHostname = PropsUtil.get(
				PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME);

			try {
				PropsUtil.set(
					PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME, "true");

				ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validatePortalProperties",
					new Class<?>[0]);

				PropsUtil.set(PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME, "");

				FIPSModeTestUtil.assertSecurityException(
					"Server hostnames must be verified in FIPS mode",
					() -> ReflectionTestUtil.invoke(
						FIPSModeValidator.class, "_validatePortalProperties",
						new Class<?>[0]));

				PropsUtil.set(
					PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME, "false");

				FIPSModeTestUtil.assertSecurityException(
					"Server hostnames must be verified in FIPS mode",
					() -> ReflectionTestUtil.invoke(
						FIPSModeValidator.class, "_validatePortalProperties",
						new Class<?>[0]));
			}
			finally {
				PropsUtil.set(
					PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME,
					tunnelVerifySSLHostname);
			}
		}
	}

	@Test
	public void testValidateProviders() {
		Map<String, List<String>> allowedProviderNames =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedProviderNames");

		for (String allowedProviderName : allowedProviderNames.keySet()) {
			FIPSModeTestUtil.assertSecurityException(
				"are not allowed in FIPS mode for",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateProviders",
					new Class<?>[] {Provider[].class},
					(Object)new Provider[] {
						_createProvider(allowedProviderName),
						_createProvider(RandomTestUtil.randomString())
					}));
		}
	}

	@Test
	public void testValidateRequiredValues() {
		String key = RandomTestUtil.randomString();

		for (String[] values :
				new String[][] {
					{"PKIX", "PKIX"}, {"SSLv3, TLSv1", "TLSv1"},
					{"TLSv1.2,TLSv1.3", "TLSv1.2"}, {"TRUE", "true"},
					{"pkix", "PKIX"}, {"true", "true"}
				}) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateRequiredValues",
				new Class<?>[] {Function.class, Map.class},
				(Function<String, String>)curKey -> values[0],
				Map.of(key, new String[] {values[1]}));
		}

		for (String[] values :
				new String[][] {
					{"SunPKIXFoo", "PKIX"}, {"TLSv1.1", "TLSv1"},
					{"untrue", "true"}, {null, "true"}
				}) {

			FIPSModeTestUtil.assertSecurityException(
				"FIPS mode requires the property \"" + key + "\"",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateRequiredValues",
					new Class<?>[] {Function.class, Map.class},
					(Function<String, String>)curKey -> values[0],
					Map.of(key, new String[] {values[1]})));
		}

		System.setProperty(key, "true");

		try {
			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateRequiredValues",
				new Class<?>[] {Function.class, Map.class},
				(Function<String, String>)System::getProperty,
				Map.of(key, new String[] {"true"}));

			FIPSModeTestUtil.assertSecurityException(
				"FIPS mode requires the property \"" + key + "\"",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateRequiredValues",
					new Class<?>[] {Function.class, Map.class},
					(Function<String, String>)Security::getProperty,
					Map.of(key, new String[] {"true"})));
		}
		finally {
			System.clearProperty(key);
		}
	}

	@Test
	public void testValidateServerCertificateVerification() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			FIPSModeValidator.validateServerCertificateVerification(false);
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateServerCertificateVerification(true);

			FIPSModeTestUtil.assertSecurityException(
				"Server certificates must be verified in FIPS mode",
				() -> FIPSModeValidator.validateServerCertificateVerification(
					false));
		}
	}

	@Test
	public void testValidateServerHostnameVerification() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			FIPSModeValidator.validateServerHostnameVerification(false);
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateServerHostnameVerification(true);

			FIPSModeTestUtil.assertSecurityException(
				"Server hostnames must be verified in FIPS mode",
				() -> FIPSModeValidator.validateServerHostnameVerification(
					false));
		}
	}

	@Test
	public void testValidateTransformation() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateTransformation",
				new Class<?>[] {String.class},
				FIPSModeTestUtil.TRANSFORMATION_SYM);

			for (String transformation :
					new String[] {
						"AES", "RSA", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding",
						null
					}) {

				FIPSModeTestUtil.assertSecurityException(
					"is not allowed in FIPS mode",
					() -> ReflectionTestUtil.invoke(
						FIPSModeValidator.class, "_validateTransformation",
						new Class<?>[] {String.class}, transformation));
			}
		}
	}

	@Test
	public void testValidateURL() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			for (String url :
					new String[] {
						"ldap://" + RandomTestUtil.randomString(),
						"ldaps://" + RandomTestUtil.randomString(), null
					}) {

				FIPSModeValidator.validateURL(url);
			}
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateURL(
				"ldaps://" + RandomTestUtil.randomString());

			for (String url :
					new String[] {
						"", "ldap://" + RandomTestUtil.randomString(), null
					}) {

				FIPSModeTestUtil.assertSecurityException(
					"protocol scheme",
					() -> FIPSModeValidator.validateURL(url));
			}
		}
	}

	private void _assertClusterPropertiesSecurityException(
			String channelPropertiesXML, Path path, String expectedMessage)
		throws Exception {

		Files.write(
			path, channelPropertiesXML.getBytes(StandardCharsets.UTF_8));

		FIPSModeTestUtil.assertSecurityException(
			expectedMessage,
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateClusterProperties",
				new Class<?>[0]));
	}

	private Provider _createProvider(String name) {
		return new Provider(
			name, RandomTestUtil.randomString(),
			RandomTestUtil.randomString()) {
		};
	}

	private void _invokeValidateJGroupsProfileAuthElement(
		String channelPropertiesXML) {

		Document document = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_toDocument",
			new Class<?>[] {String.class}, channelPropertiesXML);

		Map<String, Element> securityElements = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_getSecurityElements",
			new Class<?>[] {Document.class}, document);

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateJGroupsProfileAuthElement",
			new Class<?>[] {Element.class, String.class},
			securityElements.get("AUTH"), RandomTestUtil.randomString());
	}

	private void _invokeValidateJGroupsProfileSymEncryptElement(
		String channelPropertiesXML) {

		Document document = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_toDocument",
			new Class<?>[] {String.class}, channelPropertiesXML);

		Map<String, Element> securityElements = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_getSecurityElements",
			new Class<?>[] {Document.class}, document);

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateJGroupsProfileSymEncryptElement",
			new Class<?>[] {String.class, Element.class},
			RandomTestUtil.randomString(), securityElements.get("SYM_ENCRYPT"));
	}

}