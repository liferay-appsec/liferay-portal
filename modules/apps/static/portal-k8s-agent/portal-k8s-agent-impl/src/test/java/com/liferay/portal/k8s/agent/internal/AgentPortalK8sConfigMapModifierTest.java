/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.k8s.agent.internal;

import com.liferay.portal.k8s.agent.configuration.PortalK8sAgentConfiguration;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import io.fabric8.kubernetes.client.Config;

import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class AgentPortalK8sConfigMapModifierTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testToConfig() {
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
		String saToken = RandomTestUtil.randomString();

		SecretResolver secretResolver = (companyId, value) -> {
			if ((companyId == CompanyConstants.SYSTEM) &&
				Objects.equals(value, keyReferenceString)) {

				return saToken;
			}

			return value;
		};

		_assertToConfig(saToken, keyReferenceString, secretResolver);
		_assertToConfig(saToken, saToken, secretResolver);
	}

	private void _assertToConfig(
		String expectedOauthToken, String saToken,
		SecretResolver secretResolver) {

		PortalK8sAgentConfiguration portalK8sAgentConfiguration = Mockito.mock(
			PortalK8sAgentConfiguration.class);

		Mockito.when(
			portalK8sAgentConfiguration.saToken()
		).thenReturn(
			saToken
		);

		Config config = ReflectionTestUtil.invoke(
			Mockito.mock(
				AgentPortalK8sConfigMapModifier.class,
				Mockito.CALLS_REAL_METHODS),
			"_toConfig",
			new Class<?>[] {
				PortalK8sAgentConfiguration.class, SecretResolver.class
			},
			portalK8sAgentConfiguration, secretResolver);

		Assert.assertEquals(expectedOauthToken, config.getOauthToken());
	}

}