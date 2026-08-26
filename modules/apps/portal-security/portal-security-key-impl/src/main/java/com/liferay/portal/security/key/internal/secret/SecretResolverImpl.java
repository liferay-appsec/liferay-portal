/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.secret;

import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Victor Silvestre
 */
@Component(service = SecretResolver.class)
public class SecretResolverImpl implements SecretResolver {

	@Override
	public String resolve(long companyId, String value) throws SecretException {
		if (!KeyReferenceUtil.isKeyReference(value)) {
			return value;
		}

		if (!KeyReferenceUtil.isValidKeyReference(value)) {
			throw new SecretException("Unable to parse the key reference");
		}

		KeyReference keyReference = KeyReferenceUtil.toKeyReference(value);

		if (keyReference.getType() != KeyReference.Type.SECRET) {
			throw new SecretException(
				"Crypto key references are not supported by the secret " +
					"resolver");
		}

		try (Secret secret = _secretManager.getSecret(
				companyId, keyReference)) {

			return new String(secret.getChars());
		}
	}

	@Reference
	private SecretManager _secretManager;

}