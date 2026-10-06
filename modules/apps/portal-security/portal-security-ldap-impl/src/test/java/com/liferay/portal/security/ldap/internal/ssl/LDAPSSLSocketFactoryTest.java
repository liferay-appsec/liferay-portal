/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.ssl;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.net.InetSocketAddress;
import java.net.Socket;

import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 */
public class LDAPSSLSocketFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testConnect() throws Exception {
		SSLSocket sslSocket = Mockito.mock(SSLSocket.class);

		Mockito.when(
			sslSocket.getSSLParameters()
		).thenReturn(
			new SSLParameters()
		);

		LDAPSSLSocket ldapSSLSocket = new LDAPSSLSocket(sslSocket);

		ldapSSLSocket.connect(
			InetSocketAddress.createUnresolved("localhost", 636));

		ArgumentCaptor<SSLParameters> argumentCaptor = ArgumentCaptor.forClass(
			SSLParameters.class);

		Mockito.verify(
			sslSocket
		).setSSLParameters(
			argumentCaptor.capture()
		);

		SSLParameters sslParameters = argumentCaptor.getValue();

		List<SNIServerName> serverNames = sslParameters.getServerNames();

		SNIHostName sniHostName = (SNIHostName)serverNames.get(0);

		Assert.assertEquals("localhost", sniHostName.getAsciiName());

		Assert.assertEquals(serverNames.toString(), 1, serverNames.size());
	}

	@Test
	public void testCreateSocket() throws Exception {
		SocketFactory socketFactory = LDAPSSLSocketFactory.getDefault();

		Socket socket = socketFactory.createSocket();

		Assert.assertTrue(socket instanceof LDAPSSLSocket);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			SSLSocket sslSocket = (SSLSocket)socketFactory.createSocket();

			Assert.assertEquals(
				SetUtil.fromArray(_CIPHER_SUITES),
				SetUtil.fromArray(sslSocket.getEnabledCipherSuites()));
			Assert.assertEquals(
				SetUtil.fromArray(_PROTOCOLS),
				SetUtil.fromArray(sslSocket.getEnabledProtocols()));
		}
	}

	private static final String[] _CIPHER_SUITES = {
		"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
	};

	private static final String[] _PROTOCOLS = {"TLSv1.2", "TLSv1.3"};

}