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

package com.liferay.portal.kernel.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Tomas Polesovsky
 * @author Olivér Kecskeméty
 */
public class Base64Test {

	@Test
	public void testBase64Consistency() throws Base64DecodingException {
		byte[] decoded = Base64.decode(Base64.encode(_BYTES));

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testBase64URLConsistency() throws Base64DecodingException {
		byte[] decoded = Base64.decodeFromURL(Base64.encodeToURL(_BYTES));

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testDecode() throws Base64DecodingException {
		byte[] decoded = Base64.decode(_BYTES_BASE64);

		Assert.assertArrayEquals(_BYTES, decoded);

		decoded = Base64.decode(
			CharPool.SPACE + _BYTES_BASE64 + CharPool.SPACE);

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testDecodeFromURL() throws Base64DecodingException {
		byte[] decoded = Base64.decodeFromURL(_BYTES_BASE64URL);

		Assert.assertArrayEquals(_BYTES, decoded);

		decoded = Base64.decodeFromURL(
			CharPool.SPACE + _BYTES_BASE64URL + CharPool.SPACE);

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testDecodeFromURLBackwardsCompatible()
		throws Base64DecodingException {

		Assert.assertTrue(
			StringUtil.endsWith(_BYTES_BASE64URL, StringPool.EQUAL));

		String liferayCustomBase64URL = StringUtil.replace(
			_BYTES_BASE64URL, CharPool.EQUAL, CharPool.STAR);

		byte[] decoded = Base64.decodeFromURL(liferayCustomBase64URL);

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testDecodeFromURLInvalidInput() throws Base64DecodingException {
		byte[] decoded = Base64.decodeFromURL(null);

		Assert.assertTrue(decoded.length == 0);

		decoded = Base64.decode("   ");

		Assert.assertTrue(decoded.length == 0);

		Assert.assertThrows(
			Base64DecodingException.class,
			() -> Base64.decodeFromURL("!@#$%^&*()<>"));

		Assert.assertThrows(
			Base64DecodingException.class, () -> Base64.decodeFromURL("A"));

		Assert.assertThrows(
			Base64DecodingException.class, () -> Base64.decodeFromURL("A==="));
	}

	@Ignore
	@Test
	public void testDecodeFromURLWithNoPadding()
		throws Base64DecodingException {

		Assert.assertTrue(
			StringUtil.endsWith(_BYTES_BASE64URL, StringPool.EQUAL));

		byte[] decoded = Base64.decodeFromURL(
			StringUtil.removeChar(_BYTES_BASE64URL, CharPool.EQUAL));

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testDecodeInvalidInputs() throws Base64DecodingException {
		byte[] decoded = Base64.decode(null);

		Assert.assertTrue(decoded.length == 0);

		decoded = Base64.decode("   ");

		Assert.assertTrue(decoded.length == 0);

		Assert.assertThrows(
			Base64DecodingException.class, () -> Base64.decode("!@#$%^&*()<>"));

		Assert.assertThrows(
			Base64DecodingException.class, () -> Base64.decode("A"));

		Assert.assertThrows(
			Base64DecodingException.class, () -> Base64.decode("A==="));
	}

	@Ignore
	@Test
	public void testDecodeMime() throws Base64DecodingException {
		byte[] decoded = Base64.decode(_BYTES_BASE64MIME);

		Assert.assertArrayEquals(_BYTES, decoded);
	}

	@Test
	public void testEncode() {
		String encoded = Base64.encode(_BYTES);

		Assert.assertEquals(_BYTES_BASE64, encoded);
	}

	@Test
	public void testEncodeToURL() {
		String encoded = Base64.encodeToURL(_BYTES);

		String liferayCustomBase64URL = StringUtil.replace(
			_BYTES_BASE64URL, CharPool.EQUAL, CharPool.STAR);

		Assert.assertEquals(liferayCustomBase64URL, encoded);
	}

	private static final byte[] _BYTES = new byte[256];

	private static final String _BYTES_BASE64;

	private static final String _BYTES_BASE64MIME;

	private static final String _BYTES_BASE64URL;

	static {
		for (int i = 0; i < 256; i++) {
			_BYTES[i] = (byte)i;
		}

		java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

		_BYTES_BASE64 = encoder.encodeToString(_BYTES);

		java.util.Base64.Encoder urlEncoder = java.util.Base64.getUrlEncoder();

		_BYTES_BASE64URL = urlEncoder.encodeToString(_BYTES);

		java.util.Base64.Encoder mimeEncoder = java.util.Base64.getMimeEncoder(
			80, new byte[] {CharPool.RETURN, CharPool.NEW_LINE});

		_BYTES_BASE64MIME = mimeEncoder.encodeToString(_BYTES);
	}

}