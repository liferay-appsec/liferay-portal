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

package com.liferay.portal.security.sso.openid.connect.persistence.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.base.OpenIdConnectSessionLocalServiceBaseImpl;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Arthur Chan
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession",
	service = AopService.class
)
public class OpenIdConnectSessionLocalServiceImpl
	extends OpenIdConnectSessionLocalServiceBaseImpl {

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public OpenIdConnectSession addOpenIdConnectSession(
		OpenIdConnectSession openIdConnectSession) {

		if (openIdConnectSession.getAccessTokenExpiredDate() == null) {
			openIdConnectSession.setAccessTokenExpiredDate(
				_getAccessTokenExpiredDate(openIdConnectSession));
		}

		return super.addOpenIdConnectSession(openIdConnectSession);
	}

	@Override
	public void deleteOpenIdConnectSessions(long userId) {
		openIdConnectSessionPersistence.removeByUserId(userId);
	}

	@Override
	public void deleteOpenIdConnectSessions(
		long companyId, String authServerWellKnownURI, String clientId) {

		openIdConnectSessionPersistence.removeByC_A_C(
			companyId, authServerWellKnownURI, clientId);
	}

	@Override
	public OpenIdConnectSession fetchOpenIdConnectSession(
		long userId, String authServerWellKnownURI, String clientId) {

		return openIdConnectSessionPersistence.fetchByU_A_C(
			userId, authServerWellKnownURI, clientId);
	}

	@Override
	public List<OpenIdConnectSession>
		getAccessTokenExpiredOpenIdConnectSessions(int start, int end) {

		return openIdConnectSessionFinder.findAccessTokenExpiredSessions(
			start, end);
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public OpenIdConnectSession updateOpenIdConnectSession(
		OpenIdConnectSession openIdConnectSession) {

		if (openIdConnectSession.getAccessTokenExpiredDate() == null) {
			openIdConnectSession.setAccessTokenExpiredDate(
				_getAccessTokenExpiredDate(openIdConnectSession));
		}

		return super.updateOpenIdConnectSession(openIdConnectSession);
	}

	private Date _getAccessTokenExpiredDate(
		OpenIdConnectSession openIdConnectSession) {

		try {
			AccessToken accessToken = AccessToken.parse(
				JSONObjectUtils.parse(openIdConnectSession.getAccessToken()));

			long accessTokenLifetime = accessToken.getLifetime();

			if (accessTokenLifetime == 0) {
				accessTokenLifetime = 3600;
			}

			Date modifiedDate = openIdConnectSession.getModifiedDate();

			return new Date(
				modifiedDate.getTime() + (accessTokenLifetime * Time.SECOND));
		}
		catch (ParseException parseException) {
			if (_log.isWarnEnabled()) {
				_log.warn(parseException);
			}

			return new Date();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectSessionLocalServiceImpl.class);

}