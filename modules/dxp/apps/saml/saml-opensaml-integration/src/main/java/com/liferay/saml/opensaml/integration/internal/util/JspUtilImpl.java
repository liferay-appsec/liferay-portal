/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.saml.opensaml.integration.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.struts.Definition;
import com.liferay.portal.struts.TilesUtil;
import com.liferay.saml.util.JspUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mika Koivisto
 */
@Component(immediate = true, service = {})
public class JspUtilImpl extends JspUtil {

	@Override
	public void doDispatch(
			final HttpServletRequest httpServletRequest,
			final HttpServletResponse httpServletResponse, final String path,
			final String title)
		throws Exception {

		dispatch(httpServletRequest, httpServletResponse, path, title, false);
	}

	@Override
	public void doDispatch(
			final HttpServletRequest httpServletRequest,
			final HttpServletResponse httpServletResponse, final String path,
			final String title, final boolean popUp)
		throws Exception {

		httpServletRequest.setAttribute(
			TilesUtil.DEFINITION,
			new Definition(
				StringPool.BLANK,
				HashMapBuilder.put(
					"content", path
				).put(
					"pop_up", String.valueOf(popUp)
				).put(
					"title", title
				).build()));

		RequestDispatcher requestDispatcher =
			httpServletRequest.getRequestDispatcher(
				_PATH_HTML_COMMON_THEMES_PORTAL);

		if (_servletContext != null) {
			httpServletRequest.setAttribute(
				"contentServletContext", _servletContext);
		}

		if (popUp) {
			requestDispatcher.include(httpServletRequest, httpServletResponse);

			return;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		boolean stateMaximized = themeDisplay.isStateMaximized();

		themeDisplay.setStateMaximized(true);

		try {
			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		finally {
			themeDisplay.setStateMaximized(stateMaximized);
		}
	}

	@Activate
	protected void activate() {
		JspUtil.setJspUtil(this);
	}

	@Deactivate
	protected void deactivate() {
		JspUtil.setJspUtil(null);
	}

	private static final String _PATH_HTML_COMMON_THEMES_PORTAL =
		"/html/common/themes/portal.jsp";

	@Reference(target = "(osgi.web.symbolicname=com.liferay.saml.web)")
	private ServletContext _servletContext;

}