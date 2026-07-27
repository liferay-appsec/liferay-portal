/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.function.UnsafeTriConsumer;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

import java.security.Provider;
import java.security.Security;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * @author Caio Farias
 */
public class FIPSModeValidatorTest {

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
				"AES key must be 128, 192, or 256 bits",
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
	public void testValidatePortalProperties() {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "AES");
			SafeCloseable safeCloseable3 = _swapPortalProperty(
				PropsKeys.COMPANY_ENCRYPTION_ALGORITHM, "AES");
			SafeCloseable safeCloseable4 = _swapPortalProperty(
				PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM,
				"PBKDF2WithHmacSHA256/256/1300000")) {

			try (SafeCloseable safeCloseable5 = _swapPortalProperty(
					_TUNNEL_VERIFY_SSL_HOSTNAME_KEY, "true")) {

				_validatePortalProperties();
			}

			for (String value : new String[] {"", "false"}) {
				try (SafeCloseable safeCloseable5 = _swapPortalProperty(
						_TUNNEL_VERIFY_SSL_HOSTNAME_KEY, value)) {

					_assertSecurityException(
						"Servers hostname must be verified in FIPS mode",
						this::_validatePortalProperties);
				}
			}
		}
	}

	@Test
	public void testValidateProperties() {
		String name = RandomTestUtil.randomString();

		for (String[] validProperty :
				new String[][] {
					{"PKIX", "PKIX"}, {"SSLv3, TLSv1", "TLSv1"},
					{"TLSv1.2,TLSv1.3", "TLSv1.2"}, {"TRUE", "true"},
					{"pkix", "PKIX"}, {"true", "true"}
				}) {

			_validateProperties(
				Map.of(name, new String[] {validProperty[1]}),
				curName -> validProperty[0], this::_assertAllRequiredValues);
		}

		for (String[] invalidProperty :
				new String[][] {
					{"SunPKIXFoo", "PKIX"}, {"TLSv1.1", "TLSv1"},
					{"untrue", "true"}
				}) {

			_assertSecurityException(
				_REQUIRED_PROPERTY_MESSAGE_PREFIX + name + "\"",
				() -> _validateProperties(
					Map.of(name, new String[] {invalidProperty[1]}),
					curName -> invalidProperty[0],
					this::_assertAllRequiredValues));
		}

		_assertSecurityException(
			_REQUIRED_PROPERTY_MESSAGE_PREFIX + name + "\"",
			() -> _validateProperties(
				Map.of(name, new String[] {"true"}), curName -> null,
				this::_assertAllRequiredValues));

		try (SafeCloseable safeCloseable = _swapSystemProperty(name, "true")) {
			_validateProperties(
				Map.of(name, new String[] {"true"}), System::getProperty,
				this::_assertAllRequiredValues);

			_assertSecurityException(
				_REQUIRED_PROPERTY_MESSAGE_PREFIX + name + "\"",
				() -> _validateProperties(
					Map.of(name, new String[] {"true"}), Security::getProperty,
					this::_assertAllRequiredValues));
		}

		for (String value : List.of("TLSv1.2", "TLSv1.2,TLSv1.3", "TLSv1.3")) {
			_validateProperties(
				Map.of(name, new String[] {"TLSv1.2", "TLSv1.3"}),
				curName -> value, this::_assertAllowedValue);
		}

		for (String value :
				List.of("TLSv1", "TLSv1.1", "TLSv1.11", "tlsv1.2")) {

			_assertSecurityException(
				_REQUIRED_PROPERTY_MESSAGE_PREFIX + name + "\"",
				() -> _validateProperties(
					Map.of(name, new String[] {"TLSv1.2", "TLSv1.3"}),
					curName -> value, this::_assertAllowedValue));
		}

		_assertSecurityException(
			_REQUIRED_PROPERTY_MESSAGE_PREFIX + name + "\"",
			() -> _validateProperties(
				Map.of(name, new String[] {"TLSv1.2", "TLSv1.3"}),
				curName -> null, this::_assertAllowedValue));

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

		Map<String, String[]> requiredOneOfSystemProperties =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class, "_allowedSystemProperties");

		Assert.assertEquals(
			requiredOneOfSystemProperties.toString(), 1,
			requiredOneOfSystemProperties.size());
		Assert.assertArrayEquals(
			new String[] {"TLSv1.2", "TLSv1.3"},
			requiredOneOfSystemProperties.get("jdk.tls.client.protocols"));
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
				"Servers certificates must be verified in FIPS mode",
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
				"Servers hostname must be verified in FIPS mode",
				() -> FIPSModeValidator.validateServerHostnameVerification(
					false));
		}
	}

	private void _assertAllowedValue(
		String name, String value, String[] requiredValues) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_assertAllowedValue",
			new Class<?>[] {String.class, String.class, String[].class}, name,
			value, requiredValues);
	}

	private void _assertAllRequiredValues(
		String name, String value, String[] requiredValues) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_assertAllRequiredValues",
			new Class<?>[] {String.class, String.class, String[].class}, name,
			value, requiredValues);
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

	private SafeCloseable _swapPortalProperty(String key, String value) {
		String oldValue = GetterUtil.getString(PropsUtil.get(key));

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

	private void _validatePortalProperties() {
		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validatePortalProperties",
			new Class<?>[0]);
	}

	private void _validateProperties(
		Map<String, String[]> properties, Function<String, String> function,
		UnsafeTriConsumer<String, String, String[], SecurityException>
			unsafeTriConsumer) {

		ReflectionTestUtil.invoke(
			FIPSModeValidator.class, "_validateProperties",
			new Class<?>[] {Map.class, Function.class, UnsafeTriConsumer.class},
			properties, function, unsafeTriConsumer);
	}

	private static final String _REQUIRED_PROPERTY_MESSAGE_PREFIX =
		"FIPS mode requires the property \"";

	private static final String _TUNNEL_VERIFY_SSL_HOSTNAME_KEY =
		"com.liferay.portal.kernel.service.http.TunnelUtil.verify.ssl.hostname";

}