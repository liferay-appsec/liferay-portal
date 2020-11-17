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

package com.liferay.portal.servlet.filters.authverifier;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.url.pattern.mapper.URLPatternMapper;
import com.liferay.petra.url.pattern.mapper.URLPatternMapperFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.access.control.AccessControlUtil;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierConfiguration;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.servlet.ProtectedServletRequest;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.AuthVerifierPipeline;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.liferay.portal.util.PropsUtil;
import com.liferay.registry.Filter;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.ServiceReference;
import com.liferay.registry.ServiceTracker;
import com.liferay.registry.ServiceTrackerCustomizer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * See https://issues.liferay.com/browse/LPS-27888.
 * </p>
 *
 * @author Tomas Polesovsky
 * @author Raymond Augé
 */
public class AuthVerifierFilter extends BasePortalFilter {

	@Override
	public void destroy() {
		_authVerifierServiceTrackerCustomizer.removeAuthVerifierFilter(this);

		super.destroy();
	}

	@Override
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);

		Enumeration<String> enumeration = filterConfig.getInitParameterNames();

		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();

			String value = filterConfig.getInitParameter(name);

			_initParametersMap.put(name, value);
		}

		String portalPropertyPrefix = GetterUtil.getString(
			_initParametersMap.get("portal_property_prefix"));

		if (Validator.isNotNull(portalPropertyPrefix)) {
			Properties properties = PropsUtil.getProperties(
				portalPropertyPrefix, true);

			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				_initParametersMap.put(
					(String)entry.getKey(), entry.getValue());
			}
		}

		if (_initParametersMap.containsKey("guest.allowed")) {
			_guestAllowed = GetterUtil.getBoolean(
				_initParametersMap.get("guest.allowed"), true);

			_initParametersMap.remove("guest.allowed");
		}

		if (_initParametersMap.containsKey("hosts.allowed")) {
			String hostsAllowedString = (String)_initParametersMap.get(
				"hosts.allowed");

			String[] hostsAllowed = StringUtil.split(hostsAllowedString);

			for (String hostAllowed : hostsAllowed) {
				_hostsAllowed.add(hostAllowed);
			}

			_initParametersMap.remove("hosts.allowed");
		}

		if (_initParametersMap.containsKey("https.required")) {
			_httpsRequired = GetterUtil.getBoolean(
				_initParametersMap.get("https.required"));

			_initParametersMap.remove("https.required");
		}

		if (_initParametersMap.containsKey("use_permission_checker")) {
			_initParametersMap.remove("use_permission_checker");

			if (_log.isWarnEnabled()) {
				_log.warn("use_permission_checker is deprecated");
			}
		}

		_authVerifierServiceTrackerCustomizer.addAuthVerifierFilter(this);
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		if (!_isAccessAllowed(httpServletRequest, httpServletResponse)) {
			return;
		}

		if (_isApplySSL(httpServletRequest, httpServletResponse)) {
			return;
		}

		if (_isCORSPreflightRequest(httpServletRequest)) {
			Class<?> clazz = getClass();

			processFilter(
				clazz.getName(), httpServletRequest, httpServletResponse,
				filterChain);

			return;
		}

		AccessControlUtil.initAccessControlContext(
			httpServletRequest, httpServletResponse, _initParametersMap);

		AuthVerifierResult.State state = AccessControlUtil.verifyRequest();

		AccessControlContext accessControlContext =
			AccessControlUtil.getAccessControlContext();

		AuthVerifierResult authVerifierResult =
			accessControlContext.getAuthVerifierResult();

		if (_log.isDebugEnabled()) {
			_log.debug("Auth verifier result " + authVerifierResult);
		}

		if (state == AuthVerifierResult.State.INVALID_CREDENTIALS) {
			if (_log.isDebugEnabled()) {
				_log.debug("Result state does not allow us to continue");
			}
		}
		else if (state == AuthVerifierResult.State.NOT_APPLICABLE) {
			_log.error("Invalid state " + state);
		}
		else if (!_guestAllowed &&
				 (state == AuthVerifierResult.State.UNSUCCESSFUL)) {

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Guest is not allowed to access " +
						httpServletRequest.getRequestURI());
			}

			httpServletResponse.sendError(
				HttpServletResponse.SC_FORBIDDEN, "Authorization required");
		}
		else if (_guestAllowed || (state == AuthVerifierResult.State.SUCCESS)) {
			long userId = authVerifierResult.getUserId();

			AccessControlUtil.initContextUser(userId);

			String authType = MapUtil.getString(
				accessControlContext.getSettings(),
				AuthVerifierPipeline.AUTH_TYPE);

			ProtectedServletRequest protectedServletRequest =
				new ProtectedServletRequest(
					httpServletRequest, String.valueOf(userId), authType);

			accessControlContext.setRequest(protectedServletRequest);

			Class<?> clazz = getClass();

			processFilter(
				clazz.getName(), protectedServletRequest, httpServletResponse,
				filterChain);
		}
		else {
			_log.error("Unimplemented state " + state);
		}
	}

	private boolean _isAccessAllowed(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		String remoteAddr = httpServletRequest.getRemoteAddr();

		if (AccessControlUtil.isAccessAllowed(
				httpServletRequest, _hostsAllowed)) {

			if (_log.isDebugEnabled()) {
				_log.debug("Access allowed for " + remoteAddr);
			}

			return true;
		}

		if (_log.isWarnEnabled()) {
			_log.warn("Access denied for " + remoteAddr);
		}

		httpServletResponse.sendError(
			HttpServletResponse.SC_FORBIDDEN,
			"Access denied for " + remoteAddr);

		return false;
	}

	private boolean _isApplySSL(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		if (!_httpsRequired || PortalUtil.isSecure(httpServletRequest)) {
			return false;
		}

		if (_log.isDebugEnabled()) {
			String completeURL = HttpUtil.getCompleteURL(httpServletRequest);

			_log.debug("Securing " + completeURL);
		}

		StringBundler sb = new StringBundler(5);

		sb.append(PortalUtil.getPortalURL(httpServletRequest, true));
		sb.append(PortalUtil.getPathContext(httpServletRequest));
		sb.append(httpServletRequest.getRequestURI());

		if (Validator.isNotNull(httpServletRequest.getQueryString())) {
			sb.append(StringPool.QUESTION);
			sb.append(httpServletRequest.getQueryString());
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Redirect to " + sb.toString());
		}

		httpServletResponse.sendRedirect(sb.toString());

		return true;
	}

	private boolean _isCORSPreflightRequest(
		HttpServletRequest httpServletRequest) {

		if (StringUtil.equals(httpServletRequest.getMethod(), "OPTIONS") &&
			Validator.isNotNull(httpServletRequest.getHeader("Origin"))) {

			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuthVerifierFilter.class.getName());

	private static final AuthVerifierServiceTrackerCustomizer
		_authVerifierServiceTrackerCustomizer;

	private URLPatternMapper<List<AuthVerifierConfiguration>>
		_excludeURLPatternMapper;
	private boolean _guestAllowed = true;
	private final Set<String> _hostsAllowed = new HashSet<>();
	private boolean _httpsRequired;
	private URLPatternMapper<List<AuthVerifierConfiguration>>
		_includeURLPatternMapper;
	private final Map<String, Object> _initParametersMap = new HashMap<>();

	private static class AuthVerifierServiceTrackerCustomizer
		implements ServiceTrackerCustomizer
			<AuthVerifier, AuthVerifierConfiguration> {

		public void addAuthVerifierFilter(
			AuthVerifierFilter authVerifierFilter) {

			_authVerifierFilters.add(authVerifierFilter);

			_rebuildFor(authVerifierFilter);
		}

		@Override
		public AuthVerifierConfiguration addingService(
			ServiceReference<AuthVerifier> serviceReference) {

			Registry registry = RegistryUtil.getRegistry();

			AuthVerifier authVerifier = registry.getService(serviceReference);

			if (authVerifier == null) {
				return null;
			}

			Class<?> authVerifierClass = authVerifier.getClass();

			AuthVerifierConfiguration authVerifierConfiguration =
				new AuthVerifierConfiguration();

			authVerifierConfiguration.setAuthVerifier(authVerifier);
			authVerifierConfiguration.setAuthVerifierClassName(
				authVerifierClass.getName());
			authVerifierConfiguration.setProperties(
				_loadProperties(serviceReference, authVerifierClass.getName()));

			if (!_validate(authVerifierConfiguration)) {
				return null;
			}

			_authVerifierConfigurations.add(0, authVerifierConfiguration);

			_rebuildAll();

			return authVerifierConfiguration;
		}

		@Override
		public void modifiedService(
			ServiceReference<AuthVerifier> serviceReference,
			AuthVerifierConfiguration authVerifierConfiguration) {

			_authVerifierConfigurations.remove(authVerifierConfiguration);

			authVerifierConfiguration.setProperties(
				_loadProperties(
					serviceReference,
					authVerifierConfiguration.getAuthVerifierClassName()));

			if (_validate(authVerifierConfiguration)) {
				_authVerifierConfigurations.add(0, authVerifierConfiguration);

				_rebuildAll();
			}
		}

		public void removeAuthVerifierFilter(
			AuthVerifierFilter authVerifierFilter) {

			_authVerifierFilters.remove(authVerifierFilter);
		}

		@Override
		public void removedService(
			ServiceReference<AuthVerifier> serviceReference,
			AuthVerifierConfiguration authVerifierConfiguration) {

			Registry registry = RegistryUtil.getRegistry();

			registry.ungetService(serviceReference);

			_authVerifierConfigurations.remove(authVerifierConfiguration);

			_rebuildAll();
		}

		private Properties _loadProperties(
			ServiceReference<AuthVerifier> serviceReference,
			String authVerifierClassName) {

			Properties properties = new Properties();

			String authVerifierPropertyName =
				AuthVerifierPipeline.getAuthVerifierPropertyName(
					authVerifierClassName);

			Map<String, Object> serviceReferenceProperties =
				serviceReference.getProperties();

			for (Map.Entry<String, Object> entry :
					serviceReferenceProperties.entrySet()) {

				String key = entry.getKey();

				if (key.startsWith(authVerifierPropertyName)) {
					key = key.substring(authVerifierPropertyName.length());
				}

				properties.setProperty(key, String.valueOf(entry.getValue()));
			}

			return properties;
		}

		private void _rebuildAll() {
			for (AuthVerifierFilter authVerifierFilter : _authVerifierFilters) {
				_rebuildFor(authVerifierFilter);
			}
		}

		private void _rebuildFor(AuthVerifierFilter authVerifierFilter) {
			Map<String, List<AuthVerifierConfiguration>>
				excludeAuthVerifierConfigurations = new HashMap<>();

			Map<String, List<AuthVerifierConfiguration>>
				includeAuthVerifierConfigurations = new HashMap<>();

			for (AuthVerifierConfiguration authVerifierConfiguration :
					_authVerifierConfigurations) {

				Properties properties =
					authVerifierConfiguration.getProperties();

				String[] urlsExcludes = StringUtil.split(
					properties.getProperty("urls.excludes"));

				for (String urlsExclude : urlsExcludes) {
					if (!excludeAuthVerifierConfigurations.containsKey(
							urlsExclude)) {

						excludeAuthVerifierConfigurations.put(
							urlsExclude, new ArrayList<>());
					}

					List<AuthVerifierConfiguration>
						excludeAuthVerifierConfiguration =
							excludeAuthVerifierConfigurations.get(urlsExclude);

					excludeAuthVerifierConfiguration.add(
						authVerifierConfiguration);
				}

				String[] urlsIncludes = StringUtil.split(
					properties.getProperty("urls.includes"));

				for (String urlsInclude : urlsIncludes) {
					if (!includeAuthVerifierConfigurations.containsKey(
							urlsInclude)) {

						includeAuthVerifierConfigurations.put(
							urlsInclude, new ArrayList<>());
					}

					List<AuthVerifierConfiguration>
						includeAuthVerifierConfiguration =
							includeAuthVerifierConfigurations.get(urlsInclude);

					includeAuthVerifierConfiguration.add(
						authVerifierConfiguration);
				}
			}

			authVerifierFilter._excludeURLPatternMapper =
				URLPatternMapperFactory.create(
					excludeAuthVerifierConfigurations);
			authVerifierFilter._includeURLPatternMapper =
				URLPatternMapperFactory.create(
					includeAuthVerifierConfigurations);
		}

		private boolean _validate(
			AuthVerifierConfiguration authVerifierConfiguration) {

			Properties properties = authVerifierConfiguration.getProperties();

			String[] urlsIncludes = StringUtil.split(
				properties.getProperty("urls.includes"));

			if (urlsIncludes.length == 0) {
				if (_log.isWarnEnabled()) {
					String authVerifierClassName =
						authVerifierConfiguration.getAuthVerifierClassName();

					_log.warn(
						"Auth verifier " + authVerifierClassName +
							" does not have URLs configured");
				}

				return false;
			}

			return true;
		}

		private static final List<AuthVerifierConfiguration>
			_authVerifierConfigurations = new CopyOnWriteArrayList<>();
		private static final List<AuthVerifierFilter> _authVerifierFilters =
			new CopyOnWriteArrayList<>();

	}

	static {
		_authVerifierServiceTrackerCustomizer =
			new AuthVerifierServiceTrackerCustomizer();

		Registry registry = RegistryUtil.getRegistry();

		Filter filter = registry.getFilter(
			"(objectClass=" + AuthVerifier.class.getName() + ")");

		ServiceTracker<AuthVerifier, AuthVerifierConfiguration>
			authVerifierServiceTracker = registry.trackServices(
				filter, _authVerifierServiceTrackerCustomizer);

		authVerifierServiceTracker.open();
	}

}