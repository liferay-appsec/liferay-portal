/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.opensearch2.internal.connection;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.opensearch2.internal.OpenSearchTestRule;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.security.KeyStore;
import java.security.KeyStoreException;

import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author André de Oliveira
 * @author Petteri Karttunen
 */
public class OpenSearchConnectionTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@ClassRule
	public static final OpenSearchTestRule openSearchTestRule =
		OpenSearchTestRule.INSTANCE;

	@Before
	public void setUp() {
		OpenSearchConnection.Builder openSearchConnectionBuilder =
			new OpenSearchConnection.Builder();

		openSearchConnectionBuilder.networkHostAddresses(
			new String[] {"http://localhost:9200"});

		_postCloseRunnable = Mockito.mock(Runnable.class);

		openSearchConnectionBuilder.postCloseRunnable(_postCloseRunnable);

		_preConnectOpenSearchConnectionConsumer = Mockito.mock(Consumer.class);

		openSearchConnectionBuilder.preConnectOpenSearchConnectionConsumer(
			_preConnectOpenSearchConnectionConsumer);

		_openSearchConnection = openSearchConnectionBuilder.build();
	}

	@Test
	public void testConnectAndClose() throws Exception {
		Assert.assertFalse(_openSearchConnection.isConnected());

		_openSearchConnection.connect();

		Assert.assertTrue(_openSearchConnection.isConnected());

		Mockito.verify(
			_preConnectOpenSearchConnectionConsumer
		).accept(
			Mockito.any()
		);

		_openSearchConnection.close();

		Assert.assertFalse(_openSearchConnection.isConnected());

		Mockito.verify(
			_postCloseRunnable
		).run();
	}

	@Test
	public void testCreateSSLContext() throws Exception {
		Path path = Files.createTempFile(null, ".p12");

		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			keyStore.load(null, null);

			String password = RandomTestUtil.randomString();

			try (OutputStream outputStream = Files.newOutputStream(path)) {
				keyStore.store(outputStream, password.toCharArray());
			}

			OpenSearchConnection openSearchConnection =
				_createOpenSearchConnection(password, path, "pkcs12");

			try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
					CryptoUtil.class, Mockito.CALLS_REAL_METHODS)) {

				SSLContext sslContext = ReflectionTestUtil.invoke(
					openSearchConnection, "_createSSLContext", new Class<?>[0]);

				Assert.assertEquals("TLS", sslContext.getProtocol());

				mockedStatic.verify(
					() -> CryptoUtil.createKeyManagerFactory(
						KeyManagerFactory.getDefaultAlgorithm()));
				mockedStatic.verify(() -> CryptoUtil.createKeyStore("pkcs12"));
				mockedStatic.verify(
					() -> CryptoUtil.createTrustManagerFactory(
						TrustManagerFactory.getDefaultAlgorithm()));
			}

			OpenSearchConnection jksOpenSearchConnection =
				_createOpenSearchConnection(password, path, "jks");

			try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
					OpenSearchConnection.class.getName(), LoggerTestUtil.WARN);
				MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
					FIPSAuditUtil.class);
				SafeCloseable safeCloseable =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"FIPS_ENABLED", true)) {

				RuntimeException runtimeException = Assert.assertThrows(
					RuntimeException.class,
					() -> ReflectionTestUtil.invoke(
						jksOpenSearchConnection, "_createSSLContext",
						new Class<?>[0]));

				Assert.assertTrue(
					String.valueOf(runtimeException.getCause()),
					runtimeException.getCause() instanceof KeyStoreException);

				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					StringBundler.concat(
						"The truststore type \"jks\" of the configuration ",
						"property \"truststoreType\" is not allowed in FIPS ",
						"mode"),
					logEntry.getMessage());
			}
		}
		finally {
			Files.delete(path);
		}
	}

	private OpenSearchConnection _createOpenSearchConnection(
		String password, Path path, String truststoreType) {

		OpenSearchConnection.Builder openSearchConnectionBuilder =
			new OpenSearchConnection.Builder();

		openSearchConnectionBuilder.networkHostAddresses(
			new String[] {"https://localhost:9200"});
		openSearchConnectionBuilder.truststorePassword(password);
		openSearchConnectionBuilder.truststorePath(path.toString());
		openSearchConnectionBuilder.truststoreType(truststoreType);

		return openSearchConnectionBuilder.build();
	}

	private OpenSearchConnection _openSearchConnection;
	private Runnable _postCloseRunnable;
	private Consumer<OpenSearchConnection>
		_preConnectOpenSearchConnectionConsumer;

}