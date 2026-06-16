/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import java.nio.channels.SocketChannel;

import java.util.Collections;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * @author Jorge García Jiménez
 */
public class LDAPSSLSocket extends SSLSocket {

	public LDAPSSLSocket(SSLSocket delegate) {
		_delegate = delegate;
	}

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
			InetSocketAddress inetSocketAddress = (InetSocketAddress)endpoint;

			SSLParameters sslParameters = _delegate.getSSLParameters();

			sslParameters.setServerNames(
				Collections.singletonList(
					new SNIHostName(inetSocketAddress.getHostString())));

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

		_delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
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

	private final SSLSocket _delegate;

}