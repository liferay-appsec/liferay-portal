/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.banner.web.internal.servlet.taglib;

import com.liferay.cookies.configuration.CookiesConfigurationProvider;
import com.liferay.cookies.configuration.CookiesPreferenceHandlingConfiguration;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.servlet.taglib.BaseJSPDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Moura
 */
@Component(service = DynamicInclude.class)
public class FloatingIconBottomJSPDynamicInclude extends BaseJSPDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		if (LiferayWindowState.isPopUp(httpServletRequest)) {
			return;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		try {
			CookiesPreferenceHandlingConfiguration
				cookiesPreferenceHandlingConfiguration =
					_cookiesConfigurationProvider.
						getCookiesPreferenceHandlingConfiguration(themeDisplay);

			if (!cookiesPreferenceHandlingConfiguration.enabled() ||
				!FeatureFlagManagerUtil.isEnabled(
					themeDisplay.getCompanyId(), "LPD-75027") ||
				!cookiesPreferenceHandlingConfiguration.floatingIconEnabled()) {

				return;
			}

			httpServletRequest.setAttribute(
				"floatingIcon",
				cookiesPreferenceHandlingConfiguration.floatingIcon());
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		super.include(httpServletRequest, httpServletResponse, key);
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/bottom.jsp#post");
	}

	@Override
	protected String getJspPath() {
		return "/dynamic_include/floating_icon.jsp";
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected ServletContext getServletContext() {
		return _servletContext;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FloatingIconBottomJSPDynamicInclude.class);

	@Reference
	private CookiesConfigurationProvider _cookiesConfigurationProvider;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.cookies.banner.web)"
	)
	private ServletContext _servletContext;

}