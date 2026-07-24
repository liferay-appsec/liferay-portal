/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.test;

import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.security.key.spi.ProviderStatus;
import com.liferay.portal.security.key.spi.secret.SecretProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

/**
 * @author Christopher Kian
 */
@Component(
	property = "secret.provider.id=" + TestSecretProvider.PROVIDER_ID,
	service = SecretProvider.class
)
public class TestSecretProvider implements SecretProvider {

	public static final String PROVIDER_ID = "test-oauth2-secret";

	@Override
	public void deleteSecret(long companyId, String secretIdentifier) {
		_secrets.remove(_key(companyId, secretIdentifier));
	}

	@Override
	public ProviderStatus getProviderStatus() {
		return ProviderStatus.OPERATIONAL;
	}

	@Override
	public Secret getSecret(long companyId, String secretIdentifier)
		throws SecretException {

		String value = _secrets.get(_key(companyId, secretIdentifier));

		if (value == null) {
			throw new SecretException(
				"No secret found for identifier " + secretIdentifier);
		}

		return new Secret(
			new KeyReference(
				secretIdentifier, PROVIDER_ID, KeyReference.Type.SECRET),
			value);
	}

	@Override
	public List<String> getSecretIdentifiers(long companyId) {
		List<String> secretIdentifiers = new ArrayList<>();

		String prefix = companyId + "/";

		for (String key : _secrets.keySet()) {
			if (key.startsWith(prefix)) {
				secretIdentifiers.add(key.substring(prefix.length()));
			}
		}

		return secretIdentifiers;
	}

	@Override
	public boolean isAllowedCompany(long companyId) {
		return true;
	}

	@Override
	public void putSecret(long companyId, Secret secret) {
		KeyReference keyReference = secret.getKeyReference();

		_secrets.put(
			_key(companyId, keyReference.getIdentifier()),
			new String(secret.getChars()));
	}

	private String _key(long companyId, String secretIdentifier) {
		return companyId + "/" + secretIdentifier;
	}

	private final Map<String, String> _secrets = new ConcurrentHashMap<>();

}