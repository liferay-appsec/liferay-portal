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

package com.liferay.portal.crypto.hash.internal.generation.context;

import com.liferay.portal.crypto.hash.generation.context.CryptoHashGenerationContext;
import com.liferay.portal.crypto.hash.generation.context.salt.SaltCommand;

import java.util.Map;

/**
 * @author Arthur Chan
 */
public class CryptoHashGenerationContextImpl
	implements CryptoHashGenerationContext {

	public CryptoHashGenerationContextImpl(
		String cryptoHashProviderName,
		Map<String, ?> cryptoHashProviderProperties,
		SaltCommand... saltCommands) {

		_cryptoHashProviderName = cryptoHashProviderName;
		_cryptoHashProviderProperties = cryptoHashProviderProperties;
		_saltCommands = saltCommands;
	}

	@Override
	public String getCryptoHashProviderName() {
		return _cryptoHashProviderName;
	}

	@Override
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