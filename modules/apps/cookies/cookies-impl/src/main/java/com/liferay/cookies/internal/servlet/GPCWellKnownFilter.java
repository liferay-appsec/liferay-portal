/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.internal.servlet;

import com.liferay.cookies.configuration.CookiesConfigurationProvider;
import com.liferay.cookies.internal.configuration.admin.service.CookiesPreferenceHandlingManagedServiceFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.servlet.filters.BasePortalFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.Objects;

import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Moura
 */
@Component(
	property = {
		"dispatcher=REQUEST", "servlet-context-name=",
		"servlet-filter-name=GPC Well-Known Filter",
		"url-pattern=/.well-known/gpc.json"
	},
	service = Filter.class
)
public class GPCWellKnownFilter extends BasePortalFilter {

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		long companyId = CompanyThreadLocal.getCompanyId();

		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-75064")) {
			filterChain.doFilter(httpServletRequest, httpServletResponse);

			return;
		}

		if (!Objects.equals(httpServletRequest.getMethod(), "GET")) {
			httpServletResponse.setHeader("Allow", "GET");
			httpServletResponse.sendError(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED);

			return;
		}

		JSONObject jsonObject = _buildResponse(companyId);

		httpServletResponse.setCharacterEncoding(StringPool.UTF8);
		httpServletResponse.setContentType(ContentTypes.APPLICATION_JSON);
		httpServletResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);

		try (PrintWriter printWriter = httpServletResponse.getWriter()) {
			printWriter.write(jsonObject.toString());
		}
	}

	private JSONObject _buildResponse(long companyId) {
		boolean enabled =
			_cookiesConfigurationProvider.
				isCookiesPreferenceHandlingGlobalPrivacyControlEnabled(
					ExtendedObjectClassDefinition.Scope.COMPANY, companyId);

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		if (!enabled) {
			return jsonObject.put("gpc", false);
		}

		jsonObject.put("gpc", true);

		String lastUpdate = _getLastUpdate(companyId);

		if (lastUpdate != null) {
			jsonObject.put("lastUpdate", lastUpdate);
		}

		return jsonObject;
	}

	private String _getLastUpdate(long companyId) {
		CookiesPreferenceHandlingManagedServiceFactory
			cookiesPreferenceHandlingManagedServiceFactory =
				(CookiesPreferenceHandlingManagedServiceFactory)
					_managedServiceFactory;

		long modifiedDate =
			cookiesPreferenceHandlingManagedServiceFactory.
				getCompanyModifiedDate(companyId);

		if (modifiedDate <= 0) {
			return null;
		}

		LocalDate localDate = Instant.ofEpochMilli(
			modifiedDate
		).atZone(
			ZoneOffset.UTC
		).toLocalDate();

		return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	@Reference
	private CookiesConfigurationProvider _cookiesConfigurationProvider;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference(
		target = "(component.name=com.liferay.cookies.internal.configuration.admin.service.CookiesPreferenceHandlingManagedServiceFactory)"
	)
	private ManagedServiceFactory _managedServiceFactory;

}