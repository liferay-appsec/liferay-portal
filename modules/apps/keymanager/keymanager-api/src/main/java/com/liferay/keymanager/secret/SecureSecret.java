/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.secret;

import com.liferay.keymanager.KeyReference;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;

import javax.security.auth.Destroyable;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
public final class SecureSecret implements AutoCloseable, Destroyable {

	public SecureSecret(byte[] bytes, KeyReference keyReference) {
		if (bytes == null) {
			_bytes = new byte[0];
		}
		else {
			_bytes = Arrays.copyOf(bytes, bytes.length);
		}

		_keyReference = keyReference;
	}

	public SecureSecret(KeyReference keyReference, String value) {
		_keyReference = keyReference;

		if (value == null) {
			_bytes = new byte[0];

			return;
		}

		char[] chars = value.toCharArray();

		try {
			_init(chars);
		}
		finally {
			Arrays.fill(chars, '\0');
		}
	}

	@Override
	public void close() {
		destroy();
	}

	@Override
	public synchronized void destroy() {
		if (_bytes != null) {
			Arrays.fill(_bytes, (byte)0);
		}

		if (_chars != null) {
			Arrays.fill(_chars, '\0');
		}

		_destroyed = true;
	}

	public synchronized byte[] getBytes() {
		if (_destroyed) {
			throw new IllegalArgumentException("Secret is destroyed");
		}

		return _bytes;
	}

	public synchronized char[] getChars() {
		if (_destroyed) {
			throw new IllegalArgumentException("Secret is destroyed");
		}

		if (_chars != null) {
			return _chars;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(_bytes);

		CharsetDecoder charsetDecoder = StandardCharsets.UTF_8.newDecoder();

		long maxChars = (long)Math.ceil(
			(long)_bytes.length * charsetDecoder.maxCharsPerByte());

		if (maxChars > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				"Stored secret is too large to be decoded");
		}

		CharBuffer charBuffer = CharBuffer.allocate((int)maxChars);

		charsetDecoder.decode(byteBuffer, charBuffer, true);
		charsetDecoder.flush(charBuffer);

		charBuffer.flip();

		_chars = new char[charBuffer.remaining()];

		charBuffer.get(_chars);

		Arrays.fill(charBuffer.array(), '\0');

		return _chars;
	}

	public KeyReference getKeyReference() {
		return _keyReference;
	}

	@Override
	public synchronized boolean isDestroyed() {
		return _destroyed;
	}

	private void _init(char[] chars) {
		_chars = Arrays.copyOf(chars, chars.length);

		CharsetEncoder charsetEncoder = StandardCharsets.UTF_8.newEncoder();
		CharBuffer charBuffer = CharBuffer.wrap(chars);

		long maxBytes = (long)Math.ceil(
			(long)chars.length * charsetEncoder.maxBytesPerChar());

		if (maxBytes > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				"Input char array is too large to be encoded");
		}

		ByteBuffer byteBuffer = ByteBuffer.allocate((int)maxBytes);

		charsetEncoder.encode(charBuffer, byteBuffer, true);
		charsetEncoder.flush(byteBuffer);

		byteBuffer.flip();

		_bytes = new byte[byteBuffer.remaining()];

		byteBuffer.get(_bytes);

		Arrays.fill(byteBuffer.array(), (byte)0);
	}

	private volatile byte[] _bytes;
	private volatile char[] _chars;
	private volatile boolean _destroyed;
	private final KeyReference _keyReference;

}