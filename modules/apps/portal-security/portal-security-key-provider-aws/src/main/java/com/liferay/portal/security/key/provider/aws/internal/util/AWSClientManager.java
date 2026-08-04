/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.provider.aws.internal.util;

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
		ClientFactory<T> clientFactory, String fipsEndpointTemplate,
		String region, boolean useFipsEndpoint) {

		_clientFactory = clientFactory;
		_fipsEndpointTemplate = fipsEndpointTemplate;
		_region = region;
		_useFipsEndpoint = useFipsEndpoint;

		if (Validator.isNull(region)) {
			_region = AWSRegionUtil.getRegion();
		}

		_awsCredentialsProvider =
			DefaultAWSCredentialsProviderChain.getInstance();
	}

	public void close() {
		_reentrantReadWriteLock.writeLock(
		).lock();

		try {
			_closeClient();
		}
		finally {
			_reentrantReadWriteLock.writeLock(
			).unlock();
		}
	}

	public <R> R execute(AWSOperation<T, R> awsOperation) throws Exception {
		_reentrantReadWriteLock.readLock(
		).lock();

		try {
			T client = _getClient();

			try {
				return awsOperation.apply(client);
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn("AWS client operation failed", exception);
				}

				throw exception;
			}
		}
		finally {
			_reentrantReadWriteLock.readLock(
			).unlock();
		}
	}

	public String getRegion() {
		return _region;
	}

	public void updateConfiguration(String region, boolean useFipsEndpoint) {
		if (Validator.isNull(region)) {
			region = AWSRegionUtil.getRegion();
		}

		if (Validator.isNull(region)) {
			throw new IllegalArgumentException(
				"AWS region could not be resolved");
		}

		_reentrantReadWriteLock.writeLock(
		).lock();

		try {
			if (!region.equals(_region) ||
				(useFipsEndpoint != _useFipsEndpoint)) {

				_region = region;
				_useFipsEndpoint = useFipsEndpoint;

				_closeClient();
			}
		}
		finally {
			_reentrantReadWriteLock.writeLock(
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
				AWSCredentialsProvider awsCredentialsProvider,
				AwsClientBuilder.EndpointConfiguration endpointConfiguration,
				String region)
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

		_reentrantReadWriteLock.readLock(
		).unlock();

		_reentrantReadWriteLock.writeLock(
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
					_awsCredentialsProvider, endpointConfiguration, _region);
			}

			_reentrantReadWriteLock.readLock(
			).lock();
		}
		finally {
			_reentrantReadWriteLock.writeLock(
			).unlock();
		}

		return _client;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AWSClientManager.class);

	private final AWSCredentialsProvider _awsCredentialsProvider;
	private volatile T _client;
	private final ClientFactory<T> _clientFactory;
	private final String _fipsEndpointTemplate;
	private final ReentrantReadWriteLock _reentrantReadWriteLock =
		new ReentrantReadWriteLock();
	private volatile String _region;
	private volatile boolean _useFipsEndpoint;

}