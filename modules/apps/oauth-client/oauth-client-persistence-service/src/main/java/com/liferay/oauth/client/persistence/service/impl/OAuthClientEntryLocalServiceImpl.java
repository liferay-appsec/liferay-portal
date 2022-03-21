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

package com.liferay.oauth.client.persistence.service.impl;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.base.OAuthClientEntryLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Arthur Chan
 */
@Component(
	property = "model.class.name=com.liferay.oauth.client.persistence.model.OAuthClientEntry",
	service = AopService.class
)
public class OAuthClientEntryLocalServiceImpl
	extends OAuthClientEntryLocalServiceBaseImpl {

	@Override
	public OAuthClientEntry fetchOAuthClientEntry(
		long companyId, String asMetadataIssuer, String clientId) {

		return oAuthClientEntryPersistence.fetchByC_A_C(
			companyId, asMetadataIssuer, clientId);
	}

	@Override
	public List<OAuthClientEntry> getOAuthClientEntries(long companyId) {
		return oAuthClientEntryPersistence.findByCompanyId(companyId);
	}

	@Override
	public List<OAuthClientEntry> getOAuthClientEntries(
		long companyId, long userId) {

		return oAuthClientEntryPersistence.findByC_U(companyId, userId);
	}

	@Override
	public List<OAuthClientEntry> getOAuthClientEntries(
		long companyId, String asMetadataIssuer) {

		return oAuthClientEntryPersistence.findByC_A(
			companyId, asMetadataIssuer);
	}

}