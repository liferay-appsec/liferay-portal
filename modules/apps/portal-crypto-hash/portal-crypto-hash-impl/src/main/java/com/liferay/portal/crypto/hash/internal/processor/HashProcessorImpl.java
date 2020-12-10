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

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.crypto.hash.flavor.HashFlavor;
import com.liferay.portal.crypto.hash.generation.context.HashGenerationContext;
import com.liferay.portal.crypto.hash.generation.context.salt.SaltCommand;
import com.liferay.portal.crypto.hash.generation.response.HashGenerationResponse;
import com.liferay.portal.crypto.hash.generator.spi.HashGenerator;
import com.liferay.portal.crypto.hash.generator.spi.factory.HashGeneratorFactory;
import com.liferay.portal.crypto.hash.generator.spi.salt.VariableSizeSaltGenerator;
import com.liferay.portal.crypto.hash.internal.flavor.HashFlavorImpl;
import com.liferay.portal.crypto.hash.internal.generation.context.HashGenerationContextImpl;
import com.liferay.portal.crypto.hash.internal.generation.response.HashGenerationResponseImpl;
import com.liferay.portal.crypto.hash.internal.pepper.storage.DummyHashPepperStorage;
import com.liferay.portal.crypto.hash.internal.verification.context.HashVerificationContextImpl;
import com.liferay.portal.crypto.hash.pepper.storage.spi.HashPepperStorage;
import com.liferay.portal.crypto.hash.processor.HashProcessor;
import com.liferay.portal.crypto.hash.verification.context.HashVerificationContext;

import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@Component(
	configurationPid = "com.liferay.portal.crypto.hash.configuration.HashProcessorConfiguration",
	configurationPolicy = ConfigurationPolicy.REQUIRE,
	service = HashProcessor.class
)
public class HashProcessorImpl implements HashProcessor {

	@Override
	public HashGenerationContext.Builder createHashGenerationContextBuilder(
		String hashGeneratorName, JSONObject hashGenerationMetaJSONObject) {

		_verifyHashGeneratorName(hashGeneratorName);

		return new HashGenerationContextImpl.BuilderImpl(
			hashGeneratorName, hashGenerationMetaJSONObject);
	}

	@Override
	public HashVerificationContext.Builder createHashVerificationContextBuilder(
		String hashGeneratorName, JSONObject hashGenerationMetaJSONObject) {

		_verifyHashGeneratorName(hashGeneratorName);

		return new HashVerificationContextImpl.BuilderImpl(
			hashGeneratorName, hashGenerationMetaJSONObject);
	}

	@Override
	public HashGenerationResponse generate(
			byte[] input, HashGenerationContext hashGenerationContext)
		throws Exception {

		Optional<JSONObject> optionalHashGeneratorMeta =
			hashGenerationContext.getHashGeneratorMeta();

		HashGenerator hashGenerator = _createHashGenerator(
			hashGenerationContext.getHashGeneratorName(),
			optionalHashGeneratorMeta);

		// process pepper

		String pepperId = null;

		if (!(_hashPepperStorage instanceof DummyHashPepperStorage)) {
			pepperId = _hashPepperStorage.getCurrentPepperId();

			hashGenerator.setPepper(_hashPepperStorage.getPepper(pepperId));
		}

		// process salt

		Optional<byte[]> optionalSalt = _processSaltCommands(
			hashGenerator, hashGenerationContext.getSaltCommands());

		optionalSalt.ifPresent(hashGenerator::setSalt);

		return new HashGenerationResponseImpl(
			new HashFlavorImpl(pepperId, optionalSalt.orElse(new byte[0])),
			hashGenerator.generate(input));
	}

	@Override
	public Set<String> getAvailableHashGeneratorNames() {
		return _hashGeneratorFactories.keySet();
	}

	@Override
	public boolean verify(
			byte[] input, byte[] hash,
			HashVerificationContext... hashVerificationContexts)
		throws Exception {

		for (HashVerificationContext hashVerificationContext :
				hashVerificationContexts) {

			HashGenerator hashGenerator = _createHashGenerator(
				hashVerificationContext.getHashGeneratorName(),
				hashVerificationContext.getHashGeneratorMeta());

			HashFlavor hashFlavor = hashVerificationContext.getHashFlavor();

			// process pepper

			if (!(_hashPepperStorage instanceof DummyHashPepperStorage)) {
				Optional<String> optionalPepperId = hashFlavor.getPepperId();

				optionalPepperId.map(
					_hashPepperStorage::getPepper
				).ifPresent(
					hashGenerator::setPepper
				);
			}

			// process salt

			Optional<byte[]> optionalSalt = hashFlavor.getSalt();

			optionalSalt.ifPresent(hashGenerator::setSalt);

			input = hashGenerator.generate(input);
		}

		return _compare(input, hash);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_hashGeneratorFactories = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, HashGeneratorFactory.class,
			"crypto.hash.generator.name");
	}

	@Deactivate
	protected void deactivate() {
		_hashGeneratorFactories.close();
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

	private HashGenerator _createHashGenerator(
			String generatorName, Optional<JSONObject> generatorMeta)
		throws Exception {

		HashGeneratorFactory hashGeneratorFactory =
			_hashGeneratorFactories.getService(generatorName);

		if (hashGeneratorFactory == null) {
			throw new IllegalArgumentException(
				"There is no generator of the name: " + generatorName);
		}

		return hashGeneratorFactory.create(
			generatorName, generatorMeta.orElse(new JSONObject()));
	}

	private Optional<byte[]> _processSaltCommands(
		HashGenerator hashGenerator, SaltCommand... saltCommands) {

		if ((saltCommands == null) || (saltCommands.length < 1)) {
			return Optional.empty();
		}

		for (SaltCommand saltCommand : saltCommands) {
			if (saltCommand instanceof SaltCommand.VariableSizeSaltCommand) {
				if (hashGenerator instanceof VariableSizeSaltGenerator) {
					SaltCommand.VariableSizeSaltCommand
						variableSizeSaltCommand =
							(SaltCommand.VariableSizeSaltCommand)saltCommand;

					VariableSizeSaltGenerator variableSizeSaltGenerator =
						(VariableSizeSaltGenerator)hashGenerator;

					return Optional.of(
						variableSizeSaltGenerator.generateSalt(
							variableSizeSaltCommand.getSaltSize()));
				}
			}
			else {
				return Optional.of(hashGenerator.generateSalt());
			}
		}

		throw new UnsupportedOperationException();
	}

	private void _verifyHashGeneratorName(String hashGeneratorName) {
		Set<String> availableHashGeneratorNames =
			getAvailableHashGeneratorNames();

		if (!availableHashGeneratorNames.contains(hashGeneratorName)) {
			throw new IllegalArgumentException(
				"There is no generator of name: " + hashGeneratorName);
		}
	}

	private ServiceTrackerMap<String, HashGeneratorFactory>
		_hashGeneratorFactories;

	@Reference(
		cardinality = ReferenceCardinality.MANDATORY,
		name = "HashPepperStorage",
		target = "(component.name=com.liferay.portal.crypto.hash.internal.pepper.storage.DummyHashPepperStorage)"
	)
	private HashPepperStorage _hashPepperStorage;

}