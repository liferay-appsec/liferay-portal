/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.rest.internal.endpoint.dynamic.registration.container.request.filter;

import com.liferay.oauth2.provider.constants.OAuth2ApplicationConstants;
import com.liferay.oauth2.provider.constants.OAuth2ProviderActionKeys;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.rest.internal.configuration.DynamicRegistrationConfiguration;
import com.liferay.oauth2.provider.rest.internal.endpoint.util.OAuth2ErrorUtil;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.ProtectedPrincipal;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.annotation.Priority;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.rs.security.jose.jws.JwsJwtCompactConsumer;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.OAuth2.Application)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=DynamicRegistrationServiceContainerRequestFilter"
	},
	service = ContainerRequestFilter.class
)
@PreMatching
@Priority(Priorities.AUTHENTICATION)
@Provider
public class DynamicRegistrationServiceContainerRequestFilter
	implements ContainerRequestFilter {

	public static final String REQUEST_PROPERTY_ANONYMOUS_REGISTRATION =
		"com.liferay.oauth2.dynamic.registration.anonymous";

	@Override
	public void filter(ContainerRequestContext containerRequestContext) {
		UriInfo uriInfo = containerRequestContext.getUriInfo();

		if (!StringUtil.startsWith(uriInfo.getPath(), "register")) {
			return;
		}

		Message message = JAXRSUtils.getCurrentMessage();

		HttpServletRequest httpServletRequest = (HttpServletRequest)message.get(
			AbstractHTTPDestination.HTTP_REQUEST);

		long companyId = _portal.getCompanyId(httpServletRequest);

		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-63416")) {
			containerRequestContext.abortWith(
				Response.status(
					Response.Status.NOT_FOUND
				).build());

			return;
		}

		String method = httpServletRequest.getMethod();

		boolean post = StringUtil.equalsIgnoreCase(method, "POST");

		String authorization = httpServletRequest.getHeader("Authorization");

		boolean hasBearer = StringUtil.startsWith(authorization, "Bearer ");

		User user;
		boolean anonymous = false;

		try {
			if (post && !hasBearer) {
				DynamicRegistrationConfiguration
					dynamicRegistrationConfiguration =
						_getDynamicRegistrationConfiguration(companyId);

				if (dynamicRegistrationConfiguration.
						requireInitialAccessToken()) {

					_auditRejection(
						httpServletRequest, companyId, "anonymous",
						"invalid_token", "Initial access token required");

					throw ExceptionUtils.toNotAuthorizedException(null, null);
				}

				_validateAnonymousHosts(
					httpServletRequest, companyId,
					dynamicRegistrationConfiguration.anonymousAllowedHosts());

				_checkAnonymousRateLimit(
					httpServletRequest, companyId,
					_getClientHost(httpServletRequest),
					dynamicRegistrationConfiguration.
						anonymousRegistrationsPerHour());

				user = _userLocalService.getUserByScreenName(
					companyId, "default-service-account");
				anonymous = true;

				if (_log.isInfoEnabled()) {
					_log.info(
						StringBundler.concat(
							"Anonymous dynamic client registration accepted ",
							"for company ", companyId, " from ",
							_getClientHost(httpServletRequest)));
				}
			}
			else {
				user = _authorizeWithBearer(httpServletRequest, method);
			}
		}
		catch (WebApplicationException webApplicationException) {
			if (_log.isDebugEnabled()) {
				_log.debug(webApplicationException);
			}

			throw webApplicationException;
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			_auditRejection(
				httpServletRequest, companyId,
				hasBearer ? "authenticated" : "anonymous", "invalid_token",
				"Bearer token authorization failed");

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		if (anonymous) {
			httpServletRequest.setAttribute(
				REQUEST_PROPERTY_ANONYMOUS_REGISTRATION, Boolean.TRUE);
		}

		_setSecurityContext(containerRequestContext, httpServletRequest, user);
	}

	private void _auditRejection(
		HttpServletRequest httpServletRequest, long companyId, String mode,
		String error, String errorDescription) {

		AuditRouter auditRouter = _auditRouterSnapshot.get();

		if (auditRouter == null) {
			return;
		}

		try {
			AuditMessage auditMessage = new AuditMessage(
				_EVENT_TYPE_DCR_REJECT, companyId, 0, 0, StringPool.BLANK,
				OAuth2Application.class.getName(), StringPool.BLANK,
				StringPool.BLANK, null,
				JSONUtil.put(
					"clientHost", _getClientHost(httpServletRequest)
				).put(
					"error", error
				).put(
					"errorDescription", errorDescription
				).put(
					"mode", mode
				).put(
					"userAgent",
					GetterUtil.getString(
						httpServletRequest.getHeader("User-Agent"))
				));

			auditRouter.route(auditMessage);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private User _authorizeWithBearer(
			HttpServletRequest httpServletRequest, String method)
		throws Exception {

		JwtToken jwtToken = _getJwtToken(httpServletRequest);

		long currentTime = TimeUnit.SECONDS.convert(
			System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		long expirationTime = GetterUtil.getLong(jwtToken.getClaim("exp"));

		if (currentTime > expirationTime) {
			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		User user = _userLocalService.getUser(
			GetterUtil.getLong(jwtToken.getClaim("sub")));

		OAuth2Application oAuth2Application = null;

		String tokenClientId = GetterUtil.getString(
			jwtToken.getClaim("client_id"));

		if (!Validator.isBlank(tokenClientId)) {
			oAuth2Application =
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					user.getCompanyId(), tokenClientId);
		}
		else {
			oAuth2Application =
				_oAuth2ApplicationLocalService.fetchOAuth2Application(
					GetterUtil.getLong(jwtToken.getClaim("application_id")));
		}

		PermissionChecker permissionChecker = _permissionCheckerFactory.create(
			user);

		if ((oAuth2Application == null) ||
			!_oAuth2ApplicationModelResourcePermission.contains(
				permissionChecker, oAuth2Application,
				OAuth2ProviderActionKeys.REGISTER_APPLICATION)) {

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		if (StringUtil.equalsIgnoreCase(method, "DELETE") &&
			!_oAuth2ApplicationModelResourcePermission.contains(
				permissionChecker, oAuth2Application, ActionKeys.DELETE)) {

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		if (StringUtil.equalsIgnoreCase(method, "PUT") &&
			!_oAuth2ApplicationModelResourcePermission.contains(
				permissionChecker, oAuth2Application, ActionKeys.UPDATE)) {

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		boolean dynamicRegistrator = StringUtil.equalsIgnoreCase(
			OAuth2ApplicationConstants.NAME_DYNAMIC_REGISTRATOR,
			oAuth2Application.getName());

		if (StringUtil.equalsIgnoreCase(method, "POST") &&
			!dynamicRegistrator) {

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		String clientId = _getClientId(httpServletRequest);

		if (Validator.isNotNull(clientId) && !dynamicRegistrator &&
			!StringUtil.equalsIgnoreCase(
				clientId, oAuth2Application.getClientId())) {

			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		return user;
	}

	private void _checkAnonymousRateLimit(
		HttpServletRequest httpServletRequest, long companyId,
		String clientHost, int registrationsPerHour) {

		if (registrationsPerHour <= 0) {
			return;
		}

		long currentTimeMillis = System.currentTimeMillis();

		long windowStart =
			(currentTimeMillis / _RATE_LIMIT_WINDOW_MILLIS) *
				_RATE_LIMIT_WINDOW_MILLIS;

		String key = companyId + StringPool.COLON + clientHost;

		_rateLimitBuckets.entrySet(
		).removeIf(
			entry -> {
				RateLimitBucket bucket = entry.getValue();

				return bucket.windowStart < windowStart;
			}
		);

		RateLimitBucket bucket = _rateLimitBuckets.compute(
			key,
			(unusedKey, currentBucket) -> {
				if ((currentBucket == null) ||
					(currentBucket.windowStart != windowStart)) {

					return new RateLimitBucket(windowStart);
				}

				return currentBucket;
			});

		int count = bucket.count.incrementAndGet();

		if (count <= registrationsPerHour) {
			return;
		}

		long retryAfterSeconds =
			((windowStart + _RATE_LIMIT_WINDOW_MILLIS) - currentTimeMillis) /
				1000;

		if (retryAfterSeconds < 1) {
			retryAfterSeconds = 1;
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Anonymous dynamic client registration rate limit ",
					"exceeded for company ", companyId, " from ", clientHost,
					"; retry after ", retryAfterSeconds, " seconds"));
		}

		_auditRejection(
			httpServletRequest, companyId, "anonymous", "rate_limited",
			"Anonymous client registration rate limit exceeded for host " +
				clientHost);

		throw new WebApplicationException(
			Response.status(
				Response.Status.TOO_MANY_REQUESTS
			).entity(
				StringBundler.concat(
					"{\"error\":\"rate_limited\",\"error_description\":\"",
					"Anonymous client registration rate limit exceeded for ",
					"host ", clientHost, ". Retry after ", retryAfterSeconds,
					" seconds.\"}")
			).header(
				"Retry-After", retryAfterSeconds
			).type(
				MediaType.APPLICATION_JSON
			).build());
	}

	private String _getClientHost(HttpServletRequest httpServletRequest) {
		String forwardedFor = httpServletRequest.getHeader("X-Forwarded-For");

		if (!Validator.isBlank(forwardedFor)) {
			int index = forwardedFor.indexOf(',');

			if (index > 0) {
				forwardedFor = forwardedFor.substring(0, index);
			}

			return forwardedFor.trim();
		}

		return httpServletRequest.getRemoteAddr();
	}

	private String _getClientId(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		String clientId = requestURI.substring(
			requestURI.lastIndexOf(StringPool.SLASH) + 1);

		if (clientId.startsWith("id-")) {
			return clientId;
		}

		return null;
	}

	private DynamicRegistrationConfiguration
			_getDynamicRegistrationConfiguration(long companyId)
		throws ConfigurationException {

		return _configurationProvider.getConfiguration(
			DynamicRegistrationConfiguration.class,
			new CompanyServiceSettingsLocator(
				companyId, DynamicRegistrationConfiguration.class.getName()));
	}

	private JwtToken _getJwtToken(HttpServletRequest httpServletRequest) {
		String authorization = httpServletRequest.getHeader("Authorization");

		if (!StringUtil.startsWith(authorization, "Bearer ")) {
			throw ExceptionUtils.toNotAuthorizedException(null, null);
		}

		String accessTokenContent = authorization.substring("Bearer ".length());

		OAuth2Authorization oAuth2Authorization =
			_oAuth2AuthorizationLocalService.
				fetchOAuth2AuthorizationByAccessTokenContent(
					accessTokenContent);

		if (oAuth2Authorization != null) {
			JwtClaims jwtClaims = new JwtClaims();

			jwtClaims.setClaim(
				"application_id", oAuth2Authorization.getOAuth2ApplicationId());
			jwtClaims.setClaim("sub", oAuth2Authorization.getUserId());

			Date accessTokenExpirationDate =
				oAuth2Authorization.getAccessTokenExpirationDate();

			jwtClaims.setExpiryTime(accessTokenExpirationDate.getTime());

			return new JwtToken(jwtClaims);
		}

		JwsJwtCompactConsumer jwsJwtCompactConsumer = new JwsJwtCompactConsumer(
			accessTokenContent);

		return jwsJwtCompactConsumer.getJwtToken();
	}

	private void _setSecurityContext(
		ContainerRequestContext containerRequestContext,
		HttpServletRequest httpServletRequest, User user) {

		try {
			if (user.isGuestUser()) {
				return;
			}

			long userId = user.getUserId();

			containerRequestContext.setSecurityContext(
				new PortalCXFSecurityContext() {

					@Override
					public Principal getUserPrincipal() {
						return new ProtectedPrincipal(String.valueOf(userId));
					}

					@Override
					public boolean isSecure() {
						return _portal.isSecure(httpServletRequest);
					}

				});
		}
		catch (Exception exception) {
			_log.error("Unable to resolve authenticated user", exception);

			containerRequestContext.abortWith(
				Response.status(
					Response.Status.INTERNAL_SERVER_ERROR
				).build());
		}
	}

	private void _validateAnonymousHosts(
		HttpServletRequest httpServletRequest, long companyId,
		String[] allowedHosts) {

		if (ArrayUtil.isEmpty(allowedHosts)) {
			return;
		}

		Set<String> normalizedAllowedHosts = new HashSet<>();

		for (String allowedHost : allowedHosts) {
			if (Validator.isBlank(allowedHost)) {
				continue;
			}

			for (String line : allowedHost.split("\\s+")) {
				if (Validator.isBlank(line)) {
					continue;
				}

				normalizedAllowedHosts.add(line);
			}
		}

		if (normalizedAllowedHosts.isEmpty()) {
			return;
		}

		String clientHost = _getClientHost(httpServletRequest);

		if (!normalizedAllowedHosts.contains(clientHost)) {
			_auditRejection(
				httpServletRequest, companyId, "anonymous", "access_denied",
				"Host " + clientHost +
					" is not permitted for anonymous registration");

			OAuth2ErrorUtil.reportInvalidRequestError(
				"Host " + clientHost +
					" is not permitted for anonymous registration",
				"access_denied", Response.Status.FORBIDDEN);
		}
	}

	private static final String _EVENT_TYPE_DCR_REJECT =
		"DYNAMIC_REGISTRATION_REJECT";

	private static final long _RATE_LIMIT_WINDOW_MILLIS =
		TimeUnit.HOURS.toMillis(1);

	private static final Log _log = LogFactoryUtil.getLog(
		DynamicRegistrationServiceContainerRequestFilter.class);

	private static final Snapshot<AuditRouter> _auditRouterSnapshot =
		new Snapshot<>(
			DynamicRegistrationServiceContainerRequestFilter.class,
			AuditRouter.class, null, true);
	private static final Map<String, RateLimitBucket> _rateLimitBuckets =
		new ConcurrentHashMap<>();

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.oauth2.provider.model.OAuth2Application)"
	)
	private ModelResourcePermission<OAuth2Application>
		_oAuth2ApplicationModelResourcePermission;

	@Reference
	private OAuth2AuthorizationLocalService _oAuth2AuthorizationLocalService;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

	private abstract static class PortalCXFSecurityContext
		implements org.apache.cxf.security.SecurityContext, SecurityContext {

		@Override
		public String getAuthenticationScheme() {
			return "session";
		}

		@Override
		public boolean isUserInRole(String role) {
			return false;
		}

	}

	private static final class RateLimitBucket {

		public RateLimitBucket(long windowStart) {
			this.windowStart = windowStart;
		}

		public final AtomicInteger count = new AtomicInteger();
		public final long windowStart;

	}

}