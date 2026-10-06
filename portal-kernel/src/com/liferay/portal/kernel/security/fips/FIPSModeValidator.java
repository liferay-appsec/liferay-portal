/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.xml.SecureXMLFactoryProviderUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

/**
 * @author Caio Farias
 */
public class FIPSModeValidator {

	public static Provider fetchApprovedModeProvider() {
		return _approvedModeProviderAtomicReference.get();
	}

	public static Provider fetchProvider() {
		Provider[] providers = Security.getProviders();

		if (ArrayUtil.isEmpty(providers)) {
			return null;
		}

		return providers[0];
	}

	public static String[] getAllowedTLSCipherSuites(String[] tlsCipherSuites) {
		if (!PropsValues.FIPS_ENABLED) {
			return tlsCipherSuites;
		}

		return ArrayUtil.toStringArray(
			SetUtil.intersect(
				_allowedTLSCipherSuites, SetUtil.fromArray(tlsCipherSuites)));
	}

	public static String[] getAllowedTLSProtocols(String[] tlsProtocols) {
		if (!PropsValues.FIPS_ENABLED) {
			return tlsProtocols;
		}

		return ArrayUtil.toStringArray(
			SetUtil.intersect(
				_allowedTLSProtocols, SetUtil.fromArray(tlsProtocols)));
	}

	public static String[] getApprovedAlgorithms(Class<?> factoryClass) {
		return ArrayUtil.clone(
			_approvedAlgorithms.getOrDefault(factoryClass, new String[0]));
	}

	public static int[] getKeySizes(String keyAlgorithm) {
		if (StringUtil.equalsIgnoreCase(keyAlgorithm, "AES")) {
			return ArrayUtil.clone(_AES_KEY_SIZES);
		}

		if (StringUtil.equalsIgnoreCase(keyAlgorithm, "RSA")) {
			return ArrayUtil.clone(_RSA_KEY_SIZES);
		}

		return new int[0];
	}

	public static boolean isAllowedAlgorithm(
		CryptoFunctionApprovalType approvalType, Class<?> factoryClass,
		String algorithm) {

		if (ArrayUtil.contains(
				_approvedAlgorithms.get(factoryClass), algorithm, true)) {

			return true;
		}

		if (approvalType == CryptoFunctionApprovalType.APPROVED) {
			return false;
		}

		return ArrayUtil.contains(
			_allowedAlgorithms.get(factoryClass), algorithm, true);
	}

	public static boolean isAllowedKeyAlgorithm(
		CryptoFunctionApprovalType approvalType, Class<?> factoryClass,
		String keyAlgorithm) {

		if (_isKeyAlgorithm(_approvedAlgorithms, factoryClass, keyAlgorithm)) {
			return true;
		}

		if (approvalType == CryptoFunctionApprovalType.APPROVED) {
			return false;
		}

		return _isKeyAlgorithm(_allowedAlgorithms, factoryClass, keyAlgorithm);
	}

	public static boolean isAllowedKeySize(String keyAlgorithm, int keySize) {
		if (ArrayUtil.contains(getKeySizes(keyAlgorithm), keySize)) {
			return true;
		}

		for (String hmacAlgorithm : _HMAC_ALGORITHMS) {
			if (StringUtil.equalsIgnoreCase(keyAlgorithm, hmacAlgorithm) &&
				(keySize >= _HMAC_KEY_SIZE_MIN)) {

				return true;
			}
		}

		return false;
	}

	public static boolean isAllowedSSLContextProtocol(String protocol) {
		if (StringUtil.equalsIgnoreCase(protocol, "TLS")) {
			return true;
		}

		for (String allowedTLSProtocol : _allowedTLSProtocols) {
			if (StringUtil.equalsIgnoreCase(protocol, allowedTLSProtocol)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isAllowedTLSFactoryAlgorithm(String algorithm) {
		return StringUtil.equalsIgnoreCase(algorithm, _TLS_FACTORY_ALGORITHM);
	}

	public static boolean isHealthyProvider(Provider provider) {
		try {
			_validateFIPSProvider(new Provider[] {provider});

			return true;
		}
		catch (SecurityException securityException) {
			return false;
		}
	}

	public static boolean isNotAllowedAlgorithm(String algorithm) {
		if (!PropsValues.FIPS_ENABLED) {
			return false;
		}

		if (Validator.isNull(algorithm)) {
			return true;
		}

		for (String allowedAlgorithm : _allowedAlgorithmPrefixes) {
			if (StringUtil.startsWith(algorithm, allowedAlgorithm)) {
				return false;
			}
		}

		return true;
	}

	public static void validate() {
		Provider approvedModeProvider = null;

		try {
			Provider[] providers = Security.getProviders();

			_validateFIPSProvider(providers);
			_validateProviders(providers);

			_validateAllowedPropertyValues(System::getProperty);
			_validatePortalProperties();
			_validateRequiredPropertyValues(
				Security::getProperty, _requiredSecurityProperties);
			_validateRequiredPropertyValues(
				System::getProperty, _requiredSystemProperties);
			_validateSecureRandomProvider();

			approvedModeProvider = providers[0];
		}
		finally {
			_approvedModeProviderAtomicReference.set(approvedModeProvider);
		}
	}

	public static void validateAlgorithm(String algorithm) {
		if (isNotAllowedAlgorithm(algorithm)) {
			throw new SecurityException(
				"Algorithm \"" + algorithm + "\" is not allowed in FIPS mode");
		}
	}

	public static void validateJWSAlgorithm(String algorithm) {
		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		if ((algorithm == null) || !_allowedJWSAlgorithms.contains(algorithm)) {
			throw new SecurityException(
				"JWS algorithm \"" + algorithm +
					"\" is not allowed in FIPS mode");
		}
	}

	public static void validateKey(String algorithm, int keySize) {
		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		validateAlgorithm(algorithm);

		if ((keySize == 0) || _allowedSymmetricKeySizes.contains(keySize)) {
			return;
		}

		throw new SecurityException(
			"Key size " + keySize + " is not allowed in FIPS mode");
	}

	public static void validateSessionTimeout(int sessionTimeout) {
		if (!PropsValues.FIPS_ENABLED || (sessionTimeout <= 720)) {
			return;
		}

		throw new SecurityException(
			"Session timeout must not be greater than 12 hours in FIPS mode");
	}

	public static void validateURL(String url) {
		if (!PropsValues.FIPS_ENABLED ||
			(Validator.isNotNull(url) &&
			 StringUtil.startsWith(url, "ldaps://"))) {

			return;
		}

		throw new SecurityException(
			"URL protocol scheme is not allowed in FIPS mode");
	}

	private static String _getKeyAlgorithm(
		Class<?> factoryClass, String algorithm) {

		if (factoryClass == Cipher.class) {
			String[] segments = StringUtil.split(algorithm, CharPool.SLASH);

			return segments[0];
		}

		if (factoryClass == Signature.class) {
			int index = algorithm.indexOf("with");

			return algorithm.substring(index + 4);
		}

		return algorithm;
	}

	private static List<String> _getPlaintextSecretProperties(
		Properties properties) {

		List<String> plaintextSecretProperties = new ArrayList<>();

		for (String key : PropsValues.ADMIN_OBFUSCATED_PROPERTIES) {
			String value = properties.getProperty(key);

			if (Validator.isNull(value)) {
				continue;
			}

			value = value.trim();

			if (value.startsWith("${") && value.endsWith("}")) {
				continue;
			}

			plaintextSecretProperties.add(key);
		}

		return plaintextSecretProperties;
	}

	private static boolean _isKeyAlgorithm(
		Map<Class<?>, String[]> algorithms, Class<?> factoryClass,
		String keyAlgorithm) {

		Class<?>[] factoryClasses = {factoryClass};

		if (!_approvedAlgorithms.containsKey(factoryClass)) {
			factoryClasses = _KEY_FACTORY_CLASSES;
		}

		for (Class<?> currentFactoryClass : factoryClasses) {
			String[] currentAlgorithms = algorithms.get(currentFactoryClass);

			if (currentAlgorithms == null) {
				continue;
			}

			for (String algorithm : currentAlgorithms) {
				if (StringUtil.equalsIgnoreCase(
						keyAlgorithm,
						_getKeyAlgorithm(currentFactoryClass, algorithm))) {

					return true;
				}
			}
		}

		return false;
	}

	private static Document _readChannelPropertiesDocument(
		String channelPropertiesLocation) {

		try (InputStream inputStream = Files.newInputStream(
				Paths.get(channelPropertiesLocation))) {

			DocumentBuilderFactory documentBuilderFactory =
				SecureXMLFactoryProviderUtil.newDocumentBuilderFactory();

			DocumentBuilder documentBuilder =
				documentBuilderFactory.newDocumentBuilder();

			documentBuilder.setEntityResolver(
				(publicId, systemId) -> new InputSource(
					new StringReader(StringPool.BLANK)));

			return documentBuilder.parse(
				new InputSource(
					new StringReader(StringUtil.read(inputStream))));
		}
		catch (IOException ioException) {
			throw new SecurityException(
				"Unable to read the cluster link channel properties \"" +
					channelPropertiesLocation + "\" in FIPS mode",
				ioException);
		}
		catch (Exception exception) {
			throw new SecurityException(
				"Unable to parse the cluster link channel properties \"" +
					channelPropertiesLocation + "\" in FIPS mode",
				exception);
		}
	}

	private static void _validateAllowedPropertyValues(
		Function<String, String> function) {

		for (Map.Entry<String, String[]> entry :
				_allowedSystemProperties.entrySet()) {

			_validateAllowedPropertyValues(
				entry.getValue(), entry.getKey(),
				StringUtil.removeChar(
					function.apply(entry.getKey()), CharPool.SPACE));
		}
	}

	private static void _validateAllowedPropertyValues(
		String[] allowedValues, String key, String value) {

		if (ArrayUtil.containsAll(
				allowedValues, StringUtil.split(value, CharPool.COMMA))) {

			return;
		}

		throw new SecurityException(
			StringBundler.concat(
				"FIPS mode requires the property \"", key,
				"\" to be set to only ", Arrays.toString(allowedValues)));
	}

	private static void _validateClusterLinkChannelAuthElement(
		Element authElement, String channelPropertiesLocation) {

		String authClass = authElement.getAttribute("auth_class");

		if (authClass.equals("org.jgroups.auth.X509Token")) {
			return;
		}

		throw new SecurityException(
			StringBundler.concat(
				"The cluster link channel properties \"",
				channelPropertiesLocation,
				"\" must authenticate cluster members with ",
				"\"org.jgroups.auth.X509Token\" in FIPS mode"));
	}

	private static void _validateClusterLinkChannelConfiguration(
		String channelPropertiesLocation) {

		Document document = _readChannelPropertiesDocument(
			channelPropertiesLocation);

		NodeList nodeList = document.getElementsByTagName(StringPool.STAR);

		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);

			String tagName = element.getTagName();

			if (!(tagName.equals("AUTH") || tagName.contains("ENCRYPT"))) {
				continue;
			}

			if (tagName.equals("AUTH")) {
				_validateClusterLinkChannelAuthElement(
					element, channelPropertiesLocation);
			}
			else if (tagName.equals("SYM_ENCRYPT")) {
				_validateClusterLinkChannelSymEncryptElement(element);
			}
			else {
				throw new SecurityException(
					StringBundler.concat(
						"The cluster link channel properties \"",
						channelPropertiesLocation,
						"\" must encrypt intracluster traffic with ",
						"\"SYM_ENCRYPT\" in FIPS mode"));
			}
		}
	}

	private static void _validateClusterLinkChannelSymEncryptElement(
		Element symEncryptElement) {

		String symAlgorithm = symEncryptElement.getAttribute("sym_algorithm");

		if (!Objects.equals(symAlgorithm, "AES/CBC/PKCS5Padding")) {
			throw new SecurityException(
				"Transformation \"" + symAlgorithm +
					"\" is not allowed in FIPS mode");
		}

		String[] symAlgorithmParts = StringUtil.split(
			symAlgorithm, CharPool.SLASH);

		validateKey(
			symAlgorithmParts[0],
			GetterUtil.getInteger(
				symEncryptElement.getAttribute("sym_keylength")));

		int symIVLength = GetterUtil.getInteger(
			symEncryptElement.getAttribute("sym_iv_length"));

		if (symIVLength != 16) {
			throw new SecurityException(
				"Initialization vector size " + symIVLength +
					" is not allowed in FIPS mode");
		}

		String providerName = symEncryptElement.getAttribute("provider");

		if (Validator.isNotNull(providerName) &&
			!_allowedProviderNames.containsKey(providerName)) {

			throw new SecurityException(
				"Security provider \"" + providerName +
					"\" is not allowed in FIPS mode");
		}
	}

	private static void _validateClusterLinkConfiguration() {
		if (!PropsValues.CLUSTER_LINK_ENABLED) {
			return;
		}

		_validateAllowedPropertyValues(
			new String[] {"PKCS12"}, PropsKeys.CLUSTER_LINK_AUTH_KEYSTORE_TYPE,
			PropsUtil.get(PropsKeys.CLUSTER_LINK_AUTH_KEYSTORE_TYPE));
		_validateClusterLinkChannelConfiguration(
			GetterUtil.getString(
				PropsUtil.get(
					PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_CONTROL)));

		Properties properties = PropsUtil.getProperties(
			PropsKeys.CLUSTER_LINK_CHANNEL_PROPERTIES_TRANSPORT, true);

		for (Object channelPropertiesLocation : properties.values()) {
			_validateClusterLinkChannelConfiguration(
				GetterUtil.getString(channelPropertiesLocation));
		}
	}

	private static void _validateFIPSProvider(Provider[] providers) {
		if (ArrayUtil.isEmpty(providers)) {
			throw new SecurityException("There are no security providers");
		}

		Provider provider = providers[0];

		String name = provider.getName();

		if (Validator.isNull(name) ||
			!_allowedProviderNames.containsKey(name)) {

			throw new SecurityException(
				"The first security provider must be an allowed FIPS provider");
		}

		try {
			if (Objects.equals(name, "AmazonCorrettoCryptoProvider")) {
				Class<?> providerClass = provider.getClass();

				Method assertHealthyMethod = ReflectionUtil.getDeclaredMethod(
					providerClass, "assertHealthy");

				assertHealthyMethod.invoke(provider);

				Method isExperimentalFIPSMethod =
					ReflectionUtil.getDeclaredMethod(
						providerClass, "isExperimentalFips");
				Method isFIPSMethod = ReflectionUtil.getDeclaredMethod(
					providerClass, "isFips");

				if (!GetterUtil.getBoolean(
						isExperimentalFIPSMethod.invoke(provider)) &&
					GetterUtil.getBoolean(isFIPSMethod.invoke(provider))) {

					return;
				}

				throw new SecurityException(
					"AmazonCorrettoCryptoProvider must be a nonexperimental " +
						"FIPS build");
			}
			else if (Objects.equals(name, "BCFIPS")) {
				Class<?> providerClass = provider.getClass();

				ClassLoader classLoader = providerClass.getClassLoader();

				Method isInApprovedOnlyModeMethod =
					ReflectionUtil.getDeclaredMethod(
						Class.forName(
							"org.bouncycastle.crypto.CryptoServicesRegistrar",
							true, classLoader),
						"isInApprovedOnlyMode");

				if (!GetterUtil.getBoolean(
						isInApprovedOnlyModeMethod.invoke(null))) {

					throw new SecurityException(
						"BCFIPS is not in approved only mode");
				}

				Class<?> fipsStatusClass = Class.forName(
					"org.bouncycastle.crypto.fips.FipsStatus", true,
					classLoader);

				Method isReadyMethod = ReflectionUtil.getDeclaredMethod(
					fipsStatusClass, "isReady");

				if (!GetterUtil.getBoolean(isReadyMethod.invoke(null))) {
					Method getStatusMessageMethod =
						ReflectionUtil.getDeclaredMethod(
							fipsStatusClass, "getStatusMessage");

					throw new SecurityException(
						"BCFIPS integrity self test failed: " +
							getStatusMessageMethod.invoke(null));
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

			String message = causeThrowable.getMessage();

			if (message == null) {
				message = causeThrowable.toString();
			}

			throw new SecurityException(
				"FIPS provider integrity failed: " + message, causeThrowable);
		}
	}

	private static void _validatePasswordsEncryptionAlgorithm(
		String algorithm) {

		String upperCaseAlgorithm = StringUtil.toUpperCase(
			GetterUtil.getString(algorithm));

		String[] upperCaseAlgorithmParts = StringUtil.split(
			upperCaseAlgorithm, CharPool.SLASH);

		if (ArrayUtil.isEmpty(upperCaseAlgorithmParts) ||
			!_allowedPasswordsEncryptionAlgorithms.contains(
				upperCaseAlgorithmParts[0])) {

			throw new SecurityException(
				"Algorithm \"" + algorithm + "\" is not allowed in FIPS mode");
		}

		int keySize = _PASSWORDS_ENCRYPTION_ALGORITHM_KEY_SIZE_MIN;
		int rounds = _PASSWORDS_ENCRYPTION_ALGORITHM_ROUNDS_MIN;

		Matcher matcher = _pbkdf2Pattern.matcher(upperCaseAlgorithm);

		if (matcher.matches()) {
			keySize = GetterUtil.getInteger(
				matcher.group(1), _PASSWORDS_ENCRYPTION_ALGORITHM_KEY_SIZE_MIN);
			rounds = GetterUtil.getInteger(
				matcher.group(2), _PASSWORDS_ENCRYPTION_ALGORITHM_ROUNDS_MIN);
		}

		if (keySize < _PASSWORDS_ENCRYPTION_ALGORITHM_KEY_SIZE_MIN) {
			throw new SecurityException(
				StringBundler.concat(
					"PBKDF2 output length ", keySize,
					" bits is below the minimum allowed value of ",
					_PASSWORDS_ENCRYPTION_ALGORITHM_KEY_SIZE_MIN, " bits"));
		}

		if (rounds < _PASSWORDS_ENCRYPTION_ALGORITHM_ROUNDS_MIN) {
			throw new SecurityException(
				StringBundler.concat(
					"PBKDF2 iteration count ", rounds,
					" is below the minimum allowed value of ",
					_PASSWORDS_ENCRYPTION_ALGORITHM_ROUNDS_MIN));
		}
	}

	private static void _validatePlaintextSecrets() {
		List<String> messages = new ArrayList<>();

		for (String source : PropsUtil.getLoadedSources()) {
			String fileName = source.substring(source.lastIndexOf('/') + 1);

			if (fileName.equals("portal.properties")) {
				continue;
			}

			Properties properties = null;

			try {
				properties = PropertiesUtil.load(new URL(source));
			}
			catch (IOException ioException) {
				continue;
			}

			for (String key : _getPlaintextSecretProperties(properties)) {
				messages.add(
					StringBundler.concat(
						"property \"", key, "\" in \"", fileName, "\""));
			}
		}

		if (!messages.isEmpty()) {
			throw new SecurityException(
				"A plaintext value for " + StringUtil.merge(messages, ", ") +
					" is not allowed in FIPS mode");
		}
	}

	private static void _validatePortalProperties() {
		if (GetterUtil.getBoolean(PropsUtil.get(PropsKeys.AUTH_MAC_ALLOW))) {
			String authMACAlgorithm = PropsUtil.get(
				PropsKeys.AUTH_MAC_ALGORITHM);

			if (!isAllowedAlgorithm(
					CryptoFunctionApprovalType.APPROVED, MessageDigest.class,
					authMACAlgorithm) &&
				_log.isWarnEnabled()) {

				_log.warn(
					StringBundler.concat(
						"Auth MAC is disabled in FIPS mode because the ",
						"property \"", PropsKeys.AUTH_MAC_ALGORITHM,
						"\" is set to \"", authMACAlgorithm, "\""));
			}
		}

		validateAlgorithm(
			PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM));
		validateAlgorithm(PropsValues.TUNNELING_SERVLET_ENCRYPTION_ALGORITHM);

		if (!GetterUtil.getBoolean(
				PropsUtil.get(PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME))) {

			throw new SecurityException(
				"FIPS mode requires the property \"" +
					PropsKeys.TUNNEL_UTIL_VERIFY_SSL_HOSTNAME +
						"\" to be enabled");
		}

		_validateClusterLinkConfiguration();
		_validatePasswordsEncryptionAlgorithm(
			PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM));
		_validatePlaintextSecrets();
		validateSessionTimeout(PropsValues.SESSION_TIMEOUT);
	}

	private static void _validateProviders(Provider[] providers) {
		Provider provider = providers[0];

		List<String> allowedProviderNames = _allowedProviderNames.get(
			provider.getName());

		Provider[] notAllowedProviders = ArrayUtil.filter(
			providers,
			curProvider ->
				!curProvider.equals(provider) &&
				!allowedProviderNames.contains(curProvider.getName()));

		if (ArrayUtil.isEmpty(notAllowedProviders)) {
			return;
		}

		throw new SecurityException(
			StringBundler.concat(
				"The security providers ", Arrays.toString(notAllowedProviders),
				" are not allowed in FIPS mode for ", provider.getName()));
	}

	private static void _validateRequiredPropertyValues(
		Function<String, String> function,
		Map<String, String[]> propertiesMap) {

		for (Map.Entry<String, String[]> entry : propertiesMap.entrySet()) {
			String value = StringUtil.removeChar(
				function.apply(entry.getKey()), CharPool.SPACE);

			for (String requiredValue : entry.getValue()) {
				if (StringUtil.containsIgnoreCase(value, requiredValue)) {
					continue;
				}

				throw new SecurityException(
					StringBundler.concat(
						"FIPS mode requires the property \"", entry.getKey(),
						"\" to include \"", requiredValue, "\""));
			}
		}
	}

	private static void _validateSecureRandomProvider() {
		try {
			Field field = ReflectionUtil.getDeclaredField(
				SecureRandomUtil.class, "_random");

			Object secureRandom = field.get(null);

			Method getProviderMethod = ReflectionUtil.getDeclaredMethod(
				secureRandom.getClass(), "getProvider");

			Provider provider = (Provider)getProviderMethod.invoke(
				secureRandom);

			String providerName = provider.getName();

			if (Validator.isNull(providerName) ||
				!_allowedProviderNames.containsKey(providerName)) {

				throw new SecurityException(
					"The secure random provider \"" + providerName +
						"\" is not allowed in FIPS mode");
			}
		}
		catch (SecurityException securityException) {
			throw securityException;
		}
		catch (Throwable throwable) {
			throw new SecurityException(
				"Unable to determine the default SecureRandom provider",
				throwable);
		}
	}

	private static final int[] _AES_KEY_SIZES = {128, 192, 256};

	private static final String[] _HMAC_ALGORITHMS = {
		"HmacSHA224", "HmacSHA256", "HmacSHA384", "HmacSHA512"
	};

	private static final int _HMAC_KEY_SIZE_MIN = 112;

	private static final Class<?>[] _KEY_FACTORY_CLASSES = {
		Cipher.class, KeyGenerator.class, KeyPairGenerator.class, Mac.class
	};

	private static final int _PASSWORDS_ENCRYPTION_ALGORITHM_KEY_SIZE_MIN = 112;

	private static final int _PASSWORDS_ENCRYPTION_ALGORITHM_ROUNDS_MIN =
		1300000;

	private static final int[] _RSA_KEY_SIZES = {2048, 3072, 4096};

	private static final String _TLS_FACTORY_ALGORITHM = "PKIX";

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSModeValidator.class);

	private static final Set<String> _allowedAlgorithmPrefixes = Set.of(
		"AES", "HmacSHA256", "HmacSHA384", "HmacSHA512", "PBKDF2WithHmacSHA256",
		"PBKDF2WithHmacSHA384", "PBKDF2WithHmacSHA512", "SHA-256", "SHA-384",
		"SHA-512");
	private static final Map<Class<?>, String[]> _allowedAlgorithms = Map.of(
		KeyStore.class, new String[] {"JKS"}, MessageDigest.class,
		new String[] {"MD5", "SHA-1"});
	private static final Set<String> _allowedJWSAlgorithms = Set.of(
		"ES256", "ES384", "ES512", "EdDSA", "PS256", "PS384", "PS512", "RS256",
		"RS384", "RS512");
	private static final Set<String> _allowedPasswordsEncryptionAlgorithms =
		Set.of(
			"PBKDF2WITHHMACSHA256", "PBKDF2WITHHMACSHA384",
			"PBKDF2WITHHMACSHA512");
	private static final Map<String, List<String>> _allowedProviderNames =
		Map.of(
			"AmazonCorrettoCryptoProvider",
			List.of(
				"JdkLDAP", "JdkSASL", "SUN", "SunEC", "SunJCE", "SunJGSS",
				"SunJSSE", "SunRsaSign", "SunSASL", "XMLDSig"),
			"BCFIPS",
			List.of(
				"BCJSSE", "JdkLDAP", "JdkSASL", "SUN", "SunJCE", "SunJGSS",
				"SunSASL", "XMLDSig"));
	private static final Set<Integer> _allowedSymmetricKeySizes = Set.of(
		128, 192, 256);
	private static final Map<String, String[]> _allowedSystemProperties =
		Map.of("jdk.tls.client.protocols", new String[] {"TLSv1.2", "TLSv1.3"});
	private static final Set<String> _allowedTLSCipherSuites = Set.of(
		"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
	private static final Set<String> _allowedTLSProtocols = Set.of(
		"TLSv1.2", "TLSv1.3");
	private static final Map<Class<?>, String[]> _approvedAlgorithms = Map.of(
		Cipher.class, new String[] {"AES/GCM/NoPadding"}, KeyGenerator.class,
		new String[] {"AES"}, KeyPairGenerator.class, new String[] {"RSA"},
		KeyStore.class, new String[] {"BCFKS", "PKCS12"}, Mac.class,
		new String[] {"HmacSHA256", "HmacSHA384", "HmacSHA512"},
		MessageDigest.class, new String[] {"SHA-256", "SHA-384", "SHA-512"},
		Signature.class,
		new String[] {"SHA256withRSA", "SHA384withRSA", "SHA512withRSA"});
	private static final AtomicReference<Provider>
		_approvedModeProviderAtomicReference = new AtomicReference<>();
	private static final Pattern _pbkdf2Pattern = Pattern.compile(
		"^[^/]*(?:/([0-9]+))?/([0-9]+)$");
	private static final Map<String, String[]> _requiredSecurityProperties =
		Map.of(
			"jdk.tls.disabledAlgorithms",
			new String[] {"SSLv3", "TLS_RSA_*", "TLSv1", "TLSv1.1"},
			"ocsp.enable", new String[] {"true"},
			"ssl.TrustManagerFactory.algorithm",
			new String[] {_TLS_FACTORY_ALGORITHM});
	private static final Map<String, String[]> _requiredSystemProperties =
		Map.of(
			"com.sun.net.ssl.checkRevocation", new String[] {"true"},
			"com.sun.security.enableCRLDP", new String[] {"true"});

}