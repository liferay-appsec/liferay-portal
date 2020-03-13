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

package com.liferay.portal.crypto.hash.generator.message.digest.factory;

import com.liferay.portal.crypto.hash.generator.message.digest.MessageDigestHashGenerator;
import com.liferay.portal.crypto.hash.generator.spi.HashGenerator;
import com.liferay.portal.crypto.hash.generator.spi.factory.HashGeneratorFactory;

import org.json.JSONObject;

import org.osgi.service.component.annotations.Component;

/**
 * @author Arthur Chan
 */
@Component(
	immediate = true,
	property = {
		"crypto.hash.generator.name=MD2", "crypto.hash.generator.name=MD5",
		"crypto.hash.generator.name=SHA-1",
		"crypto.hash.generator.name=SHA-224",
		"crypto.hash.generator.name=SHA-256",
		"crypto.hash.generator.name=SHA-384",
		"crypto.hash.generator.name=SHA-512"
	},
	service = HashGeneratorFactory.class
)
public class MessageDigestHashGeneratorFactory implements HashGeneratorFactory {

	@Override
	public HashGenerator create(
			String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject)
		throws Exception {

		return new MessageDigestHashGenerator(hashGeneratorName);
	}

}