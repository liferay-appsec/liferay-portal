/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.nio.ByteBuffer;

import java.util.Arrays;

/**
 * @author Christopher Kian
 */
public class AWSByteBufferUtil {

	/**
	 * Copies the buffer contents into a fresh byte[] and best-effort zeroes the
	 * underlying backing array. Direct buffers and read-only buffers do not
	 * expose their backing array; for those, the source bytes remain in
	 * memory until the JVM reclaims them. A warning is logged so the caller is
	 * aware the wipe was skipped.
	 */
	public static byte[] consumeAndZero(ByteBuffer byteBuffer) {
		if (byteBuffer == null) {
			return new byte[0];
		}

		byte[] bytes = new byte[byteBuffer.remaining()];

		byteBuffer.get(bytes);

		if (byteBuffer.hasArray() && !byteBuffer.isReadOnly()) {
			int arrayOffset = byteBuffer.arrayOffset();

			Arrays.fill(
				byteBuffer.array(), arrayOffset,
				arrayOffset + byteBuffer.limit(), (byte)0);
		}
		else if (_log.isWarnEnabled()) {
			_log.warn(
				"Unable to zero AWS response buffer (direct or read-only); " +
					"sensitive bytes remain until JVM reclaims them");
		}

		return bytes;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AWSByteBufferUtil.class);

}