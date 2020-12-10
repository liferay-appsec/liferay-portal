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

package com.liferay.portal.crypto.hash.internal;

import com.liferay.portal.crypto.hash.CryptoHashGenerationContext;
import com.liferay.portal.crypto.hash.CryptoHashGenerator;
import com.liferay.portal.crypto.hash.CryptoHashResponse;
import com.liferay.portal.crypto.hash.CryptoHashVerificationContext;
import com.liferay.portal.crypto.hash.exception.CryptoHashException;
import com.liferay.portal.crypto.hash.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.spi.CryptoHashProviderResponse;
import com.liferay.portal.crypto.hash.spi.VariableSizeSaltProvider;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
public class CryptoHashGeneratorImpl implements CryptoHashGenerator {

	@Override
	public CryptoHashResponse generate(
			byte[] input,
			CryptoHashGenerationContext cryptoHashGenerationContext)
		throws CryptoHashException {

		CryptoHashProvider cryptoHashProvider =
			CryptoHashProviderRegistry.getCryptoHashProvider(
				cryptoHashGenerationContext.getCryptoHashProviderName());

		byte[] salt = cryptoHashProvider.generateSalt();

		if (cryptoHashGenerationContext.getSaltSize() > 0) {
			if (!(cryptoHashProvider instanceof VariableSizeSaltProvider)) {
				throw new CryptoHashException(
					cryptoHashGenerationContext.getCryptoHashProviderName() +
						" does not support to generate variable size salt.");
			}

			VariableSizeSaltProvider variableSizeSaltProvider =
				(VariableSizeSaltProvider)cryptoHashProvider;

			salt = variableSizeSaltProvider.generateSalt(
				cryptoHashGenerationContext.getSaltSize());
		}

		CryptoHashProviderResponse cryptoHashProviderResponse =
			cryptoHashProvider.generate(salt, input);

		return new CryptoHashResponse(
			new CryptoHashVerificationContext(
				cryptoHashProviderResponse.getCryptoHashProviderName(), salt),
			cryptoHashProviderResponse.getHash());
	}

}