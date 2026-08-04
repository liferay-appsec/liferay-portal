/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.Provider;
import java.security.Security;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.UUID;

/**
 * Process wide entry point for emitting FIPS audit events, and the authority on
 * what a record contains: the common envelope, carrying the CMVP certificate ID
 * and deployment instance ID, the event schema version, the event type, the
 * validated provider name and version active at emission, the severity, and the
 * §5.1 timestamp. The finished record goes to {@link FIPSAuditUtil}, which owns
 * rendering and storage.
 *
 * <p>
 * Event specific fields are nested under a single {@code fields} object rather
 * than merged into the envelope, so an event can never overwrite an envelope key
 * and misattribute the record.
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
		FIPSAuditUtil.write(
			fipsAuditEvent.getFIPSAuditSeverity(),
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
				"severity",
				() -> {
					FIPSAuditSeverity fipsAuditSeverity =
						fipsAuditEvent.getFIPSAuditSeverity();

					return fipsAuditSeverity.getValue();
				}
			).put(
				"timestamp",
				() -> {
					Instant instant = Instant.now();

					return _dateTimeFormatter.format(
						instant.atZone(ZoneOffset.UTC));
				}
			).build());
	}

	private static Provider _fetchProvider() {
		Provider[] providers = Security.getProviders();

		if (ArrayUtil.isEmpty(providers)) {
			return null;
		}

		return providers[0];
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

	private static final DateTimeFormatter _dateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

}