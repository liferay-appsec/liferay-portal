/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ServerDetector;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.security.Provider;
import java.security.Security;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
public class FIPSAuditEventEmitterUtilTest {

	@Before
	public void setUp() {

		// Loading PropsUtil also loads ServerDetector, and ServerDetector logs
		// while it loads. Both have to load before LogManager is mocked,
		// because a class that loads inside the mock keeps a null logger.

		PropsUtil.get("fips.enabled");

		_safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID", RandomTestUtil.randomString());

		_logger = Mockito.mock(Logger.class);

		_logManagerMockedStatic = Mockito.mockStatic(
			LogManager.class, Mockito.CALLS_REAL_METHODS);

		_whenLogManager(null);
	}

	@After
	public void tearDown() {
		_logManagerMockedStatic.close();

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

			Map<String, Object> record = _getRecord(Level.ERROR);

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

			List<Map<String, Object>> records = _getRecords(Level.INFO);

			Assert.assertEquals(records.toString(), 2, records.size());

			Map<String, Object> record = records.get(0);

			Object deploymentInstanceId = record.get("deployment-instance-id");

			Assert.assertTrue(
				String.valueOf(deploymentInstanceId),
				Validator.isNotNull(String.valueOf(deploymentInstanceId)));

			Map<String, Object> secondRecord = records.get(1);

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

			Map<String, Object> record = _getRecord(Level.INFO);

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
	public void testEmitLogsRecordAtTheSeverityLevel() {
		_testEmitLogsRecordAtTheSeverityLevel(
			FIPSAuditSeverity.CRITICAL, Level.ERROR);
		_testEmitLogsRecordAtTheSeverityLevel(
			FIPSAuditSeverity.INFO, Level.INFO);
	}

	@Test
	public void testEmitNestsFields() {
		FIPSAuditEvent fipsAuditEvent = new FIPSAuditEvent(
			RandomTestUtil.randomString(), FIPSAuditSeverity.INFO);

		String spoofedProviderName = RandomTestUtil.randomString();

		fipsAuditEvent.put("provider-name", spoofedProviderName);

		FIPSAuditEventEmitterUtil.emit(fipsAuditEvent);

		Map<String, Object> record = _getRecord(Level.INFO);

		Provider provider = _getProvider();

		Assert.assertEquals(provider.getName(), record.get("provider-name"));

		Map<?, ?> fields = (Map<?, ?>)record.get("fields");

		Assert.assertEquals(spoofedProviderName, fields.get("provider-name"));
	}

	@Test
	public void testEmitSyncsCriticalRecordOnly() {
		_whenLogManager(_mockRollingFileAppender("/dev/null/missing.ndjson"));

		FIPSAuditEventEmitterUtil.emit(
			new FIPSAuditEvent(
				RandomTestUtil.randomString(), FIPSAuditSeverity.INFO));

		Assert.assertThrows(
			UncheckedIOException.class,
			() -> FIPSAuditEventEmitterUtil.emit(
				new FIPSAuditEvent(
					RandomTestUtil.randomString(),
					FIPSAuditSeverity.CRITICAL)));
	}

	@Test
	public void testEmitThrowsWhenAppenderIsMissing() {
		Mockito.when(
			_logger.isEnabled(Level.INFO)
		).thenReturn(
			true
		);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> FIPSAuditEventEmitterUtil.emit(
					new FIPSAuditEvent(
						RandomTestUtil.randomString(),
						FIPSAuditSeverity.INFO)));
		}
	}

	@Test
	public void testEmitThrowsWhenLayoutIsNotTheNDJSONLayout() {
		Mockito.when(
			_logger.isEnabled(Level.INFO)
		).thenReturn(
			true
		);

		RollingFileAppender rollingFileAppender = _mockRollingFileAppender(
			"/dev/null/missing.ndjson");

		Mockito.doReturn(
			Mockito.mock(Layout.class)
		).when(
			rollingFileAppender
		).getLayout();

		_whenLogManager(rollingFileAppender);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> FIPSAuditEventEmitterUtil.emit(
					new FIPSAuditEvent(
						RandomTestUtil.randomString(),
						FIPSAuditSeverity.INFO)));
		}
	}

	@Test
	public void testEmitThrowsWhenLoggerIsDisabled() {
		Mockito.when(
			_logger.isEnabled(Level.INFO)
		).thenReturn(
			false
		);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> FIPSAuditEventEmitterUtil.emit(
					new FIPSAuditEvent(
						RandomTestUtil.randomString(),
						FIPSAuditSeverity.INFO)));
		}
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

	private Provider _getProvider() {
		Provider[] providers = Security.getProviders();

		return providers[0];
	}

	private Map<String, Object> _getRecord(Level level) {
		List<Map<String, Object>> records = _getRecords(level);

		return records.get(records.size() - 1);
	}

	private List<Map<String, Object>> _getRecords(Level level) {
		List<Map<String, Object>> records = new ArrayList<>();

		ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(
			Message.class);

		Mockito.verify(
			_logger, Mockito.atLeastOnce()
		).log(
			Mockito.eq(level), argumentCaptor.capture()
		);

		for (Message message : argumentCaptor.getAllValues()) {
			ObjectMessage objectMessage = (ObjectMessage)message;

			records.add((Map<String, Object>)objectMessage.getParameter());
		}

		return records;
	}

	private RollingFileAppender _mockRollingFileAppender(String fileName) {
		RollingFileManager rollingFileManager = Mockito.mock(
			RollingFileManager.class);

		Mockito.when(
			rollingFileManager.getFileName()
		).thenReturn(
			fileName
		);

		RollingFileAppender rollingFileAppender = Mockito.mock(
			RollingFileAppender.class);

		Mockito.when(
			rollingFileAppender.getManager()
		).thenReturn(
			rollingFileManager
		);

		return rollingFileAppender;
	}

	private void _testEmitLogsRecordAtTheSeverityLevel(
		FIPSAuditSeverity fipsAuditSeverity, Level level) {

		FIPSAuditEventEmitterUtil.emit(
			new FIPSAuditEvent(
				RandomTestUtil.randomString(), fipsAuditSeverity));

		Map<String, Object> record = _getRecord(level);

		Assert.assertEquals(
			fipsAuditSeverity.getValue(), record.get("severity"));
	}

	private void _whenLogManager(RollingFileAppender rollingFileAppender) {
		_logManagerMockedStatic.when(
			() -> LogManager.getLogger(_LOGGER_NAME)
		).thenReturn(
			_logger
		);

		Configuration configuration = Mockito.mock(Configuration.class);

		Mockito.when(
			configuration.getAppender(_APPENDER_NAME)
		).thenReturn(
			rollingFileAppender
		);

		LoggerContext loggerContext = Mockito.mock(LoggerContext.class);

		Mockito.when(
			loggerContext.getConfiguration()
		).thenReturn(
			configuration
		);

		_logManagerMockedStatic.when(
			() -> LogManager.getContext(false)
		).thenReturn(
			loggerContext
		);
	}

	private static final String _APPENDER_NAME = "FIPS_AUDIT_FILE";

	private static final String _LOGGER_NAME = "liferay.fips.audit";

	private Logger _logger;
	private MockedStatic<LogManager> _logManagerMockedStatic;
	private SafeCloseable _safeCloseable;

}