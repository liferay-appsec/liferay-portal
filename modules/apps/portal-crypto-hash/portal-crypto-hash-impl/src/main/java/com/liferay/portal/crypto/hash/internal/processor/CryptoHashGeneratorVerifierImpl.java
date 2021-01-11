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

package com.liferay.portal.crypto.hash.internal.processor;

import com.liferay.portal.crypto.hash.generation.CryptoHashGenerationResponse;
import com.liferay.portal.crypto.hash.generation.CryptoHashGenerator;
import com.liferay.portal.crypto.hash.internal.pepper.storage.DummyCryptoHashPepperStorage;
import com.liferay.portal.crypto.hash.pepper.storage.spi.CryptoHashPepperStorage;
import com.liferay.portal.crypto.hash.provider.spi.CryptoHashProvider;
import com.liferay.portal.crypto.hash.provider.spi.CryptoHashProviderResponse;
import com.liferay.portal.crypto.hash.provider.spi.factory.CryptoHashProviderFactory;
import com.liferay.portal.crypto.hash.verification.CryptoHashVerificationContext;
import com.liferay.portal.crypto.hash.verification.CryptoHashVerifier;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@Component(
	configurationPid = "com.liferay.portal.crypto.hash.configuration.CryptoHashProcessorConfiguration",
	configurationPolicy = ConfigurationPolicy.REQUIRE,
	service = {CryptoHashGenerator.class, CryptoHashVerifier.class}
)
public class CryptoHashGeneratorVerifierImpl
	implements CryptoHashGenerator, CryptoHashVerifier {

	@Override
	public CryptoHashGenerationResponse generate(byte[] input)
		throws Exception {

		byte[] pepper = null;
		String pepperId = null;

		if (!(_cryptoHashPepperStorage instanceof
				DummyCryptoHashPepperStorage)) {

			pepperId = _cryptoHashPepperStorage.getCurrentPepperId();

			pepper = _cryptoHashPepperStorage.getPepper(pepperId);
		}

		final CryptoHashProviderResponse cryptoHashProviderResponse =
			_cryptoHashProvider.generate(
				pepper, _cryptoHashProvider.generateSalt(), input);

		return new CryptoHashGenerationResponse(
			cryptoHashProviderResponse.getHash(),
			new CryptoHashVerificationContext(
				pepperId, cryptoHashProviderResponse.getSalt(),
				cryptoHashProviderResponse.getCryptoHashProviderName(),
				cryptoHashProviderResponse.getCryptoHashProviderProperties()));
	}

	@Override
	public boolean verify(
			byte[] input, byte[] hash,
			CryptoHashVerificationContext... cryptoHashVerificationContexts)
		throws Exception {

		for (CryptoHashVerificationContext cryptoHashVerificationContext :
				cryptoHashVerificationContexts) {

			CryptoHashProvider cryptoHashProvider = _getCryptoHashProvider(
				cryptoHashVerificationContext.getCryptoHashProviderName(),
				cryptoHashVerificationContext.
					getCryptoHashProviderProperties());

			// process pepper

			byte[] pepper = null;

			if (!(_cryptoHashPepperStorage instanceof
					DummyCryptoHashPepperStorage)) {

				Optional<String> optionalPepperId =
					cryptoHashVerificationContext.getPepperId();

				pepper = optionalPepperId.map(
					_cryptoHashPepperStorage::getPepper
				).orElse(
					null
				);
			}

			// process salt

			Optional<byte[]> optionalSalt =
				cryptoHashVerificationContext.getSalt();

			final CryptoHashProviderResponse hashProviderResponse =
				cryptoHashProvider.generate(
					pepper, optionalSalt.orElse(null), input);

			input = hashProviderResponse.getHash();
		}

		return _compare(input, hash);
	}

	/**
	 * A comparison algorithm that prevents timing attack
	 *
	 * @param bytes1 the input bytes
	 * @param bytes2 the expected bytes
	 * @return true if two given arrays of bytes are the same, otherwise false
	 */
	private boolean _compare(byte[] bytes1, byte[] bytes2) {
		int diff = bytes1.length ^ bytes2.length;

		for (int i = 0; (i < bytes1.length) && (i < bytes2.length); ++i) {
			diff |= bytes1[i] ^ bytes2[i];
		}

		if (diff == 0) {
			return true;
		}

		return false;
	}

	private CryptoHashProvider _getCryptoHashProvider(
			String hashGeneratorName, Map<String, ?> properties)
		throws Exception {

		CryptoHashProviderFactory cryptoHashProviderFactory =
			_cryptoHashProviderFactoryRegistry.getCryptoHashProviderFactory(
				hashGeneratorName);

		return cryptoHashProviderFactory.create(hashGeneratorName, properties);
	}

	@Reference(
		cardinality = ReferenceCardinality.MANDATORY,
		name = "HashPepperStorage",
		target = "(component.name=com.liferay.portal.crypto.hash.internal.pepper.storage.DummyCryptoHashPepperStorage)"
	)
	private CryptoHashPepperStorage _cryptoHashPepperStorage;

	@Reference(
		cardinality = ReferenceCardinality.MANDATORY,
		name = "CryptoHashProvider"
	)
	private CryptoHashProvider _cryptoHashProvider;

	@Reference
	private CryptoHashProviderFactoryRegistry
		_cryptoHashProviderFactoryRegistry;

}