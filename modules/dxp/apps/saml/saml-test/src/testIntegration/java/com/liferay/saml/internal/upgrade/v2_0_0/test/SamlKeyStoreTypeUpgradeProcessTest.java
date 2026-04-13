/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.internal.upgrade.v2_0_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.saml.runtime.certificate.CertificateEntityId;
import com.liferay.saml.runtime.certificate.CertificateTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class SamlKeyStoreTypeUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			"SamlKeyStoreTypeUpgradeProcess");

		_companyId = TestPropsValues.getCompanyId();

		_cleanUpDLKeystores();
		_cleanUpFileSystemKeystores();
	}

	@After
	public void tearDown() throws Exception {
		_cleanUpDLKeystores();
		_cleanUpFileSystemKeystores();

		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}

		_restoreSamlConfiguration();
	}

	@Test
	public void testUpgradeConfigurationFromJKSToPKCS12() throws Exception {
		_setSamlConfiguration("jks", "/data/keystore.jks");

		_upgradeProcess.upgrade();

		Configuration configuration = _configurationAdmin.getConfiguration(
			_SAML_CONFIGURATION_PID, StringPool.QUESTION);

		Dictionary<String, Object> properties = configuration.getProperties();

		Assert.assertEquals("PKCS12", properties.get("saml.keystore.type"));
		Assert.assertEquals(
			"/data/keystore.p12", properties.get("saml.keystore.path"));
	}

	@Test
	public void testUpgradeConfigurationAlreadyPKCS12() throws Exception {
		_setSamlConfiguration("PKCS12", "/data/keystore.p12");

		_upgradeProcess.upgrade();

		Configuration configuration = _configurationAdmin.getConfiguration(
			_SAML_CONFIGURATION_PID, StringPool.QUESTION);

		Dictionary<String, Object> properties = configuration.getProperties();

		Assert.assertEquals("PKCS12", properties.get("saml.keystore.type"));
	}

	@Test
	public void testUpgradeDLKeystoreJKSToPKCS12() throws Exception {
		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		_store.addFile(
			_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
			Store.VERSION_DEFAULT, new ByteArrayInputStream(jksBytes));

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		Assert.assertTrue(
			_store.hasFile(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT));

		try (InputStream inputStream = _store.getFileAsStream(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT)) {

			KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

			pkcs12KeyStore.load(inputStream, password);

			Assert.assertTrue(pkcs12KeyStore.containsAlias("test-alias"));
		}

		Assert.assertFalse(
			_store.hasFile(
				_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT));
	}

	@Test
	public void testUpgradeDLKeystoreNeitherExists() throws Exception {
		_setSamlConfiguration("PKCS12", null);

		_upgradeProcess.upgrade();

		Assert.assertFalse(
			_store.hasFile(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT));
	}

	@Test
	public void testUpgradeDLKeystoreFIPSKeepsCompliantCert()
		throws Exception {

		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		_store.addFile(
			_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
			Store.VERSION_DEFAULT, new ByteArrayInputStream(jksBytes));

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		try (InputStream inputStream = _store.getFileAsStream(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT)) {

			KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

			pkcs12KeyStore.load(inputStream, password);

			X509Certificate certificate =
				(X509Certificate)pkcs12KeyStore.getCertificate("test-alias");

			Assert.assertEquals("RSA", certificate.getPublicKey().getAlgorithm());
			Assert.assertEquals(
				2048,
				((RSAPublicKey)certificate.getPublicKey()
				).getModulus().bitLength());
		}
	}

	@Test
	public void testUpgradeDLKeystoreFIPSRegeneratesSmallRSACert()
		throws Exception {

		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 1024, password);

		_store.addFile(
			_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
			Store.VERSION_DEFAULT, new ByteArrayInputStream(jksBytes));

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		try (InputStream inputStream = _store.getFileAsStream(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT)) {

			KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

			pkcs12KeyStore.load(inputStream, password);

			X509Certificate certificate =
				(X509Certificate)pkcs12KeyStore.getCertificate("test-alias");

			Assert.assertEquals("RSA", certificate.getPublicKey().getAlgorithm());
			Assert.assertTrue(
				((RSAPublicKey)certificate.getPublicKey()
				).getModulus().bitLength() >= 2048);
		}
	}

	@Test
	public void testUpgradeDLKeystoreFIPSRegeneratesDSACert()
		throws Exception {

		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("DSA", 2048, password);

		_store.addFile(
			_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
			Store.VERSION_DEFAULT, new ByteArrayInputStream(jksBytes));

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		try (InputStream inputStream = _store.getFileAsStream(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT)) {

			KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

			pkcs12KeyStore.load(inputStream, password);

			X509Certificate certificate =
				(X509Certificate)pkcs12KeyStore.getCertificate("test-alias");

			Assert.assertEquals("RSA", certificate.getPublicKey().getAlgorithm());
		}
	}

	@Test
	public void testUpgradeFileSystemKeystoreJKSToPKCS12() throws Exception {
		String liferayHome = PropsUtil.get(PropsKeys.LIFERAY_HOME);

		char[] password = "liferay".toCharArray();

		byte[] jksBytes = _createJKSKeystoreBytes("RSA", 2048, password);

		File dataDir = new File(liferayHome, "data");

		dataDir.mkdirs();

		File jksFile = new File(dataDir, "keystore.jks");

		try (FileOutputStream fos = new FileOutputStream(jksFile)) {
			fos.write(jksBytes);
		}

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		File p12File = new File(dataDir, "keystore.p12");

		Assert.assertTrue(p12File.exists());

		KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

		try (FileInputStream fis = new FileInputStream(p12File)) {
			pkcs12KeyStore.load(fis, password);
		}

		Assert.assertTrue(pkcs12KeyStore.containsAlias("test-alias"));
	}

	@Test
	public void testUpgradeCertificatePreservesSubjectDN() throws Exception {
		_enableFIPSMode();

		char[] password = "liferay".toCharArray();

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

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		jksKeyStore.store(baos, password);

		_store.addFile(
			_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
			Store.VERSION_DEFAULT,
			new ByteArrayInputStream(baos.toByteArray()));

		_setSamlConfiguration("jks", null);

		_upgradeProcess.upgrade();

		try (InputStream inputStream = _store.getFileAsStream(
				_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
				Store.VERSION_DEFAULT)) {

			KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");

			pkcs12KeyStore.load(inputStream, password);

			X509Certificate newCert =
				(X509Certificate)pkcs12KeyStore.getCertificate("test-alias");

			Assert.assertEquals(
				"RSA", newCert.getPublicKey().getAlgorithm());
			Assert.assertEquals(
				2048,
				((RSAPublicKey)newCert.getPublicKey()
				).getModulus().bitLength());

			String subjectDN =
				newCert.getSubjectX500Principal().getName();

			Assert.assertTrue(subjectDN.contains("CN=Test CN"));
			Assert.assertTrue(subjectDN.contains("O=Test Org"));
			Assert.assertTrue(subjectDN.contains("OU=Test OU"));
		}
	}

	private void _cleanUpDLKeystores() {
		try {
			if (_store.hasFile(
					_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH,
					Store.VERSION_DEFAULT)) {

				_store.deleteDirectory(
					_companyId, CompanyConstants.SYSTEM, _OLD_DL_KEYSTORE_PATH);
			}
		}
		catch (Exception exception) {
		}

		try {
			if (_store.hasFile(
					_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH,
					Store.VERSION_DEFAULT)) {

				_store.deleteDirectory(
					_companyId, CompanyConstants.SYSTEM, _NEW_DL_KEYSTORE_PATH);
			}
		}
		catch (Exception exception) {
		}
	}

	private void _cleanUpFileSystemKeystores() {
		String liferayHome = PropsUtil.get(PropsKeys.LIFERAY_HOME);

		File dataDir = new File(liferayHome, "data");

		File jksFile = new File(dataDir, "keystore.jks");

		if (jksFile.exists()) {
			jksFile.delete();
		}

		File p12File = new File(dataDir, "keystore.p12");

		if (p12File.exists()) {
			p12File.delete();
		}
	}

	private byte[] _createJKSKeystoreBytes(
			String algorithm, int keySize, char[] password)
		throws Exception {

		KeyStore jksKeyStore = KeyStore.getInstance("JKS");

		jksKeyStore.load(null, null);

		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);

		kpg.initialize(keySize);

		KeyPair keyPair = kpg.generateKeyPair();

		CertificateEntityId entityId = new CertificateEntityId(
			"Test", null, null, null, null, null);

		Calendar startDate = Calendar.getInstance();

		Calendar endDate = (Calendar)startDate.clone();

		endDate.add(Calendar.DAY_OF_YEAR, 365);

		X509Certificate certificate = _certificateTool.generateCertificate(
			keyPair, entityId, entityId, startDate.getTime(),
			endDate.getTime(), "SHA256with" + algorithm);

		jksKeyStore.setKeyEntry(
			"test-alias", keyPair.getPrivate(), password,
			new X509Certificate[] {certificate});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		jksKeyStore.store(baos, password);

		return baos.toByteArray();
	}

	private void _enableFIPSMode() {
		_autoCloseable =
			ReflectionTestUtil.setFieldValueWithAutoCloseable(
				PropsValues.class, "PORTAL_SECURITY_FIPS_MODE_ENABLED", true);
	}

	private void _restoreSamlConfiguration() {
		try {
			if (_originalSamlProperties != null) {
				Configuration configuration =
					_configurationAdmin.getConfiguration(
						_SAML_CONFIGURATION_PID, StringPool.QUESTION);

				configuration.update(_originalSamlProperties);
			}
		}
		catch (Exception exception) {
		}

		_originalSamlProperties = null;
	}

	private void _setSamlConfiguration(String keystoreType, String keystorePath)
		throws Exception {

		Configuration configuration = _configurationAdmin.getConfiguration(
			_SAML_CONFIGURATION_PID, StringPool.QUESTION);

		_originalSamlProperties = configuration.getProperties();

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("saml.keystore.password", "liferay");
		properties.put("saml.keystore.type", keystoreType);

		if (keystorePath != null) {
			properties.put("saml.keystore.path", keystorePath);
		}

		configuration.update(properties);
	}

	private static final String _NEW_DL_KEYSTORE_PATH = "saml/keystore.p12";

	private static final String _OLD_DL_KEYSTORE_PATH = "saml/keystore.jks";

	private static final String _SAML_CONFIGURATION_PID =
		"com.liferay.saml.runtime.configuration.SamlConfiguration";

	private static UpgradeProcess _upgradeProcess;

	private AutoCloseable _autoCloseable;

	@Inject
	private CertificateTool _certificateTool;

	private long _companyId;

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	private Dictionary<String, ?> _originalSamlProperties;

	@Inject(filter = "(default=true)")
	private Store _store;

	@Inject(
		filter = "(&(component.name=com.liferay.saml.internal.upgrade.registry.SamlImplUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}
