/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.connection;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class RestClientTransportFactoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_path = Files.createTempFile(null, ".p12");

		KeyStore keyStore = KeyStore.getInstance("PKCS12");

		keyStore.load(null, null);

		try (OutputStream outputStream = Files.newOutputStream(_path)) {
			keyStore.store(outputStream, _PASSWORD.toCharArray());
		}
	}

	@After
	public void tearDown() throws Exception {
		Files.delete(_path);
	}

	@Test
	public void testCreateSSLConnectionSocketFactory() {
		RestClientTransportFactory restClientTransportFactory =
			_createRestClientTransportFactory("pkcs12");

		try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
				CryptoUtil.class, Mockito.CALLS_REAL_METHODS)) {

			Assert.assertNotNull(
				ReflectionTestUtil.invoke(
					restClientTransportFactory,
					"_createSSLConnectionSocketFactory", new Class<?>[0]));

			SSLParameters sslParameters = _captureSSLParameters(mockedStatic);

			Assert.assertNull(sslParameters.getCipherSuites());
			Assert.assertNull(sslParameters.getProtocols());
		}

		try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
				CryptoUtil.class, Mockito.CALLS_REAL_METHODS);
			SafeCloseable fipsEnabledSafeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable keyManagerFactoryAlgorithmSafeCloseable =
				_swapKeyManagerFactoryAlgorithm()) {

			Assert.assertNotNull(
				ReflectionTestUtil.invoke(
					restClientTransportFactory,
					"_createSSLConnectionSocketFactory", new Class<?>[0]));

			SSLParameters sslParameters = _captureSSLParameters(mockedStatic);

			String[] cipherSuites = sslParameters.getCipherSuites();

			Assert.assertTrue(
				Arrays.toString(cipherSuites),
				ArrayUtil.isNotEmpty(cipherSuites));
			Assert.assertArrayEquals(
				cipherSuites,
				FIPSModeValidator.getAllowedTLSCipherSuites(cipherSuites));

			String[] protocols = sslParameters.getProtocols();

			Assert.assertTrue(
				Arrays.toString(protocols), ArrayUtil.isNotEmpty(protocols));
			Assert.assertArrayEquals(
				protocols, FIPSModeValidator.getAllowedTLSProtocols(protocols));
		}
	}

	@Test
	public void testCreateSSLContext() {
		RestClientTransportFactory restClientTransportFactory =
			_createRestClientTransportFactory("pkcs12");

		try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
				CryptoUtil.class, Mockito.CALLS_REAL_METHODS)) {

			SSLContext sslContext = ReflectionTestUtil.invoke(
				restClientTransportFactory, "_createSSLContext",
				new Class<?>[0]);

			Assert.assertEquals("TLS", sslContext.getProtocol());

			mockedStatic.verify(
				() -> CryptoUtil.createKeyManagerFactory(
					KeyManagerFactory.getDefaultAlgorithm()));
			mockedStatic.verify(() -> CryptoUtil.createKeyStore("pkcs12"));
			mockedStatic.verify(
				() -> CryptoUtil.createTrustManagerFactory(
					TrustManagerFactory.getDefaultAlgorithm()));
		}

		RestClientTransportFactory jksRestClientTransportFactory =
			_createRestClientTransportFactory("jks");

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				RestClientTransportFactory.class.getName(),
				LoggerTestUtil.WARN);
			MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			RuntimeException runtimeException = Assert.assertThrows(
				RuntimeException.class,
				() -> ReflectionTestUtil.invoke(
					jksRestClientTransportFactory, "_createSSLContext",
					new Class<?>[0]));

			Assert.assertTrue(
				String.valueOf(runtimeException.getCause()),
				runtimeException.getCause() instanceof KeyStoreException);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"The truststore type \"jks\" of the configuration property " +
					"\"truststoreType\" is not allowed in FIPS mode",
				logEntry.getMessage());
		}
	}

	private SSLParameters _captureSSLParameters(
		MockedStatic<CryptoUtil> mockedStatic) {

		ArgumentCaptor<SSLParameters> argumentCaptor = ArgumentCaptor.forClass(
			SSLParameters.class);

		mockedStatic.verify(
			() -> CryptoUtil.configureSSLParameters(
				Mockito.any(), Mockito.any(), argumentCaptor.capture()));

		return argumentCaptor.getValue();
	}

	private RestClientTransportFactory _createRestClientTransportFactory(
		String truststoreType) {

		RestClientTransportFactory.Builder builder =
			new RestClientTransportFactory.Builder(
				new String[] {"https://localhost:9200"});

		builder.truststorePassword(
			_PASSWORD
		).truststorePath(
			_path.toString()
		).truststoreType(
			truststoreType
		);

		return builder.build();
	}

	private SafeCloseable _swapKeyManagerFactoryAlgorithm() {
		String algorithm = KeyManagerFactory.getDefaultAlgorithm();

		Security.setProperty(_KEY_MANAGER_FACTORY_ALGORITHM, "PKIX");

		return () -> Security.setProperty(
			_KEY_MANAGER_FACTORY_ALGORITHM, algorithm);
	}

	private static final String _KEY_MANAGER_FACTORY_ALGORITHM =
		"ssl.KeyManagerFactory.algorithm";

	private static final String _PASSWORD = RandomTestUtil.randomString();

	private Path _path;

}