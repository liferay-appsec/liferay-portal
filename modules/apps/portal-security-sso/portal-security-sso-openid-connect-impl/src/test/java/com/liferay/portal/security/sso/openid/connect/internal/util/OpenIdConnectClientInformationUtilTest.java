/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.util;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.sso.openid.connect.internal.configuration.OpenIdConnectProviderConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;

import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Caio Farias
 */
public class OpenIdConnectClientInformationUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetOIDCClientInformation() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		long guestUserId = RandomTestUtil.randomLong();

		Mockito.when(
			_userLocalService.getGuestUserId(companyId)
		).thenReturn(
			guestUserId
		);

		String keyReferenceString = _getKeyReferenceString();
		String otherKeyReferenceString = _getKeyReferenceString();
		String systemKeyReferenceString = _getKeyReferenceString();

		Configuration[] configurations = {
			_getConfiguration(
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", companyId
				).put(
					"openIdConnectClientSecret", keyReferenceString
				).build()),
			_getConfiguration(
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", companyId + 1
				).put(
					"openIdConnectClientSecret", otherKeyReferenceString
				).build()),
			_getConfiguration(
				HashMapDictionaryBuilder.<String, Object>put(
					"openIdConnectClientSecret", systemKeyReferenceString
				).build())
		};

		Mockito.when(
			_configurationAdmin.listConfigurations(
				StringBundler.concat(
					"(|(service.factoryPid=",
					OpenIdConnectProviderConfiguration.class.getName(),
					")(service.factoryPid=",
					OpenIdConnectProviderConfiguration.class.getName(),
					".scoped))"))
		).thenReturn(
			configurations
		);

		String clientSecret = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						_isConfigurationSecretContext(
							keyReferenceString, secretContext)),
				Mockito.eq(keyReferenceString))
		).thenReturn(
			clientSecret
		);

		Mockito.when(
			_secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						_isConfigurationSecretContext(
							systemKeyReferenceString, secretContext)),
				Mockito.eq(systemKeyReferenceString))
		).thenReturn(
			clientSecret
		);

		long primaryKey = RandomTestUtil.randomLong();
		String userKeyReferenceString = _getKeyReferenceString();

		Mockito.when(
			_secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						(secretContext != null) &&
						Objects.equals(
							secretContext.getClassName(),
							OAuthClientEntry.class.getName()) &&
						(secretContext.getCompanyId() == companyId) &&
						Objects.equals(
							secretContext.getKey(), "clientSecret") &&
						(secretContext.getPrimaryKey() == primaryKey)),
				Mockito.eq(userKeyReferenceString))
		).thenReturn(
			clientSecret
		);

		_testGetOIDCClientInformation(
			keyReferenceString, companyId, clientSecret, primaryKey,
			guestUserId);
		_testGetOIDCClientInformation(
			otherKeyReferenceString, companyId, otherKeyReferenceString,
			primaryKey, guestUserId);
		_testGetOIDCClientInformation(
			systemKeyReferenceString, companyId, clientSecret, primaryKey,
			guestUserId);

		_testGetOIDCClientInformation(
			clientSecret, companyId, clientSecret, primaryKey, guestUserId + 1);
		_testGetOIDCClientInformation(
			userKeyReferenceString, companyId, clientSecret, primaryKey,
			guestUserId + 1);

		Mockito.when(
			_configurationAdmin.listConfigurations(Mockito.anyString())
		).thenReturn(
			null
		);

		_testGetOIDCClientInformation(
			keyReferenceString, companyId, keyReferenceString, primaryKey,
			guestUserId);
	}

	private Configuration _getConfiguration(
		Dictionary<String, Object> properties) {

		Configuration configuration = Mockito.mock(Configuration.class);

		Mockito.when(
			configuration.getProperties()
		).thenReturn(
			properties
		);

		return configuration;
	}

	private String _getKeyReferenceString() {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	private boolean _isConfigurationSecretContext(
		String keyReferenceString, SecretContext secretContext) {

		if ((secretContext == null) ||
			(secretContext.getType() != SecretContext.Type.CONFIGURATION) ||
			!Objects.equals(
				secretContext.getKey(), "openIdConnectClientSecret")) {

			return false;
		}

		Map<String, Object> properties = secretContext.getProperties();

		return Objects.equals(
			keyReferenceString, properties.get("openIdConnectClientSecret"));
	}

	private void _testGetOIDCClientInformation(
			String clientSecret, long companyId, String expectedClientSecret,
			long primaryKey, long userId)
		throws Exception {

		OAuthClientEntry oAuthClientEntry = Mockito.mock(
			OAuthClientEntry.class);

		Mockito.when(
			oAuthClientEntry.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			oAuthClientEntry.getInfoJSON()
		).thenReturn(
			JSONUtil.put(
				"client_id", RandomTestUtil.randomString()
			).put(
				"client_secret", clientSecret
			).toString()
		);

		Mockito.when(
			oAuthClientEntry.getPrimaryKey()
		).thenReturn(
			primaryKey
		);

		Mockito.when(
			oAuthClientEntry.getUserId()
		).thenReturn(
			userId
		);

		OIDCClientInformation oidcClientInformation =
			OpenIdConnectClientInformationUtil.getOIDCClientInformation(
				companyId, _configurationAdmin, oAuthClientEntry,
				_secretResolver, _userLocalService);

		Secret secret = oidcClientInformation.getSecret();

		Assert.assertEquals(expectedClientSecret, secret.getValue());
	}

	private final ConfigurationAdmin _configurationAdmin = Mockito.mock(
		ConfigurationAdmin.class);
	private final SecretResolver _secretResolver = Mockito.mock(
		SecretResolver.class);
	private final UserLocalService _userLocalService = Mockito.mock(
		UserLocalService.class);

}