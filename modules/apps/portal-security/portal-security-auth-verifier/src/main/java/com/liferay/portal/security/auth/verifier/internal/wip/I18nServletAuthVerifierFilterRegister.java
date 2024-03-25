/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.auth.verifier.internal.wip;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.servlet.filters.authverifier.AuthVerifierFilter;
import com.liferay.portal.util.PropsValues;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Pedro Silvestre
 */
@Component(service = {})
public class I18nServletAuthVerifierFilterRegister {

	@Activate
	protected void activate(BundleContext bundleContext) {
		String[] locales = PropsValues.LOCALES;

		List<String> urlPatterns = new ArrayList<>();

		for (String locale : locales) {
			urlPatterns.add(
				"/" + locale.replace(StringPool.UNDERLINE, StringPool.DASH) +
					"/o/*");
			urlPatterns.add("/" + locale + "/o/*");

			if (!urlPatterns.contains("/" + locale.substring(0, 2) + "/o/*")) {
				urlPatterns.add("/" + locale.substring(0, 2) + "/o/*");
			}
		}

		_serviceRegistration = bundleContext.registerService(
			Filter.class, new AuthVerifierFilter(),
			HashMapDictionaryBuilder.<String, Object>put(
				"dispatcher", new String[] {"REQUEST", "FORWARD"}
			).put(
				"servlet-context-name", ""
			).put(
				"servlet-filter-name", "I18n Servlet Auth Verifier Filter"
			).put(
				"url-pattern", ArrayUtil.toStringArray(urlPatterns)
			).put(
				"after-filter", "CTCollection Preview Filter"
			).put(
				"init.param.auth.verifier.PortalSessionAuthVerifier.urls.includes", "*"
			).put(
				"init.param.auth.verifier.BasicAuthHeaderAuthVerifier.urls.includes", "*"
			).build());
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	private ServiceRegistration<Filter> _serviceRegistration;

}