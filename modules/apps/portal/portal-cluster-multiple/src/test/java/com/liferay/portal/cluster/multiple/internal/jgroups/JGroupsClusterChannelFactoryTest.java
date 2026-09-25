/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.cluster.multiple.internal.jgroups;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.cluster.multiple.configuration.ClusterExecutorConfiguration;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.File;

import java.nio.file.Files;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class JGroupsClusterChannelFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testParseChannelProperties() throws Exception {
		String authValue = RandomTestUtil.randomString();
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));

		JGroupsClusterChannelFactory jGroupsClusterChannelFactory =
			new JGroupsClusterChannelFactory(
				Mockito.mock(ClusterExecutorConfiguration.class),
				(companyId, value) -> {
					if ((companyId == CompanyConstants.SYSTEM) &&
						Objects.equals(value, keyReferenceString)) {

						return authValue;
					}

					return value;
				});

		File file = File.createTempFile("jgroups", ".xml");

		String clusterLinkAuthValue = PropsUtil.get(
			PropsKeys.CLUSTER_LINK_AUTH_VALUE);

		try {
			Files.writeString(
				file.toPath(),
				StringBundler.concat(
					"<config xmlns=\"urn:org:jgroups\"><SHARED_LOOPBACK />",
					"<AUTH auth_class=\"org.jgroups.auth.SimpleToken\" ",
					"auth_value=\"${cluster.link.auth.value}\" /></config>"));

			_assertParseChannelProperties(
				authValue, Collections.singleton(authValue), file,
				jGroupsClusterChannelFactory, keyReferenceString);
			_assertParseChannelProperties(
				authValue, Collections.emptySet(), file,
				jGroupsClusterChannelFactory, authValue);
		}
		finally {
			if (clusterLinkAuthValue != null) {
				PropsUtil.set(
					PropsKeys.CLUSTER_LINK_AUTH_VALUE, clusterLinkAuthValue);
			}

			FileUtil.delete(file);
		}
	}

	private void _assertParseChannelProperties(
			String expectedAuthValue, Set<String> expectedResolvedValues,
			File file,
			JGroupsClusterChannelFactory jGroupsClusterChannelFactory,
			String clusterLinkAuthValue)
		throws Exception {

		PropsUtil.set(PropsKeys.CLUSTER_LINK_AUTH_VALUE, clusterLinkAuthValue);

		Set<String> resolvedValues = new HashSet<>();

		ProtocolStackConfigurator protocolStackConfigurator =
			ReflectionTestUtil.invoke(
				jGroupsClusterChannelFactory, "_parseChannelProperties",
				new Class<?>[] {String.class, Set.class},
				file.getAbsolutePath(), resolvedValues);

		List<ProtocolConfiguration> protocolConfigurations =
			protocolStackConfigurator.getProtocolStack();

		ProtocolConfiguration protocolConfiguration =
			protocolConfigurations.get(1);

		Map<String, String> properties = protocolConfiguration.getProperties();

		Assert.assertEquals(expectedAuthValue, properties.get("auth_value"));

		Assert.assertEquals(expectedResolvedValues, resolvedValues);
	}

}