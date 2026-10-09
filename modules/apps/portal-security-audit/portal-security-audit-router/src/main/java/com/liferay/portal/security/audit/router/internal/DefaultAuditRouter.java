/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfigurationUtil;
import com.liferay.portal.security.audit.router.internal.constants.AuditConstants;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Michael C. Han
 * @author Brian Wing Shun Chan
 * @author Brian Greenwald
 * @author Prathima Shreenath
 */
@Component(service = AuditRouter.class)
public class DefaultAuditRouter implements AuditRouter {

	@Override
	public boolean isDeployed() {
		Set<String> keys = _serviceTrackerMap.keySet();

		return !keys.isEmpty();
	}

	@Override
	public void route(AuditMessage auditMessage) throws AuditException {
		if (!AuditConfigurationUtil.isEnabled(auditMessage.getCompanyId())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Audit is disabled for company ",
						auditMessage.getCompanyId(),
						", not processing message: ", auditMessage));
			}

			return;
		}

		_resolveResource(auditMessage);

		List<AuditMessageProcessor> globalAuditMessageProcessors =
			_serviceTrackerMap.getService(StringPool.STAR);

		if (globalAuditMessageProcessors != null) {
			for (AuditMessageProcessor globalAuditMessageProcessor :
					globalAuditMessageProcessors) {

				globalAuditMessageProcessor.process(auditMessage);
			}
		}

		List<AuditMessageProcessor> auditMessageProcessors =
			_serviceTrackerMap.getService(auditMessage.getEventType());

		if (auditMessageProcessors != null) {
			for (AuditMessageProcessor auditMessageProcessor :
					auditMessageProcessors) {

				auditMessageProcessor.process(auditMessage);
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, AuditMessageProcessor.class,
			AuditConstants.EVENT_TYPES);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private void _resolveResource(AuditMessage auditMessage) {
		long companyId = auditMessage.getCompanyId();

		if ((companyId == CompanyConstants.SYSTEM) ||
			!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-6417")) {

			auditMessage.setResourceAction(null);
			auditMessage.setResourceType(null);

			return;
		}

		String resourceAction = auditMessage.getResourceAction();

		if (Validator.isNotNull(resourceAction)) {
			_validateResourceAction(resourceAction);

			if (Validator.isNull(auditMessage.getResourceType())) {
				Matcher matcher = _resourceActionPattern.matcher(
					resourceAction);

				if (matcher.matches()) {
					auditMessage.setResourceType(matcher.group(1));
				}
			}

			return;
		}

		String resourceType = auditMessage.getResourceType();

		if (Validator.isNull(resourceType)) {
			String className = auditMessage.getClassName();

			if (Validator.isNull(className)) {
				resourceType = "unknown";
			}
			else {
				resourceType = StringUtil.toLowerCase(
					className.substring(
						className.lastIndexOf(CharPool.PERIOD) + 1));
			}

			auditMessage.setResourceType(resourceType);
		}

		String contextName = auditMessage.getContextName();
		String eventType = auditMessage.getEventType();

		resourceAction = StringBundler.concat(
			Validator.isNull(contextName) ? "system" :
				StringUtil.toLowerCase(contextName),
			StringPool.PERIOD, resourceType, StringPool.PERIOD,
			Validator.isNull(eventType) ? "unknown" :
				StringUtil.toLowerCase(eventType));

		_validateResourceAction(resourceAction);

		auditMessage.setResourceAction(resourceAction);
	}

	private void _validateResourceAction(String resourceAction) {
		Matcher matcher = _resourceActionPattern.matcher(resourceAction);

		if (!matcher.matches() && _log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Resource action ", resourceAction,
					" does not match <featureContext>.<resource>.<action>"));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultAuditRouter.class);

	private static final Pattern _resourceActionPattern = Pattern.compile(
		"[a-z0-9_]+\\.([a-z0-9_]+)\\.[a-z0-9_]+");

	private ServiceTrackerMap<String, List<AuditMessageProcessor>>
		_serviceTrackerMap;

}