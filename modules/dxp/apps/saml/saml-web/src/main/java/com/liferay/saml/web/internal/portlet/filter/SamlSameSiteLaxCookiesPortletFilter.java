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

package com.liferay.saml.web.internal.portlet.filter;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoader;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.constants.SamlPortletKeys;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Stian Sigvartsen
 */
@Component(
	immediate = true, property = "javax.portlet.name=" + SamlPortletKeys.SAML,
	service = PortletFilter.class
)
public class SamlSameSiteLaxCookiesPortletFilter implements ActionFilter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(
			ActionRequest actionRequest, ActionResponse actionResponse,
			FilterChain filterChain)
		throws IOException, PortletException {

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			actionRequest);

		HttpServletResponse httpServletResponse =
			_portal.getHttpServletResponse(actionResponse);

		if (!isFilterEnabled(httpServletRequest, httpServletResponse)) {
			filterChain.doFilter(actionRequest, actionResponse);
		}

		httpServletResponse.setContentType("text/html");

		PrintWriter printWriter = httpServletResponse.getWriter();

		if (ParamUtil.getBoolean(httpServletRequest, "noscript")) {
			printWriter.write(
				StringBundler.concat(
					"<!DOCTYPE html>\n\n<html><body>",
					ResourceBundleUtil.getString(
						_resourceBundleLoader.loadResourceBundle(
							_portal.getLocale(httpServletRequest)),
						"your-browser-must-support-javascript-to-proceed"),
					"</body></html>"));

			printWriter.close();

			return;
		}

		StringBundler sb = new StringBundler(7 + (5 * _PARAMS.length));

		sb.append("<!DOCTYPE html>\n\n");
		sb.append("<html><body onload=\"document.forms[0].submit()\">");
		sb.append("<form action=\"?continue=true\" method=\"post\"");
		sb.append("name=\"fm\">");

		for (String param : _PARAMS) {
			_processParameter(httpServletRequest, sb, param);
		}

		sb.append("<noscript><meta http-equiv=\"refresh\" ");
		sb.append("content=\"0;URL='?noscript=true'\"/>");
		sb.append("</noscript></form></body></html>");

		printWriter.write(sb.toString());

		printWriter.close();
	}

	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
	}

	protected Log getLog() {
		return _log;
	}

	protected boolean isFilterEnabled(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		if (Objects.equals("GET", httpServletRequest.getMethod()) ||
			ParamUtil.getBoolean(httpServletRequest, "continue") ||
			(!ParamUtil.getBoolean(httpServletRequest, "noscript") &&
			 (httpServletRequest.getSession(false) != null))) {

			return false;
		}

		String actionName = httpServletRequest.getParameter(
			"_" + SamlPortletKeys.SAML + "_javax.portlet.action");

		if (actionName.endsWith("acs") || actionName.endsWith("slo") ||
			actionName.endsWith("sso")) {

			return true;
		}

		return false;
	}

	private void _processParameter(
		HttpServletRequest httpServletRequest, StringBundler sb,
		String paramName) {

		String paramValue = ParamUtil.getString(httpServletRequest, paramName);

		if (Validator.isNotNull(paramValue)) {
			sb.append("<input type=\"hidden\" name=");
			sb.append(paramName);
			sb.append(" value=\"");
			sb.append(_html.escapeAttribute(paramValue));
			sb.append("\"/>");
		}
	}

	private static final String[] _PARAMS = {
		"RelayState", "SAMLRequest", "SAMLResponse", "entityId"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		SamlSameSiteLaxCookiesPortletFilter.class);

	@Reference
	private Html _html;

	@Reference
	private Portal _portal;

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(bundle.symbolic.name=com.liferay.saml.web)"
	)
	private volatile ResourceBundleLoader _resourceBundleLoader;

}