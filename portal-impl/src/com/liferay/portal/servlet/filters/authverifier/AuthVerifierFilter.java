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
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.access.control.AccessControlUtil;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierConfiguration;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
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
import java.util.function.Consumer;

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

		if (_preProcess(httpServletRequest, httpServletResponse, filterChain)) {
			return;
		}

		AccessControlUtil.initAccessControlContext(
			httpServletRequest, httpServletResponse, _initParametersMap);

		AccessControlContext accessControlContext =
			AccessControlUtil.getAccessControlContext();

		String requestURI = httpServletRequest.getRequestURI();

		String contextPath = httpServletRequest.getContextPath();

		if (requestURI.equals(contextPath)) {
			requestURI += "/";
		}
		else {
			requestURI = requestURI.substring(contextPath.length());
		}

		Set<AuthVerifierConfiguration> excludeAuthVerifierConfigurations =
			new HashSet<>();

		_excludeURLPatternMapper.consumeValues(
			authVerifierConfigurations ->
				excludeAuthVerifierConfigurations.addAll(
					authVerifierConfigurations),
			requestURI);

		_includeURLPatternMapper.consumeValues(
			new Consumer<List<AuthVerifierConfiguration>>() {

				@Override
				public void accept(
					List<AuthVerifierConfiguration>
						authVerifierConfigurations) {

					if (_foundResult) {
						return;
					}

					for (AuthVerifierConfiguration authVerifierConfiguration :
							authVerifierConfigurations) {

						if (excludeAuthVerifierConfigurations.contains(
								authVerifierConfiguration)) {

							continue;
						}

						AuthVerifierResult authVerifierResult =
							_processMatchedAuthVerifierConfiguration(
								accessControlContext,
								authVerifierConfiguration);

						if (authVerifierResult == null) {
							continue;
						}

						Map<String, Object> settings = _mergeSettings(
							authVerifierConfiguration,
							authVerifierResult.getSettings());

						AuthVerifier authVerifier =
							authVerifierConfiguration.getAuthVerifier();

						settings.put(
							AuthVerifierPipeline.AUTH_TYPE,
							authVerifier.getAuthType());

						authVerifierResult.setSettings(settings);

						accessControlContext.setAuthVerifierResult(
							authVerifierResult);

						_foundResult = true;

						return;
					}
				}

				private boolean _foundResult;

			},
			requestURI);

		AuthVerifierResult authVerifierResult =
			accessControlContext.getAuthVerifierResult();

		if (authVerifierResult == null) {
			accessControlContext.setAuthVerifierResult(
				_createGuestVerificationResult(accessControlContext));
		}

		Map<String, Object> settings = accessControlContext.getSettings();

		authVerifierResult = accessControlContext.getAuthVerifierResult();

		if (authVerifierResult.getSettings() != null) {
			settings.putAll(authVerifierResult.getSettings());
		}

		_postProcess(
			accessControlContext, httpServletRequest, httpServletResponse,
			filterChain);
	}

	private AuthVerifierResult _createGuestVerificationResult(
			AccessControlContext accessControlContext)
		throws Exception {

		AuthVerifierResult authVerifierResult = new AuthVerifierResult();

		authVerifierResult.setState(AuthVerifierResult.State.UNSUCCESSFUL);

		HttpServletRequest httpServletRequest =
			accessControlContext.getRequest();

		long defaultUserId = UserLocalServiceUtil.getDefaultUserId(
			PortalUtil.getCompanyId(httpServletRequest));

		authVerifierResult.setUserId(defaultUserId);

		return authVerifierResult;
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

	private Map<String, Object> _mergeSettings(
		AuthVerifierConfiguration authVerifierConfiguration,
		Map<String, Object> settings) {

		AuthVerifier authVerifier = authVerifierConfiguration.getAuthVerifier();

		Properties properties = authVerifierConfiguration.getProperties();

		Map<String, Object> mergedSettings = new HashMap<>(settings);

		if (properties != null) {
			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				mergedSettings.put((String)entry.getKey(), entry.getValue());
			}
		}

		mergedSettings.put(
			AuthVerifierPipeline.AUTH_TYPE, authVerifier.getAuthType());

		return mergedSettings;
	}

	private void _postProcess(
			AccessControlContext accessControlContext,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		AuthVerifierResult authVerifierResult =
			accessControlContext.getAuthVerifierResult();

		AuthVerifierResult.State state = authVerifierResult.getState();

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

	private boolean _preProcess(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		if (!_isAccessAllowed(httpServletRequest, httpServletResponse)) {
			return true;
		}

		if (_isApplySSL(httpServletRequest, httpServletResponse)) {
			return true;
		}

		if (_isCORSPreflightRequest(httpServletRequest)) {
			Class<?> clazz = getClass();

			processFilter(
				clazz.getName(), httpServletRequest, httpServletResponse,
				filterChain);

			return true;
		}

		return false;
	}

	private AuthVerifierResult _processMatchedAuthVerifierConfiguration(
		AccessControlContext accessControlContext,
		AuthVerifierConfiguration authVerifierConfiguration) {

		AuthVerifier authVerifier = authVerifierConfiguration.getAuthVerifier();

		AuthVerifierResult authVerifierResult = null;

		try {
			authVerifierResult = authVerifier.verify(
				accessControlContext,
				authVerifierConfiguration.getProperties());
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				Class<?> authVerifierClass = authVerifier.getClass();

				_log.debug(
					"Skipping " + authVerifierClass.getName(), exception);
			}

			return null;
		}

		if (authVerifierResult == null) {
			Class<?> authVerifierClass = authVerifier.getClass();

			_log.error(
				"Auth verifier " + authVerifierClass.getName() +
					" did not return an auth verifier result");

			return null;
		}

		if (authVerifierResult.getState() ==
				AuthVerifierResult.State.NOT_APPLICABLE) {

			return null;
		}

		User user = UserLocalServiceUtil.fetchUser(
			authVerifierResult.getUserId());

		if ((user == null) || !user.isActive()) {
			if (_log.isDebugEnabled()) {
				Class<?> authVerifierClass = authVerifier.getClass();

				if (user == null) {
					_log.debug(
						StringBundler.concat(
							"Auth verifier ", authVerifierClass.getName(),
							" returned null user",
							authVerifierResult.getUserId()));
				}
				else {
					_log.debug(
						StringBundler.concat(
							"Auth verifier ", authVerifierClass.getName(),
							" returned inactive user",
							authVerifierResult.getUserId()));
				}
			}

			return null;
		}

		return authVerifierResult;
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

		/**
		 * We have non-standard url-pattern across the portal for AuthVerifiers,
		 * in order to be backward compatible, we need to do runtime fix, like:
		 * "*" will be fixed to "/*"
		 * "/abc*" will be fixed to "/abc/*"
		 *
		 * Notice both "*" and "/abc*" are valid url-pattern, but the ending "*"
		 * is not considered as wildcard by servlet spec.
		 *
		 * In the future, all non-standard url-patterns need rewriting.
		 */
		private String _fixLegacyURLPattern(String urlPattern) {
			if ((urlPattern == null) || (urlPattern.length() == 0)) {
				return urlPattern;
			}

			if (urlPattern.charAt(urlPattern.length() - 1) != '*') {
				return urlPattern;
			}

			if ((urlPattern.length() > 1) &&
				(urlPattern.charAt(urlPattern.length() - 2) == '/')) {

				return urlPattern;
			}

			return urlPattern.substring(0, urlPattern.length() - 1) + "/*";
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

		/**
		 * Because we allow Filter to overwrite authVerifier's properties,
		 * we need to create a new configuration that takes the overwritten
		 * properties instead of authVerifier's original properties.
		 *
		 * The job is used to be done by AuthVerifierPipeline.
		 * _mergeAuthVerifierConfiguration() at a per request basis, move the
		 * routine here would benefit request performance.
		 */
		private AuthVerifierConfiguration _mergeAuthVerifierConfiguration(
			AuthVerifierConfiguration authVerifierConfiguration,
			AuthVerifierFilter authVerifierFilter) {

			String authVerifierPropertyName =
				AuthVerifierPipeline.getAuthVerifierPropertyName(
					authVerifierConfiguration.getAuthVerifierClassName());

			Map<String, Object> filterProperties =
				authVerifierFilter._initParametersMap;

			Properties mergedProperties = new Properties(
				authVerifierConfiguration.getProperties());

			for (Map.Entry<String, Object> propertyEntry :
					filterProperties.entrySet()) {

				String propertyName = propertyEntry.getKey();
				Object propertyValue = propertyEntry.getValue();

				if (propertyName.startsWith(authVerifierPropertyName) &&
					(propertyValue instanceof String)) {

					mergedProperties.setProperty(
						propertyName.substring(
							authVerifierPropertyName.length()),
						(String)propertyValue);
				}
			}

			if (mergedProperties.size() < 1) {
				String portalPropertyProperty = GetterUtil.getString(
					filterProperties.get("portal_property_prefix"));

				if (Validator.isNotNull(portalPropertyProperty)) {
					return authVerifierConfiguration;
				}

				return null;
			}

			AuthVerifierConfiguration mergedAuthVerifierConfiguration =
				new AuthVerifierConfiguration();

			mergedAuthVerifierConfiguration.setAuthVerifier(
				authVerifierConfiguration.getAuthVerifier());
			mergedAuthVerifierConfiguration.setAuthVerifierClassName(
				authVerifierConfiguration.getAuthVerifierClassName());
			mergedAuthVerifierConfiguration.setProperties(mergedProperties);

			return mergedAuthVerifierConfiguration;
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

				authVerifierConfiguration = _mergeAuthVerifierConfiguration(
					authVerifierConfiguration, authVerifierFilter);

				if (authVerifierConfiguration == null) {
					continue;
				}

				Properties properties =
					authVerifierConfiguration.getProperties();

				String[] urlsExcludes = StringUtil.split(
					properties.getProperty("urls.excludes"));

				for (String urlsExclude : urlsExcludes) {
					urlsExclude = _fixLegacyURLPattern(urlsExclude);

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
					urlsInclude = _fixLegacyURLPattern(urlsInclude);

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