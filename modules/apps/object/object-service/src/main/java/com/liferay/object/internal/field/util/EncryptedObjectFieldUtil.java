/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.field.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.security.Key;

/**
 * @author Manuele Castro
 */
public class EncryptedObjectFieldUtil {

	public static String decrypt(Encryptor encryptor, Key key, String value)
		throws EncryptorException {

		if (Validator.isNull(value)) {
			return value;
		}

		if (!isAESKey(key)) {
			return encryptor.decrypt(key, _getCiphertext(value));
		}

		if (_isMarked(value)) {
			return encryptor.decryptAuthenticated(key, _getCiphertext(value));
		}

		return encryptor.decrypt(key, value);
	}

	public static String encrypt(Encryptor encryptor, Key key, String value)
		throws EncryptorException {

		if (!isAESKey(key)) {
			return encryptor.encrypt(key, value);
		}

		return StringBundler.concat(
			_MARKER_PREFIX, encryptor.encryptAuthenticated(key, value),
			StringPool.CLOSE_CURLY_BRACE);
	}

	public static boolean isAESKey(Key key) {
		return StringUtil.equalsIgnoreCase(key.getAlgorithm(), "AES");
	}

	private static String _getCiphertext(String value) {
		if (!_isMarked(value)) {
			return value;
		}

		return value.substring(
			_MARKER_PREFIX.length(),
			value.length() - StringPool.CLOSE_CURLY_BRACE.length());
	}

	private static boolean _isMarked(String value) {
		if (value.startsWith(_MARKER_PREFIX) &&
			value.endsWith(StringPool.CLOSE_CURLY_BRACE)) {

			return true;
		}

		return false;
	}

	private static final String _MARKER_PREFIX = "${aesGcm:v1:";

}