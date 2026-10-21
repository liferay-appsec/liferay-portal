/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.configuration.module.configuration.BaseManagedServiceFactory;
import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;
import com.liferay.portal.security.audit.router.internal.constants.AuditConstants;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Michael C. Han
 * @author Brian Wing Shun Chan
 * @author Brian Greenwald
 * @author Prathima Shreenath
 */
@Component(
	configurationPid = "com.liferay.portal.security.audit.configuration.AuditConfiguration",
	service = AuditRouter.class
)
public class DefaultAuditRouter implements AuditRouter {

	@Override
	public boolean isDeployed() {
		Set<String> keys = _serviceTrackerMap.keySet();

		return !keys.isEmpty();
	}

	@Override
	public void route(AuditMessage auditMessage) throws AuditException {
		if (!_systemAuditConfiguration.enabled()) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Audit disabled, not processing message: " + auditMessage);
			}

			return;
		}

		long companyId = auditMessage.getCompanyId();

		if (FeatureFlagManagerUtil.isEnabled(companyId, "LPD-6417")) {
			AuditConfiguration auditConfiguration =
				_companyAuditConfigurations.getOrDefault(
					companyId, _systemAuditConfiguration);

			if (auditConfiguration.pseudonymizationEnabled()) {
				_auditMessagePseudonymizer.pseudonymize(auditMessage);
			}
		}

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
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		modified(properties);

		_serviceRegistration = bundleContext.registerService(
			ManagedServiceFactory.class,
			new AuditConfigurationManagedServiceFactory(),
			MapUtil.singletonDictionary(Constants.SERVICE_PID, _FACTORY_PID));
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, AuditMessageProcessor.class,
			AuditConstants.EVENT_TYPES);
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
		_serviceTrackerMap.close();
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		_systemAuditConfiguration = ConfigurableUtil.createConfigurable(
			AuditConfiguration.class, properties);
	}

	private static final String _FACTORY_PID =
		AuditConfiguration.class.getName() + ".scoped";

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultAuditRouter.class);

	private final AuditMessagePseudonymizer _auditMessagePseudonymizer =
		new AuditMessagePseudonymizer();
	private final Map<Long, AuditConfiguration> _companyAuditConfigurations =
		new ConcurrentHashMap<>();
	private ServiceRegistration<ManagedServiceFactory> _serviceRegistration;
	private ServiceTrackerMap<String, List<AuditMessageProcessor>>
		_serviceTrackerMap;
	private volatile AuditConfiguration _systemAuditConfiguration;

	private class AuditConfigurationManagedServiceFactory
		extends BaseManagedServiceFactory {

		@Override
		public String getName() {
			return _FACTORY_PID;
		}

		@Override
		protected void doDeleted(long companyId, String pid) {
			_companyAuditConfigurations.remove(companyId);
		}

		@Override
		protected void doUpdated(
			long companyId, Dictionary<String, ?> dictionary, String pid) {

			if (companyId == CompanyConstants.SYSTEM) {
				return;
			}

			_companyAuditConfigurations.put(
				companyId,
				ConfigurableUtil.createConfigurable(
					AuditConfiguration.class, dictionary));
		}

	}

}