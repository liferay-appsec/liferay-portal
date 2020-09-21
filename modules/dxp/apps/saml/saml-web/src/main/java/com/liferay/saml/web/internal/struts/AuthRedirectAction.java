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

package com.liferay.saml.web.internal.struts;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.constants.SamlWebKeys;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.exception.AuthnAgeException;
import com.liferay.saml.runtime.exception.ForceAuthnException;
import com.liferay.saml.runtime.servlet.profile.SamlSpIdpConnectionsProfile;
import com.liferay.saml.util.JspUtil;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Tomas Polesovsky
 */
@Component(
	immediate = true, property = "path=/portal/saml/auth_redirect",
	service = StrutsAction.class
)
public class AuthRedirectAction extends BaseSamlStrutsAction {

	@Override
	public boolean isEnabled() {
		if (samlProviderConfigurationHelper.isRoleSp()) {
			return super.isEnabled();
		}

		return false;
	}

	@Override
	@Reference(unbind = "-")
	public void setSamlProviderConfigurationHelper(
		SamlProviderConfigurationHelper samlProviderConfigurationHelper) {

		super.setSamlProviderConfigurationHelper(
			samlProviderConfigurationHelper);
	}

	@Override
	protected String doExecute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		String redirect = ParamUtil.getString(httpServletRequest, "redirect");

		redirect = _portal.escapeRedirect(redirect);

		if (Validator.isNull(redirect)) {
			redirect = _portal.getHomeURL(httpServletRequest);
		}

		HttpServletRequest originalHttpServletRequest =
			_portal.getOriginalServletRequest(httpServletRequest);

		HttpSession session = originalHttpServletRequest.getSession();

		String error = (String)session.getAttribute(SamlWebKeys.SAML_SSO_ERROR);

		if (!ArrayUtil.contains(_ERRORS, error)) {
			try {
				httpServletResponse.sendRedirect(redirect);

				return null;
			}
			catch (IOException ioException) {
				throw new SystemException(ioException);
			}
		}

		SamlSpIdpConnection samlSpIdpConnection =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnection(
				_portal.getCompanyId(httpServletRequest),
				ParamUtil.get(
					originalHttpServletRequest, "idpEntityId",
					StringPool.BLANK));

		List<SamlSpIdpConnection> samlSpIdpConnections =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(
				_portal.getCompanyId(originalHttpServletRequest));

		Stream<SamlSpIdpConnection> stream = samlSpIdpConnections.stream();

		samlSpIdpConnections = stream.filter(
			samlSpIdpConnection2 -> isEnabled(
				samlSpIdpConnection2, httpServletRequest)
		).collect(
			Collectors.toList()
		);

		httpServletRequest.setAttribute(
			SamlWebKeys.SAML_SSO_LOGIN_CONTEXT,
			toJSONObject(
				samlSpIdpConnections, samlSpIdpConnection.getName(), error));

		JspUtil.dispatch(
			httpServletRequest, httpServletResponse,
			"/portal/saml/select_idp.jsp",
			"please-select-your-identity-provider", false);

		return null;
	}

	protected boolean isEnabled(
		SamlSpIdpConnection samlSpIdpConnection,
		HttpServletRequest httpServletRequest) {

		if (_samlSpIdpConnectionsProfile != null) {
			return _samlSpIdpConnectionsProfile.isEnabled(
				samlSpIdpConnection, httpServletRequest);
		}

		return samlSpIdpConnection.isEnabled();
	}

	private static final String[] _ERRORS = {
		AuthnAgeException.class.getSimpleName(),
		ForceAuthnException.class.getSimpleName()
	};

	@Reference
	private Portal _portal;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private volatile SamlSpIdpConnectionsProfile _samlSpIdpConnectionsProfile;

}