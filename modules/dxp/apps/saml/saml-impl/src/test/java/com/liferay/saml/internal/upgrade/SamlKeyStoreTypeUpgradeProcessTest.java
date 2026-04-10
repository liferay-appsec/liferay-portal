/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.internal.upgrade;

import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.saml.internal.upgrade.v2_0_0.SamlKeyStoreTypeUpgradeProcess;
import com.liferay.saml.opensaml.integration.internal.certificate.CertificateToolImpl;
import com.liferay.saml.runtime.certificate.CertificateEntityId;
import com.liferay.saml.runtime.certificate.CertificateTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Rafael Praxedes
 */
public class SamlKeyStoreTypeUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		_configurationAdmin = Mockito.mock(ConfigurationAdmin.class);
		_configuration = Mockito.mock(Configuration.class);
		_companyLocalService = Mockito.mock(CompanyLocalService.class);
		_store = Mockito.mock(Store.class);
		_certificateTool = new CertificateToolImpl();

		Mockito.when(
			_configurationAdmin.getConfiguration(
				Mockito.eq(_SAML_CONFIGURATION_PID),
				Mockito.eq(StringPool.QUESTION))
		).thenReturn(
			_configuration
		);

		Mockito.doAnswer(
			invocation -> {
				UnsafeConsumer<Long, Exception> consumer =
					invocation.getArgument(0);

				consumer.accept(_TEST_COMPANY_ID);

				return null;
			}
		).when(
			_companyLocalService
		).forEachCompanyId(
			Mockito.any()
		);
	}

	@After
	public void tearDown() throws Exception {
		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}
	}

	@Test
	public void testUpgradeConfigurationAlreadyPKCS12() throws Exception {
		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("saml.keystore.type", "PKCS12");

		Mockito.when(
			_configuration.getProperties()
		).thenReturn(
			properties
		);

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_configuration, Mockito.never()
		).update(
			Mockito.any(Dictionary.class)
		);
	}

	@Test
	public void testUpgradeConfigurationFromJKSToPKCS12() throws Exception {
		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("saml.keystore.path", "/data/keystore.jks");
		properties.put("saml.keystore.type", "jks");

		Mockito.when(
			_configuration.getProperties()
		).thenReturn(
			properties
		);

		_createUpgradeProcess().doUpgrade();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Dictionary<String, Object>> captor =
			ArgumentCaptor.forClass(Dictionary.class);

		Mockito.verify(
			_configuration
		).update(
			captor.capture()
		);

		Dictionary<String, Object> updatedProperties = captor.getValue();

		Assert.assertEquals("PKCS12", updatedProperties.get("saml.keystore.type"));
		Assert.assertEquals(
			"/data/keystore.p12", updatedProperties.get("saml.keystore.path"));
	}

	@Test
	public void testUpgradeConfigurationNullProperties() throws Exception {
		Mockito.when(
			_configuration.getProperties()
		).thenReturn(
			null
		);

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_configuration, Mockito.never()
		).update(
			Mockito.any(Dictionary.class)
		);
	}

	@Test
	public void testUpgradeDLKeystoreAlreadyPKCS12() throws Exception {
		_mockDLStoreFiles(false, true);

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_store, Mockito.never()
		).addFile(
			Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
			Mockito.anyString(), Mockito.any(InputStream.class)
		);
	}

	@Test
	public void testUpgradeDLKeystoreJKSToPKCS12() throws Exception {
		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		_mockDLStoreFiles(true, false);

		Mockito.when(
			_store.getFileAsStream(
				Mockito.eq(_TEST_COMPANY_ID),
				Mockito.eq((long)CompanyConstants.SYSTEM),
				Mockito.eq("saml/keystore.jks"),
				Mockito.eq(Store.VERSION_DEFAULT))
		).thenReturn(
			new ByteArrayInputStream(jksBytes)
		);

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_store
		).addFile(
			Mockito.eq(_TEST_COMPANY_ID),
			Mockito.eq((long)CompanyConstants.SYSTEM),
			Mockito.eq("saml/keystore.p12"), Mockito.eq(Store.VERSION_DEFAULT),
			Mockito.any(InputStream.class)
		);

		Mockito.verify(
			_store
		).deleteDirectory(
			Mockito.eq(_TEST_COMPANY_ID),
			Mockito.eq((long)CompanyConstants.SYSTEM),
			Mockito.eq("saml/keystore.jks")
		);
	}

	@Test
	public void testUpgradeDLKeystoreNeitherExists() throws Exception {
		_mockDLStoreFiles(false, false);

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_store, Mockito.never()
		).addFile(
			Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
			Mockito.anyString(), Mockito.any(InputStream.class)
		);
	}

	@Test
	public void testUpgradeDLKeystoreFIPSKeepsCompliantCert() throws Exception {
		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		_mockDLStoreFiles(true, false);

		Mockito.when(
			_store.getFileAsStream(
				Mockito.eq(_TEST_COMPANY_ID),
				Mockito.eq((long)CompanyConstants.SYSTEM),
				Mockito.eq("saml/keystore.jks"),
				Mockito.eq(Store.VERSION_DEFAULT))
		).thenReturn(
			new ByteArrayInputStream(jksBytes)
		);

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		// addFile called once for JKS->PKCS12 conversion, not twice for cert
		// regeneration

		Mockito.verify(
			_store, Mockito.times(1)
		).addFile(
			Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
			Mockito.anyString(), Mockito.any(InputStream.class)
		);
	}

	@Test
	public void testUpgradeDLKeystoreFIPSRegeneratesSmallRSACert()
		throws Exception {

		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 1024, password);

		_mockDLStoreFiles(true, false);

		Mockito.when(
			_store.getFileAsStream(
				Mockito.eq(_TEST_COMPANY_ID),
				Mockito.eq((long)CompanyConstants.SYSTEM),
				Mockito.eq("saml/keystore.jks"),
				Mockito.eq(Store.VERSION_DEFAULT))
		).thenReturn(
			new ByteArrayInputStream(jksBytes)
		);

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		Mockito.verify(
			_store
		).addFile(
			Mockito.eq(_TEST_COMPANY_ID),
			Mockito.eq((long)CompanyConstants.SYSTEM),
			Mockito.eq("saml/keystore.p12"), Mockito.eq(Store.VERSION_DEFAULT),
			Mockito.any(InputStream.class)
		);
	}

	@Test
	public void testUpgradeFileSystemKeystoreJKSToPKCS12() throws Exception {
		File dataDir = temporaryFolder.newFolder("data");

		PropsUtil.set(
			PropsKeys.LIFERAY_HOME,
			temporaryFolder.getRoot().getAbsolutePath());

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		File jksFile = new File(dataDir, "keystore.jks");

		try (FileOutputStream fos = new FileOutputStream(jksFile)) {
			fos.write(jksBytes);
		}

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		File p12File = new File(dataDir, "keystore.p12");

		Assert.assertTrue(p12File.exists());

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		try (java.io.FileInputStream fis =
				new java.io.FileInputStream(p12File)) {

			pkcs12KeyStore.load(fis, password);
		}

		Assert.assertTrue(pkcs12KeyStore.containsAlias("test-alias"));
	}

	@Test
	public void testUpgradeFileSystemKeystoreAlreadyPKCS12() throws Exception {
		File dataDir = temporaryFolder.newFolder("data");

		PropsUtil.set(
			PropsKeys.LIFERAY_HOME,
			temporaryFolder.getRoot().getAbsolutePath());

		char[] password = "liferay".toCharArray();

		// Create only the PKCS12 file, no JKS

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		pkcs12KeyStore.load(null, null);

		File p12File = new File(dataDir, "keystore.p12");

		try (FileOutputStream fos = new FileOutputStream(p12File)) {
			pkcs12KeyStore.store(fos, password);
		}

		long lastModified = p12File.lastModified();

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		// File should not have been rewritten (non-FIPS mode, no certs to
		// upgrade)

		Assert.assertEquals(lastModified, p12File.lastModified());
	}

	@Test
	public void testUpgradeCertificatePreservesSubjectDN() throws Exception {
		_enableFIPSMode();

		File dataDir = temporaryFolder.newFolder("data");

		PropsUtil.set(
			PropsKeys.LIFERAY_HOME,
			temporaryFolder.getRoot().getAbsolutePath());

		char[] password = "liferay".toCharArray();

		// Create a JKS keystore with a DSA cert that has a specific subject DN

		KeyStore jksKeyStore = KeyStore.getInstance("JKS");

		jksKeyStore.load(null, null);

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");

		kpg.initialize(2048);

		KeyPair keyPair = kpg.generateKeyPair();

		CertificateEntityId entityId = new CertificateEntityId(
			"Test CN", "Test Org", "Test OU", "Test City", "CA", "US");

		Calendar startDate = Calendar.getInstance();

		Calendar endDate = (Calendar)startDate.clone();

		endDate.add(Calendar.DAY_OF_YEAR, 365);

		X509Certificate dsaCert = _certificateTool.generateCertificate(
			keyPair, entityId, entityId, startDate.getTime(),
			endDate.getTime(), "SHA256withDSA");

		jksKeyStore.setKeyEntry(
			"test-alias", keyPair.getPrivate(), password,
			new X509Certificate[] {dsaCert});

		File jksFile = new File(dataDir, "keystore.jks");

		try (FileOutputStream fos = new FileOutputStream(jksFile)) {
			jksKeyStore.store(fos, password);
		}

		_setConfigPassword("liferay");

		_createUpgradeProcess().doUpgrade();

		// Load the new PKCS12 keystore and check the regenerated cert

		File p12File = new File(dataDir, "keystore.p12");

		Assert.assertTrue(p12File.exists());

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		try (java.io.FileInputStream fis =
				new java.io.FileInputStream(p12File)) {

			pkcs12KeyStore.load(fis, password);
		}

		X509Certificate newCert =
			(X509Certificate)pkcs12KeyStore.getCertificate("test-alias");

		Assert.assertNotNull(newCert);

		// Verify it's now RSA 2048

		Assert.assertEquals("RSA", newCert.getPublicKey().getAlgorithm());
		Assert.assertEquals(
			2048,
			((RSAPublicKey)newCert.getPublicKey()).getModulus().bitLength());

		// Verify subject DN preserved

		String subjectDN = newCert.getSubjectX500Principal().getName();

		Assert.assertTrue(subjectDN.contains("CN=Test CN"));
		Assert.assertTrue(subjectDN.contains("O=Test Org"));
		Assert.assertTrue(subjectDN.contains("OU=Test OU"));
	}

	private byte[] _createJKSKeystoreBytes(
			String algorithm, int keySize, char[] password)
		throws Exception {

		KeyStore jksKeyStore = KeyStore.getInstance("JKS");

		jksKeyStore.load(null, null);

		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);

		kpg.initialize(keySize);

		KeyPair keyPair = kpg.generateKeyPair();

		String signatureAlgorithm = "SHA256with" + algorithm;

		CertificateEntityId entityId = new CertificateEntityId(
			"Test", null, null, null, null, null);

		Calendar startDate = Calendar.getInstance();

		Calendar endDate = (Calendar)startDate.clone();

		endDate.add(Calendar.DAY_OF_YEAR, 365);

		X509Certificate certificate = _certificateTool.generateCertificate(
			keyPair, entityId, entityId, startDate.getTime(),
			endDate.getTime(), signatureAlgorithm);

		jksKeyStore.setKeyEntry(
			"test-alias", keyPair.getPrivate(), password,
			new X509Certificate[] {certificate});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		jksKeyStore.store(baos, password);

		return baos.toByteArray();
	}

	private SamlKeyStoreTypeUpgradeProcess _createUpgradeProcess() {
		return new SamlKeyStoreTypeUpgradeProcess(
			_certificateTool, _companyLocalService, _configurationAdmin,
			_store);
	}

	private void _enableFIPSMode() {
		_autoCloseable =
			ReflectionTestUtil.setFieldValueWithAutoCloseable(
				PropsValues.class, "PORTAL_SECURITY_FIPS_MODE_ENABLED", true);
	}

	private void _mockDLStoreFiles(boolean hasOld, boolean hasNew) {
		Mockito.when(
			_store.hasFile(
				Mockito.eq(_TEST_COMPANY_ID),
				Mockito.eq((long)CompanyConstants.SYSTEM),
				Mockito.eq("saml/keystore.jks"),
				Mockito.eq(Store.VERSION_DEFAULT))
		).thenReturn(
			hasOld
		);

		Mockito.when(
			_store.hasFile(
				Mockito.eq(_TEST_COMPANY_ID),
				Mockito.eq((long)CompanyConstants.SYSTEM),
				Mockito.eq("saml/keystore.p12"),
				Mockito.eq(Store.VERSION_DEFAULT))
		).thenReturn(
			hasNew
		);
	}

	private void _setConfigPassword(String password) {
		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("saml.keystore.password", password);

		Mockito.when(
			_configuration.getProperties()
		).thenReturn(
			properties
		);
	}

	private static final String _SAML_CONFIGURATION_PID =
		"com.liferay.saml.runtime.configuration.SamlConfiguration";

	private static final long _TEST_COMPANY_ID = 12345L;

	private AutoCloseable _autoCloseable;
	private CertificateTool _certificateTool;
	private CompanyLocalService _companyLocalService;
	private Configuration _configuration;
	private ConfigurationAdmin _configurationAdmin;
	private Store _store;

}
