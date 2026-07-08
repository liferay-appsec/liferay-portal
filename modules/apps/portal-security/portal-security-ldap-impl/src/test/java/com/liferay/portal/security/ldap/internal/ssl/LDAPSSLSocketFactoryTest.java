/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.ssl;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;

import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jorge García Jiménez
 */
public class LDAPSSLSocketFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() {
		LDAPSSLSocketFactory.setCipherSuitesOverride(null);
	}

	@Test
	public void testCreateSocket() throws IOException {
		SocketFactory socketFactory = LDAPSSLSocketFactory.getDefault();

		Socket socket = socketFactory.createSocket();

		SSLSocket sslSocket = ReflectionTestUtil.getFieldValue(
			socket, "_sslSocket");

		try {
			socket.connect(new InetSocketAddress("localhost", 64999), 1000);
		}
		catch (IOException ioException) {
		}

		SSLParameters sslParameters = sslSocket.getSSLParameters();

		List<SNIServerName> serverNames = sslParameters.getServerNames();

		Assert.assertEquals(serverNames.toString(), 1, serverNames.size());
		Assert.assertTrue(serverNames.get(0) instanceof SNIHostName);

		SNIHostName sniHostName = (SNIHostName)serverNames.get(0);

		Assert.assertEquals("localhost", sniHostName.getAsciiName());

		LDAPSSLSocketFactory.setCipherSuitesOverride(
			new String[] {"TLS_FAKE_NONEXISTENT_CIPHER"});

		Assert.assertThrows(
			SecurityException.class, socketFactory::createSocket);
	}

}