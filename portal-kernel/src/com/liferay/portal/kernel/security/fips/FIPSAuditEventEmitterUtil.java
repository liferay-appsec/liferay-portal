/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.internal.log4j.FIPSAuditNDJSONLayout;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.ServerDetector;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import java.security.Provider;
import java.security.Security;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.ObjectMessage;

/**
 * Process wide entry point for emitting FIPS audit events, and the authority on
 * what a record contains: the common envelope, carrying the CMVP certificate ID
 * and deployment instance ID, the event schema version, the event type, the
 * validated provider name and version active at emission, the severity, and the
 * §5.1 timestamp.
 *
 * <p>
 * {@link #emit} is the only way in. The record is assembled and written by the
 * same class so that no caller can reach the sink with a map of its own, which
 * would put a record in the trail with no envelope at all and misattribute the
 * module and deployment that produced it. Event specific fields are nested under
 * a single {@code fields} object for the same reason, so an event can never
 * overwrite an envelope key.
 * </p>
 *
 * <p>
 * Every record carries the §5.1 timestamp in one canonical representation: UTC,
 * ISO 8601 extended form with millisecond precision and a literal
 * <code>Z</code> suffix (for example <code>2026-05-06T14:19:23.471Z</code>),
 * read from the host clock and emitted in UTC regardless of the host default
 * time zone. Sub millisecond precision is truncated rather than rounded. The
 * timestamp alone does not order two events emitted within the same millisecond,
 * so the §5.4 audit log integrity chain, not the timestamp, is the authority on
 * order.
 * </p>
 *
 * <p>
 * The FIPS application state machine drives events during boot, before the OSGi
 * runtime exists, so the envelope sources are read without any framework
 * dependency: the CMVP certificate ID and deployment instance ID come from the
 * <code>fips.audit.provider.cmvp.certificate.id</code> and
 * <code>fips.audit.deployment.instance.id</code> properties, and the provider
 * name and version from the validated JCE provider.
 * </p>
 *
 * <p>
 * A finished record travels to the <code>FIPS_AUDIT_FILE</code> appender as an
 * {@link ObjectMessage}, so the appender owns the rolling file, its permissions
 * and its retention, the layout owns the NDJSON line, and no part of the format
 * lives here. The logger is taken straight from {@link LogManager} rather than
 * from <code>LogFactoryUtil</code> because the record has to reach the layout
 * as an object rather than as text, and through that facade it would not:
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
 * {@link FileChannel#force} below. The path comes from the appender's manager
 * rather than from the appender, because a
 * <code>DirectWriteRolloverStrategy</code> leaves the appender with no file name
 * of its own and only the manager resolves the file a record was written to.
 * </p>
 *
 * <p>
 * Emission does not lock, because nothing it touches is both shared and mutable:
 * the timestamp formatter is immutable, {@link Security#getProviders} hands back
 * a copy, and the deployment instance ID either comes from a property or from a
 * file created on the first emission, while the portal is still single threaded.
 * The appender serializes the write itself.
 * </p>
 *
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
public class FIPSAuditEventEmitterUtil {

	public static void emit(FIPSAuditEvent fipsAuditEvent) {
		FIPSAuditSeverity fipsAuditSeverity =
			fipsAuditEvent.getFIPSAuditSeverity();

		_write(
			fipsAuditSeverity,
			LinkedHashMapBuilder.<String, Object>put(
				"cmvp-certificate-id",
				PropsValues.FIPS_AUDIT_PROVIDER_CMVP_CERTIFICATE_ID
			).put(
				"deployment-instance-id", _getDeploymentInstanceId()
			).put(
				"event-schema-version", "1.0"
			).put(
				"event-type", fipsAuditEvent.getEventType()
			).put(
				"fields", fipsAuditEvent.getFields()
			).put(
				"provider-name",
				() -> {
					Provider provider = _fetchProvider();

					if (provider == null) {
						return "";
					}

					return provider.getName();
				}
			).put(
				"provider-version",
				() -> {
					Provider provider = _fetchProvider();

					if (provider == null) {
						return "";
					}

					return provider.getVersionStr();
				}
			).put(
				"severity", fipsAuditSeverity.getValue()
			).put(
				"timestamp",
				() -> {
					Instant instant = Instant.now();

					return _dateTimeFormatter.format(
						instant.atZone(ZoneOffset.UTC));
				}
			).build());
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
					"\". Check that the portal property ",
					"\"log4j.configure.on.startup\" is enabled and that no ",
					"configuration lowers the level of that logger"));
		}

		RollingFileAppender rollingFileAppender = _fetchRollingFileAppender();

		if (rollingFileAppender == null) {
			throw new IllegalStateException(
				StringBundler.concat(
					"Unable to write a FIPS audit record because the appender ",
					"\"", _APPENDER_NAME, "\" is not configured"));
		}

		if (!(rollingFileAppender.getLayout() instanceof
				FIPSAuditNDJSONLayout)) {

			throw new IllegalStateException(
				StringBundler.concat(
					"Unable to write a FIPS audit record because the appender ",
					"\"", _APPENDER_NAME, "\" does not render it with \"",
					FIPSAuditNDJSONLayout.PLUGIN_NAME, "\""));
		}

		_warnUnprotectedAuditLog(rollingFileAppender);
	}

	private static Provider _fetchProvider() {
		Provider[] providers = Security.getProviders();

		if (ArrayUtil.isEmpty(providers)) {
			return null;
		}

		return providers[0];
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

	private static String _getDeploymentInstanceId() {
		String deploymentInstanceId =
			PropsValues.FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID;

		if (Validator.isNotNull(deploymentInstanceId)) {
			return deploymentInstanceId;
		}

		Path path = Paths.get(
			PropsValues.LIFERAY_HOME, "data",
			"fips-audit-deployment-instance-id");

		try {
			if (Files.exists(path)) {
				String persistedId = new String(
					Files.readAllBytes(path), StandardCharsets.UTF_8);

				return persistedId.trim();
			}

			String generatedId = String.valueOf(UUID.randomUUID());

			Files.createDirectories(path.getParent());

			Files.write(path, generatedId.getBytes(StandardCharsets.UTF_8));

			return generatedId;
		}
		catch (IOException ioException) {
			throw new UncheckedIOException(
				"Unable to resolve the FIPS deployment instance ID",
				ioException);
		}
	}

	private static String _getFileName(
		RollingFileAppender rollingFileAppender) {

		RollingFileManager rollingFileManager =
			rollingFileAppender.getManager();

		return rollingFileManager.getFileName();
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

		String fileName = _getFileName(rollingFileAppender);

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

	private static void _warnUnprotectedAuditLog(
		RollingFileAppender rollingFileAppender) {

		if (!_filePermissionsChecked.compareAndSet(false, true)) {
			return;
		}

		RollingFileManager rollingFileManager =
			rollingFileAppender.getManager();

		Set<PosixFilePermission> posixFilePermissions =
			rollingFileManager.getFilePermissions();

		String fileName = _getFileName(rollingFileAppender);

		if ((posixFilePermissions == null) || (fileName == null)) {
			return;
		}

		try {
			Set<PosixFilePermission> currentPosixFilePermissions =
				Files.getPosixFilePermissions(Paths.get(fileName));

			if (posixFilePermissions.equals(currentPosixFilePermissions) ||
				!_log.isWarnEnabled()) {

				return;
			}

			_log.warn(
				StringBundler.concat(
					"The FIPS audit log ", fileName, " has the permissions ",
					PosixFilePermissions.toString(currentPosixFilePermissions),
					" instead of the configured ",
					PosixFilePermissions.toString(posixFilePermissions),
					", so it is not protected against unauthorized reading"));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to verify the permissions of the FIPS audit ",
						"log ", fileName,
						", so it is not known to be protected against ",
						"unauthorized reading"),
					exception);
			}
		}
	}

	private static void _write(
		FIPSAuditSeverity fipsAuditSeverity, Map<String, Object> record) {

		Level level = _getLevel(fipsAuditSeverity);

		Logger logger = LogManager.getLogger(_LOGGER_NAME);

		_assertDeliverable(level, logger);

		logger.log(level, new ObjectMessage(record));

		if (fipsAuditSeverity == FIPSAuditSeverity.CRITICAL) {
			_sync();
		}
	}

	private static final String _APPENDER_NAME = "FIPS_AUDIT_FILE";

	private static final String _LOGGER_NAME = "liferay.fips.audit";

	private static final Log _log = LogFactoryUtil.getLog(
		FIPSAuditEventEmitterUtil.class);

	private static final DateTimeFormatter _dateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final AtomicBoolean _filePermissionsChecked =
		new AtomicBoolean();

}