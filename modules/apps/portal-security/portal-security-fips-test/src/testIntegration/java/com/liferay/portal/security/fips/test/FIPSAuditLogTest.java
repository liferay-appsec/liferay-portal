/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Asserts that the FIPS audit trail reaches disk through the real
 * <code>FIPS_AUDIT_FILE</code> appender, which no unit test covers because every
 * one of them stubs the logger. ISO/IEC 19790 7.11.6.2 [AS11.29] asks for
 * functional testing that the module behaves according to its functional
 * specification, and the trail only exists once the appender, the layout and the
 * logger are wired together at boot.
 *
 * @author Jorge García Jiménez
 */
@RunWith(Arquillian.class)
public class FIPSAuditLogTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Assume.assumeTrue(PropsValues.FIPS_ENABLED);
	}

	@Test
	public void testAuditLogFileExists() {
		Path path = _getPath();

		Assert.assertTrue(
			"Unable to find the FIPS audit log " + path, Files.exists(path));
	}

	@Test
	public void testAuditLogFileIsReadableByTheOwnerOnly() throws Exception {
		Path path = _getPath();

		FileSystem fileSystem = path.getFileSystem();

		Set<String> supportedFileAttributeViews =
			fileSystem.supportedFileAttributeViews();

		Assume.assumeTrue(supportedFileAttributeViews.contains("posix"));

		Assert.assertEquals(
			PosixFilePermissions.fromString("rw-------"),
			Files.getPosixFilePermissions(path));
	}

	@Test
	public void testAuditLogRecordsAreParseable() throws Exception {
		for (String line : Files.readAllLines(_getPath())) {
			Assert.assertTrue(
				"Blank line in the FIPS audit log", Validator.isNotNull(line));

			JSONFactoryUtil.createJSONObject(line);
		}
	}

	@Test
	public void testAuditLogRecordsCarryTheCommonEnvelope() throws Exception {
		for (JSONObject jsonObject : _getRecordJSONObjects()) {
			for (String envelopeKey : _ENVELOPE_KEYS) {
				Assert.assertTrue(
					StringBundler.concat(
						"Unable to find \"", envelopeKey,
						"\" in the FIPS audit record ", jsonObject),
					jsonObject.has(envelopeKey));
			}
		}
	}

	@Test
	public void testAuditLogRecordsTheBootStateTransitions() throws Exception {
		List<String> transitions = new ArrayList<>();

		for (JSONObject jsonObject : _getRecordJSONObjects()) {
			String eventType = jsonObject.getString("event-type");

			if (!eventType.equals("fips-state-transition")) {
				continue;
			}

			JSONObject fieldsJSONObject = jsonObject.getJSONObject("fields");

			transitions.add(
				StringBundler.concat(
					fieldsJSONObject.getString("from-state"), " to ",
					fieldsJSONObject.getString("to-state")));
		}

		Assert.assertTrue(
			transitions.toString(),
			transitions.contains("Initializing to Self-Test"));
		Assert.assertTrue(
			transitions.toString(),
			transitions.contains("Self-Test to Operational"));
	}

	private Path _getPath() {
		LocalDate localDate = LocalDate.now(ZoneOffset.UTC);

		return Paths.get(
			PropsValues.LIFERAY_HOME, "logs",
			StringBundler.concat("fips-audit.", localDate, ".ndjson"));
	}

	private List<JSONObject> _getRecordJSONObjects() throws Exception {
		List<JSONObject> jsonObjects = new ArrayList<>();

		for (String line : Files.readAllLines(_getPath())) {
			jsonObjects.add(JSONFactoryUtil.createJSONObject(line));
		}

		return jsonObjects;
	}

	private static final String[] _ENVELOPE_KEYS = {
		"cmvp-certificate-id", "deployment-instance-id", "event-schema-version",
		"event-type", "fields", "provider-name", "provider-version", "severity",
		"timestamp"
	};

}