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

package com.liferay.portal.crypto.hash.generator.spi.factory;

import com.liferay.portal.crypto.hash.generator.spi.HashGenerator;

import org.json.JSONObject;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * @author Arthur Chan
 */
@ConsumerType
public interface HashGeneratorFactory {

	/**
	 * Construct a {@link HashGenerator} from given hash generator information.
	 *
	 * @param hashGeneratorName The name of the generator
	 * @param hashGeneratorMetaJSONObject A JSON Object of meta info required by some algorithms
	 * @return An instance of HashGenerator
	 */
	public HashGenerator create(
			String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject)
		throws Exception;

}