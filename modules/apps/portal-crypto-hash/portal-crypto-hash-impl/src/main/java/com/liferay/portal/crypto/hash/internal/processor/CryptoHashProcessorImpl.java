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
import com.liferay.portal.crypto.hash.generation.context.CryptoHashGenerationContext;
import com.liferay.portal.crypto.hash.generation.response.CryptoHashGenerationResponse;
import com.liferay.portal.crypto.hash.internal.generation.response.CryptoHashGenerationResponseImpl;
import com.liferay.portal.crypto.hash.processor.CryptoHashProcessor;
import com.liferay.portal.crypto.hash.verification.context.CryptoHashVerificationContext;

import java.util.Arrays;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
@Component(service = CryptoHashProcessor.class)
public class CryptoHashProcessorImpl implements CryptoHashProcessor {

	@Override
	public CryptoHashGenerationResponse generate(
			byte[] input,
			CryptoHashGenerationContext cryptoHashGenerationContext)
		throws Exception {

		cryptoHashGenerationContext.getCryptoHashProviderProperties();

		// Stub code: wrap generated salt and pepper into a CryptoHashFlavor,
		// and let CryptoHashProvider generate the actual hash

		return new CryptoHashGenerationResponseImpl(null, null);
	}

	@Override
	public Set<String> getAvailableCryptoHashProviderNames() {
		return _cryptoHashProviderFactories.keySet();
	}

	@Override
	public boolean verify(
			byte[] input, byte[] hash,
			CryptoHashVerificationContext... cryptoHashVerificationContexts)
		throws Exception {

		// Stub code: let CryptoHashProvider to generate a hash from input with
		// every verification context, and compare it to the stored hash.

		return Arrays.equals(input, hash);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {

		// stub code. require SPI to be added

		_cryptoHashProviderFactories = null;
	}

	@Deactivate
	protected void deactivate() {
		_cryptoHashProviderFactories.close();
	}

	private ServiceTrackerMap<String, ?> _cryptoHashProviderFactories;

}