/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Christopher Kian
 */
public class KeyReferenceUtil {

	public static boolean isKeyReference(String value) {
		if ((value != null) &&
			(value.startsWith(_REFERENCE_PREFIX_CRYPTO) ||
			 value.startsWith(_REFERENCE_PREFIX_SECRET))) {

			return true;
		}

		return false;
	}

	public static KeyReference toKeyReference(String referenceString) {
		String prefix = _REFERENCE_PREFIX_SECRET;
		KeyReference.Type type = KeyReference.Type.SECRET;

		if (referenceString.startsWith(_REFERENCE_PREFIX_CRYPTO)) {
			prefix = _REFERENCE_PREFIX_CRYPTO;
			type = KeyReference.Type.CRYPTO;
		}

		String[] providerIdAndIdentifier = StringUtil.split(
			referenceString.substring(
				prefix.length(), referenceString.length() - 1),
			CharPool.COLON);

		return new KeyReference(
			providerIdAndIdentifier[1], providerIdAndIdentifier[0], type);
	}

	public static String toReferenceString(KeyReference keyReference) {
		String prefix = _REFERENCE_PREFIX_SECRET;

		if (keyReference.getType() == KeyReference.Type.CRYPTO) {
			prefix = _REFERENCE_PREFIX_CRYPTO;
		}

		return StringBundler.concat(
			prefix, keyReference.getProviderId(), StringPool.COLON,
			keyReference.getIdentifier(), StringPool.CLOSE_CURLY_BRACE);
	}

	private static final String _REFERENCE_PREFIX_CRYPTO = "${keyRef:";

	private static final String _REFERENCE_PREFIX_SECRET = "${secretRef:";

}