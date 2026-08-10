/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.function.UnsafeTriConsumer;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.service.http.TunnelUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import java.security.Provider;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * @author Caio Farias
 */
public class FIPSModeValidatorTest {

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

			Assert.assertTrue(
				plaintextSecretProperties.contains(obfuscatedKey1));
			Assert.assertEquals(
				plaintextSecretProperties.toString(), 1,
				plaintextSecretProperties.size());
		}
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
				_assertSecurityException(
					"is not allowed in FIPS mode",
					() -> FIPSModeValidator.validateAlgorithm(algorithm));
			}
		}
	}

	@Test
	public void testValidateFIPSProvider() {
		for (String name : List.of("AmazonCorrettoCryptoProvider", "BCFIPS")) {
			_assertSecurityException(
				"FIPS provider integrity failed:",
				() -> ReflectionTestUtil.invoke(
					FIPSModeValidator.class, "_validateFIPSProvider",
					new Class<?>[] {Provider[].class},
					(Object)new Provider[] {_createProvider(name)}));
		}

		_assertSecurityException(
			"The first security provider must be an allowed FIPS provider",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class},
				(Object)new Provider[] {
					_createProvider(RandomTestUtil.randomString())
				}));
		_assertSecurityException(
			"There are no security providers",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class, "_validateFIPSProvider",
				new Class<?>[] {Provider[].class}, (Object)new Provider[0]));
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

			for (int keySize : List.of(128, 192, 256)) {
				FIPSModeValidator.validateKey("AES", keySize);
			}

			_assertSecurityException(
				"Key size 64 is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("AES", 64));
			_assertSecurityException(
				"is not allowed in FIPS mode",
				() -> FIPSModeValidator.validateKey("DES", 128));
		}
	}

	@Test
	public void testValidatePasswordsEncryptionAlgorithm() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePasswordsEncryptionAlgorithm",
			new Class<?>[] {String.class}, "PBKDF2WithHmacSHA256/256/1300000");

		_assertSecurityException(
			"is not allowed in FIPS mode",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class}, "bcrypt/10"));
		_assertSecurityException(
			"is not allowed in FIPS mode",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA1/160/1300000"));
		_assertSecurityException(
			"iteration count",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA256/256/600000"));
		_assertSecurityException(
			"output length",
			() -> ReflectionTestUtil.invoke(
				FIPSModeValidator.class,
				"_validatePasswordsEncryptionAlgorithm",
				new Class<?>[] {String.class},
				"PBKDF2WithHmacSHA256/64/1300000"));
	}

	@Test
	public void testValidateProviders() {
		Map<String, List<String>> allowedProviderNames =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedProviderNames");

		for (String allowedProviderName : allowedProviderNames.keySet()) {
			_assertSecurityException(
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
						"ldap://" + RandomTestUtil.randomString(), "", null
					}) {

				_assertSecurityException(
					"protocol scheme",
					() -> FIPSModeValidator.validateURL(url));
			}
		}
	}

	private void _assertSecurityException(
		String expectedMessage, ThrowingRunnable throwingRunnable) {

		SecurityException securityException = Assert.assertThrows(
			SecurityException.class, throwingRunnable);

		String message = securityException.getMessage();

		Assert.assertTrue(message, message.contains(expectedMessage));
	}

	private Provider _createProvider(String name) {
		return new Provider(
			name, RandomTestUtil.randomString(),
			RandomTestUtil.randomString()) {
		};
	}

	@Test
	public void testValidateAllowedValues() {
		String name = RandomTestUtil.randomString();

		for (String value :
				List.of("TLSv1.2", "TLSv1.2,", "TLSv1.2,TLSv1.3", "TLSv1.3")) {

			_validateProperties(
				curName -> value,
				Map.of(name, new String[] {"TLSv1.2", "TLSv1.3"}),
				this::_validateAllowedValues);
		}

		for (String value :
				new String[] {
					"", ",TLSv1.2", "SSLv3,TLSv1.3", "TLSv1", "TLSv1.1",
					"TLSv1.1,TLSv1.2", "TLSv1.11", "TLSv1.2,SSLv2Hello",
					"tlsv1.2", null
				}) {

			_assertSecurityException(
				"FIPS mode requires the property \"" + name + "\"",
				() -> _validateProperties(
					curName -> value,
					Map.of(name, new String[] {"TLSv1.2", "TLSv1.3"}),
					this::_validateAllowedValues));
		}

		Map<String, String[]> allowedSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedSystemProperties");

		Assert.assertEquals(
			allowedSystemProperties.toString(), 1,
			allowedSystemProperties.size());
		Assert.assertArrayEquals(
			new String[] {"TLSv1.2", "TLSv1.3"},
			allowedSystemProperties.get("jdk.tls.client.protocols"));
	}

	@Test
	public void testValidatePortalProperties() {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "AES");
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"COMPANY_ENCRYPTION_ALGORITHM", "AES", false);
			SafeCloseable safeCloseable4 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"PASSWORDS_ENCRYPTION_ALGORITHM",
					"PBKDF2WithHmacSHA256/256/1300000", false)) {

			try (SafeCloseable safeCloseable5 = _swapPortalProperty(
					_TUNNEL_VERIFY_SSL_HOSTNAME_KEY, "true")) {

				_validatePortalProperties();
			}

			for (String value : new String[] {"", "false"}) {
				try (SafeCloseable safeCloseable5 = _swapPortalProperty(
						_TUNNEL_VERIFY_SSL_HOSTNAME_KEY, value)) {

					_assertSecurityException(
						"Server hostnames must be verified in FIPS mode",
						this::_validatePortalProperties);
				}
			}
		}
	}

	@Test
	public void testValidateProperties() {
		String name = RandomTestUtil.randomString();

		try (SafeCloseable safeCloseable = _swapSystemProperty(name, "true")) {
			_validateProperties(
				System::getProperty, Map.of(name, new String[] {"true"}),
				this::_validateRequiredValues);

			_assertSecurityException(
				"FIPS mode requires the property \"" + name + "\"",
				() -> _validateProperties(
					Security::getProperty, Map.of(name, new String[] {"true"}),
					this::_validateRequiredValues));
		}
	}

	@Test
	public void testValidateRequiredValues() {
		String name = RandomTestUtil.randomString();

		for (String[] validProperty :
				new String[][] {
					{"PKIX", "PKIX"}, {"SSLv3, TLSv1", "TLSv1"},
					{"TLSv1.2,TLSv1.3", "TLSv1.2"}, {"TRUE", "true"},
					{"pkix", "PKIX"}, {"true", "true"}
				}) {

			_validateProperties(
				curName -> validProperty[0],
				Map.of(name, new String[] {validProperty[1]}),
				this::_validateRequiredValues);
		}

		for (String[] invalidProperty :
				new String[][] {
					{"SunPKIXFoo", "PKIX"}, {"TLSv1.1", "TLSv1"},
					{"untrue", "true"}
				}) {

			_assertSecurityException(
				"FIPS mode requires the property \"" + name + "\"",
				() -> _validateProperties(
					curName -> invalidProperty[0],
					Map.of(name, new String[] {invalidProperty[1]}),
					this::_validateRequiredValues));
		}

		_assertSecurityException(
			"FIPS mode requires the property \"" + name + "\"",
			() -> _validateProperties(
				curName -> null, Map.of(name, new String[] {"true"}),
				this::_validateRequiredValues));

		Map<String, String[]> requiredSecurityProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSecurityProperties");

		Assert.assertEquals(
			requiredSecurityProperties.toString(), 3,
			requiredSecurityProperties.size());
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

		Map<String, String[]> requiredSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_requiredSystemProperties");

		Assert.assertEquals(
			requiredSystemProperties.toString(), 2,
			requiredSystemProperties.size());
		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSystemProperties.get("com.sun.net.ssl.checkRevocation"));
		Assert.assertArrayEquals(
			new String[] {"true"},
			requiredSystemProperties.get("com.sun.security.enableCRLDP"));
	}

	@Test
	public void testValidateServerCertificateVerification() {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false)) {

			for (boolean verifyServerCertificate :
					new boolean[] {false, true}) {

				FIPSModeValidator.validateServerCertificateVerification(
					verifyServerCertificate);
			}
		}

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			FIPSModeValidator.validateServerCertificateVerification(true);

			_assertSecurityException(
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

			_assertSecurityException(
				"Server hostnames must be verified in FIPS mode",
				() -> FIPSModeValidator.validateServerHostnameVerification(
					false));
		}
	}

	private SafeCloseable _swapPortalProperty(String key, String value) {
		String oldValue = PropsUtil.get(key);

		PropsUtil.set(key, value);

		return () -> PropsUtil.set(key, oldValue);
	}

	private SafeCloseable _swapSystemProperty(String name, String value) {
		String oldValue = System.getProperty(name);

		System.setProperty(name, value);

		return () -> {
			if (oldValue == null) {
				System.clearProperty(name);
			}
			else {
				System.setProperty(name, oldValue);
			}
		};
	}

	private void _validateAllowedValues(
		String[] allowedValues, String name, String value) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateAllowedValues",
			new Class<?>[] {String[].class, String.class, String.class},
			allowedValues, name, value);
	}

	private void _validatePortalProperties() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePortalProperties",
			new Class<?>[0]);
	}

	private void _validateProperties(
		Function<String, String> function, Map<String, String[]> properties,
		UnsafeTriConsumer<String[], String, String, SecurityException>
			unsafeTriConsumer) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateProperties",
			new Class<?>[] {Function.class, Map.class, UnsafeTriConsumer.class},
			function, properties, unsafeTriConsumer);
	}

	private void _validateRequiredValues(
		String[] requiredValues, String name, String value) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateRequiredValues",
			new Class<?>[] {String[].class, String.class, String.class},
			requiredValues, name, value);
	}

	private static final String _TUNNEL_VERIFY_SSL_HOSTNAME_KEY =
		TunnelUtil.class.getName() + ".verify.ssl.hostname";

}