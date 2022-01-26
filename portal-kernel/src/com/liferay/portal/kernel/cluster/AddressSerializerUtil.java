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

package com.liferay.portal.kernel.cluster;

import com.liferay.portal.kernel.io.Deserializer;
import com.liferay.portal.kernel.io.Serializer;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Base64DecodingException;

import java.nio.ByteBuffer;

/**
 * @author Shuyang Zhou
 */
public class AddressSerializerUtil {

	public static Address deserialize(String serializedAddress) {
		try {
			byte[] bytes = Base64.decode(serializedAddress);

			Deserializer deserializer = new Deserializer(
				ByteBuffer.wrap(bytes));

			return deserializer.readObject();
		}
		catch (Base64DecodingException | ClassNotFoundException exception) {
			throw new RuntimeException(
				"Unable to deserialize address " + serializedAddress,
				exception);
		}
	}

	public static String serialize(Address address) {
		Serializer serializer = new Serializer();

		serializer.writeObject(address);

		ByteBuffer byteBuffer = serializer.toByteBuffer();

		return Base64.encode(byteBuffer.array());
	}

}