/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.UnsafeFunction;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.cyclonedx.model.component.crypto.AlgorithmProperties;
import org.cyclonedx.model.component.crypto.enums.AssetType;
import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.Mode;
import org.cyclonedx.model.component.crypto.enums.Padding;
import org.cyclonedx.model.component.crypto.enums.Primitive;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMUtil {

	public static CBOMData.ResolutionContext fetchBoundResolutionContext(
		String methodKey, CBOMData.ResolutionContext resolutionContext) {

		while (resolutionContext != null) {
			if (Objects.equals(resolutionContext.getMethodKey(), methodKey)) {
				return resolutionContext;
			}

			resolutionContext = resolutionContext.getParentResolutionContext();
		}

		return null;
	}

	public static CryptoFunction fetchCipherCryptoFunction(String mode) {
		return _cipherCryptoFunctions.get(mode);
	}

	public static CryptoFunction fetchCryptoFunction(String jcaClassName) {
		return _cryptoFunctions.get(jcaClassName);
	}

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

	public static String fetchJDKPropertyKey(String methodKey) {
		return _jdkPropertyKeys.get(methodKey);
	}

	public static RelatedCryptoMaterialType
		fetchKeyFactoryRelatedCryptoMaterialType(String methodName) {

		return _keyFactoryRelatedCryptoMaterialTypes.get(methodName);
	}

	public static String fetchKeySizeMethodName(String internalName) {
		return _keySizeMethodNames.get(internalName);
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

	public static AssetType getAssetType(String jcaClassName) {
		return _assetTypes.getOrDefault(jcaClassName, AssetType.ALGORITHM);
	}

	public static String getCycloneDXName(
		String algorithm, String jcaClassName, int keySize) {

		String name = _getCycloneDXName(algorithm, jcaClassName);

		if (keySize <= 0) {
			return name;
		}

		if (name.equals("AES") || name.startsWith("AES-")) {
			return "AES-" + keySize + name.substring(3);
		}

		if (Objects.equals(jcaClassName, "KeyPairGenerator")) {
			return name + StringPool.DASH + keySize;
		}

		return name;
	}

	public static String getDefaultSecureRandomAlgorithm() {
		SecureRandom secureRandom = new SecureRandom();

		return secureRandom.getAlgorithm();
	}

	public static Primitive getPrimitive(
		String algorithm, String jcaClassName) {

		Primitive primitive = _primitives.get(jcaClassName);

		if (primitive != null) {
			return primitive;
		}

		if (algorithm == null) {
			return Primitive.UNKNOWN;
		}

		String[] algorithmParts = StringUtil.split(
			StringUtil.toUpperCase(algorithm), CharPool.SLASH);

		String name = algorithmParts[0];

		if (name.startsWith("HMAC")) {
			return Primitive.MAC;
		}

		if (name.startsWith("PBE") || name.startsWith("PBKDF2")) {
			return Primitive.KDF;
		}

		if (name.equals("CHACHA20-POLY1305")) {
			return Primitive.AE;
		}

		if (_streamCipherNames.contains(name)) {
			return Primitive.STREAM_CIPHER;
		}

		if (name.startsWith("AES") || _blockCipherNames.contains(name)) {
			if (Objects.equals(jcaClassName, "Cipher") &&
				(algorithmParts.length > 1) &&
				_aeModeNames.contains(algorithmParts[1])) {

				return Primitive.AE;
			}

			return Primitive.BLOCK_CIPHER;
		}

		if (name.equals("RSA") && Objects.equals(jcaClassName, "Cipher")) {
			return Primitive.PKE;
		}

		if (_keyAgreementNames.contains(name)) {
			return Primitive.KEY_AGREE;
		}

		if (_signatureNames.contains(name)) {
			return Primitive.SIGNATURE;
		}

		return Primitive.UNKNOWN;
	}

	public static String getStandardAlgorithmName(
		String algorithm, String jcaClassName) {

		Provider provider = fetchProvider(algorithm, jcaClassName);

		if (provider == null) {
			return algorithm;
		}

		String name = algorithm;
		String suffix = StringPool.BLANK;

		if (Objects.equals(jcaClassName, "Cipher")) {
			int index = algorithm.indexOf(CharPool.SLASH);

			if (index != -1) {
				name = algorithm.substring(0, index);
				suffix = algorithm.substring(index);
			}
		}

		Provider.Service service = provider.getService(jcaClassName, name);

		if (service == null) {
			return algorithm;
		}

		return service.getAlgorithm() + suffix;
	}

	public static String getUnresolvedReason(String reference) {
		int index = reference.indexOf(CharPool.POUND);

		if (index != -1) {
			String methodName = reference.substring(
				0, reference.indexOf(CharPool.OPEN_PARENTHESIS));

			if (methodName.endsWith(".<init>")) {
				methodName =
					"the constructor of " +
						StringUtil.removeLast(methodName, ".<init>");
			}

			return StringBundler.concat(
				"parameter ",
				GetterUtil.getInteger(reference.substring(index + 1)) + 1,
				" of ", methodName);
		}

		index = reference.indexOf(CharPool.OPEN_PARENTHESIS);

		if (index != -1) {
			return "the return value of " + reference.substring(0, index) +
				"()";
		}

		return "the field " + reference;
	}

	public static String getVisitedKey(
		String reference, CBOMData.ResolutionContext resolutionContext) {

		if (resolutionContext == null) {
			return reference;
		}

		return resolutionContext.getId() + StringPool.COLON + reference;
	}

	public static boolean isKeyAlgorithm(
		String algorithm, String jcaClassName, String keyAlgorithm) {

		String upperCaseAlgorithm = StringUtil.toUpperCase(algorithm);
		String upperCaseKeyAlgorithm = StringUtil.toUpperCase(keyAlgorithm);

		if (Objects.equals(jcaClassName, "Cipher")) {
			String[] transformationParts = StringUtil.split(
				upperCaseAlgorithm, CharPool.SLASH);

			return transformationParts[0].equals(upperCaseKeyAlgorithm);
		}

		if (Objects.equals(jcaClassName, "KeyAgreement")) {
			if (upperCaseAlgorithm.equals(upperCaseKeyAlgorithm) ||
				(upperCaseAlgorithm.equals("ECDH") &&
				 upperCaseKeyAlgorithm.equals("EC"))) {

				return true;
			}

			return false;
		}

		if (Objects.equals(jcaClassName, "Signature")) {
			if (upperCaseAlgorithm.endsWith("WITH" + upperCaseKeyAlgorithm) ||
				(upperCaseAlgorithm.endsWith("WITHECDSA") &&
				 upperCaseKeyAlgorithm.equals("EC"))) {

				return true;
			}

			return false;
		}

		return upperCaseAlgorithm.equals(upperCaseKeyAlgorithm);
	}

	public static boolean isKeyClassName(String internalName) {
		return _keyClassNames.contains(internalName);
	}

	public static boolean isKeyMethodName(String methodName) {
		return _keyMethodNames.contains(methodName);
	}

	public static void setCipherProperties(
		AlgorithmProperties algorithmProperties, String transformation) {

		String[] transformationParts = StringUtil.split(
			StringUtil.toUpperCase(transformation), CharPool.SLASH);

		if (transformationParts.length != 3) {
			return;
		}

		algorithmProperties.setMode(_getMode(transformationParts[1]));

		String paddingName = transformationParts[2];

		if (paddingName.equals("NOPADDING")) {
			return;
		}

		if (paddingName.startsWith("OAEP")) {
			algorithmProperties.setPadding(Padding.OAEP);
		}
		else if (paddingName.equals("PKCS1PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS1V15);
		}
		else if (paddingName.equals("PKCS5PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS5);
		}
		else if (paddingName.equals("PKCS7PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS7);
		}
		else {
			algorithmProperties.setPadding(Padding.OTHER);
		}
	}

	public static CryptoUse toComputedCryptoUse(CryptoUse cryptoUse) {
		CryptoUse computedCryptoUse = new CryptoUse();

		for (String propertyKey : cryptoUse.getPropertyKeys()) {
			computedCryptoUse.addPropertyKey(propertyKey);
		}

		for (String unresolvedReason : cryptoUse.getUnresolvedReasons()) {
			computedCryptoUse.addUnresolvedReason(unresolvedReason);
		}

		if (computedCryptoUse.isConstant()) {
			computedCryptoUse.addUnresolvedReason("a value the code computes");
		}

		return computedCryptoUse;
	}

	private static String _getCipherName(String algorithm) {
		String[] transformationParts = StringUtil.split(
			StringUtil.toUpperCase(algorithm), CharPool.SLASH);

		String name = _cycloneDXCipherNames.get(transformationParts[0]);

		if (name == null) {
			return algorithm;
		}

		if (name.equals("RSA")) {
			if (transformationParts.length != 3) {
				return algorithm;
			}

			return _getRSACipherName(algorithm, transformationParts[2]);
		}

		if ((transformationParts.length == 1) ||
			transformationParts[1].equals("NONE")) {

			return name;
		}

		return name + StringPool.DASH + transformationParts[1];
	}

	private static String _getCycloneDXName(
		String algorithm, String jcaClassName) {

		if (Objects.equals(jcaClassName, "Cipher")) {
			return _getCipherName(algorithm);
		}

		String upperCaseAlgorithm = StringUtil.toUpperCase(algorithm);

		if (Objects.equals(jcaClassName, "KeyAgreement")) {
			return _cycloneDXKeyAgreementNames.getOrDefault(
				upperCaseAlgorithm, algorithm);
		}

		if (Objects.equals(jcaClassName, "MessageDigest")) {
			return _cycloneDXDigestNames.getOrDefault(
				upperCaseAlgorithm, algorithm);
		}

		if (Objects.equals(jcaClassName, "Signature")) {
			return _getSignatureName(algorithm, upperCaseAlgorithm);
		}

		if (upperCaseAlgorithm.startsWith("HMAC")) {
			String digestName = _cycloneDXDigestNames.get(
				upperCaseAlgorithm.substring(4));

			if (digestName != null) {
				return "HMAC-" + digestName;
			}
		}

		return _cycloneDXCipherNames.getOrDefault(
			upperCaseAlgorithm, algorithm);
	}

	private static Mode _getMode(String name) {
		for (Mode mode : Mode.values()) {
			if (StringUtil.equalsIgnoreCase(mode.getName(), name)) {
				return mode;
			}
		}

		return Mode.OTHER;
	}

	private static String _getRSACipherName(
		String algorithm, String paddingName) {

		if (paddingName.equals("OAEPPADDING")) {
			return "RSA-OAEP";
		}

		if (paddingName.equals("PKCS1PADDING")) {
			return "RSA-PKCS1-1.5";
		}

		if (paddingName.startsWith("OAEPWITH") &&
			paddingName.endsWith("ANDMGF1PADDING")) {

			String digestName = _cycloneDXDigestNames.get(
				StringUtil.removeSubstrings(
					paddingName, "OAEPWITH", "ANDMGF1PADDING"));

			if (digestName != null) {
				return "RSA-OAEP-" + digestName;
			}
		}

		return algorithm;
	}

	private static String _getSignatureName(
		String algorithm, String upperCaseAlgorithm) {

		int index = upperCaseAlgorithm.indexOf("WITH");

		if (index == -1) {
			return _cycloneDXSignatureNames.getOrDefault(
				upperCaseAlgorithm, algorithm);
		}

		String digestName = _cycloneDXDigestNames.get(
			upperCaseAlgorithm.substring(0, index));
		String prefix = _cycloneDXSignaturePrefixes.get(
			upperCaseAlgorithm.substring(index + 4));

		if ((digestName == null) || (prefix == null)) {
			return algorithm;
		}

		return prefix + StringPool.DASH + digestName;
	}

	private static final Log _log = LogFactoryUtil.getLog(CBOMUtil.class);

	private static final Set<String> _aeModeNames = SetUtil.fromArray(
		"CCM", "GCM");
	private static final Map<String, AssetType> _assetTypes =
		HashMapBuilder.put(
			"KeyFactory", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"KeyStore", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"SecretKeySpec", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"SSLContext", AssetType.PROTOCOL
		).build();
	private static final Set<String> _blockCipherNames = SetUtil.fromArray(
		"ARIA", "BLOWFISH", "CAMELLIA", "DES", "DESEDE", "RC2", "SM4",
		"TRIPLEDES");
	private static final Map<String, CryptoFunction> _cipherCryptoFunctions =
		HashMapBuilder.put(
			String.valueOf(Cipher.DECRYPT_MODE), CryptoFunction.DECRYPT
		).put(
			String.valueOf(Cipher.ENCRYPT_MODE), CryptoFunction.ENCRYPT
		).put(
			String.valueOf(Cipher.UNWRAP_MODE), CryptoFunction.DECRYPT
		).put(
			String.valueOf(Cipher.WRAP_MODE), CryptoFunction.ENCRYPT
		).build();
	private static final Map<String, CryptoFunction> _cryptoFunctions =
		HashMapBuilder.put(
			"BCrypt", CryptoFunction.KEYDERIVE
		).put(
			"Crypt", CryptoFunction.DIGEST
		).put(
			"KeyAgreement", CryptoFunction.KEYDERIVE
		).put(
			"KeyFactory", CryptoFunction.OTHER
		).put(
			"KeyGenerator", CryptoFunction.KEYGEN
		).put(
			"KeyManagerFactory", CryptoFunction.OTHER
		).put(
			"KeyPairGenerator", CryptoFunction.KEYGEN
		).put(
			"KeyStore", CryptoFunction.OTHER
		).put(
			"Mac", CryptoFunction.TAG
		).put(
			"MessageDigest", CryptoFunction.DIGEST
		).put(
			"SecretKeyFactory", CryptoFunction.KEYDERIVE
		).put(
			"SecretKeySpec", CryptoFunction.OTHER
		).put(
			"SecureRandom", CryptoFunction.GENERATE
		).put(
			"SSLContext", CryptoFunction.OTHER
		).put(
			"TrustManagerFactory", CryptoFunction.OTHER
		).build();
	private static final Map<String, String> _cycloneDXCipherNames =
		HashMapBuilder.put(
			"AES", "AES"
		).put(
			"ARCFOUR", "RC4"
		).put(
			"BLOWFISH", "Blowfish"
		).put(
			"CHACHA20", "ChaCha20"
		).put(
			"CHACHA20-POLY1305", "ChaCha20-Poly1305"
		).put(
			"DES", "DES"
		).put(
			"DESEDE", "3DES"
		).put(
			"RC4", "RC4"
		).put(
			"RSA", "RSA"
		).put(
			"TRIPLEDES", "3DES"
		).build();
	private static final Map<String, String> _cycloneDXDigestNames =
		HashMapBuilder.put(
			"MD2", "MD2"
		).put(
			"MD4", "MD4"
		).put(
			"MD5", "MD5"
		).put(
			"SHA", "SHA-1"
		).put(
			"SHA-1", "SHA-1"
		).put(
			"SHA-224", "SHA-224"
		).put(
			"SHA-256", "SHA-256"
		).put(
			"SHA-384", "SHA-384"
		).put(
			"SHA-512", "SHA-512"
		).put(
			"SHA-512/224", "SHA-512/224"
		).put(
			"SHA-512/256", "SHA-512/256"
		).put(
			"SHA1", "SHA-1"
		).put(
			"SHA3-224", "SHA3-224"
		).put(
			"SHA3-256", "SHA3-256"
		).put(
			"SHA3-384", "SHA3-384"
		).put(
			"SHA3-512", "SHA3-512"
		).put(
			"SHA224", "SHA-224"
		).put(
			"SHA256", "SHA-256"
		).put(
			"SHA384", "SHA-384"
		).put(
			"SHA512", "SHA-512"
		).put(
			"SHA512/224", "SHA-512/224"
		).put(
			"SHA512/256", "SHA-512/256"
		).build();
	private static final Map<String, String> _cycloneDXKeyAgreementNames =
		HashMapBuilder.put(
			"DH", "FFDH"
		).put(
			"DIFFIEHELLMAN", "FFDH"
		).put(
			"ECDH", "ECDH"
		).put(
			"X448", "x448"
		).put(
			"X25519", "x25519"
		).build();
	private static final Map<String, String> _cycloneDXSignatureNames =
		HashMapBuilder.put(
			"ED448", "Ed448"
		).put(
			"ED25519", "Ed25519"
		).put(
			"RSASSA-PSS", "RSA-PSS"
		).build();
	private static final Map<String, String> _cycloneDXSignaturePrefixes =
		HashMapBuilder.put(
			"DSA", "DSA"
		).put(
			"ECDSA", "ECDSA"
		).put(
			"RSA", "RSA-PKCS1-1.5"
		).put(
			"RSA/PSS", "RSA-PSS"
		).build();
	private static final Set<String> _jcaPackageNames = SetUtil.fromArray(
		"java/security", "javax/crypto", "javax/net/ssl");
	private static final Map<String, String> _jdkPropertyKeys =
		HashMapBuilder.put(
			"java/security/KeyStore.getDefaultType", "keystore.type"
		).put(
			"javax/net/ssl/KeyManagerFactory.getDefaultAlgorithm",
			"ssl.KeyManagerFactory.algorithm"
		).put(
			"javax/net/ssl/TrustManagerFactory.getDefaultAlgorithm",
			"ssl.TrustManagerFactory.algorithm"
		).build();
	private static final Set<String> _keyAgreementNames = SetUtil.fromArray(
		"DH", "DIFFIEHELLMAN", "ECDH", "X25519", "X448", "XDH");
	private static final Set<String> _keyClassNames = SetUtil.fromArray(
		"java/security/Key", "java/security/PrivateKey",
		"java/security/PublicKey", "javax/crypto/SecretKey",
		"javax/crypto/spec/SecretKeySpec");
	private static final Map<String, RelatedCryptoMaterialType>
		_keyFactoryRelatedCryptoMaterialTypes = HashMapBuilder.put(
			"generatePrivate", RelatedCryptoMaterialType.PRIVATE_KEY
		).put(
			"generatePublic", RelatedCryptoMaterialType.PUBLIC_KEY
		).build();
	private static final Set<String> _keyMethodNames = SetUtil.fromArray(
		"generateKey", "generatePrivate", "generatePublic", "generateSecret",
		"getAlgorithm");
	private static final Map<String, String> _keySizeMethodNames =
		HashMapBuilder.put(
			"java/security/KeyPairGenerator", "initialize"
		).put(
			"javax/crypto/KeyGenerator", "init"
		).build();
	private static final Map<String, Primitive> _primitives =
		HashMapBuilder.put(
			"BCrypt", Primitive.KDF
		).put(
			"Crypt", Primitive.HASH
		).put(
			"KeyAgreement", Primitive.KEY_AGREE
		).put(
			"KeyManagerFactory", Primitive.OTHER
		).put(
			"KeyStore", Primitive.OTHER
		).put(
			"Mac", Primitive.MAC
		).put(
			"MessageDigest", Primitive.HASH
		).put(
			"SecureRandom", Primitive.DRBG
		).put(
			"Signature", Primitive.SIGNATURE
		).put(
			"SSLContext", Primitive.OTHER
		).put(
			"TrustManagerFactory", Primitive.OTHER
		).build();

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

	private static final Set<String> _signatureNames = SetUtil.fromArray(
		"DSA", "ECDSA", "ED25519", "ED448", "EDDSA");
	private static final Set<String> _streamCipherNames = SetUtil.fromArray(
		"ARCFOUR", "CHACHA20", "RC4");

}