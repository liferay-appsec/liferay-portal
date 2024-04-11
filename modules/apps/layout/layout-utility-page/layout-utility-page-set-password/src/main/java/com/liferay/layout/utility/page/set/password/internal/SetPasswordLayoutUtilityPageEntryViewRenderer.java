/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.utility.page.set.password.internal;

import com.liferay.layout.utility.page.kernel.LayoutUtilityPageEntryViewRenderer;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;

import java.io.IOException;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alvaro Saugar
 */
@Component(
	property = "utility.page.type=" + LayoutUtilityPageEntryConstants.TYPE_SET_PASSWORD,
	service = LayoutUtilityPageEntryViewRenderer.class
)
public class SetPasswordLayoutUtilityPageEntryViewRenderer
	implements LayoutUtilityPageEntryViewRenderer {

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "set-password");
	}

	@Override
	public String getType() {
		return LayoutUtilityPageEntryConstants.TYPE_SET_PASSWORD;
	}

	@Override
	public boolean isEnabled() {
		return FeatureFlagManagerUtil.isEnabled("LPD-6378");
	}

	@Override
	public void renderHTML(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse)
		throws IOException, ServletException {

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher("/set_password.jsp");

		requestDispatcher.include(httpServletRequest, httpServletResponse);
	}

	@Reference
	private Language _language;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.layout.utility.page.set.password)"
	)
	private ServletContext _servletContext;

}