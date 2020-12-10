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

/**
 * @author Carlos Sierra Andrés
 * @author Arthur Chan
 */
public final class CryptoHashGenerationContext {

	public CryptoHashGenerationContext(String cryptoHashProviderName) {
		_cryptoHashProviderName = cryptoHashProviderName;

		_saltSize = -1;
	}

	public CryptoHashGenerationContext(
		String cryptoHashProviderName, int saltSize) {

		_cryptoHashProviderName = cryptoHashProviderName;
		_saltSize = saltSize;
	}

	public String getCryptoHashProviderName() {
		return _cryptoHashProviderName;
	}

	public int getSaltSize() {
		return _saltSize;
	}

	private final String _cryptoHashProviderName;
	private final int _saltSize;

}