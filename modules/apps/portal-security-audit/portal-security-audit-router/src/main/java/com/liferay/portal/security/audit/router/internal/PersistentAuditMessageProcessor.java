/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.change.tracking.CTTransactionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.BatchProcessor;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.security.audit.AuditEventManager;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorConfiguration;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mika Koivisto
 * @author Brian Wing Shun Chan
 */
@Component(
	configurationPid = "com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorConfiguration",
	property = "eventTypes=*", service = AuditMessageProcessor.class
)
public class PersistentAuditMessageProcessor implements AuditMessageProcessor {

	@Override
	public void process(AuditMessage auditMessage) {
		try {
			doProcess(auditMessage);
		}
		catch (CTTransactionException ctTransactionException) {
			throw ctTransactionException;
		}
		catch (Exception exception) {
			_log.fatal(
				"Unable to process audit message " + auditMessage, exception);
		}
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		modified(properties);

		_serviceRegistration = bundleContext.registerService(
			ManagedServiceFactory.class,
			new PersistentAuditMessageProcessorConfigurationManagedServiceFactory(),
			MapUtil.singletonDictionary(Constants.SERVICE_PID, _FACTORY_PID));
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();

		for (BatchProcessor<AuditMessage> batchProcessor :
				_companyBatchProcessors.values()) {

			_close(batchProcessor);
		}

		_close(_batchProcessor);
	}

	protected void doProcess(AuditMessage auditMessage) throws Exception {
		long companyId = auditMessage.getCompanyId();

		PersistentAuditMessageProcessorConfiguration
			companyPersistentAuditMessageProcessorConfiguration =
				_companyPersistentAuditMessageProcessorConfigurations.get(
					companyId);

		if (companyPersistentAuditMessageProcessorConfiguration != null) {
			if (companyPersistentAuditMessageProcessorConfiguration.enabled()) {
				BatchProcessor<AuditMessage> batchProcessor =
					_companyBatchProcessors.getOrDefault(
						companyId, _batchProcessor);

				batchProcessor.add(auditMessage);
			}

			return;
		}

		PersistentAuditMessageProcessorConfiguration
			persistentAuditMessageProcessorConfiguration =
				_persistentAuditMessageProcessorConfiguration;

		if (!persistentAuditMessageProcessorConfiguration.enabled()) {
			return;
		}

		_batchProcessor.add(auditMessage);
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		PersistentAuditMessageProcessorConfiguration
			persistentAuditMessageProcessorConfiguration =
				ConfigurableUtil.createConfigurable(
					PersistentAuditMessageProcessorConfiguration.class,
					properties);

		if (_batchProcessor != null) {
			_batchProcessor.configure(
				persistentAuditMessageProcessorConfiguration.flushInterval(),
				persistentAuditMessageProcessorConfiguration.bufferSize());
		}
		else if (persistentAuditMessageProcessorConfiguration.enabled()) {
			_batchProcessor = new BatchProcessor<>(
				persistentAuditMessageProcessorConfiguration.flushInterval(),
				persistentAuditMessageProcessorConfiguration.bufferSize(),
				_auditEventManager::addAuditEvents,
				PersistentAuditMessageProcessor.class.getName());
		}

		_persistentAuditMessageProcessorConfiguration =
			persistentAuditMessageProcessorConfiguration;

		for (Map.Entry<Long, PersistentAuditMessageProcessorConfiguration>
				entry :
					_companyPersistentAuditMessageProcessorConfigurations.
						entrySet()) {

			_configureCompanyBatchProcessor(entry.getKey(), entry.getValue());
		}
	}

	private void _close(BatchProcessor<AuditMessage> batchProcessor) {
		if (batchProcessor != null) {
			batchProcessor.close();
		}
	}

	private void _configureCompanyBatchProcessor(
		long companyId,
		PersistentAuditMessageProcessorConfiguration
			companyPersistentAuditMessageProcessorConfiguration) {

		int bufferSize =
			companyPersistentAuditMessageProcessorConfiguration.bufferSize();
		long flushInterval =
			companyPersistentAuditMessageProcessorConfiguration.flushInterval();

		if (!companyPersistentAuditMessageProcessorConfiguration.enabled() ||
			((_batchProcessor != null) &&
			 (bufferSize ==
				 _persistentAuditMessageProcessorConfiguration.bufferSize()) &&
			 (flushInterval ==
				 _persistentAuditMessageProcessorConfiguration.
					 flushInterval()))) {

			_close(_companyBatchProcessors.remove(companyId));

			return;
		}

		BatchProcessor<AuditMessage> batchProcessor =
			_companyBatchProcessors.get(companyId);

		if (batchProcessor == null) {
			_companyBatchProcessors.put(
				companyId,
				new BatchProcessor<>(
					flushInterval, bufferSize,
					_auditEventManager::addAuditEvents,
					PersistentAuditMessageProcessor.class.getName() +
						StringPool.DASH + companyId));
		}
		else {
			batchProcessor.configure(flushInterval, bufferSize);
		}
	}

	private void _unmapPid(String pid) {
		Long companyId = _companyIds.remove(pid);

		if (companyId == null) {
			return;
		}

		_companyPersistentAuditMessageProcessorConfigurations.remove(companyId);

		_close(_companyBatchProcessors.remove(companyId));
	}

	private static final String _FACTORY_PID =
		PersistentAuditMessageProcessorConfiguration.class.getName() +
			".scoped";

	private static final Log _log = LogFactoryUtil.getLog(
		PersistentAuditMessageProcessor.class);

	@Reference
	private AuditEventManager _auditEventManager;

	private volatile BatchProcessor<AuditMessage> _batchProcessor;
	private final Map<Long, BatchProcessor<AuditMessage>>
		_companyBatchProcessors = new ConcurrentHashMap<>();
	private final Map<String, Long> _companyIds = new ConcurrentHashMap<>();
	private final Map<Long, PersistentAuditMessageProcessorConfiguration>
		_companyPersistentAuditMessageProcessorConfigurations =
			new ConcurrentHashMap<>();
	private volatile PersistentAuditMessageProcessorConfiguration
		_persistentAuditMessageProcessorConfiguration;
	private ServiceRegistration<ManagedServiceFactory> _serviceRegistration;

	private class
		PersistentAuditMessageProcessorConfigurationManagedServiceFactory
			implements ManagedServiceFactory {

		@Override
		public void deleted(String pid) {
			_unmapPid(pid);
		}

		@Override
		public String getName() {
			return _FACTORY_PID;
		}

		@Override
		public void updated(String pid, Dictionary<String, ?> dictionary)
			throws ConfigurationException {

			long companyId = GetterUtil.getLong(
				dictionary.get("companyId"), CompanyConstants.SYSTEM);

			Long previousCompanyId = _companyIds.get(pid);

			if ((previousCompanyId != null) &&
				(previousCompanyId != companyId)) {

				_unmapPid(pid);
			}

			if (companyId == CompanyConstants.SYSTEM) {
				return;
			}

			PersistentAuditMessageProcessorConfiguration
				companyPersistentAuditMessageProcessorConfiguration =
					ConfigurableUtil.createConfigurable(
						PersistentAuditMessageProcessorConfiguration.class,
						dictionary);

			_companyIds.put(pid, companyId);
			_companyPersistentAuditMessageProcessorConfigurations.put(
				companyId, companyPersistentAuditMessageProcessorConfiguration);

			_configureCompanyBatchProcessor(
				companyId, companyPersistentAuditMessageProcessorConfiguration);
		}

	}

}