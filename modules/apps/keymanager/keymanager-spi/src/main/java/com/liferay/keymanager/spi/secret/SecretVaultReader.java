/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.secret;

import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;

import java.util.List;

/**
 * @author Tomas Polesovsky
 * @author Christopher Kian
 */
public interface SecretVaultReader extends SecretVaultProvider {

	public SecureSecret getSecret(long companyId, String identifier)
		throws SecretManagerException;

	public List<String> getSecretIdentifiers(long companyId)
		throws SecretManagerException;

}