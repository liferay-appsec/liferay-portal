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

import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.oauth.client.persistence.service.base.OAuthClientASMetadataLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Arthur Chan
 */
@Component(
	property = "model.class.name=com.liferay.oauth.client.persistence.model.OAuthClientASMetadata",
	service = AopService.class
)
public class OAuthClientASMetadataLocalServiceImpl
	extends OAuthClientASMetadataLocalServiceBaseImpl {

	@Override
	public OAuthClientASMetadata fetchOAuthClientASMetadata(
		long companyId, String issuer) {

		return oAuthClientASMetadataPersistence.fetchByC_I(companyId, issuer);
	}

	@Override
	public List<OAuthClientASMetadata> getOAuthClientASMetadatas(
		long companyId) {

		return oAuthClientASMetadataPersistence.findByCompanyId(companyId);
	}

	@Override
	public List<OAuthClientASMetadata> getOAuthClientASMetadatas(
		long companyId, long userId) {

		return oAuthClientASMetadataPersistence.findByC_U(companyId, userId);
	}

}