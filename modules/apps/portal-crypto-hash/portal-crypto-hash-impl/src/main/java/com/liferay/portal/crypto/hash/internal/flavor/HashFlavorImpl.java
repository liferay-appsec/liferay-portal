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

package com.liferay.portal.crypto.hash.internal.flavor;

import com.liferay.portal.crypto.hash.flavor.HashFlavor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Optional;

/**
 * @author Arthur Chan
 */
public class HashFlavorImpl implements HashFlavor {

	public HashFlavorImpl(String pepperId, byte[] salt) {
		_pepperId = pepperId;
		_salt = salt;
	}

	@Override
	public Optional<String> getPepperId() {
		return Optional.ofNullable(_pepperId);
	}

	@Override
	public Optional<byte[]> getSalt() {
		return Optional.ofNullable(_salt);
	}

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		DataOutputStream dataOutputStream = new DataOutputStream(
			byteArrayOutputStream);

		//version
		dataOutputStream.writeByte(1);

		if (_pepperId != null) {
			dataOutputStream.writeUTF(_pepperId);
		}
		else {
			dataOutputStream.writeUTF("");
		}

		if (_salt != null) {
			dataOutputStream.writeShort(_salt.length);
			dataOutputStream.write(_salt);
		}
		else {
			dataOutputStream.writeShort(0);
		}

		dataOutputStream.flush();

		dataOutputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

	private final String _pepperId;
	private final byte[] _salt;

}