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

import java.util.Optional;

import org.json.JSONObject;

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
	public String toString() {
		JSONObject jsonObject = new JSONObject();

		if (_pepperId.isPresent()) {
			jsonObject.put("pepperId", _pepperId.get());
		}

		if (_salt.isPresent()) {
			jsonObject.put("salt", _salt.get());
		}

		return jsonObject.toString();
	}

	private final Optional<String> _pepperId;
	private final Optional<byte[]> _salt;

}