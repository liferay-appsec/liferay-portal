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

package com.liferay.portal.crypto.hash.internal.verification.context;

import com.liferay.portal.crypto.hash.flavor.HashFlavor;
import com.liferay.portal.crypto.hash.internal.flavor.HashFlavorImpl;
import com.liferay.portal.crypto.hash.verification.context.HashVerificationContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.Optional;

import org.json.JSONObject;

/**
 * @author Arthur Chan
 */
public class HashVerificationContextImpl implements HashVerificationContext {

	public HashVerificationContextImpl(
		String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject,
		HashFlavor hashFlavor) {

		_hashGeneratorName = hashGeneratorName;
		_hashGeneratorMetaJSONObject = hashGeneratorMetaJSONObject;
		_hashFlavor = hashFlavor;
	}

	@Override
	public HashFlavor getHashFlavor() {
		return _hashFlavor;
	}

	@Override
	public Optional<JSONObject> getHashGeneratorMeta() {
		return Optional.ofNullable(_hashGeneratorMetaJSONObject);
	}

	@Override
	public String getHashGeneratorName() {
		return _hashGeneratorName;
	}

	public static class BuilderImpl implements HashVerificationContext.Builder {

		public BuilderImpl(
			String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject) {

			this(hashGeneratorName, hashGeneratorMetaJSONObject, null, null);
		}

		public BuilderImpl(
			String hashGeneratorName, JSONObject hashGeneratorMetaJSONObject,
			String pepperId, byte[] salt) {

			_hashGeneratorName = hashGeneratorName;
			_hashGeneratorMetaJSONObject = hashGeneratorMetaJSONObject;
			_pepperId = pepperId;
			_salt = salt;
		}

		@Override
		public HashVerificationContext build() {
			HashFlavor hashFlavor = new HashFlavorImpl(_pepperId, _salt);

			return new HashVerificationContextImpl(
				_hashGeneratorName, _hashGeneratorMetaJSONObject, hashFlavor);
		}

		@Override
		public HashVerificationContext build(byte[] serializedHashFlavor)
			throws IOException {

			DataInputStream dataInputStream = new DataInputStream(
				new ByteArrayInputStream(serializedHashFlavor));

			byte b = dataInputStream.readByte();

			if (b != 1) {
				throw new IllegalArgumentException(
					"Version " + b + " is not supported");
			}

			String pepperId = dataInputStream.readUTF();

			int unsignedByte = dataInputStream.readUnsignedShort();

			byte[] salt = new byte[unsignedByte];

			dataInputStream.readFully(salt);

			return new HashVerificationContextImpl(
				_hashGeneratorName, _hashGeneratorMetaJSONObject,
				new HashFlavorImpl(pepperId, salt));
		}

		@Override
		public HashVerificationContext build(HashFlavor hashFlavor) {
			Optional<String> optionalPepperId = hashFlavor.getPepperId();

			Optional<byte[]> optionalSalt = hashFlavor.getSalt();

			return new HashVerificationContextImpl(
				_hashGeneratorName, _hashGeneratorMetaJSONObject,
				new HashFlavorImpl(
					optionalPepperId.orElse(""),
					optionalSalt.orElse(new byte[0])));
		}

		@Override
		public SaltedBuilder pepper(String pepperId) {
			if (pepperId == null) {
				throw new IllegalArgumentException(
					"use other overloaded method if no pepperId is provided");
			}

			return new BuilderImpl(
				_hashGeneratorName, _hashGeneratorMetaJSONObject, pepperId,
				_salt);
		}

		@Override
		public Builder salt(byte[] salt) {
			if (salt == null) {
				throw new IllegalArgumentException(
					"use other overloaded method if no salt is provided");
			}

			return new BuilderImpl(
				_hashGeneratorName, _hashGeneratorMetaJSONObject, _pepperId,
				salt);
		}

		private final JSONObject _hashGeneratorMetaJSONObject;
		private final String _hashGeneratorName;
		private final String _pepperId;
		private final byte[] _salt;

	}

	private final HashFlavor _hashFlavor;
	private final JSONObject _hashGeneratorMetaJSONObject;
	private final String _hashGeneratorName;

}