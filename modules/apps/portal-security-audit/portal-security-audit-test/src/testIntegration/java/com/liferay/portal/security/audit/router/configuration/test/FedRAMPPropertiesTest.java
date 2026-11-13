/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.configuration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.file.install.properties.ConfigurationHandler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.security.audit.router.configuration.LoggingAuditMessageProcessorConfiguration;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class FedRAMPPropertiesTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testLoggingAuditMessageProcessorConfiguration()
		throws Exception {

		Properties properties = new Properties();

		ClassLoader classLoader = PortalClassLoaderUtil.getClassLoader();

		try (InputStream inputStream = classLoader.getResourceAsStream(
				"portal-fedramp.properties")) {

			properties.load(inputStream);
		}

		String prefix =
			"configuration.override." +
				LoggingAuditMessageProcessorConfiguration.class.getName() + "_";

		Map<String, Object> overrideProperties = new HashMap<>();

		for (String key : properties.stringPropertyNames()) {
			Assert.assertTrue(key.startsWith(prefix));

			overrideProperties.put(
				key.substring(prefix.length()),
				ConfigurationHandler.read(properties.getProperty(key)));
		}

		Assert.assertEquals(
			"?", overrideProperties.get("service.bundleLocation"));
		Assert.assertEquals(
			overrideProperties.toString(), 3, overrideProperties.size());

		LoggingAuditMessageProcessorConfiguration
			loggingAuditMessageProcessorConfiguration =
				ConfigurableUtil.createConfigurable(
					LoggingAuditMessageProcessorConfiguration.class,
					overrideProperties);

		Assert.assertTrue(loggingAuditMessageProcessorConfiguration.enabled());
		Assert.assertEquals(
			"JSON",
			loggingAuditMessageProcessorConfiguration.logMessageFormat());
	}

	@Test
	public void testLoggingAuditMessageProcessorLogLevel() {
		Log log = LogFactoryUtil.getLog(
			"com.liferay.portal.security.audit.router.internal." +
				"LoggingAuditMessageProcessor");

		Assert.assertTrue(log.isInfoEnabled());
	}

}