/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.configuration;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.FIPSModeTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.math.BigInteger;

import java.security.interfaces.RSAPublicKey;

import java.util.Dictionary;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Rafael Praxedes
 */
public class OAuth2AuthorizationServerConfigurationGeneratorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testActivate() throws Exception {
		FIPSModeTestUtil.assertAlgorithm("RSA", this::_activate);

		Dictionary<String, Object> dictionary = _activate();

		RsaJsonWebKey rsaJsonWebKey = (RsaJsonWebKey)JsonWebKey.Factory.newJwk(
			(String)dictionary.get(
				"oauth2.authorization.server.jwt.access.token.signing.json." +
					"web.key"));

		Assert.assertEquals("RS256", rsaJsonWebKey.getAlgorithm());
		Assert.assertEquals("authServer", rsaJsonWebKey.getKeyId());
		Assert.assertNotNull(rsaJsonWebKey.getPrivateKey());

		RSAPublicKey rsaPublicKey = rsaJsonWebKey.getRsaPublicKey();

		BigInteger modulus = rsaPublicKey.getModulus();

		Assert.assertEquals(2048, modulus.bitLength());
	}

	private Dictionary<String, Object> _activate() throws Exception {
		Configuration configuration = Mockito.mock(Configuration.class);

		ConfigurationAdmin configurationAdmin = Mockito.mock(
			ConfigurationAdmin.class);

		Mockito.when(
			configurationAdmin.getConfiguration(
				Mockito.anyString(), Mockito.anyString())
		).thenReturn(
			configuration
		);

		OAuth2AuthorizationServerConfigurationGenerator
			oAuth2AuthorizationServerConfigurationGenerator =
				new OAuth2AuthorizationServerConfigurationGenerator();

		ReflectionTestUtil.setFieldValue(
			oAuth2AuthorizationServerConfigurationGenerator,
			"_configurationAdmin", configurationAdmin);

		oAuth2AuthorizationServerConfigurationGenerator.activate();

		ArgumentCaptor<Dictionary<String, Object>> argumentCaptor =
			ArgumentCaptor.forClass(Dictionary.class);

		Mockito.verify(
			configuration
		).update(
			argumentCaptor.capture()
		);

		return argumentCaptor.getValue();
	}

}