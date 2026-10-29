/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cyclonedx.Version;
import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;
import org.cyclonedx.parsers.JsonParser;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Manuele Castro
 */
public class CBOMBuilderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testBuild() throws Exception {
		CryptoUse cipherCryptoUse = _createCryptoUse(
			"AES/GCM/NoPadding", "com.liferay.test.Class1",
			Arrays.asList(CryptoFunction.ENCRYPT, CryptoFunction.DECRYPT),
			"Cipher");

		cipherCryptoUse.setKeySize(256);

		CryptoUse keyGeneratorCryptoUse = _createCryptoUse(
			null, "com.liferay.test.Class2",
			Collections.singletonList(CryptoFunction.KEYGEN), "KeyGenerator");

		keyGeneratorCryptoUse.setConfiguredBy(
			Arrays.asList("test.encryption.algorithm", "test.key.size"));

		String featureName = _FEATURE_NAME + RandomTestUtil.randomString();

		keyGeneratorCryptoUse.setFeatureName(featureName);

		CryptoUse cipherConfigurableCryptoUse = _createCryptoUse(
			null, "com.liferay.test.Class2",
			Collections.singletonList(CryptoFunction.ENCRYPT), "Cipher");

		cipherConfigurableCryptoUse.setConfiguredBy(
			Arrays.asList("test.encryption.algorithm", "test.key.size"));

		CryptoUse keyFactoryCryptoUse = _createCryptoUse(
			"DSA", "com.liferay.test.Class3",
			Collections.singletonList(CryptoFunction.OTHER), "KeyFactory");

		keyFactoryCryptoUse.setRelatedCryptoMaterialTypes(
			Collections.singletonList(RelatedCryptoMaterialType.PUBLIC_KEY));

		CryptoUse unresolvedKeyStoreCryptoUse = _createCryptoUse(
			null, "com.liferay.test.Class10",
			Collections.singletonList(CryptoFunction.OTHER), "KeyStore");

		unresolvedKeyStoreCryptoUse.setUnresolvedReasons(
			Collections.singletonList(
				"the return value of " +
					"com.liferay.test.Class10.getKeyStoreType()"));

		CryptoUse unresolvedMessageDigestCryptoUse = _createCryptoUse(
			null, "com.liferay.test.Class9",
			Collections.singletonList(CryptoFunction.DIGEST), "MessageDigest");

		unresolvedMessageDigestCryptoUse.setUnresolvedReasons(
			Arrays.asList(
				"parameter 1 of com.liferay.test.Class9.digest",
				"the field com.liferay.test.Class9._algorithm"));

		List<CryptoUse> cryptoUses = Arrays.asList(
			_createCryptoUse(
				"SHA-256", "com.liferay.test.Class4",
				Collections.singletonList(CryptoFunction.DIGEST),
				"MessageDigest"),
			keyGeneratorCryptoUse, cipherCryptoUse,
			_createCryptoUse(
				"SHA-256", "com.liferay.test.Class5",
				Collections.singletonList(CryptoFunction.DIGEST),
				"MessageDigest"),
			cipherConfigurableCryptoUse, keyFactoryCryptoUse,
			_createCryptoUse(
				"SHA1withDSA", "com.liferay.test.Class3",
				Collections.singletonList(CryptoFunction.VERIFY), "Signature"),
			_createCryptoUse(
				"HmacSHA1", "com.liferay.test.Class6",
				Collections.singletonList(CryptoFunction.TAG), "Mac"),
			_createCryptoUse(
				"RAW", "com.liferay.test.Class6",
				Collections.singletonList(CryptoFunction.OTHER),
				"SecretKeySpec"),
			_createCryptoUse(
				"PKCS12", "com.liferay.test.Class7",
				Collections.singletonList(CryptoFunction.OTHER), "KeyStore"),
			_createCryptoUse(
				"TLSv1.2", "com.liferay.test.Class8",
				Collections.singletonList(CryptoFunction.OTHER), "SSLContext"),
			unresolvedKeyStoreCryptoUse, unresolvedMessageDigestCryptoUse);

		String configurationName = RandomTestUtil.randomString();

		String cbomJSON = CBOMBuilder.build(configurationName, cryptoUses);

		Assert.assertEquals(
			cbomJSON, CBOMBuilder.build(configurationName, cryptoUses));

		JsonParser jsonParser = new JsonParser();

		List<ParseException> parseExceptions = jsonParser.validate(
			cbomJSON.getBytes(StandardCharsets.UTF_8), Version.VERSION_16);

		Assert.assertEquals(
			parseExceptions.toString(), 0, parseExceptions.size());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(cbomJSON);

		JSONAssert.assertEquals(
			JSONUtil.putAll(
				_toJSONObject(
					"crypto:Cipher:AES/GCM/NoPadding:256",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions",
							JSONUtil.putAll("encrypt", "decrypt")
						).put(
							"mode", "gcm"
						).put(
							"parameterSetIdentifier", "256"
						).put(
							"primitive", "ae"
						)
					).put(
						"assetType", "algorithm"
					),
					"AES-256-GCM",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "Cipher"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SunJCE")),
					"com.liferay.test.Class1"),
				_toJSONObject(
					"crypto:Cipher:test.encryption.algorithm",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions",
							JSONUtil.putAll("keygen", "encrypt")
						).put(
							"primitive", "unknown"
						)
					).put(
						"assetType", "algorithm"
					),
					"test.encryption.algorithm",
					JSONUtil.putAll(
						_toPropertyJSONObject(
							"configured-by", "test.encryption.algorithm"),
						_toPropertyJSONObject("configured-by", "test.key.size"),
						_toPropertyJSONObject(
							"default-value", "test.encryption.algorithm="),
						_toPropertyJSONObject(
							"default-value", "test.key.size="),
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("feature", featureName),
						_toPropertyJSONObject("jca-class", "Cipher"),
						_toPropertyJSONObject("jca-class", "KeyGenerator"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME)),
					"com.liferay.test.Class2"),
				_toJSONObject(
					"crypto:KeyFactory:DSA",
					JSONUtil.put(
						"assetType", "related-crypto-material"
					).put(
						"relatedCryptoMaterialProperties",
						JSONUtil.put(
							"algorithmRef", "crypto:Signature:SHA1withDSA"
						).put(
							"type", "public-key"
						)
					),
					"DSA",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "KeyFactory"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SUN")),
					"com.liferay.test.Class3"),
				_toJSONObject(
					"crypto:KeyStore:PKCS12",
					JSONUtil.put(
						"assetType", "related-crypto-material"
					).put(
						"relatedCryptoMaterialProperties",
						JSONUtil.put(
							"format", "PKCS12"
						).put(
							"type", "other"
						)
					),
					"PKCS12",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "KeyStore"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SUN")),
					"com.liferay.test.Class7"),
				_toJSONObject(
					"crypto:KeyStore:com.liferay.test.Class10",
					JSONUtil.put(
						"assetType", "related-crypto-material"
					).put(
						"relatedCryptoMaterialProperties",
						JSONUtil.put("type", "other")
					),
					"KeyStore",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "KeyStore"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject(
							"unresolved-reason",
							"the return value of " +
								"com.liferay.test.Class10.getKeyStoreType()")),
					"com.liferay.test.Class10"),
				_toJSONObject(
					"crypto:Mac:HmacSHA1",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions", JSONUtil.putAll("tag")
						).put(
							"primitive", "mac"
						)
					).put(
						"assetType", "algorithm"
					),
					"HMAC-SHA-1",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "Mac"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SunJCE")),
					"com.liferay.test.Class6"),
				_toJSONObject(
					"crypto:MessageDigest:SHA-256",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions", JSONUtil.putAll("digest")
						).put(
							"primitive", "hash"
						)
					).put(
						"assetType", "algorithm"
					),
					"SHA-256",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "MessageDigest"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SUN")),
					"com.liferay.test.Class4", "com.liferay.test.Class5"),
				_toJSONObject(
					"crypto:MessageDigest:com.liferay.test.Class9",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions", JSONUtil.putAll("digest")
						).put(
							"primitive", "hash"
						)
					).put(
						"assetType", "algorithm"
					),
					"MessageDigest",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "MessageDigest"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject(
							"unresolved-reason",
							"parameter 1 of com.liferay.test.Class9.digest"),
						_toPropertyJSONObject(
							"unresolved-reason",
							"the field com.liferay.test.Class9._algorithm")),
					"com.liferay.test.Class9"),
				_toJSONObject(
					"crypto:SSLContext:TLSv1.2",
					JSONUtil.put(
						"assetType", "protocol"
					).put(
						"protocolProperties",
						JSONUtil.put(
							"type", "tls"
						).put(
							"version", "1.2"
						)
					),
					"TLSv1.2",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "SSLContext"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SunJSSE")),
					"com.liferay.test.Class8"),
				_toJSONObject(
					"crypto:SecretKeySpec:RAW",
					JSONUtil.put(
						"assetType", "related-crypto-material"
					).put(
						"relatedCryptoMaterialProperties",
						JSONUtil.put(
							"algorithmRef", "crypto:Mac:HmacSHA1"
						).put(
							"type", "secret-key"
						)
					),
					"RAW",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "SecretKeySpec"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME)),
					"com.liferay.test.Class6"),
				_toJSONObject(
					"crypto:Signature:SHA1withDSA",
					JSONUtil.put(
						"algorithmProperties",
						JSONUtil.put(
							"cryptoFunctions", JSONUtil.putAll("verify")
						).put(
							"primitive", "signature"
						)
					).put(
						"assetType", "algorithm"
					),
					"DSA-SHA-1",
					JSONUtil.putAll(
						_toPropertyJSONObject("feature", _FEATURE_NAME),
						_toPropertyJSONObject("jca-class", "Signature"),
						_toPropertyJSONObject("module", _BUNDLE_SYMBOLIC_NAME),
						_toPropertyJSONObject("provider", "SUN")),
					"com.liferay.test.Class3")
			).toString(),
			String.valueOf(jsonObject.getJSONArray("components")),
			JSONCompareMode.STRICT);
		Assert.assertFalse(jsonObject.has("dependencies"));
		JSONAssert.assertEquals(
			JSONUtil.put(
				"component",
				JSONUtil.put(
					"name", ReleaseInfo.getName()
				).put(
					"type", "application"
				).put(
					"version", ReleaseInfo.getVersionDisplayName()
				)
			).put(
				"lifecycles",
				JSONUtil.putAll(JSONUtil.put("phase", "post-build"))
			).put(
				"properties",
				JSONUtil.putAll(
					_toPropertyJSONObject("configuration", configurationName),
					_toPropertyJSONObject("schema-version", "1.0"))
			).toString(),
			String.valueOf(jsonObject.getJSONObject("metadata")),
			JSONCompareMode.STRICT);

		String serialNumber = jsonObject.getString("serialNumber");

		Assert.assertTrue(
			serialNumber.matches(
				"urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-" +
					"[89ab][0-9a-f]{3}-[0-9a-f]{12}"));

		Assert.assertEquals("1.6", jsonObject.getString("specVersion"));
		Assert.assertEquals(1, jsonObject.getInt("version"));

		cbomJSON = CBOMBuilder.build(RandomTestUtil.randomString(), cryptoUses);

		jsonObject = JSONFactoryUtil.createJSONObject(cbomJSON);

		Assert.assertNotEquals(
			serialNumber, jsonObject.getString("serialNumber"));
	}

	private CryptoUse _createCryptoUse(
		String algorithm, String className,
		List<CryptoFunction> cryptoFunctions, String jcaClassName) {

		CryptoUse cryptoUse = new CryptoUse(
			_BUNDLE_SYMBOLIC_NAME, className, _FEATURE_NAME, jcaClassName);

		cryptoUse.setAlgorithm(algorithm);
		cryptoUse.setCryptoFunctions(cryptoFunctions);

		return cryptoUse;
	}

	private JSONObject _toJSONObject(
			String bomRef, JSONObject cryptoPropertiesJSONObject, String name,
			JSONArray propertiesJSONArray, String... locations)
		throws Exception {

		JSONArray occurrencesJSONArray = JSONUtil.toJSONArray(
			Arrays.asList(locations),
			location -> JSONUtil.put("location", location));

		return JSONUtil.put(
			"bom-ref", bomRef
		).put(
			"cryptoProperties", cryptoPropertiesJSONObject
		).put(
			"evidence", JSONUtil.put("occurrences", occurrencesJSONArray)
		).put(
			"name", name
		).put(
			"properties", propertiesJSONArray
		).put(
			"type", "cryptographic-asset"
		);
	}

	private JSONObject _toPropertyJSONObject(String name, String value) {
		return JSONUtil.put(
			"name", "liferay:cbom:" + name
		).put(
			"value", value
		);
	}

	private static final String _BUNDLE_SYMBOLIC_NAME =
		RandomTestUtil.randomString();

	private static final String _FEATURE_NAME = RandomTestUtil.randomString();

}