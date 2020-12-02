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

package com.liferay.portal.crypto.hash.internal.context.builder;

import com.liferay.portal.crypto.hash.context.builder.HashContextBuilder;
import com.liferay.portal.crypto.hash.generation.context.HashGenerationContext;
import com.liferay.portal.crypto.hash.internal.generation.context.HashGenerationContextImpl;
import com.liferay.portal.crypto.hash.internal.verification.context.HashVerificationContextImpl;
import com.liferay.portal.crypto.hash.verification.context.HashVerificationContext;

import org.json.JSONObject;

/**
 * @author Arthur Chan
 */
public class HashContextBuilderImpl implements HashContextBuilder {

	public HashContextBuilderImpl(
		String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject) {

		this.hashGeneratorName = hashGeneratorName;
		this.hashGeneratorMetaJSONObject = hashGeneratorMetaJSONObject;
	}

	@Override
	public HashGenerationContext.Builder createHashGenerationContextBuilder() {
		return new HashGenerationContextImpl.BuilderImpl(
			hashGeneratorName, hashGeneratorMetaJSONObject);
	}

	@Override
	public HashVerificationContext.Builder
		createHashVerificationContextBuilder() {

		return new HashVerificationContextImpl.BuilderImpl(
			hashGeneratorName, hashGeneratorMetaJSONObject);
	}

	protected final JSONObject hashGeneratorMetaJSONObject;
	protected final String hashGeneratorName;

}