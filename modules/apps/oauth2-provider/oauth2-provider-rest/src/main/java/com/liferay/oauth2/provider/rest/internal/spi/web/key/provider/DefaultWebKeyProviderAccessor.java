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

package com.liferay.oauth2.provider.rest.internal.spi.web.key.provider;

import com.liferay.oauth2.provider.rest.spi.web.key.provider.WebKeyProvider;
import com.liferay.oauth2.provider.rest.spi.web.key.provider.WebKeyProviderAccessor;
import com.liferay.oauth2.provider.scope.liferay.ScopedServiceTrackerMap;
import com.liferay.oauth2.provider.scope.liferay.ScopedServiceTrackerMapFactory;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Stian Sigvartsen
 */
@Component(immediate = true, service = WebKeyProviderAccessor.class)
public class DefaultWebKeyProviderAccessor implements WebKeyProviderAccessor {

	@Override
	public WebKeyProvider getWebKeyProvider(long companyId, String issuer) {
		return _scopedServiceTrackerMap.getService(companyId, issuer);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_scopedServiceTrackerMap = _scopedServiceTrackerMapFactory.create(
			bundleContext, WebKeyProvider.class, "liferay.oauth2.issuer",
			() -> _defaultWebKeyProvider);
	}

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY, target = "(name=default)"
	)
	private volatile WebKeyProvider _defaultWebKeyProvider;

	private ScopedServiceTrackerMap<WebKeyProvider> _scopedServiceTrackerMap;

	@Reference
	private ScopedServiceTrackerMapFactory _scopedServiceTrackerMapFactory;

}