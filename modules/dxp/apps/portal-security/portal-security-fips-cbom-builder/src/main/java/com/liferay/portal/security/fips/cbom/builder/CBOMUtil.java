/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.UnsafeFunction;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.SetUtil;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMUtil {

	public static String fetchJCAClassName(String internalName) {
		int index = internalName.lastIndexOf(CharPool.SLASH);

		if ((index == -1) ||
			!_jcaPackageNames.contains(internalName.substring(0, index))) {

			return null;
		}

		String jcaClassName = internalName.substring(index + 1);

		if (!_providerUnsafeFunctions.containsKey(jcaClassName)) {
			return null;
		}

		return jcaClassName;
	}

	public static Provider fetchProvider(
		String algorithm, String jcaClassName) {

		UnsafeFunction<String, Provider, GeneralSecurityException>
			unsafeFunction = _providerUnsafeFunctions.get(jcaClassName);

		if (unsafeFunction == null) {
			return null;
		}

		try {
			return unsafeFunction.apply(algorithm);
		}
		catch (GeneralSecurityException generalSecurityException) {
			if (_log.isDebugEnabled()) {
				_log.debug(generalSecurityException);
			}

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(CBOMUtil.class);

	private static final Set<String> _jcaPackageNames = SetUtil.fromArray(
		"java/security", "javax/crypto", "javax/net/ssl");

	private static final Map
		<String, UnsafeFunction<String, Provider, GeneralSecurityException>>
			_providerUnsafeFunctions =
				HashMapBuilder.
					<String,
					 UnsafeFunction<String, Provider, GeneralSecurityException>>
						put(
							"Cipher",
							algorithm -> {
								Cipher cipher = Cipher.getInstance(algorithm);

								return cipher.getProvider();
							}
					).put(
						"KeyAgreement",
						algorithm -> {
							KeyAgreement keyAgreement =
								KeyAgreement.getInstance(algorithm);

							return keyAgreement.getProvider();
						}
					).put(
						"KeyFactory",
						algorithm -> {
							KeyFactory keyFactory = KeyFactory.getInstance(
								algorithm);

							return keyFactory.getProvider();
						}
					).put(
						"KeyGenerator",
						algorithm -> {
							KeyGenerator keyGenerator =
								KeyGenerator.getInstance(algorithm);

							return keyGenerator.getProvider();
						}
					).put(
						"KeyManagerFactory",
						algorithm -> {
							KeyManagerFactory keyManagerFactory =
								KeyManagerFactory.getInstance(algorithm);

							return keyManagerFactory.getProvider();
						}
					).put(
						"KeyPairGenerator",
						algorithm -> {
							KeyPairGenerator keyPairGenerator =
								KeyPairGenerator.getInstance(algorithm);

							return keyPairGenerator.getProvider();
						}
					).put(
						"KeyStore",
						algorithm -> {
							KeyStore keyStore = KeyStore.getInstance(algorithm);

							return keyStore.getProvider();
						}
					).put(
						"Mac",
						algorithm -> {
							Mac mac = Mac.getInstance(algorithm);

							return mac.getProvider();
						}
					).put(
						"MessageDigest",
						algorithm -> {
							MessageDigest messageDigest =
								MessageDigest.getInstance(algorithm);

							return messageDigest.getProvider();
						}
					).put(
						"SecretKeyFactory",
						algorithm -> {
							SecretKeyFactory secretKeyFactory =
								SecretKeyFactory.getInstance(algorithm);

							return secretKeyFactory.getProvider();
						}
					).put(
						"SecureRandom",
						algorithm -> {
							SecureRandom secureRandom =
								SecureRandom.getInstance(algorithm);

							return secureRandom.getProvider();
						}
					).put(
						"Signature",
						algorithm -> {
							Signature signature = Signature.getInstance(
								algorithm);

							return signature.getProvider();
						}
					).put(
						"SSLContext",
						algorithm -> {
							SSLContext sslContext = SSLContext.getInstance(
								algorithm);

							return sslContext.getProvider();
						}
					).put(
						"TrustManagerFactory",
						algorithm -> {
							TrustManagerFactory trustManagerFactory =
								TrustManagerFactory.getInstance(algorithm);

							return trustManagerFactory.getProvider();
						}
					).build();

}