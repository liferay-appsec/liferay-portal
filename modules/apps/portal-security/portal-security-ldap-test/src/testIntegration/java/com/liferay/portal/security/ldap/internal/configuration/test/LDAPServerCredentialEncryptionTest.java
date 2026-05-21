/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.ldap.LDAPCredentialCipher;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Caio Farias
 */
@RunWith(Arquillian.class)
public class LDAPServerCredentialEncryptionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testCredentialRoundTrip() throws Exception {
		String plaintext = RandomTestUtil.randomString();

		long companyId = TestPropsValues.getCompanyId();

		String pid = ConfigurationTestUtil.createFactoryConfiguration(
			LDAPServerConfiguration.class.getName(),
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", companyId
			).put(
				"securityCredential", plaintext
			).build());

		try {
			Configuration configuration = _configurationAdmin.getConfiguration(
				pid, "?");

			String stored = (String)configuration.getProperties(
			).get(
				"securityCredential"
			);

			Assert.assertTrue(
				"Expected {ENC} prefix, got: " + stored,
				stored.startsWith("{ENC}"));

			Assert.assertEquals(
				plaintext, _ldapCredentialCipher.resolve(companyId, stored));
		}
		finally {
			ConfigurationTestUtil.deleteFactoryConfiguration(
				pid, LDAPServerConfiguration.class.getName());
		}
	}

	@Test
	public void testResolveReturnsOriginalForMalformedValue() throws Exception {
		String malformedValue = "{ENC}." + RandomTestUtil.randomString();

		Assert.assertEquals(
			malformedValue,
			_ldapCredentialCipher.resolve(
				TestPropsValues.getCompanyId(), malformedValue));
	}

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	@Inject
	private LDAPCredentialCipher _ldapCredentialCipher;

}