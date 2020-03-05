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
		_pepperId = Optional.ofNullable(pepperId);
		_salt = Optional.ofNullable(salt);
	}

	@Override
	public Optional<String> getPepperId() {
		return _pepperId;
	}

	@Override
	public Optional<byte[]> getSalt() {
		return _salt;
	}

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		DataOutputStream dataOutputStream = new DataOutputStream(
			byteArrayOutputStream);

		//version
		dataOutputStream.writeByte(1);
		dataOutputStream.writeUTF(_pepperId.orElse(""));

		if (_salt.isPresent()) {
			byte[] salt = _salt.get();

			dataOutputStream.writeShort(salt.length);
			dataOutputStream.write(salt);
		}
		else {
			dataOutputStream.writeShort(0);
		}

		dataOutputStream.flush();

		dataOutputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

	private final Optional<String> _pepperId;
	private final Optional<byte[]> _salt;

}