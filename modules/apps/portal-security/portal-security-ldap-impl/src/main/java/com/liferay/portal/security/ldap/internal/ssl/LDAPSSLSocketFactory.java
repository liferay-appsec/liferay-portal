/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.ssl;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.SetUtil;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.security.GeneralSecurityException;

import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Jorge García Jiménez
 */
public class LDAPSSLSocketFactory extends SocketFactory {

	public static SocketFactory getDefault() {
		return _INSTANCE;
	}

	public static void setCipherSuitesOverride(String[] cipherSuites) {
		if (ArrayUtil.isEmpty(cipherSuites)) {
			_cipherSuites.remove();
		}
		else {
			_cipherSuites.set(cipherSuites.clone());
		}
	}

	@Override
	public Socket createSocket() throws IOException {
		SSLSocket sslSocket = (SSLSocket)_sslSocketFactory.createSocket();

		_configure(sslSocket);

		return new LDAPSSLSocket(sslSocket);
	}

	@Override
	public Socket createSocket(InetAddress inetAddress, int port)
		throws IOException {

		SSLSocket sslSocket = (SSLSocket)_sslSocketFactory.createSocket(
			inetAddress, port);

		_configure(sslSocket);

		return sslSocket;
	}

	@Override
	public Socket createSocket(
			InetAddress inetAddress, int port, InetAddress localAddress,
			int localPort)
		throws IOException {

		SSLSocket sslSocket = (SSLSocket)_sslSocketFactory.createSocket(
			inetAddress, port, localAddress, localPort);

		_configure(sslSocket);

		return sslSocket;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		SSLSocket sslSocket = (SSLSocket)_sslSocketFactory.createSocket(
			host, port);

		_configure(sslSocket);

		return sslSocket;
	}

	@Override
	public Socket createSocket(
			String host, int port, InetAddress inetAddress, int localPort)
		throws IOException {

		SSLSocket sslSocket = (SSLSocket)_sslSocketFactory.createSocket(
			host, port, inetAddress, localPort);

		_configure(sslSocket);

		return sslSocket;
	}

	private LDAPSSLSocketFactory() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(null, null, null);

			_sslSocketFactory = sslContext.getSocketFactory();
		}
		catch (GeneralSecurityException generalSecurityException) {
			throw new IllegalStateException(
				"Unable to initialize LDAP SSL context",
				generalSecurityException);
		}
	}

	private void _configure(SSLSocket sslSocket) {
		Set<String> enabledProtocols = SetUtil.intersect(
			ListUtil.fromArray(_ENABLED_PROTOCOLS),
			ListUtil.fromArray(sslSocket.getSupportedProtocols()));

		sslSocket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));

		String[] cipherSuites = _cipherSuites.get();

		if (cipherSuites == null) {
			cipherSuites = _ALLOWED_CIPHER_SUITES;
		}

		Set<String> enabledCipherSuites = SetUtil.intersect(
			ListUtil.fromArray(cipherSuites),
			ListUtil.fromArray(sslSocket.getSupportedCipherSuites()));

		if (enabledCipherSuites.isEmpty()) {
			throw new SecurityException(
				"No FIPS approved cipher suites are supported by the JSSE " +
					"provider");
		}

		sslSocket.setEnabledCipherSuites(
			enabledCipherSuites.toArray(new String[0]));

		SSLParameters sslParameters = sslSocket.getSSLParameters();

		sslParameters.setEndpointIdentificationAlgorithm("LDAPS");

		sslSocket.setSSLParameters(sslParameters);
	}

	private static final String[] _ALLOWED_CIPHER_SUITES = {
		"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
	};

	private static final String[] _ENABLED_PROTOCOLS = {"TLSv1.2", "TLSv1.3"};

	private static final LDAPSSLSocketFactory _INSTANCE =
		new LDAPSSLSocketFactory();

	private static final ThreadLocal<String[]> _cipherSuites =
		new CentralizedThreadLocal<>(
			LDAPSSLSocketFactory.class + "._cipherSuites");

	private final SSLSocketFactory _sslSocketFactory;

}