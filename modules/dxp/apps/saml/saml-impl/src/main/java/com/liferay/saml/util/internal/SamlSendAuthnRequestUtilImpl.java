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

package com.liferay.saml.util.internal;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.saml.constants.SamlWebKeys;
import com.liferay.saml.runtime.servlet.profile.WebSsoProfile;
import com.liferay.saml.util.SamlSendAuthnRequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(immediate = true, service = SamlSendAuthnRequestUtil.class)
public class SamlSendAuthnRequestUtilImpl implements SamlSendAuthnRequestUtil {

	public void send(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		if (GetterUtil.getBoolean(
				ParamUtil.getBoolean(httpServletRequest, "forceAuthn"))) {

			AuthTokenUtil.checkCSRFToken(
				httpServletRequest,
				SamlSendAuthnRequestUtilImpl.class.getName());

			httpServletRequest.setAttribute(
				SamlWebKeys.FORCE_REAUTHENTICATION, Boolean.TRUE);
		}

		String relayState = ParamUtil.getString(httpServletRequest, "redirect");

		if (Validator.isNotNull(relayState)) {
			relayState = _portal.escapeRedirect(relayState);
		}

		HttpSession session = httpServletRequest.getSession();

		LastPath lastPath = (LastPath)session.getAttribute(WebKeys.LAST_PATH);

		if (GetterUtil.getBoolean(
				_props.get(PropsKeys.AUTH_FORWARD_BY_LAST_PATH)) &&
			(lastPath != null) && Validator.isNull(relayState)) {

			StringBundler sb = new StringBundler(4);

			sb.append(_portal.getPortalURL(httpServletRequest));
			sb.append(lastPath.getContextPath());
			sb.append(lastPath.getPath());
			sb.append(lastPath.getParameters());

			relayState = sb.toString();
		}
		else if (Validator.isNull(relayState)) {
			relayState = _portal.getHomeURL(httpServletRequest);
		}

		_webSsoProfile.sendAuthnRequest(
			httpServletRequest, httpServletResponse, relayState);
	}

	@Reference
	private Portal _portal;

	@Reference
	private Props _props;

	@Reference
	private WebSsoProfile _webSsoProfile;

}