/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.secret;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.key.KeyReference;

/**
 * @author Christopher Kian
 */
public class SecretReferenceCodec {

	public static boolean isSecretReference(String value) {
		if ((value != null) && value.startsWith(_SECRET_REFERENCE_PREFIX)) {
			return true;
		}

		return false;
	}

	public static KeyReference toKeyReference(String secretReference) {
		String[] providerIdAndIdentifier = StringUtil.split(
			secretReference.substring(
				_SECRET_REFERENCE_PREFIX.length(),
				secretReference.length() - 1),
			CharPool.COLON);

		return new KeyReference(
			providerIdAndIdentifier[1], providerIdAndIdentifier[0],
			KeyReference.Type.SECRET);
	}

	public static String toSecretReference(KeyReference keyReference) {
		return StringBundler.concat(
			_SECRET_REFERENCE_PREFIX, keyReference.getProviderId(),
			StringPool.COLON, keyReference.getIdentifier(),
			StringPool.CLOSE_CURLY_BRACE);
	}

	private static final String _SECRET_REFERENCE_PREFIX = "${secretRef:";

}