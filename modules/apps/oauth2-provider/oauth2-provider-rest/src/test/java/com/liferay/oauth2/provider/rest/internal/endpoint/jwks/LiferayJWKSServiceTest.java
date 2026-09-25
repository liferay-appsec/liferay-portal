/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.jwks;

import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.core.Response;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKeys;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Caio Farias
 */
public class LiferayJWKSServiceTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testJwks() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

		keyPairGenerator.initialize(2048);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		JsonWebKey publicJsonWebKey = JwkUtils.fromRSAPublicKey(
			(RSAPublicKey)keyPair.getPublic(), "RS256",
			RandomTestUtil.randomString());

		JsonWebKey jsonWebKey = new JsonWebKey(
			new HashMap<>(publicJsonWebKey.asMap()));

		JsonWebKey privateJsonWebKey = JwkUtils.fromRSAPrivateKey(
			(RSAPrivateKey)keyPair.getPrivate(), "RS256");

		for (Map.Entry<String, Object> entry :
				privateJsonWebKey.asMap(
				).entrySet()) {

			jsonWebKey.setProperty(entry.getKey(), entry.getValue());
		}

		jsonWebKey.setProperty(
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		String jsonWebKeyJSON = JwkUtils.jwkKeyToJson(jsonWebKey);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));

		_assertJwks(
			publicJsonWebKey, keyReferenceString,
			(companyId, value) -> {
				if ((companyId == CompanyConstants.SYSTEM) &&
					Objects.equals(value, keyReferenceString)) {

					return jsonWebKeyJSON;
				}

				return value;
			});

		_assertJwks(
			publicJsonWebKey, jsonWebKeyJSON, (companyId, value) -> value);
	}

	private void _assertJwks(
			JsonWebKey expectedJsonWebKey,
			String jwtAccessTokenSigningJSONWebKey,
			SecretResolver secretResolver)
		throws Exception {

		LiferayJWKSService liferayJWKSService = new LiferayJWKSService();

		ReflectionTestUtil.setFieldValue(
			liferayJWKSService, "_secretResolver", secretResolver);

		liferayJWKSService.activate(
			HashMapBuilder.<String, Object>put(
				"oauth2.authorization.server.jwt.access.token.signing.json." +
					"web.key",
				jwtAccessTokenSigningJSONWebKey
			).build());

		Response response = liferayJWKSService.jwks();

		JsonWebKeys jsonWebKeys = JwkUtils.readJwkSet(
			(String)response.getEntity());

		List<JsonWebKey> jsonWebKeysList = jsonWebKeys.getKeys();

		Assert.assertEquals(
			jsonWebKeysList.toString(), 1, jsonWebKeysList.size());

		JsonWebKey jsonWebKey = jsonWebKeysList.get(0);

		Assert.assertEquals(expectedJsonWebKey.asMap(), jsonWebKey.asMap());
	}

}