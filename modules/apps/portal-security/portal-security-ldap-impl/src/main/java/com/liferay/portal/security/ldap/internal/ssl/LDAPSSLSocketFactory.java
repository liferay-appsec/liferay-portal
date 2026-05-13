/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.ssl;

import com.liferay.portal.kernel.util.ArrayUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import java.nio.channels.SocketChannel;

import java.security.GeneralSecurityException;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
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
			_cipherSuitesOverride.remove();
		}
		else {
			_cipherSuitesOverride.set(cipherSuites.clone());
		}
	}

	@Override
	public Socket createSocket() throws IOException {
		return new LDAPSSLSocket(
			_constrain((SSLSocket)_sslSocketFactory.createSocket()));
	}

	@Override
	public Socket createSocket(InetAddress address, int port)
		throws IOException {

		return _constrain(
			(SSLSocket)_sslSocketFactory.createSocket(address, port));
	}

	@Override
	public Socket createSocket(
			InetAddress address, int port, InetAddress localAddress,
			int localPort)
		throws IOException {

		return _constrain(
			(SSLSocket)_sslSocketFactory.createSocket(
				address, port, localAddress, localPort));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return _constrain(
			(SSLSocket)_sslSocketFactory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(
			String host, int port, InetAddress localAddress, int localPort)
		throws IOException {

		return _constrain(
			(SSLSocket)_sslSocketFactory.createSocket(
				host, port, localAddress, localPort));
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

	private SSLSocket _constrain(SSLSocket sslSocket) {
		sslSocket.setEnabledProtocols(
			_intersect(_ENABLED_PROTOCOLS, sslSocket.getSupportedProtocols()));

		String[] cipherSuitesOverride = _cipherSuitesOverride.get();

		String[] desired =
			(cipherSuitesOverride != null) ? cipherSuitesOverride :
				_FIPS_CIPHER_SUITES_ALLOWLIST;

		String[] enabled = _intersect(
			desired, sslSocket.getSupportedCipherSuites());

		if (enabled.length == 0) {
			throw new SecurityException(
				"No FIPS-approved cipher suites are supported by the " +
					"installed JSSE provider; check the FIPS JCE/JSSE " +
						"configuration");
		}

		sslSocket.setEnabledCipherSuites(enabled);

		SSLParameters sslParameters = sslSocket.getSSLParameters();

		sslParameters.setEndpointIdentificationAlgorithm("LDAPS");

		sslSocket.setSSLParameters(sslParameters);

		return sslSocket;
	}

	private String[] _intersect(String[] desired, String[] supported) {
		Set<String> supportedSet = new LinkedHashSet<>(
			Arrays.asList(supported));

		Set<String> result = new LinkedHashSet<>();

		for (String candidate : desired) {
			if (supportedSet.contains(candidate)) {
				result.add(candidate);
			}
		}

		return result.toArray(new String[0]);
	}

	private static final String[] _ENABLED_PROTOCOLS = {"TLSv1.2", "TLSv1.3"};

	private static final String[] _FIPS_CIPHER_SUITES_ALLOWLIST = {
		"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
	};

	private static final LDAPSSLSocketFactory _INSTANCE =
		new LDAPSSLSocketFactory();

	private static final ThreadLocal<String[]> _cipherSuitesOverride =
		new ThreadLocal<>();

	private final SSLSocketFactory _sslSocketFactory;

	private static class LDAPSSLSocket extends SSLSocket {

		@Override
		public void addHandshakeCompletedListener(
			HandshakeCompletedListener listener) {

			_delegate.addHandshakeCompletedListener(listener);
		}

		@Override
		public void bind(SocketAddress bindpoint) throws IOException {
			_delegate.bind(bindpoint);
		}

		@Override
		public void close() throws IOException {
			_delegate.close();
		}

		@Override
		public void connect(SocketAddress endpoint) throws IOException {
			connect(endpoint, 0);
		}

		@Override
		public void connect(SocketAddress endpoint, int timeout)
			throws IOException {

			if (endpoint instanceof InetSocketAddress) {
				InetSocketAddress addr = (InetSocketAddress)endpoint;

				SSLParameters sslParameters = _delegate.getSSLParameters();

				sslParameters.setServerNames(
					Collections.singletonList(
						new SNIHostName(addr.getHostString())));

				_delegate.setSSLParameters(sslParameters);
			}

			_delegate.connect(endpoint, timeout);
		}

		@Override
		public SocketChannel getChannel() {
			return _delegate.getChannel();
		}

		@Override
		public String[] getEnabledCipherSuites() {
			return _delegate.getEnabledCipherSuites();
		}

		@Override
		public String[] getEnabledProtocols() {
			return _delegate.getEnabledProtocols();
		}

		@Override
		public boolean getEnableSessionCreation() {
			return _delegate.getEnableSessionCreation();
		}

		@Override
		public SSLSession getHandshakeSession() {
			return _delegate.getHandshakeSession();
		}

		@Override
		public InetAddress getInetAddress() {
			return _delegate.getInetAddress();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return _delegate.getInputStream();
		}

		@Override
		public boolean getKeepAlive() throws SocketException {
			return _delegate.getKeepAlive();
		}

		@Override
		public InetAddress getLocalAddress() {
			return _delegate.getLocalAddress();
		}

		@Override
		public int getLocalPort() {
			return _delegate.getLocalPort();
		}

		@Override
		public SocketAddress getLocalSocketAddress() {
			return _delegate.getLocalSocketAddress();
		}

		@Override
		public boolean getNeedClientAuth() {
			return _delegate.getNeedClientAuth();
		}

		@Override
		public boolean getOOBInline() throws SocketException {
			return _delegate.getOOBInline();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return _delegate.getOutputStream();
		}

		@Override
		public int getPort() {
			return _delegate.getPort();
		}

		@Override
		public int getReceiveBufferSize() throws SocketException {
			return _delegate.getReceiveBufferSize();
		}

		@Override
		public SocketAddress getRemoteSocketAddress() {
			return _delegate.getRemoteSocketAddress();
		}

		@Override
		public boolean getReuseAddress() throws SocketException {
			return _delegate.getReuseAddress();
		}

		@Override
		public int getSendBufferSize() throws SocketException {
			return _delegate.getSendBufferSize();
		}

		@Override
		public SSLSession getSession() {
			return _delegate.getSession();
		}

		@Override
		public int getSoLinger() throws SocketException {
			return _delegate.getSoLinger();
		}

		@Override
		public int getSoTimeout() throws SocketException {
			return _delegate.getSoTimeout();
		}

		@Override
		public SSLParameters getSSLParameters() {
			return _delegate.getSSLParameters();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return _delegate.getSupportedCipherSuites();
		}

		@Override
		public String[] getSupportedProtocols() {
			return _delegate.getSupportedProtocols();
		}

		@Override
		public boolean getTcpNoDelay() throws SocketException {
			return _delegate.getTcpNoDelay();
		}

		@Override
		public int getTrafficClass() throws SocketException {
			return _delegate.getTrafficClass();
		}

		@Override
		public boolean getUseClientMode() {
			return _delegate.getUseClientMode();
		}

		@Override
		public boolean getWantClientAuth() {
			return _delegate.getWantClientAuth();
		}

		@Override
		public boolean isBound() {
			return _delegate.isBound();
		}

		@Override
		public boolean isClosed() {
			return _delegate.isClosed();
		}

		@Override
		public boolean isConnected() {
			return _delegate.isConnected();
		}

		@Override
		public boolean isInputShutdown() {
			return _delegate.isInputShutdown();
		}

		@Override
		public boolean isOutputShutdown() {
			return _delegate.isOutputShutdown();
		}

		@Override
		public void removeHandshakeCompletedListener(
			HandshakeCompletedListener listener) {

			_delegate.removeHandshakeCompletedListener(listener);
		}

		@Override
		public void sendUrgentData(int data) throws IOException {
			_delegate.sendUrgentData(data);
		}

		@Override
		public void setEnabledCipherSuites(String[] suites) {
			_delegate.setEnabledCipherSuites(suites);
		}

		@Override
		public void setEnabledProtocols(String[] protocols) {
			_delegate.setEnabledProtocols(protocols);
		}

		@Override
		public void setEnableSessionCreation(boolean flag) {
			_delegate.setEnableSessionCreation(flag);
		}

		@Override
		public void setKeepAlive(boolean on) throws SocketException {
			_delegate.setKeepAlive(on);
		}

		@Override
		public void setNeedClientAuth(boolean need) {
			_delegate.setNeedClientAuth(need);
		}

		@Override
		public void setOOBInline(boolean on) throws SocketException {
			_delegate.setOOBInline(on);
		}

		@Override
		public void setPerformancePreferences(
			int connectionTime, int latency, int bandwidth) {

			_delegate.setPerformancePreferences(
				connectionTime, latency, bandwidth);
		}

		@Override
		public void setReceiveBufferSize(int size) throws SocketException {
			_delegate.setReceiveBufferSize(size);
		}

		@Override
		public void setReuseAddress(boolean on) throws SocketException {
			_delegate.setReuseAddress(on);
		}

		@Override
		public void setSendBufferSize(int size) throws SocketException {
			_delegate.setSendBufferSize(size);
		}

		@Override
		public void setSoLinger(boolean on, int linger) throws SocketException {
			_delegate.setSoLinger(on, linger);
		}

		@Override
		public void setSoTimeout(int timeout) throws SocketException {
			_delegate.setSoTimeout(timeout);
		}

		@Override
		public void setSSLParameters(SSLParameters params) {
			_delegate.setSSLParameters(params);
		}

		@Override
		public void setTcpNoDelay(boolean on) throws SocketException {
			_delegate.setTcpNoDelay(on);
		}

		@Override
		public void setTrafficClass(int tc) throws SocketException {
			_delegate.setTrafficClass(tc);
		}

		@Override
		public void setUseClientMode(boolean mode) {
			_delegate.setUseClientMode(mode);
		}

		@Override
		public void setWantClientAuth(boolean want) {
			_delegate.setWantClientAuth(want);
		}

		@Override
		public void shutdownInput() throws IOException {
			_delegate.shutdownInput();
		}

		@Override
		public void shutdownOutput() throws IOException {
			_delegate.shutdownOutput();
		}

		@Override
		public void startHandshake() throws IOException {
			_delegate.startHandshake();
		}

		@Override
		public String toString() {
			return _delegate.toString();
		}

		private LDAPSSLSocket(SSLSocket delegate) {
			_delegate = delegate;
		}

		private final SSLSocket _delegate;

	}

}