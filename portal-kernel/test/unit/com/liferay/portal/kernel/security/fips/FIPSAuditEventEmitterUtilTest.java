/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.security.Provider;
import java.security.Security;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
public class FIPSAuditEventEmitterUtilTest {

	@Before
	public void setUp() {
		_safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID", RandomTestUtil.randomString());

		_fipsAuditUtilMockedStatic = Mockito.mockStatic(FIPSAuditUtil.class);

		_fipsAuditUtilMockedStatic.when(
			() -> FIPSAuditUtil.write(Mockito.any(), Mockito.any())
		).thenAnswer(
			invocation -> {
				_fipsAuditSeverities.add(invocation.getArgument(0));
				_records.add(invocation.getArgument(1));

				return null;
			}
		);
	}

	@After
	public void tearDown() {
		_fipsAuditUtilMockedStatic.close();

		_safeCloseable.close();
	}

	@Test
	public void testEmit() {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID", "instance-1");
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_AUDIT_PROVIDER_CMVP_CERTIFICATE_ID", "4743")) {

			String eventType = RandomTestUtil.randomString();

			FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
				eventType, FIPSAuditSeverity.CRITICAL);

			String fromState = RandomTestUtil.randomString();
			String toState = RandomTestUtil.randomString();

			fipsAuditEvent.put("from-state", fromState);
			fipsAuditEvent.put("to-state", toState);

			FIPSAuditEventEmitterUtil.emit(fipsAuditEvent);

			Assert.assertEquals(
				FIPSAuditSeverity.CRITICAL, _getFIPSAuditSeverity());

			Map<String, Object> record = _getRecord();

			Assert.assertEquals("4743", record.get("cmvp-certificate-id"));
			Assert.assertEquals(
				"instance-1", record.get("deployment-instance-id"));
			Assert.assertEquals("1.0", record.get("event-schema-version"));
			Assert.assertEquals(eventType, record.get("event-type"));

			Map<?, ?> fields = (Map<?, ?>)record.get("fields");

			Assert.assertEquals(fromState, fields.get("from-state"));
			Assert.assertEquals(toState, fields.get("to-state"));

			Provider provider = _getProvider();

			Assert.assertEquals(
				provider.getName(), record.get("provider-name"));
			Assert.assertEquals(
				provider.getVersionStr(), record.get("provider-version"));

			Assert.assertEquals("critical", record.get("severity"));

			String timestamp = String.valueOf(record.get("timestamp"));

			Assert.assertTrue(
				timestamp,
				timestamp.matches(
					"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
		}
	}

	@Test
	public void testEmitDerivesStableDeploymentInstanceIdWhenUnset()
		throws Exception {

		Path liferayHome = Files.createTempDirectory("fips-audit-test");

		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"LIFERAY_HOME", liferayHome.toString());
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID", "");
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_AUDIT_PROVIDER_CMVP_CERTIFICATE_ID", "")) {

			FIPSAuditEventEmitterUtil.emit(
				new FIPSAuditEvent(
					RandomTestUtil.randomString(), FIPSAuditSeverity.INFO));
			FIPSAuditEventEmitterUtil.emit(
				new FIPSAuditEvent(
					RandomTestUtil.randomString(), FIPSAuditSeverity.INFO));

			Assert.assertEquals(_records.toString(), 2, _records.size());

			Map<String, Object> record = _records.get(0);

			Object deploymentInstanceId = record.get("deployment-instance-id");

			Assert.assertTrue(
				String.valueOf(deploymentInstanceId),
				Validator.isNotNull(String.valueOf(deploymentInstanceId)));

			Map<String, Object> secondRecord = _records.get(1);

			Assert.assertEquals(
				deploymentInstanceId,
				secondRecord.get("deployment-instance-id"));

			Assert.assertTrue(
				Files.exists(
					liferayHome.resolve(
						"data/fips-audit-deployment-instance-id")));
		}
		finally {
			_delete(liferayHome);
		}
	}

	@Test
	public void testEmitFormatsTimestampInUTC() {
		TimeZone timeZone = TimeZone.getDefault();

		try {
			TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

			FIPSAuditEventEmitterUtil.emit(
				new FIPSAuditEvent(
					RandomTestUtil.randomString(), FIPSAuditSeverity.INFO));

			Map<String, Object> record = _getRecord();

			String timestamp = String.valueOf(record.get("timestamp"));

			Instant instant = Instant.parse(timestamp);

			Assert.assertTrue(
				timestamp,
				Math.abs(System.currentTimeMillis() - instant.toEpochMilli()) <
					Time.MINUTE);
		}
		finally {
			TimeZone.setDefault(timeZone);
		}
	}

	@Test
	public void testEmitNestsFields() {
		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			RandomTestUtil.randomString(), FIPSAuditSeverity.INFO);

		String spoofedProviderName = RandomTestUtil.randomString();

		fipsAuditEvent.put("provider-name", spoofedProviderName);

		FIPSAuditEventEmitterUtil.emit(fipsAuditEvent);

		Map<String, Object> record = _getRecord();

		Provider provider = _getProvider();

		Assert.assertEquals(provider.getName(), record.get("provider-name"));

		Map<?, ?> fields = (Map<?, ?>)record.get("fields");

		Assert.assertEquals(spoofedProviderName, fields.get("provider-name"));
	}

	private void _delete(Path path) throws IOException {
		File file = path.toFile();

		File[] childFiles = file.listFiles();

		if (childFiles != null) {
			for (File childFile : childFiles) {
				_delete(childFile.toPath());
			}
		}

		Files.delete(path);
	}

	private FIPSAuditSeverity _getFIPSAuditSeverity() {
		return _fipsAuditSeverities.get(_fipsAuditSeverities.size() - 1);
	}

	private Provider _getProvider() {
		Provider[] providers = Security.getProviders();

		return providers[0];
	}

	private Map<String, Object> _getRecord() {
		return _records.get(_records.size() - 1);
	}

	private final List<FIPSAuditSeverity> _fipsAuditSeverities =
		new ArrayList<>();
	private MockedStatic<FIPSAuditUtil> _fipsAuditUtilMockedStatic;
	private final List<Map<String, Object>> _records = new ArrayList<>();
	private SafeCloseable _safeCloseable;

}