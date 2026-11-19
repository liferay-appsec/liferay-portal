/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.osgi.commands.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientASLocalMetadataLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Dictionary;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Caio Farias
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class FIPSOSGiCommandsTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testUpdateLocalWellKnownURIs() throws Exception {
		String clientId = RandomTestUtil.randomString();
		String issuerURL = "http://" + RandomTestUtil.randomString();

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"authorizationEndpoint",
				"http://" + RandomTestUtil.randomString()
			).put(
				"companyId", TestPropsValues.getCompanyId()
			).put(
				"issuerURL", issuerURL
			).put(
				"jwksURI", "http://" + RandomTestUtil.randomString()
			).put(
				"openIdConnectClientId", clientId
			).put(
				"subjectTypes", new String[] {"public"}
			).put(
				"tokenEndpoint", "http://" + RandomTestUtil.randomString()
			).put(
				"userInfoEndpoint", "http://" + RandomTestUtil.randomString()
			).build();

		String pid = ConfigurationTestUtil.createFactoryConfiguration(
			"com.liferay.portal.security.sso.openid.connect.internal." +
				"configuration.OpenIdConnectProviderConfiguration",
			properties);

		try {
			OAuthClientASLocalMetadata oAuthClientASLocalMetadata =
				_oAuthClientASLocalMetadataLocalService.
					fetchOAuthClientASLocalMetadata(
						TestPropsValues.getCompanyId(), issuerURL);

			String localWellKnownURI =
				oAuthClientASLocalMetadata.getLocalWellKnownURI();

			OAuthClientEntry oAuthClientEntry =
				_oAuthClientEntryLocalService.fetchOAuthClientEntry(
					TestPropsValues.getCompanyId(), localWellKnownURI,
					clientId);

			OpenIdConnectSession openIdConnectSession =
				_openIdConnectSessionLocalService.createOpenIdConnectSession(
					_counterLocalService.increment());

			openIdConnectSession.setCompanyId(TestPropsValues.getCompanyId());
			openIdConnectSession.setUserId(TestPropsValues.getUserId());
			openIdConnectSession.setAuthServerWellKnownURI(localWellKnownURI);
			openIdConnectSession.setClientId(clientId);
			openIdConnectSession.setIssuer(issuerURL);

			openIdConnectSession =
				_openIdConnectSessionLocalService.updateOpenIdConnectSession(
					openIdConnectSession);

			try (SafeCloseable safeCloseable =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"FIPS_ENABLED", true)) {

				_updateLocalWellKnownURIs();

				oAuthClientASLocalMetadata =
					_oAuthClientASLocalMetadataLocalService.
						fetchOAuthClientASLocalMetadata(
							TestPropsValues.getCompanyId(), issuerURL);

				String newLocalWellKnownURI =
					oAuthClientASLocalMetadata.getLocalWellKnownURI();

				Assert.assertNotEquals(localWellKnownURI, newLocalWellKnownURI);

				OAuthClientEntry newOAuthClientEntry =
					_oAuthClientEntryLocalService.fetchOAuthClientEntry(
						TestPropsValues.getCompanyId(), newLocalWellKnownURI,
						clientId);

				Assert.assertEquals(
					oAuthClientEntry.getOAuthClientEntryId(),
					newOAuthClientEntry.getOAuthClientEntryId());

				openIdConnectSession =
					_openIdConnectSessionLocalService.getOpenIdConnectSession(
						openIdConnectSession.getOpenIdConnectSessionId());

				Assert.assertEquals(
					newLocalWellKnownURI,
					openIdConnectSession.getAuthServerWellKnownURI());

				Assert.assertEquals(
					"Updated 0 local well known URIs",
					_updateLocalWellKnownURIs());

				int tokenConnectionTimeout = RandomTestUtil.randomInt();

				properties.put(
					"tokenConnectionTimeout", tokenConnectionTimeout);

				ConfigurationTestUtil.saveConfiguration(pid, properties);

				newOAuthClientEntry =
					_oAuthClientEntryLocalService.fetchOAuthClientEntry(
						TestPropsValues.getCompanyId(), newLocalWellKnownURI,
						clientId);

				Assert.assertEquals(
					tokenConnectionTimeout,
					newOAuthClientEntry.getTokenConnectionTimeout());

				ConfigurationTestUtil.deleteConfiguration(pid);

				pid = null;

				Assert.assertNull(
					_oAuthClientASLocalMetadataLocalService.
						fetchOAuthClientASLocalMetadata(
							TestPropsValues.getCompanyId(), issuerURL));
				Assert.assertNull(
					_oAuthClientEntryLocalService.fetchOAuthClientEntry(
						TestPropsValues.getCompanyId(), newLocalWellKnownURI,
						clientId));
			}
		}
		finally {
			if (pid != null) {
				ConfigurationTestUtil.deleteConfiguration(pid);
			}
		}
	}

	private String _updateLocalWellKnownURIs() {
		return ReflectionTestUtil.invoke(
			_osgiCommands, "updateLocalWellKnownURIs", new Class<?>[0]);
	}

	@Inject
	private CounterLocalService _counterLocalService;

	@Inject
	private OAuthClientASLocalMetadataLocalService
		_oAuthClientASLocalMetadataLocalService;

	@Inject
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Inject
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

	@Inject(filter = "osgi.command.scope=fips")
	private OSGiCommands _osgiCommands;

}