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

package com.liferay.saml.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Mika Koivisto
 */
public class JspUtil {

	public static final String PATH_PORTAL_SAML_ERROR =
		"/portal/saml/error.jsp";

	public static final String PATH_PORTAL_SAML_SELECT_IDP =
		"/portal/saml/select_idp.jsp";

	public static final String PATH_PORTAL_SAML_SLO = "/portal/saml/slo.jsp";

	public static final String PATH_PORTAL_SAML_SLO_SP_STATUS =
		"/portal/saml/slo_sp_status.jsp";

	private static JspUtil _jspUtil;

	protected void doDispatch(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String path, String title,
		boolean popUp)
		throws Exception {}

	public void doDispatch(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String path, String title)
		throws Exception {}

	public static void dispatch(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String path, String title,
			boolean popUp)
		throws Exception {

		getJspUtil().doDispatch(
			httpServletRequest, httpServletResponse, path, title, popUp);
	}

	public static void dispatch(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String path, String title)
		throws Exception {

		getJspUtil().doDispatch(
			httpServletRequest, httpServletResponse, path, title);
	}

	private static JspUtil getJspUtil() {
		return _jspUtil;
	}

	public static void setJspUtil(JspUtil jspUtil) {
		_jspUtil = jspUtil;
	}

}