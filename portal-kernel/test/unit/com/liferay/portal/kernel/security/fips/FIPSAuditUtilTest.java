/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ServerDetector;

import java.io.UncheckedIOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collections;
import java.util.Map;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class FIPSAuditUtilTest {

	@Before
	public void setUp() {

		// Loading PropsUtil also loads ServerDetector, and ServerDetector logs
		// while it loads. Both have to load before LogManager is mocked,
		// because a class that loads inside the mock keeps a null logger.

		PropsUtil.get("fips.enabled");
	}

	@Test
	public void testWriteLogsCriticalRecordAtErrorLevel() throws Exception {
		Path path = Files.createTempFile("fips-audit-test", ".ndjson");

		try {
			Logger logger = Mockito.mock(Logger.class);

			try (MockedStatic<LogManager> logManagerMockedStatic =
					Mockito.mockStatic(
						LogManager.class, Mockito.CALLS_REAL_METHODS)) {

				_whenLogManager(
					logManagerMockedStatic, logger,
					_mockRollingFileAppender(String.valueOf(path)));

				Map<String, Object> record = _write(FIPSAuditSeverity.CRITICAL);

				Assert.assertSame(record, _captureRecord(Level.ERROR, logger));
			}
		}
		finally {
			Files.deleteIfExists(path);
		}
	}

	@Test
	public void testWriteLogsRecordAtInfoLevel() {
		Logger logger = Mockito.mock(Logger.class);

		try (MockedStatic<LogManager> logManagerMockedStatic =
				Mockito.mockStatic(
					LogManager.class, Mockito.CALLS_REAL_METHODS)) {

			_whenLogManager(logManagerMockedStatic, logger, null);

			Map<String, Object> record = _write(FIPSAuditSeverity.INFO);

			Assert.assertSame(record, _captureRecord(Level.INFO, logger));
		}
	}

	@Test
	public void testWriteSyncsCriticalRecordOnly() {
		Logger logger = Mockito.mock(Logger.class);

		try (MockedStatic<LogManager> logManagerMockedStatic =
				Mockito.mockStatic(
					LogManager.class, Mockito.CALLS_REAL_METHODS)) {

			_whenLogManager(
				logManagerMockedStatic, logger,
				_mockRollingFileAppender("/dev/null/missing.ndjson"));

			_write(FIPSAuditSeverity.INFO);

			Assert.assertThrows(
				UncheckedIOException.class,
				() -> _write(FIPSAuditSeverity.CRITICAL));
		}
	}

	@Test
	public void testWriteThrowsWhenAppenderIsMissing() {
		Logger logger = Mockito.mock(Logger.class);

		Mockito.when(
			logger.isEnabled(Level.INFO)
		).thenReturn(
			true
		);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<LogManager> logManagerMockedStatic =
				Mockito.mockStatic(
					LogManager.class, Mockito.CALLS_REAL_METHODS);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			_whenLogManager(logManagerMockedStatic, logger, null);

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> _write(FIPSAuditSeverity.INFO));
		}
	}

	@Test
	public void testWriteThrowsWhenLayoutIsNotTheNDJSONLayout() {
		Logger logger = Mockito.mock(Logger.class);

		Mockito.when(
			logger.isEnabled(Level.INFO)
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

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<LogManager> logManagerMockedStatic =
				Mockito.mockStatic(
					LogManager.class, Mockito.CALLS_REAL_METHODS);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			_whenLogManager(
				logManagerMockedStatic, logger, rollingFileAppender);

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> _write(FIPSAuditSeverity.INFO));
		}
	}

	@Test
	public void testWriteThrowsWhenLoggerIsDisabled() {
		Logger logger = Mockito.mock(Logger.class);

		Mockito.when(
			logger.isEnabled(Level.INFO)
		).thenReturn(
			false
		);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			MockedStatic<LogManager> logManagerMockedStatic =
				Mockito.mockStatic(
					LogManager.class, Mockito.CALLS_REAL_METHODS);
			MockedStatic<ServerDetector> serverDetectorMockedStatic =
				Mockito.mockStatic(
					ServerDetector.class, Mockito.CALLS_REAL_METHODS)) {

			_whenLogManager(logManagerMockedStatic, logger, null);

			serverDetectorMockedStatic.when(
				ServerDetector::getServerId
			).thenReturn(
				"tomcat"
			);

			Assert.assertThrows(
				IllegalStateException.class,
				() -> _write(FIPSAuditSeverity.INFO));
		}
	}

	private Map<String, Object> _captureRecord(Level level, Logger logger) {
		ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(
			Message.class);

		Mockito.verify(
			logger
		).log(
			Mockito.eq(level), argumentCaptor.capture()
		);

		ObjectMessage objectMessage = (ObjectMessage)argumentCaptor.getValue();

		return (Map<String, Object>)objectMessage.getParameter();
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

	private void _whenLogManager(
		MockedStatic<LogManager> logManagerMockedStatic, Logger logger,
		RollingFileAppender rollingFileAppender) {

		logManagerMockedStatic.when(
			() -> LogManager.getLogger(_LOGGER_NAME)
		).thenReturn(
			logger
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

		logManagerMockedStatic.when(
			() -> LogManager.getContext(false)
		).thenReturn(
			loggerContext
		);
	}

	private Map<String, Object> _write(FIPSAuditSeverity fipsAuditSeverity) {
		Map<String, Object> record = Collections.singletonMap(
			"event-type", "fips-state-transition");

		FIPSAuditUtil.write(fipsAuditSeverity, record);

		return record;
	}

	private static final String _APPENDER_NAME = "FIPS_AUDIT_FILE";

	private static final String _LOGGER_NAME = "liferay.fips.audit";

}