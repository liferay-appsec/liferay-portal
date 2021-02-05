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
import com.liferay.portal.crypto.hash.provider.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.spi.factory.CryptoHashProviderFactory;
import com.liferay.portal.crypto.hash.provider.spi.salt.VariableSizeSaltProvider;

import java.security.MessageDigest;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@Component(service = CryptoHashGenerator.class)
public class CryptoHashGeneratorImpl implements CryptoHashGenerator {

	@Override
	public CryptoHashResponse generate(
			byte[] input,
			CryptoHashGenerationContext cryptoHashGenerationContext)
		throws CryptoHashException {

		CryptoHashProvider cryptoHashProvider = null;

		try {
			cryptoHashProvider = _getCryptoHashProvider(
				cryptoHashGenerationContext.getCryptoHashProviderName(),
				cryptoHashGenerationContext.getCryptoHashProviderProperties());
		}
		catch (Exception exception) {
			throw new CryptoHashException(exception);
		}

		// Implementation for pepper will come in next iteration.
		// In this iteration, we will deal with salt first.

		int saltSize = cryptoHashGenerationContext.getSaltSize();

		byte[] salt = null;

		if (saltSize < 1) {
			salt = cryptoHashProvider.generateSalt();
		}
		else if (cryptoHashProvider instanceof VariableSizeSaltProvider) {
			VariableSizeSaltProvider variableSizeSaltProvider =
				(VariableSizeSaltProvider)cryptoHashProvider;

			salt = variableSizeSaltProvider.generateSalt(saltSize);
		}
		else {
			throw new CryptoHashException(
				cryptoHashGenerationContext.getCryptoHashProviderName() +
					"can not generate variable size salt.");
		}

		try {
			cryptoHashProvider.setSalt(salt);

			byte[] hash = cryptoHashProvider.hash(input);

			return new CryptoHashResponse(hash, salt);
		}
		catch (Exception exception) {
			throw new CryptoHashException(exception);
		}
	}

	@Override
	public boolean verify(
			byte[] input, byte[] hash,
			CryptoHashVerificationContext... cryptoHashVerificationContexts)
		throws CryptoHashException {

		// Each password may get hashed multiple times using generate() method,
		// thus each password may have a list of hashing contexts including
		// different combination of salt, pepper, and hash algorithm.

		for (CryptoHashVerificationContext cryptoHashVerificationContext :
				cryptoHashVerificationContexts) {

			CryptoHashProvider cryptoHashProvider = null;

			try {
				cryptoHashProvider = _getCryptoHashProvider(
					cryptoHashVerificationContext.getCryptoHashProviderName(),
					cryptoHashVerificationContext.
						getCryptoHashProviderProperties());
			}
			catch (Exception exception) {
				throw new CryptoHashException(exception);
			}

			// Implementation for pepper will come in next iteration.
			// In this iteration, we will deal with salt first.

			Optional<byte[]> optionalSalt =
				cryptoHashVerificationContext.getSaltOptional();

			try {
				optionalSalt.ifPresent(cryptoHashProvider::setSalt);

				input = cryptoHashProvider.hash(input);
			}
			catch (Exception exception) {
				throw new CryptoHashException(exception);
			}
		}

		return MessageDigest.isEqual(input, hash);
	}

	private CryptoHashProvider _getCryptoHashProvider(
			String cryptoHashProviderName,
			Map<String, ?> cryptoHashProviderProperties)
		throws Exception {

		CryptoHashProviderFactory cryptoHashProviderFactory =
			_cryptoHashProviderFactoryRegistry.getCryptoHashProviderFactory(
				cryptoHashProviderName);

		return cryptoHashProviderFactory.create(
			cryptoHashProviderName, cryptoHashProviderProperties);
	}

	@Reference
	private CryptoHashProviderFactoryRegistry
		_cryptoHashProviderFactoryRegistry;

}