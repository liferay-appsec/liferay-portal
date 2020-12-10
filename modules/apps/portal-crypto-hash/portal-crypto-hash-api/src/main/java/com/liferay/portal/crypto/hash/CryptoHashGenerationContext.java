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

package com.liferay.portal.crypto.hash;

import java.util.Map;

/**
 * @author Carlos Sierra Andrés
 * @author Arthur Chan
 */
public final class CryptoHashGenerationContext {

	public CryptoHashGenerationContext(
		String cryptoHashProviderName,
		Map<String, ?> cryptoHashProviderProperties,
		SaltCommand... saltCommands) {

		_cryptoHashProviderName = cryptoHashProviderName;
		_cryptoHashProviderProperties = cryptoHashProviderProperties;
		_saltCommands = saltCommands;
	}

	public String getCryptoHashProviderName() {
		return _cryptoHashProviderName;
	}

	public Map<String, ?> getCryptoHashProviderProperties() {
		return _cryptoHashProviderProperties;
	}

	public SaltCommand[] getSaltCommands() {
		return _saltCommands;
	}

	private final String _cryptoHashProviderName;
	private final Map<String, ?> _cryptoHashProviderProperties;
	private final SaltCommand[] _saltCommands;

}