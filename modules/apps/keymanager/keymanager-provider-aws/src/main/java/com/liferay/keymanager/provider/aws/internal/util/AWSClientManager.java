/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.util;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Christopher Kian
 */
public class AWSClientManager<T> {

	public AWSClientManager(
		String region, boolean useFipsEndpoint, String fipsEndpointTemplate,
		ClientFactory<T> clientFactory) {

		_region = AWSRegionUtil.resolve(region);
		_useFipsEndpoint = useFipsEndpoint;
		_fipsEndpointTemplate = fipsEndpointTemplate;
		_clientFactory = clientFactory;

		_credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
	}

	public void close() {
		_lock.writeLock(
		).lock();

		try {
			_closeClient();
		}
		finally {
			_lock.writeLock(
			).unlock();
		}
	}

	public <R> R execute(AWSOperation<T, R> awsOperation) throws Exception {
		_lock.readLock(
		).lock();

		try {
			T client = _getClient();

			try {
				return awsOperation.apply(client);
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"AWS client operation failed: " +
							exception.getMessage(),
						exception);
				}

				throw exception;
			}
		}
		finally {
			_lock.readLock(
			).unlock();
		}
	}

	public String getRegion() {
		return _region;
	}

	public void updateConfiguration(String region, boolean useFipsEndpoint) {
		String resolvedRegion = AWSRegionUtil.resolve(region);

		if (Validator.isNull(resolvedRegion)) {
			throw new IllegalArgumentException(
				"AWS region could not be resolved");
		}

		_lock.writeLock(
		).lock();

		try {
			boolean changed = false;

			if (!resolvedRegion.equals(_region) ||
				(useFipsEndpoint != _useFipsEndpoint)) {

				changed = true;
			}

			_region = resolvedRegion;
			_useFipsEndpoint = useFipsEndpoint;

			if (changed) {
				_closeClient();
			}
		}
		finally {
			_lock.writeLock(
			).unlock();
		}
	}

	@FunctionalInterface
	public interface AWSOperation<T, R> {

		public R apply(T client) throws Exception;

	}

	@FunctionalInterface
	public interface ClientFactory<T> {

		public T build(
				AwsClientBuilder.EndpointConfiguration endpointConfiguration,
				String region, AWSCredentialsProvider credentialsProvider)
			throws Exception;

	}

	private void _closeClient() {
		if (_client == null) {
			return;
		}

		try {
			if (_client instanceof AmazonWebServiceClient) {
				AmazonWebServiceClient amazonWebServiceClient =
					(AmazonWebServiceClient)_client;

				amazonWebServiceClient.shutdown();
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to cleanly shut down AWS client", exception);
			}
		}

		_client = null;
	}

	private T _getClient() throws Exception {
		T client = _client;

		if (client != null) {
			return client;
		}

		_lock.readLock(
		).unlock();

		_lock.writeLock(
		).lock();

		try {
			if (_client == null) {
				AwsClientBuilder.EndpointConfiguration endpointConfiguration =
					null;

				if (_useFipsEndpoint &&
					Validator.isNotNull(_fipsEndpointTemplate) &&
					Validator.isNotNull(_region)) {

					String endpoint = StringUtil.replace(
						_fipsEndpointTemplate, "{region}", _region);

					endpointConfiguration =
						new AwsClientBuilder.EndpointConfiguration(
							endpoint, _region);
				}

				_client = _clientFactory.build(
					endpointConfiguration, _region, _credentialsProvider);
			}

			_lock.readLock(
			).lock();

			return _client;
		}
		finally {
			_lock.writeLock(
			).unlock();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AWSClientManager.class);

	private volatile T _client;
	private final ClientFactory<T> _clientFactory;
	private final AWSCredentialsProvider _credentialsProvider;
	private final String _fipsEndpointTemplate;
	private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
	private volatile String _region;
	private volatile boolean _useFipsEndpoint;

}