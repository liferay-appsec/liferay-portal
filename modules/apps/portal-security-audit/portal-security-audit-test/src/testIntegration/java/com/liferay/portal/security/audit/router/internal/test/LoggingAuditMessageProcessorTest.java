/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.router.configuration.LoggingAuditMessageProcessorConfiguration;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Dictionary;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class LoggingAuditMessageProcessorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testProcess() throws Exception {
		PrintStream printStream = System.out;

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					LoggingAuditMessageProcessorConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", true
					).put(
						"logMessageFormat", "JSON"
					).put(
						"outputToConsole", true
					).build())) {

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream();

			System.setOut(new PrintStream(byteArrayOutputStream));

			_testProcess(
				byteArrayOutputStream, AuditConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"enabled", false
				).build());
			_testProcess(
				byteArrayOutputStream,
				LoggingAuditMessageProcessorConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"enabled", false
				).put(
					"logMessageFormat", "JSON"
				).put(
					"outputToConsole", true
				).build());
		}
		finally {
			System.setOut(printStream);
		}
	}

	private List<String> _getActions(
		ByteArrayOutputStream byteArrayOutputStream, String pid) {

		return TransformUtil.transformToList(
			StringUtil.splitLines(byteArrayOutputStream.toString()),
			line -> {
				if (!line.startsWith("LoggingAuditMessageProcessor: ")) {
					return null;
				}

				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
					line.substring(line.indexOf(CharPool.OPEN_CURLY_BRACE)));

				JSONObject additionalInfoJSONObject = jsonObject.getJSONObject(
					"additionalInfo");

				if (!Objects.equals(
						jsonObject.getString("eventType"),
						EventTypes.AUDIT_CONFIG_CHANGE) ||
					!Objects.equals(
						additionalInfoJSONObject.getString("pid"), pid)) {

					return null;
				}

				return additionalInfoJSONObject.getString("action");
			});
	}

	private void _testProcess(
			ByteArrayOutputStream byteArrayOutputStream, String pid,
			Dictionary<String, Object> properties)
		throws Exception {

		byteArrayOutputStream.reset();

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(pid, properties)) {

			List<String> actions = _getActions(byteArrayOutputStream, pid);

			Assert.assertEquals(actions.toString(), 1, actions.size());
		}

		List<String> actions = _getActions(byteArrayOutputStream, pid);

		Assert.assertEquals(actions.toString(), 2, actions.size());
	}

}