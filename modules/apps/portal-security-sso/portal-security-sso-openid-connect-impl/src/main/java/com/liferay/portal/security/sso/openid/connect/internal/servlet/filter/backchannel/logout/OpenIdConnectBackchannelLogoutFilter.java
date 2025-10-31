/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.servlet.filter.backchannel.logout;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnect;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceException;
import com.liferay.portal.security.sso.openid.connect.internal.configuration.OpenIdConnectProviderConfiguration;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.net.URL;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lucas Miranda
 */
@Component(
	property = {
		"servlet-context-name=",
		"servlet-filter-name=OpenId Connect Backchannel Logout Filter",
		"url-pattern=/oidc/backchannel_logout"
	},
	service = Filter.class
)
public class OpenIdConnectBackchannelLogoutFilter extends BaseFilter {

	@Override
	public boolean isFilterEnabled(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		return _openIdConnect.isEnabled(CompanyThreadLocal.getCompanyId());
	}

	protected Map<String, String> getConfigurationData() {
		try {
			Configuration[] configurations =
				_configurationAdmin.listConfigurations(
					StringBundler.concat(
						"(", ConfigurationAdmin.SERVICE_FACTORYPID, "=",
						OpenIdConnectProviderConfiguration.class.getName(),
						".scoped)"));

			if (configurations != null) {
				Map<String, String> jwksUriMap = new ConcurrentHashMap<>();

				for (Configuration configuration : configurations) {
					Dictionary<String, Object> properties =
						configuration.getProperties();

					String discoveryEndPoint = (String)properties.get(
						"discoveryEndPoint");
					String issuerURL = (String)properties.get("issuerURL");
					String jwksURI = (String)properties.get("jwksURI");

					if (_log.isDebugEnabled()) {
						_log.debug("Reading OIDC Connection metadata...");
						_log.debug("discoveryEndPoint: " + discoveryEndPoint);
					}

					if (discoveryEndPoint != null) {
						OIDCProviderMetadata metadata =
							_resolveOIDCProviderMetadata(discoveryEndPoint);

						issuerURL = metadata.getIssuer(
						).getValue();
						jwksURI = metadata.getJWKSetURI(
						).toString();
					}

					if ((jwksURI != null) && (issuerURL != null)) {
						jwksUriMap.put(issuerURL, jwksURI);
					}

					if (_log.isDebugEnabled()) {
						_log.debug("issuerURL: " + issuerURL);
						_log.debug("jwksURI: " + jwksURI);
					}
				}

				return jwksUriMap;
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		String logoutToken = ParamUtil.get(
			httpServletRequest, "logout_token", StringPool.BLANK);

		if (!StringPool.BLANK.equals(logoutToken)) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Backchannel OIDC logout with token: " + logoutToken);
			}

			try {
				JWT logoutTokenJWT = JWTParser.parse(logoutToken);

				if (_log.isDebugEnabled()) {
					_log.debug(
						"Parsed Logout Token: " +
							logoutTokenJWT.getParsedString());
				}

				JWTClaimsSet logoutTokenJWTClaimsSet =
					logoutTokenJWT.getJWTClaimsSet();

				List<String> clientIdList =
					logoutTokenJWTClaimsSet.getStringListClaim("aud");

				if ((clientIdList != null) && !clientIdList.isEmpty()) {
					String sid = logoutTokenJWTClaimsSet.getClaimAsString(
						"sid");

					OpenIdConnectSession oidcSession =
						_openIdConnectSessionLocalService.
							fetchOpenIdConnectSessionBySid(sid);

					String idTokenJWT = oidcSession.getIdToken();

					try {
						JWT idToken = JWTParser.parse(idTokenJWT);

						JWTClaimsSet jwtClaimsSet = idToken.getJWTClaimsSet();

						String jwtClaimsSetIssuer = jwtClaimsSet.getIssuer();

						Issuer expectedIssuer = new Issuer(jwtClaimsSetIssuer);

						Map<String, String> jwksUriMap = getConfigurationData();

						String jwksURIString = jwksUriMap.get(
							jwtClaimsSetIssuer);

						if (jwksURIString != null) {
							URL jwkSetURI = new URL(jwksURIString);

							if (_log.isDebugEnabled()) {
								_log.debug("OIDC Provider JWKS " + jwkSetURI);
							}

							ClientID clientID = new ClientID(
								clientIdList.get(0));

							SignedJWT signedLogoutToken = SignedJWT.parse(
								logoutToken);

							JWSHeader logoutTokenHeader =
								signedLogoutToken.getHeader();

							JWSAlgorithm jwsAlgorithm =
								logoutTokenHeader.getAlgorithm();

							LogoutTokenValidator validator =
								new LogoutTokenValidator(
									expectedIssuer, clientID, jwsAlgorithm,
									jwkSetURI);

							validator.validate(logoutTokenJWT);

							_openIdConnectSessionLocalService.
								deleteOpenIdConnectSession(oidcSession);

							httpServletResponse.setStatus(
								HttpServletResponse.SC_OK);

							if (_log.isInfoEnabled()) {
								_log.info(
									StringBundler.concat(
										"Terminating oidc session ", sid,
										" for userId ",
										oidcSession.getUserId()));
							}
						}
						else {
							_log.error("URI is null");
						}
					}
					catch (BadJOSEException | JOSEException exception) {
						httpServletResponse.setStatus(
							HttpServletResponse.SC_BAD_REQUEST);

						_log.error(
							"Failed to validate Logout Token", exception);
					}
				}
				else {
					_log.error("Failed to find oidc session");
				}
			}
			catch (java.text.ParseException parseException) {
				_log.error("Failed to parse token", parseException);
			}
		}
	}

	private OIDCProviderMetadata _resolveOIDCProviderMetadata(
			String discoveryEndPointURI)
		throws IOException, OpenIdConnectServiceException.ProviderException,
			   ParseException {

		HTTPRequest httpRequest = new HTTPRequest(
			HTTPRequest.Method.GET, new URL(discoveryEndPointURI));

		HTTPResponse httpResponse = httpRequest.send();

		if (httpResponse.getStatusCode() != HTTPResponse.SC_OK) {
			throw new OpenIdConnectServiceException.ProviderException(
				httpResponse.getStatusMessage());
		}

		return OIDCProviderMetadata.parse(httpResponse.getBody());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectBackchannelLogoutFilter.class);

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private OpenIdConnect _openIdConnect;

	@Reference
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

}