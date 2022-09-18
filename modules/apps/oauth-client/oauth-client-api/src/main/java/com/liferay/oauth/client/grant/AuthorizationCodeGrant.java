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

package com.liferay.oauth.client.grant;

import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Arthur Chan
 */
public class AuthorizationCodeGrant implements Grant {

	public static final String GRANT_TYPE = "authorization_code";

	public AuthorizationCodeGrant(
		String authorizationCode, String codeVerifier, String redirectURI) {

		_authorizationCode = authorizationCode;
		_codeVerifier = codeVerifier;
		_redirectURI = redirectURI;
	}

	@Override
	public String getGrantType() {
		return GRANT_TYPE;
	}

	@Override
	public Map<String, List<String>> toParameters() {
		HashMapBuilder.HashMapWrapper hashMapWrapper = HashMapBuilder.create(
			4
		).put(
			"code", Collections.singletonList(_authorizationCode)
		).put(
			"grant_type", Collections.singletonList(getGrantType())
		);

		if (_codeVerifier != null) {
			hashMapWrapper.put(
				"code_verifier", Collections.singletonList(_codeVerifier));
		}

		if (_redirectURI != null) {
			hashMapWrapper.put(
				"redirect_uri", Collections.singletonList(_redirectURI));
		}

		return hashMapWrapper.build();
	}

	private AuthorizationCodeGrant() {
	}

	private String _authorizationCode;
	private String _codeVerifier;
	private String _redirectURI;

}