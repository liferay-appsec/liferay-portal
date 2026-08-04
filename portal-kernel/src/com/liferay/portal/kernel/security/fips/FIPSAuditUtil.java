/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.ServerDetector;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.ObjectMessage;

/**
 * Writes each FIPS audit record to the <code>FIPS_AUDIT_FILE</code> appender,
 * which owns the rolling file, its permissions, and its retention. The record
 * travels as an {@link ObjectMessage}, so the appender's layout renders the
 * NDJSON line and no part of the format lives here.
 *
 * <p>
 * The logger is taken straight from {@link LogManager} rather than from
 * <code>LogFactoryUtil</code> because the record has to reach the layout as an
 * object rather than as text, and through that facade it would not:
 * <code>LogFactoryUtil</code> wraps every log in a
 * <code>SanitizerLogWrapper</code> while <code>log.sanitizer.enabled</code> is
 * set, and that wrapper replaces the message with its <code>toString</code>, so
 * the layout would find a string where it reads the record and would render the
 * whole record as one <code>message</code> key.
 * </p>
 *
 * <p>
 * A critical record is forced to disk before the call returns. The appender
 * flushes its own stream on every event, but a flush only reaches the operating
 * system, so an Error State entry would still be lost to a crash without the
 * {@link FileChannel#force} below.
 * </p>
 *
 * @author Rafael Praxedes
 */
public class FIPSAuditUtil {

	public static void write(
		FIPSAuditSeverity fipsAuditSeverity, Map<String, Object> record) {

		Level level = _getLevel(fipsAuditSeverity);

		Logger logger = LogManager.getLogger(_LOGGER_NAME);

		_assertDeliverable(level, logger);

		logger.log(level, new ObjectMessage(record));

		if (fipsAuditSeverity == FIPSAuditSeverity.CRITICAL) {
			_sync();
		}
	}

	private static void _assertDeliverable(Level level, Logger logger) {
		if (!PropsValues.FIPS_ENABLED ||
			(ServerDetector.getServerId() == null)) {

			return;
		}

		if (!logger.isEnabled(level)) {
			throw new IllegalStateException(
				StringBundler.concat(
					"Unable to write a FIPS audit record because the logger \"",
					_LOGGER_NAME, "\" is disabled for the level \"", level,
					"\""));
		}

		if (_fetchRollingFileAppender() == null) {
			throw new IllegalStateException(
				StringBundler.concat(
					"Unable to write a FIPS audit record because the appender ",
					"\"", _APPENDER_NAME, "\" is not configured"));
		}
	}

	private static RollingFileAppender _fetchRollingFileAppender() {
		LoggerContext loggerContext = (LoggerContext)LogManager.getContext(
			false);

		Configuration configuration = loggerContext.getConfiguration();

		Appender appender = configuration.getAppender(_APPENDER_NAME);

		if (appender instanceof RollingFileAppender) {
			return (RollingFileAppender)appender;
		}

		return null;
	}

	private static Level _getLevel(FIPSAuditSeverity fipsAuditSeverity) {
		if (fipsAuditSeverity == FIPSAuditSeverity.CRITICAL) {
			return Level.ERROR;
		}

		return Level.INFO;
	}

	private static void _sync() {
		RollingFileAppender rollingFileAppender = _fetchRollingFileAppender();

		if (rollingFileAppender == null) {
			return;
		}

		String fileName = rollingFileAppender.getFileName();

		if (fileName == null) {
			return;
		}

		Path path = Paths.get(fileName);

		try (FileChannel fileChannel = FileChannel.open(
				path, StandardOpenOption.WRITE)) {

			fileChannel.force(true);
		}
		catch (IOException ioException) {
			throw new UncheckedIOException(
				"Unable to flush the FIPS audit log", ioException);
		}
	}

	private static final String _APPENDER_NAME = "FIPS_AUDIT_FILE";

	private static final String _LOGGER_NAME = "liferay.fips.audit";

}