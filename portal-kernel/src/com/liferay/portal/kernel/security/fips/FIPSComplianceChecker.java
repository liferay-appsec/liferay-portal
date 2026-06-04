/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Caio Farias
 */
public class FIPSComplianceChecker {

	public static void check() {
		Provider[] providers = Security.getProviders();

		_checkProviders(providers);

		_checkFIPSProvider(providers[0]);

		_checkNotAllowedAlgorithm(
			"DES/CBC/PKCS5Padding",
			() -> _generateCipher(
				new IvParameterSpec(new byte[8]),
				new SecretKeySpec(new byte[8], "DES"), "DES/CBC/PKCS5Padding"));
		_checkNotAllowedAlgorithm(
			"DESede",
			() -> _generateCipher(
				new IvParameterSpec(new byte[8]),
				new SecretKeySpec(new byte[24], "DESede"),
				"DESede/CBC/PKCS5Padding"));
		_checkNotAllowedAlgorithm(
			"EC/secp192r1",
			() -> _generateKey("EC", new ECGenParameterSpec("secp192r1")));
		_checkNotAllowedAlgorithm(
			"HmacMD5",
			() -> {
				Mac mac = Mac.getInstance("HmacMD5");

				mac.init(new SecretKeySpec(new byte[16], "HmacMD5"));

				mac.doFinal(new byte[0]);
			});
		_checkNotAllowedAlgorithm(
			"MD5",
			() -> {
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");

				messageDigest.digest(new byte[0]);
			});
		_checkNotAllowedAlgorithm(
			"PBEWithMD5AndDES",
			() -> {
				SecretKeyFactory secretKeyFactory =
					SecretKeyFactory.getInstance("PBEWithMD5AndDES");

				SecretKey key = secretKeyFactory.generateSecret(
					new PBEKeySpec("password".toCharArray()));

				_generateCipher(
					new PBEParameterSpec(new byte[8], 1000), key,
					"PBEWithMD5AndDES");
			});
		_checkNotAllowedAlgorithm(
			"RC4",
			() -> _generateCipher(
				null, new SecretKeySpec(new byte[16], "RC4"), "RC4"));
		_checkNotAllowedAlgorithm(
			"RSA/1024",
			() -> _generateKey(
				"RSA",
				new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)));
		_checkNotAllowedAlgorithm(
			"RSA/512",
			() -> _generateKey(
				"RSA",
				new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4)));
		_checkNotAllowedAlgorithm(
			"SHA1withECDSA",
			() -> _generateSignature(
				new ECGenParameterSpec("secp256r1"), "EC", "SHA1withECDSA"));
		_checkNotAllowedAlgorithm(
			"SHA1withRSA",
			() -> _generateSignature(
				new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4),
				"RSA", "SHA1withRSA"));

		if (_log.isInfoEnabled()) {
			_log.info("FIPS check has passed");
		}
	}

	private static void _checkFIPSProvider(Provider provider) {
		String providerName = provider.getName();

		if (!_allowedProviders.containsKey(providerName)) {
			throw new SecurityException(
				"Invalid FIPS provider: " + providerName);
		}

		try {
			if (providerName.equals("BCFIPS")) {
				Class<?> fipsStatusClass = Class.forName(
					"org.bouncycastle.crypto.fips.FipsStatus");

				Method isReadyMethod = ReflectionUtil.getDeclaredMethod(
					fipsStatusClass, "isReady");

				if (!GetterUtil.getBoolean(isReadyMethod.invoke(null))) {
					Method getStatusMessageMethod =
						ReflectionUtil.getDeclaredMethod(
							fipsStatusClass, "getStatusMessage");

					throw new SecurityException(
						"BCFIPS integrity check failed: " +
							getStatusMessageMethod.invoke(null));
				}

				Class<?> cryptoServicesRegistrarClass = Class.forName(
					"org.bouncycastle.crypto.CryptoServicesRegistrar");

				Method isInApprovedOnlyModeMethod =
					ReflectionUtil.getDeclaredMethod(
						cryptoServicesRegistrarClass, "isInApprovedOnlyMode");

				if (!GetterUtil.getBoolean(
						isInApprovedOnlyModeMethod.invoke(null))) {

					throw new SecurityException(
						"BCFIPS is not in approved-only mode");
				}
			}
			else if (providerName.equals("AmazonCorrettoCryptoProvider")) {
				Class<?> amazonCorrettoCryptoProviderClass = Class.forName(
					"com.amazon.corretto.crypto.provider." +
						"AmazonCorrettoCryptoProvider");

				Field instanceField = ReflectionUtil.getDeclaredField(
					amazonCorrettoCryptoProviderClass, "INSTANCE");

				Method getLoadingErrorMethod = ReflectionUtil.getDeclaredMethod(
					amazonCorrettoCryptoProviderClass, "getLoadingError");

				Object instance = instanceField.get(null);

				Throwable loadingErrorThrowable =
					(Throwable)getLoadingErrorMethod.invoke(instance);

				if (loadingErrorThrowable != null) {
					throw new SecurityException(
						StringBundler.concat(
							"AmazonCorrettoCryptoProvider integrity check ",
							"failed: ", loadingErrorThrowable.getMessage()),
						loadingErrorThrowable);
				}

				Method isFipsMethod = ReflectionUtil.getDeclaredMethod(
					amazonCorrettoCryptoProviderClass, "isFips");

				if (!GetterUtil.getBoolean(isFipsMethod.invoke(instance))) {
					throw new SecurityException(
						"AmazonCorrettoCryptoProvider is not a FIPS build");
				}

				Method isExperimentalFipsMethod =
					ReflectionUtil.getDeclaredMethod(
						amazonCorrettoCryptoProviderClass,
						"isExperimentalFips");

				if (GetterUtil.getBoolean(
						isExperimentalFipsMethod.invoke(instance))) {

					throw new SecurityException(
						"AmazonCorrettoCryptoProvider is an experimental " +
							"FIPS build");
				}

				Method runSelfTestsMethod = ReflectionUtil.getDeclaredMethod(
					amazonCorrettoCryptoProviderClass, "runSelfTests");

				Object result = runSelfTestsMethod.invoke(instance);

				if (!Objects.equals(String.valueOf(result), "PASSED")) {
					throw new SecurityException(
						StringBundler.concat(
							"AmazonCorrettoCryptoProvider integrity check ",
							"failed: ", result));
				}
			}
		}
		catch (SecurityException securityException) {
			throw securityException;
		}
		catch (Throwable throwable) {
			Throwable causeThrowable = throwable.getCause();

			if (causeThrowable == null) {
				causeThrowable = throwable;
			}

			throw new SecurityException(
				"FIPS provider integrity failed: " +
					causeThrowable.getMessage(),
				causeThrowable);
		}
	}

	private static void _checkNotAllowedAlgorithm(
		String algorithm, UnsafeRunnable<Exception> unsafeRunnable) {

		try {
			unsafeRunnable.run();
		}
		catch (Error | GeneralSecurityException | SecurityException exception) {
			return;
		}
		catch (Exception exception) {
			throw new SecurityException(
				"Unable to check not allowed algorithm: " + algorithm,
				exception);
		}

		throw new SecurityException(
			StringBundler.concat(
				"The algorithm \"", algorithm,
				"\" must not be accessible in FIPS mode"));
	}

	private static void _checkProviders(Provider[] providers) {
		if (providers.length == 0) {
			throw new SecurityException("There are no providers registered");
		}

		Provider firstProvider = providers[0];

		if (!_allowedProviders.containsKey(firstProvider.getName())) {
			throw new SecurityException(
				"The first provider must be an allowed FIPS provider");
		}

		List<String> allowedProviders = _allowedProviders.get(
			firstProvider.getName());

		Provider[] notAllowedProviders = ArrayUtil.filter(
			providers,
			provider -> !allowedProviders.contains(provider.getName()));

		if (ArrayUtil.isEmpty(notAllowedProviders)) {
			if (_log.isInfoEnabled()) {
				_log.info("All registered providers are allowed for FIPS mode");
			}

			return;
		}

		throw new SecurityException(
			StringBundler.concat(
				"The providers ", Arrays.toString(notAllowedProviders),
				" are not allowed in FIPS mode for ", firstProvider.getName()));
	}

	private static void _generateCipher(
			AlgorithmParameterSpec algorithmParameterSpec, SecretKey secretKey,
			String transformation)
		throws Exception {

		Cipher cipher = Cipher.getInstance(transformation);

		cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameterSpec);

		cipher.doFinal(new byte[0]);
	}

	private static void _generateKey(
			String algorithm, AlgorithmParameterSpec algorithmParameterSpec)
		throws Exception {

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			algorithm);

		keyPairGenerator.initialize(algorithmParameterSpec);

		keyPairGenerator.generateKeyPair();
	}

	private static void _generateSignature(
			AlgorithmParameterSpec algorithmParameterSpec, String keyAlgorithm,
			String signatureAlgorithm)
		throws Exception {

		Signature signature = Signature.getInstance(signatureAlgorithm);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			keyAlgorithm);

		keyPairGenerator.initialize(algorithmParameterSpec);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		signature.initSign(keyPair.getPrivate());

		signature.sign();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSComplianceChecker.class);

	private static final Map<String, List<String>> _allowedProviders = Map.of(
		"AmazonCorrettoCryptoProvider",
		List.of(
			"AmazonCorrettoCryptoProvider", "SUN", "SunRsaSign", "SunEC",
			"SunJSSE", "SunJCE", "SunJGSS", "SunSASL", "XMLDSig", "JdkLDAP",
			"JdkSASL"),
		"BCFIPS",
		List.of(
			"BCFIPS", "BCJSSE", "SUN", "SunJCE", "XMLDSig", "SunJGSS",
			"SunSASL", "JdkLDAP", "JdkSASL"));

}