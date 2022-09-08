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
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.lock.Lock;
import com.liferay.portal.kernel.lock.LockManager;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.SchedulerException;
import com.liferay.portal.kernel.scheduler.StorageType;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.Trigger;
import com.liferay.portal.kernel.scheduler.TriggerFactory;
import com.liferay.portal.kernel.scheduler.messaging.SchedulerResponse;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.security.sso.openid.connect.configuration.OpenIdConnectConfiguration;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectConstants;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;
import com.liferay.portal.security.sso.openid.connect.internal.AuthorizationServerMetadataResolver;
import com.liferay.portal.security.sso.openid.connect.internal.constants.OpenIdConnectDestinationNames;
import com.liferay.portal.security.sso.openid.connect.internal.util.OpenIdConnectTokenRequestUtil;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	configurationPid = "com.liferay.portal.security.sso.openid.connect.configuration.OpenIdConnectConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	service = OfflineOpenIdConnectSessionManager.class
)
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

		Date accessTokenExpiredDate =
			openIdConnectSession.getAccessTokenExpiredDate();
		long currentTime = System.currentTimeMillis();

		if (currentTime <=
				(accessTokenExpiredDate.getTime() - _tokenRefreshOffset)) {

			return false;
		}

		Thread currentThread = Thread.currentThread();

		String owner = currentThread.getName();

		ClusterNode clusterNode = _clusterExecutor.getLocalClusterNode();

		if (clusterNode != null) {
			owner = clusterNode.getClusterNodeId() + owner;
		}

		String key = String.valueOf(openIdConnectSessionId);

		Lock lock = _lockManager.lock(
			OpenIdConnectSession.class.getSimpleName(), key, owner);

		AccessToken accessToken;

		if (currentTime <= accessTokenExpiredDate.getTime()) {
			if (!owner.equals(lock.getOwner())) {
				return false;
			}

			accessToken = _extendOpenIdConnectSession(openIdConnectSession);

			_lockManager.unlock(
				OpenIdConnectSession.class.getSimpleName(), key, owner);
		}
		else {
			if (!owner.equals(lock.getOwner())) {
				return true;
			}

			accessToken = _extendOpenIdConnectSession(openIdConnectSession);

			_lockManager.unlock(
				OpenIdConnectSession.class.getSimpleName(), key, owner);
		}

		if (accessToken == null) {
			return true;
		}

		return false;
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

	@Modified
	protected void activate(
			BundleContext bundleContext, Map<String, Object> properties)
		throws Exception {

		OpenIdConnectConfiguration openIdConnectConfiguration =
			ConfigurableUtil.createConfigurable(
				OpenIdConnectConfiguration.class, properties);

		if ((openIdConnectConfiguration.tokenRefreshOffset() < 30) ||
			(openIdConnectConfiguration.tokenRefreshScheduledInterval() < 30)) {

			throw new IllegalArgumentException(
				"Token refresh offset needs to be at least 30 seconds");
		}

		_tokenRefreshOffset =
			openIdConnectConfiguration.tokenRefreshOffset() * Time.SECOND;

		_bundleContext = bundleContext;

		DestinationConfiguration destinationConfiguration =
			DestinationConfiguration.createSerialDestinationConfiguration(
				OpenIdConnectDestinationNames.OPENID_CONNECT_TOKEN_REFRESH);

		Destination destination = _destinationFactory.createDestination(
			destinationConfiguration);

		Dictionary<String, Object> dictionary =
			HashMapDictionaryBuilder.<String, Object>put(
				"destination.name", destination.getName()
			).build();

		_serviceRegistration1 = bundleContext.registerService(
			Destination.class, destination, dictionary);

		_serviceRegistration2 = bundleContext.registerService(
			MessageListener.class, new OpenIdConnectMessageListener(),
			dictionary);

		_scheduleTokenRefreshJob(openIdConnectConfiguration);
	}

	@Deactivate
	protected void deactivate() throws Exception {
		if (_serviceRegistration1 != null) {
			Destination destination = _bundleContext.getService(
				_serviceRegistration1.getReference());

			_serviceRegistration1.unregister();

			destination.destroy();
		}

		if (_serviceRegistration2 != null) {
			_serviceRegistration2.unregister();
		}

		try {
			_schedulerEngineHelper.delete(
				_SCHEDULED_JOB_NAME, OpenIdConnectConstants.SERVICE_NAME,
				StorageType.PERSISTED);
		}
		catch (SchedulerException schedulerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(schedulerException);
			}
		}

		_bundleContext = null;
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

	private void _scheduleTokenRefreshJob(
		OpenIdConnectConfiguration openIdConnectConfiguration) {

		try {
			if (openIdConnectConfiguration.enabled()) {
				SchedulerResponse schedulerResponse =
					_schedulerEngineHelper.getScheduledJob(
						_SCHEDULED_JOB_NAME,
						OpenIdConnectConstants.SERVICE_NAME,
						StorageType.PERSISTED);

				Trigger trigger = _triggerFactory.createTrigger(
					_SCHEDULED_JOB_NAME, OpenIdConnectConstants.SERVICE_NAME,
					new Date(), null,
					openIdConnectConfiguration.tokenRefreshScheduledInterval(),
					TimeUnit.SECOND);

				if (schedulerResponse == null) {
					_schedulerEngineHelper.schedule(
						trigger, StorageType.PERSISTED, null,
						OpenIdConnectDestinationNames.
							OPENID_CONNECT_TOKEN_REFRESH,
						null, 0);
				}
				else {
					_schedulerEngineHelper.update(
						trigger, StorageType.PERSISTED);
				}
			}
			else {
				_schedulerEngineHelper.delete(
					_SCHEDULED_JOB_NAME, OpenIdConnectConstants.SERVICE_NAME,
					StorageType.PERSISTED);
			}
		}
		catch (Exception exception) {
			try {
				_schedulerEngineHelper.delete(
					_SCHEDULED_JOB_NAME, OpenIdConnectConstants.SERVICE_NAME,
					StorageType.PERSISTED);
			}
			catch (SchedulerException schedulerException) {
				if (_log.isWarnEnabled()) {
					_log.warn(schedulerException);
				}
			}
		}
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

	private static final String _SCHEDULED_JOB_NAME = "Refresh Access Tokens";

	private static final Log _log = LogFactoryUtil.getLog(
		OfflineOpenIdConnectSessionManager.class);

	@Reference
	private AuthorizationServerMetadataResolver
		_authorizationServerMetadataResolver;

	private volatile BundleContext _bundleContext;

	@Reference
	private ClusterExecutor _clusterExecutor;

	@Reference
	private CounterLocalService _counterLocalService;

	@Reference
	private DestinationFactory _destinationFactory;

	private LockManager _lockManager;

	@Reference
	private OAuthClientEntryLocalService _oAuthClientEntryLocalService;

	@Reference
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

	private volatile ServiceRegistration<Destination> _serviceRegistration1;
	private volatile ServiceRegistration<MessageListener> _serviceRegistration2;
	private volatile long _tokenRefreshOffset = 60 * Time.SECOND;

	@Reference
	private TriggerFactory _triggerFactory;

	private class OpenIdConnectMessageListener extends BaseMessageListener {

		protected void doReceive(Message message) throws Exception {
			List<OpenIdConnectSession> openIdConnectSessions =
				_openIdConnectSessionLocalService.
					getAccessTokenExpiredOpenIdConnectSessions(0, 20);

			openIdConnectSessions.forEach(
				openIdConnectSession -> _extendOpenIdConnectSession(
					openIdConnectSession));
		}

	}

}