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

		ParameterPrefixURLBuilder parameterPrefixURLBuilder;

		if (MapUtil.getBoolean(
				oAuthAuthorizationData.getExtraApplicationProperties(),
				OAuth2ProviderRESTEndpointConstants.
					PROPERTY_KEY_CLIENT_TRUSTED_APPLICATION)) {

			parameterPrefixURLBuilder = new ParameterPrefixURLBuilder(
				"", _getReplyTo(oAuthAuthorizationData)
			).setParameter(
				OAuthConstants.AUTHORIZATION_DECISION_KEY,
				OAuthConstants.AUTHORIZATION_DECISION_ALLOW
			);
		}
		else {
			parameterPrefixURLBuilder = new ParameterPrefixURLBuilder(
				"oauth2_", authorizeScreenURL
			).setParameter(
				"reply_to", _getReplyTo(oAuthAuthorizationData)
			);
		}

		authorizeScreenURL = parameterPrefixURLBuilder.setParameter(
			OAuthConstants.AUTHORIZATION_CODE_CHALLENGE,
			oAuthAuthorizationData.getClientCodeChallenge()
		).setParameter(
			OAuthConstants.CLIENT_AUDIENCE, oAuthAuthorizationData.getAudience()
		).setParameter(
			OAuthConstants.CLIENT_ID, oAuthAuthorizationData.getClientId()
		).setParameter(
			OAuthConstants.NONCE, oAuthAuthorizationData.getNonce()
		).setParameter(
			OAuthConstants.REDIRECT_URI, oAuthAuthorizationData.getRedirectUri()
		).setParameter(
			OAuthConstants.RESPONSE_TYPE,
			oAuthAuthorizationData.getResponseType()
		).setParameter(
			OAuthConstants.SCOPE, oAuthAuthorizationData.getProposedScope()
		).setParameter(
			OAuthConstants.SESSION_AUTHENTICITY_TOKEN,
			oAuthAuthorizationData.getAuthenticityToken()
		).setParameter(
			OAuthConstants.STATE, oAuthAuthorizationData.getState()
		).build();

		if (authorizeScreenURL.length() > _invokerFilterURIMaxLength) {
			authorizeScreenURL = parameterPrefixURLBuilder.removeParameter(
				OAuthConstants.SCOPE
			).build();
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