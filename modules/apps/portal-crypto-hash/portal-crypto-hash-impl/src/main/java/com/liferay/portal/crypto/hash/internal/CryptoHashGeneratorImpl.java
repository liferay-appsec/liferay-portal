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

import com.liferay.portal.crypto.hash.CryptoHashGenerator;
import com.liferay.portal.crypto.hash.CryptoHashResponse;
import com.liferay.portal.crypto.hash.exception.CryptoHashException;
import com.liferay.portal.crypto.hash.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.spi.factory.CryptoHashProviderFactory;

import java.security.MessageDigest;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@Component(service = CryptoHashGenerator.class)
public class CryptoHashGeneratorImpl implements CryptoHashGenerator {

	@Override
	public CryptoHashResponse generate(byte[] input)
		throws CryptoHashException {

		CryptoHashProvider messageDigestCryptoHashProvider;

		// In next iteration, hash provider information will be passed in as
		// generation context.

		try {
			messageDigestCryptoHashProvider = _getCryptoHashProvider(
				"SHA-256", null);
		}
		catch (Exception exception) {
			throw new CryptoHashException(exception);
		}

		byte[] salt = messageDigestCryptoHashProvider.generateSalt();

		return new CryptoHashResponse(
			messageDigestCryptoHashProvider.generate(salt, input), salt);
	}

	@Override
	public boolean verify(byte[] input, byte[] hash, byte[] salt)
		throws CryptoHashException {

		CryptoHashProvider messageDigestCryptoHashProvider;

		// In next iteration, hash provider information will be passed in as
		// verification context.

		try {
			messageDigestCryptoHashProvider = _getCryptoHashProvider(
				"SHA-256", null);
		}
		catch (Exception exception) {
			throw new CryptoHashException(exception);
		}

		return MessageDigest.isEqual(
			messageDigestCryptoHashProvider.generate(salt, input), hash);
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