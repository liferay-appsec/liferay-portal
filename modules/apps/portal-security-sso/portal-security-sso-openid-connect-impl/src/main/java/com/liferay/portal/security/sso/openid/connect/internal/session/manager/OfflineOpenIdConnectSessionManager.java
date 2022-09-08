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

package com.liferay.portal.security.sso.openid.connect.internal.session.manager;

import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.lock.Lock;
import com.liferay.portal.kernel.lock.LockManager;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;
import com.liferay.portal.security.sso.openid.connect.internal.AuthorizationServerMetadataResolver;
import com.liferay.portal.security.sso.openid.connect.internal.util.OpenIdConnectTokenRequestUtil;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(immediate = true, service = OfflineOpenIdConnectSessionManager.class)
public class OfflineOpenIdConnectSessionManager {

	public boolean isOpenIdConnectSession(HttpSession httpSession) {
		if (httpSession == null) {
			return false;
		}

		Long openIdConnectSessionId = (Long)httpSession.getAttribute(
			OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION_ID);

		if (openIdConnectSessionId != null) {
			return true;
		}

		return false;
	}

	public boolean isOpenIdConnectSessionExpired(HttpSession httpSession) {
		Long openIdConnectSessionId = (Long)httpSession.getAttribute(
			OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION_ID);

		if (openIdConnectSessionId == null) {
			return true;
		}

		OpenIdConnectSession openIdConnectSession =
			_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
				openIdConnectSessionId);

		if (openIdConnectSession == null) {
			return true;
		}

		boolean expired = _isAccessTokenExpired(openIdConnectSession);

		if (expired) {
			Thread currentThread = Thread.currentThread();

			String owner = currentThread.getName();

			ClusterNode clusterNode = _clusterExecutor.getLocalClusterNode();

			if (clusterNode != null) {
				owner = clusterNode.getClusterNodeId() + owner;
			}

			String key = String.valueOf(openIdConnectSessionId);

			Lock lock = _lockManager.lock(
				OpenIdConnectSession.class.getSimpleName(), key, owner);

			if (owner.equals(lock.getOwner())) {

				// Only 1st thread should handle access token refreshing

				try {
					AccessToken accessToken = _extendOpenIdConnectSession(
						_openIdConnectSessionLocalService.
							fetchOpenIdConnectSession(openIdConnectSessionId));

					if (accessToken != null) {
						expired = false;
					}
				}
				finally {
					_lockManager.unlock(
						OpenIdConnectSession.class.getName(), key, owner);
				}
			}
			else {

				// All other threads across clusters will wait for 1st thread

				try {

					// sleep in loop isn't the best thing, but LockManager
					// does not seem to be able to wait, and LockListener
					// does not listen to LockManager.unLock().

					do {
						Thread.sleep(500);
					}
					while (_lockManager.isLocked(
								OpenIdConnectSession.class.getName(), key));
				}
				catch (Exception exception) {
					if (_log.isWarnEnabled()) {
						_log.warn(exception);
					}
				}

				openIdConnectSession =
					_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
						openIdConnectSession.getUserId(),
						openIdConnectSession.getAuthServerWellKnownURI(),
						openIdConnectSession.getClientId(), false);

				if (openIdConnectSession != null) {
					expired = false;
				}
			}
		}

		return expired;
	}

	public long startOpenIdConnectSession(
		String authServerWellKnownURI, String clientId, OIDCTokens oidcTokens,
		long userId) {

		OpenIdConnectSession openIdConnectSession =
			_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
				userId, authServerWellKnownURI, clientId);

		if (openIdConnectSession == null) {
			openIdConnectSession =
				_openIdConnectSessionLocalService.createOpenIdConnectSession(
					_counterLocalService.increment(
						OpenIdConnectSession.class.getName()));
		}

		_updateOpenIdConnectSession(
			oidcTokens.getAccessToken(), authServerWellKnownURI, clientId,
			oidcTokens.getIDTokenString(), oidcTokens.getRefreshToken(),
			openIdConnectSession, userId);

		return openIdConnectSession.getOpenIdConnectSessionId();
	}

	private AccessToken _extendOpenIdConnectSession(
		OpenIdConnectSession openIdConnectSession) {

		try {
			RefreshToken refreshToken = new RefreshToken(
				openIdConnectSession.getRefreshToken());

			OAuthClientEntry oAuthClientEntry =
				_oAuthClientEntryLocalService.fetchOAuthClientEntry(
					openIdConnectSession.getCompanyId(),
					openIdConnectSession.getAuthServerWellKnownURI(),
					openIdConnectSession.getClientId());

			OIDCTokens oidcTokens = OpenIdConnectTokenRequestUtil.request(
				OIDCClientInformation.parse(
					JSONObjectUtils.parse(oAuthClientEntry.getInfoJSON())),
				_authorizationServerMetadataResolver.
					resolveOIDCProviderMetadata(
						openIdConnectSession.getAuthServerWellKnownURI()),
				refreshToken, oAuthClientEntry.getTokenRequestParametersJSON());

			_updateOpenIdConnectSession(
				oidcTokens.getAccessToken(), openIdConnectSession,
				oidcTokens.getRefreshToken());

			return oidcTokens.getAccessToken();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			_openIdConnectSessionLocalService.deleteOpenIdConnectSession(
				openIdConnectSession);
		}

		return null;
	}

	private AccessToken _getAccessToken(
		OpenIdConnectSession openIdConnectSession) {

		try {
			return AccessToken.parse(
				JSONObjectUtils.parse(openIdConnectSession.getAccessToken()));
		}
		catch (ParseException parseException) {
			if (_log.isWarnEnabled()) {
				_log.warn(parseException);
			}

			return null;
		}
	}

	private boolean _isAccessTokenExpired(
		OpenIdConnectSession openIdConnectSession) {

		AccessToken accessToken = _getAccessToken(openIdConnectSession);

		if (accessToken == null) {
			return true;
		}

		long currentTime = System.currentTimeMillis();
		long lifetime = accessToken.getLifetime() * Time.SECOND;
		Date modifiedDate = openIdConnectSession.getModifiedDate();

		if ((currentTime - modifiedDate.getTime()) > lifetime) {
			return true;
		}

		return false;
	}

	private void _updateOpenIdConnectSession(
		AccessToken accessToken, OpenIdConnectSession openIdConnectSession,
		RefreshToken refreshToken) {

		openIdConnectSession.setAccessToken(accessToken.toJSONString());

		if (refreshToken != null) {
			openIdConnectSession.setRefreshToken(refreshToken.toString());
		}

		openIdConnectSession.setModifiedDate(new Date());

		_openIdConnectSessionLocalService.updateOpenIdConnectSession(
			openIdConnectSession);
	}

	private void _updateOpenIdConnectSession(
		AccessToken accessToken, String authServerWellKnownURI, String clientId,
		String idTokenString, RefreshToken refreshToken,
		OpenIdConnectSession openIdConnectSession, long userId) {

		openIdConnectSession.setUserId(userId);
		openIdConnectSession.setAuthServerWellKnownURI(authServerWellKnownURI);
		openIdConnectSession.setClientId(clientId);
		openIdConnectSession.setIdToken(idTokenString);

		_updateOpenIdConnectSession(
			accessToken, openIdConnectSession, refreshToken);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OfflineOpenIdConnectSessionManager.class);

	@Reference
	private AuthorizationServerMetadataResolver
		_authorizationServerMetadataResolver;

	@Reference
	private ClusterExecutor _clusterExecutor;

	@Reference
	private CounterLocalService _counterLocalService;

	@Reference
	private LockManager _lockManager;

	@Reference
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Reference
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

}