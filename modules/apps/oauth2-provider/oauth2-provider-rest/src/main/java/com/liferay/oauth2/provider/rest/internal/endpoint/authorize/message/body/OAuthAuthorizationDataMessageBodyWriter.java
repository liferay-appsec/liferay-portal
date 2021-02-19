/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.authorize.message.body;

import com.liferay.oauth2.provider.rest.internal.endpoint.constants.OAuth2ProviderRESTEndpointConstants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.net.URI;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.OAuth2.Application)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=OAuthAuthorizationDataMessageBodyWriter"
	},
	service = MessageBodyWriter.class
)
@Produces("text/html")
@Provider
public class OAuthAuthorizationDataMessageBodyWriter
	extends BaseMessageBodyWriter<OAuthAuthorizationData> {

	@Override
	public boolean isWriteable(
		Class<?> clazz, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		if (clazz.isAssignableFrom(OAuthAuthorizationData.class) &&
			StringUtil.equalsIgnoreCase(mediaType.getType(), "text") &&
			StringUtil.equalsIgnoreCase(mediaType.getSubtype(), "html")) {

			return true;
		}

		return false;
	}

	@Activate
	protected void activate() {
		_invokerFilterURIMaxLength = GetterUtil.getInteger(
			_props.get(PropsKeys.INVOKER_FILTER_URI_MAX_LENGTH),
			_invokerFilterURIMaxLength);
	}

	@Override
	protected String writeTo(
		OAuthAuthorizationData oAuthAuthorizationData,
		String authorizeScreenURL) {

		boolean addOAuth2Prefix = false;

		if (MapUtil.getBoolean(
				oAuthAuthorizationData.getExtraApplicationProperties(),
				OAuth2ProviderRESTEndpointConstants.
					PROPERTY_KEY_CLIENT_TRUSTED_APPLICATION)) {

			authorizeScreenURL = _getReplyTo(oAuthAuthorizationData);

			authorizeScreenURL = setParameter(
				authorizeScreenURL, OAuthConstants.AUTHORIZATION_DECISION_KEY,
				OAuthConstants.AUTHORIZATION_DECISION_ALLOW, false);
		}
		else {
			addOAuth2Prefix = true;

			authorizeScreenURL = setParameter(
				authorizeScreenURL, "reply_to",
				_getReplyTo(oAuthAuthorizationData), addOAuth2Prefix);
		}

		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.AUTHORIZATION_CODE_CHALLENGE,
			oAuthAuthorizationData.getClientCodeChallenge(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.CLIENT_AUDIENCE,
			oAuthAuthorizationData.getAudience(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.CLIENT_ID,
			oAuthAuthorizationData.getClientId(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.NONCE,
			oAuthAuthorizationData.getNonce(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.REDIRECT_URI,
			oAuthAuthorizationData.getRedirectUri(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.RESPONSE_TYPE,
			oAuthAuthorizationData.getResponseType(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.SCOPE,
			oAuthAuthorizationData.getProposedScope(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.SESSION_AUTHENTICITY_TOKEN,
			oAuthAuthorizationData.getAuthenticityToken(), addOAuth2Prefix);
		authorizeScreenURL = setParameter(
			authorizeScreenURL, OAuthConstants.STATE,
			oAuthAuthorizationData.getState(), addOAuth2Prefix);

		if (authorizeScreenURL.length() > _invokerFilterURIMaxLength) {
			authorizeScreenURL = removeParameter(
				authorizeScreenURL, OAuthConstants.SCOPE, addOAuth2Prefix);
		}

		return authorizeScreenURL;
	}

	private String _getReplyTo(OAuthAuthorizationData oAuthAuthorizationData) {
		if (portal.isForwardedSecure(messageContext.getHttpServletRequest())) {
			UriInfo uriInfo = messageContext.getUriInfo();

			UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();

			uriBuilder.path("decision");
			uriBuilder.scheme(Http.HTTPS);

			URI uri = uriBuilder.build();

			return uri.toString();
		}

		return oAuthAuthorizationData.getReplyTo();
	}

	private int _invokerFilterURIMaxLength = 4000;

	@Reference
	private Props _props;

}