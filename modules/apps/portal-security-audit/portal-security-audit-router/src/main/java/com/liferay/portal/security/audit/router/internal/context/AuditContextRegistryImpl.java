/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.context;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.constants.AuditEventConstants;
import com.liferay.portal.security.audit.context.AuditContext;
import com.liferay.portal.security.audit.context.AuditContextRegistry;
import com.liferay.portal.security.audit.context.AuditDeliveryPath;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Álvaro Saugar
 */
@Component(service = AuditContextRegistry.class)
public class AuditContextRegistryImpl implements AuditContextRegistry {

	@Override
	public AuditContext getAuditContext(String name) {
		if (name == null) {
			return null;
		}

		if (name.equals(AuditEventConstants.CONTEXT_NAME_INSTANCE)) {
			return _instanceAuditContext;
		}

		return _serviceTrackerMap.getService(name);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, AuditContext.class, "audit.context.name",
			new AuditContextServiceTrackerCustomizer(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private String _validate(String name, AuditContext auditContext) {
		if (name.equals(AuditEventConstants.CONTEXT_NAME_INSTANCE)) {
			return "its name is reserved";
		}

		Matcher matcher = _namePattern.matcher(name);

		if (!matcher.matches()) {
			return "its name does not match " + _namePattern.pattern();
		}

		if (auditContext.getMinimumRetentionDays() < 1) {
			return "its minimum retention is less than 1 day";
		}

		if (auditContext.getMaximumRetentionDays() <
				auditContext.getMinimumRetentionDays()) {

			return "its maximum retention is less than its minimum retention";
		}

		if (Validator.isNull(auditContext.getOAuth2ScopeName())) {
			return "it has no OAuth 2 scope name";
		}

		Set<AuditDeliveryPath> defaultAuditDeliveryPaths =
			auditContext.getDefaultAuditDeliveryPaths();

		if (SetUtil.isEmpty(defaultAuditDeliveryPaths)) {
			return "it has no default delivery path";
		}

		Set<AuditDeliveryPath> requiredAuditDeliveryPaths =
			auditContext.getRequiredAuditDeliveryPaths();

		if ((requiredAuditDeliveryPaths != null) &&
			!defaultAuditDeliveryPaths.containsAll(
				requiredAuditDeliveryPaths)) {

			return "a required delivery path is not a default delivery path";
		}

		Set<String> resourceActions = auditContext.getResourceActions();

		if (resourceActions == null) {
			return null;
		}

		String prefix = StringUtil.toLowerCase(name) + StringPool.PERIOD;

		for (String resourceAction : resourceActions) {
			if (!resourceAction.startsWith(prefix)) {
				return "resource action " + resourceAction +
					" does not belong to it";
			}
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuditContextRegistryImpl.class);

	private static final Pattern _namePattern = Pattern.compile("[A-Z0-9_]+");

	private final AuditContext _instanceAuditContext =
		new InstanceAuditContext();
	private ServiceTrackerMap<String, AuditContext> _serviceTrackerMap;

	private class AuditContextServiceTrackerCustomizer
		implements ServiceTrackerCustomizer<AuditContext, AuditContext> {

		public AuditContextServiceTrackerCustomizer(
			BundleContext bundleContext) {

			_bundleContext = bundleContext;
		}

		@Override
		public AuditContext addingService(
			ServiceReference<AuditContext> serviceReference) {

			AuditContext auditContext = _bundleContext.getService(
				serviceReference);

			if (auditContext == null) {
				return null;
			}

			String name = GetterUtil.getString(
				serviceReference.getProperty("audit.context.name"));

			String reason = _validate(name, auditContext);

			if (reason == null) {
				return auditContext;
			}

			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Ignoring audit context ", name, " because ", reason));
			}

			_bundleContext.ungetService(serviceReference);

			return null;
		}

		@Override
		public void modifiedService(
			ServiceReference<AuditContext> serviceReference,
			AuditContext auditContext) {
		}

		@Override
		public void removedService(
			ServiceReference<AuditContext> serviceReference,
			AuditContext auditContext) {

			_bundleContext.ungetService(serviceReference);
		}

		private final BundleContext _bundleContext;

	}

}