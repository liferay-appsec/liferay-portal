/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.configuration.admin.service;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorCompanyConfiguration;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Lucas Miranda
 */
@Component(
	configurationPid = "com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorCompanyConfiguration",
	property = {
		Constants.SERVICE_PID + "=com.liferay.portal.security.audit.router.configuration.PersistentAuditMessageProcessorCompanyConfiguration.scoped",
		"feature.flag.key=LPD-114718"
	},
	service = {FeatureFlagListener.class, ManagedServiceFactory.class}
)
public class PersistentAuditMessageProcessorCompanyManagedServiceFactory
	implements FeatureFlagListener, ManagedServiceFactory {

	@Override
	public void deleted(String pid) {
		Long companyId = _companyIds.remove(pid);

		if (companyId != null) {
			_enabledMap.remove(companyId);
		}
	}

	@Override
	public String getName() {
		return "com.liferay.portal.security.audit.router.configuration." +
			"PersistentAuditMessageProcessorCompanyConfiguration.scoped";
	}

	public boolean isEnabled(long companyId) {
		Boolean featureFlagEnabled = _featureFlagEnabledMap.get(companyId);

		if (featureFlagEnabled == null) {
			featureFlagEnabled = _featureFlagEnabled;
		}

		if (!featureFlagEnabled) {
			return true;
		}

		Boolean enabled = _enabledMap.get(companyId);

		if (enabled == null) {
			return _enabled;
		}

		return enabled;
	}

	@Override
	public void onValue(
		long companyId, String featureFlagKey, boolean enabled) {

		if (companyId == CompanyConstants.SYSTEM) {
			_featureFlagEnabled = enabled;
		}
		else {
			_featureFlagEnabledMap.put(companyId, enabled);
		}
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> dictionary) {
		deleted(pid);

		long companyId = GetterUtil.getLong(
			dictionary.get("companyId"), CompanyConstants.SYSTEM);

		if (companyId == CompanyConstants.SYSTEM) {
			return;
		}

		PersistentAuditMessageProcessorCompanyConfiguration
			persistentAuditMessageProcessorCompanyConfiguration =
				ConfigurableUtil.createConfigurable(
					PersistentAuditMessageProcessorCompanyConfiguration.class,
					dictionary);

		_companyIds.put(pid, companyId);
		_enabledMap.put(
			companyId,
			persistentAuditMessageProcessorCompanyConfiguration.enabled());
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		PersistentAuditMessageProcessorCompanyConfiguration
			persistentAuditMessageProcessorCompanyConfiguration =
				ConfigurableUtil.createConfigurable(
					PersistentAuditMessageProcessorCompanyConfiguration.class,
					properties);

		_enabled =
			persistentAuditMessageProcessorCompanyConfiguration.enabled();
	}

	private final Map<String, Long> _companyIds = new ConcurrentHashMap<>();
	private volatile boolean _enabled;
	private final Map<Long, Boolean> _enabledMap = new ConcurrentHashMap<>();
	private volatile boolean _featureFlagEnabled;
	private final Map<Long, Boolean> _featureFlagEnabledMap =
		new ConcurrentHashMap<>();

}