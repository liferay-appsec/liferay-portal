/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.nio.file.Files;

import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import javax.net.ssl.TrustManagerFactory;

import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Manuele Castro
 */
public class CryptoCallScannerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testScan() throws Exception {
		_testScan(ConstantTestClass.class, "MessageDigest SHA-256 [digest]");
		_testScan(DigesterUtilTestClass.class, "MessageDigest MD5 [digest]");
		_testScan(
			KeyFactoryTestClass.class, "KeyFactory RSA [other] [private-key]",
			"KeyFactory RSA [other] [public-key]");
		_testScan(KeyGeneratorTestClass.class, "KeyGenerator AES [keygen] 256");
		_testScan(
			MacTestClass.class, "Mac HmacSHA256 [tag]",
			"SecretKeySpec HmacSHA256 [other]");
		_testScan(
			ParameterTestClass.class,
			"MessageDigest unresolved: parameter 1 of " +
				ParameterTestClass.class.getName() + ".digest [digest]");
		_testScan(
			PropsUtilTestClass.class,
			"MessageDigest configured by [test.algorithm] [digest]");
		_testScan(SignatureTestClass.class, "Signature SHA256withRSA [verify]");
		_testScan(
			TrustManagerFactoryTestClass.class,
			"TrustManagerFactory configured by " +
				"[ssl.TrustManagerFactory.algorithm] [other]");
	}

	@Test
	public void testScanFile() throws Exception {
		String bundleName = RandomTestUtil.randomString();
		String bundleSymbolicName = RandomTestUtil.randomString();

		File bundleJarFile = temporaryFolder.newFile("bundle.jar");

		_writeJarFile(
			bundleName, bundleSymbolicName + ";singleton:=true", bundleJarFile);

		_testScanFile(bundleSymbolicName, bundleName, bundleJarFile);

		File libraryJarFile = temporaryFolder.newFile("library.jar");

		_writeJarFile(null, null, libraryJarFile);

		_testScanFile("library", "library", libraryJarFile);

		File lpkgFile = temporaryFolder.newFile("package.lpkg");

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				new FileOutputStream(lpkgFile))) {

			zipOutputStream.putNextEntry(new ZipEntry("bundle.jar"));

			zipOutputStream.write(Files.readAllBytes(bundleJarFile.toPath()));

			zipOutputStream.closeEntry();
		}

		_testScanFile(bundleSymbolicName, bundleName, lpkgFile);
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	public static class ConstantTestClass {

		public void digest() throws Exception {
			String algorithm = "SHA-256";

			MessageDigest.getInstance(algorithm);
		}

	}

	public static class DigesterUtilTestClass {

		public String digest() {
			return DigesterUtil.digestHex(DigesterUtil.MD5, "test");
		}

	}

	public static class KeyFactoryTestClass {

		public PrivateKey getPrivateKey(byte[] bytes) throws Exception {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
		}

		public PublicKey getPublicKey(byte[] bytes) throws Exception {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
		}

	}

	public static class KeyGeneratorTestClass {

		public void generateKey() throws Exception {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

			keyGenerator.init(256);
		}

	}

	public static class MacTestClass {

		public Key getKey(byte[] bytes) throws Exception {
			Mac mac = Mac.getInstance("HmacSHA256");

			return new SecretKeySpec(bytes, mac.getAlgorithm());
		}

	}

	public static class ParameterTestClass {

		public void digest(String algorithm) throws Exception {
			MessageDigest.getInstance(algorithm);
		}

	}

	public static class PropsUtilTestClass {

		public void digest() throws Exception {
			MessageDigest.getInstance(PropsUtil.get("test.algorithm"));
		}

	}

	public static class SignatureTestClass {

		public void verify(PublicKey publicKey) throws Exception {
			Signature signature = Signature.getInstance("SHA256withRSA");

			signature.initVerify(publicKey);
		}

	}

	public static class TrustManagerFactoryTestClass {

		public void getTrustManagerFactory() throws Exception {
			TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		}

	}

	private String _getClassFileName(Class<?> clazz) {
		String name = StringUtil.replace(
			clazz.getName(), CharPool.PERIOD, CharPool.SLASH);

		return name + ".class";
	}

	private String _getDescription(CryptoCall cryptoCall) {
		StringBundler sb = new StringBundler(10);

		sb.append(cryptoCall.getJCAClassName());
		sb.append(StringPool.SPACE);

		if (cryptoCall.getAlgorithm() != null) {
			sb.append(cryptoCall.getAlgorithm());
		}
		else if (ListUtil.isNotEmpty(cryptoCall.getConfiguredBy())) {
			sb.append("configured by ");
			sb.append(String.valueOf(cryptoCall.getConfiguredBy()));
		}
		else {
			sb.append("unresolved: ");
			sb.append(cryptoCall.getUnresolvedReason());
		}

		sb.append(StringPool.SPACE);
		sb.append(
			String.valueOf(
				TransformUtil.transform(
					cryptoCall.getCryptoFunctions(), CryptoFunction::getName)));

		if (cryptoCall.getKeySize() > 0) {
			sb.append(StringPool.SPACE);
			sb.append(String.valueOf(cryptoCall.getKeySize()));
		}

		if (ListUtil.isNotEmpty(cryptoCall.getRelatedCryptoMaterialTypes())) {
			sb.append(StringPool.SPACE);
			sb.append(
				String.valueOf(
					TransformUtil.transform(
						cryptoCall.getRelatedCryptoMaterialTypes(),
						RelatedCryptoMaterialType::getName)));
		}

		return sb.toString();
	}

	private byte[] _read(Class<?> clazz) throws Exception {
		ClassLoader classLoader = clazz.getClassLoader();

		try (InputStream inputStream = classLoader.getResourceAsStream(
				_getClassFileName(clazz))) {

			return inputStream.readAllBytes();
		}
	}

	private void _testScan(Class<?> clazz, String... expectedDescriptions)
		throws Exception {

		CryptoCallScanner cryptoCallScanner = new CryptoCallScanner();

		String bundleSymbolicName = RandomTestUtil.randomString();
		String featureName = RandomTestUtil.randomString();

		try (InputStream inputStream = new ByteArrayInputStream(_read(clazz))) {
			cryptoCallScanner.scan(
				bundleSymbolicName, featureName, inputStream);
		}

		List<CryptoCall> cryptoCalls = cryptoCallScanner.getCryptoCalls();

		for (CryptoCall cryptoCall : cryptoCalls) {
			Assert.assertEquals(
				bundleSymbolicName, cryptoCall.getBundleSymbolicName());
			Assert.assertEquals(
				CryptoCallScannerTest.class.getName(),
				cryptoCall.getClassName());
			Assert.assertEquals(featureName, cryptoCall.getFeatureName());
		}

		List<String> descriptions = TransformUtil.transform(
			cryptoCalls, this::_getDescription);

		descriptions.sort(null);

		Assert.assertEquals(Arrays.asList(expectedDescriptions), descriptions);
	}

	private void _testScanFile(
			String expectedBundleSymbolicName, String expectedFeatureName,
			File file)
		throws Exception {

		CryptoCallScanner cryptoCallScanner = new CryptoCallScanner();

		cryptoCallScanner.scan(file);

		List<CryptoCall> cryptoCalls = cryptoCallScanner.getCryptoCalls();

		Assert.assertEquals(cryptoCalls.toString(), 1, cryptoCalls.size());

		CryptoCall cryptoCall = cryptoCalls.get(0);

		Assert.assertEquals(
			expectedBundleSymbolicName, cryptoCall.getBundleSymbolicName());
		Assert.assertEquals(
			CryptoCallScannerTest.class.getName(), cryptoCall.getClassName());
		Assert.assertEquals(expectedFeatureName, cryptoCall.getFeatureName());
	}

	private void _writeJarFile(
			String bundleName, String bundleSymbolicName, File file)
		throws Exception {

		Manifest manifest = new Manifest();

		Attributes attributes = manifest.getMainAttributes();

		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

		if (bundleName != null) {
			attributes.putValue("Bundle-Name", bundleName);
		}

		if (bundleSymbolicName != null) {
			attributes.putValue("Bundle-SymbolicName", bundleSymbolicName);
		}

		try (JarOutputStream jarOutputStream = new JarOutputStream(
				new FileOutputStream(file), manifest)) {

			jarOutputStream.putNextEntry(
				new JarEntry(_getClassFileName(ConstantTestClass.class)));

			jarOutputStream.write(_read(ConstantTestClass.class));

			jarOutputStream.closeEntry();
		}
	}

}