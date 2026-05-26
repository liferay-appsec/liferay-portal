/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.consent;

import com.liferay.consent.management.platform.integration.configuration.ConsentManagementPlatformConfiguration;
import com.liferay.cookies.consent.CookiesConsentResolver;
import com.liferay.cookies.consent.CookiesConsentResolverProvider;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Moura
 */
@Component(service = CookiesConsentResolverProvider.class)
public class CookiesConsentResolverProviderImpl
	implements CookiesConsentResolverProvider {

	@Override
	public CookiesConsentResolver getCookiesConsentResolver(
		HttpServletRequest httpServletRequest) {

		String key = "default";

		if (httpServletRequest != null) {
			long companyId = _portal.getCompanyId(httpServletRequest);

			if (FeatureFlagManagerUtil.isEnabled(companyId, "LPD-65299")) {
				try {
					ConsentManagementPlatformConfiguration
						consentManagementPlatformConfiguration = null;
					long groupId = _portal.getScopeGroupId(httpServletRequest);

					if (groupId > 0) {
						consentManagementPlatformConfiguration =
							_configurationProvider.getGroupConfiguration(
								ConsentManagementPlatformConfiguration.class,
								companyId, groupId);
					}
					else {
						consentManagementPlatformConfiguration =
							_configurationProvider.getCompanyConfiguration(
								ConsentManagementPlatformConfiguration.class,
								companyId);
					}

					if ((consentManagementPlatformConfiguration != null) &&
						consentManagementPlatformConfiguration.enabled()) {

						key = "third-party-cmp";
					}
				}
				catch (Exception exception) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Unable to read consent management platform " +
								"configuration",
							exception);
					}
				}
			}
		}

		CookiesConsentResolver cookiesConsentResolver =
			_serviceTrackerMap.getService(key);

		if (cookiesConsentResolver == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"No CookiesConsentResolver is registered for key \"" + key +
						"\". Falling back to \"default\".");
			}

			cookiesConsentResolver = _serviceTrackerMap.getService("default");
		}

		return cookiesConsentResolver;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, CookiesConsentResolver.class,
			"cookies.consent.resolver.impl");
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CookiesConsentResolverProviderImpl.class);

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private Portal _portal;

	private ServiceTrackerMap<String, CookiesConsentResolver>
		_serviceTrackerMap;

}