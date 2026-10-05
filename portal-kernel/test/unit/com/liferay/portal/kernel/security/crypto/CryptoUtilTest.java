/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.crypto;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class CryptoUtilTest {

	@Before
	public void setUp() throws Exception {
		Set<String> memoKeys = ReflectionTestUtil.getFieldValue(
			CryptoUtil.class, "_memoKeys");

		memoKeys.clear();

		AtomicBoolean memoSaturated = ReflectionTestUtil.getFieldValue(
			CryptoUtil.class, "_memoSaturated");

		memoSaturated.set(false);

		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		keyGenerator.init(256);

		_key = keyGenerator.generateKey();
	}

	@After
	public void tearDown() {
		_setApprovedModeProvider(null);
	}

	@Test
	public void testCreateCipher() throws Exception {
		try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class)) {

			Cipher cipher = CryptoUtil.createCipher(
				_key, Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION);

			Assert.assertEquals(_GCM_TRANSFORMATION, cipher.getAlgorithm());

			byte[] initializationVector = cipher.getIV();

			Assert.assertEquals(
				Arrays.toString(initializationVector), 12,
				initializationVector.length);

			byte[] bytes = RandomTestUtil.randomBytes();

			byte[] encryptedBytes = cipher.doFinal(bytes);

			cipher = CryptoUtil.createCipher(
				new GCMParameterSpec(128, initializationVector), _key,
				Cipher.DECRYPT_MODE, _GCM_TRANSFORMATION);

			Assert.assertArrayEquals(bytes, cipher.doFinal(encryptedBytes));

			CryptoUtil.createCipher(
				_createGCMParameterSpec(), _key, Cipher.ENCRYPT_MODE,
				_GCM_TRANSFORMATION);

			cipher = CryptoUtil.createCipher(
				_key, Cipher.ENCRYPT_MODE, "AES/ECB/PKCS5Padding");

			Assert.assertEquals("AES/ECB/PKCS5Padding", cipher.getAlgorithm());

			mockedStatic.verifyNoInteractions();
		}
	}

	@Test
	public void testCreateCipherInFIPSMode() throws Exception {
		try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			CryptoUtil.createCipher(
				_createGCMParameterSpec(), _key, Cipher.DECRYPT_MODE,
				_GCM_TRANSFORMATION);

			mockedStatic.verifyNoInteractions();

			Assert.assertThrows(
				InvalidAlgorithmParameterException.class,
				() -> CryptoUtil.createCipher(
					_createGCMParameterSpec(), _key, Cipher.ENCRYPT_MODE,
					_GCM_TRANSFORMATION));

			_assertAlgorithmRejected(
				true, mockedStatic, "caller-supplied-parameters",
				_GCM_TRANSFORMATION);

			String keyAlgorithm = RandomTestUtil.randomString();

			Assert.assertThrows(
				InvalidKeyException.class,
				() -> CryptoUtil.createCipher(
					new SecretKeySpec(new byte[32], keyAlgorithm),
					Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION));

			_assertAlgorithmRejected(
				true, mockedStatic, "key-algorithm-not-allowed", keyAlgorithm);

			Assert.assertThrows(
				InvalidKeyException.class,
				() -> CryptoUtil.createCipher(
					new SecretKeySpec(new byte[8], "AES"), Cipher.ENCRYPT_MODE,
					_GCM_TRANSFORMATION));

			_assertAlgorithmRejected(
				true, mockedStatic, "key-size-not-allowed", "AES");

			Assert.assertThrows(
				NoSuchAlgorithmException.class,
				() -> CryptoUtil.createCipher(
					_key, Cipher.DECRYPT_MODE, "AES/ECB/PKCS5Padding"));

			_assertAlgorithmRejected(
				true, mockedStatic, "algorithm-not-allowed",
				"AES/ECB/PKCS5Padding");
		}
	}

	@Test
	public void testCreateCipherWhenTheAuditFails() throws Exception {
		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				CryptoUtil.class.getName(), LoggerTestUtil.ERROR);
			MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			mockedStatic.when(
				() -> FIPSAuditUtil.write(Mockito.any())
			).thenThrow(
				new IllegalStateException()
			);

			Assert.assertThrows(
				InvalidKeyException.class,
				() -> CryptoUtil.createCipher(
					new SecretKeySpec(new byte[8], "AES"), Cipher.ENCRYPT_MODE,
					_GCM_TRANSFORMATION));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to write the FIPS audit event \"algorithm-rejected\"",
				logEntry.getMessage());
		}
	}

	@Test
	public void testCreateCipherWhenTheMemoIsSaturated() throws Exception {
		Set<String> memoKeys = ReflectionTestUtil.getFieldValue(
			CryptoUtil.class, "_memoKeys");

		for (int i = 0; i < 1024; i++) {
			memoKeys.add(RandomTestUtil.randomString());
		}

		try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertThrows(
				InvalidKeyException.class,
				() -> CryptoUtil.createCipher(
					new SecretKeySpec(new byte[8], "AES"), Cipher.ENCRYPT_MODE,
					_GCM_TRANSFORMATION));

			_assertAlgorithmRejected(false, mockedStatic, "memo-saturated", "");

			Assert.assertThrows(
				InvalidKeyException.class,
				() -> CryptoUtil.createCipher(
					new SecretKeySpec(new byte[8], "AES"), Cipher.ENCRYPT_MODE,
					_GCM_TRANSFORMATION));

			mockedStatic.verifyNoInteractions();
		}
	}

	@Test
	public void testCreateCipherWhenTheModuleFails() throws Exception {
		Provider provider = _createProvider();

		provider.put(
			"Cipher.AES/GCM/NoPadding", FailingCipherSpi.class.getName());

		_setApprovedModeProvider(provider);

		try (MockedStatic<FIPSAuditUtil> mockedStatic = Mockito.mockStatic(
				FIPSAuditUtil.class);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertThrows(
				NoSuchAlgorithmException.class,
				() -> CryptoUtil.createCipher(
					_key, Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION));

			ArgumentCaptor<FIPSAuditEvent> argumentCaptor =
				ArgumentCaptor.forClass(FIPSAuditEvent.class);

			mockedStatic.verify(
				() -> FIPSAuditUtil.write(argumentCaptor.capture()));

			FIPSAuditEvent fipsAuditEvent = argumentCaptor.getValue();

			Assert.assertEquals(
				"provider-failure", fipsAuditEvent.getEventType());
			Assert.assertEquals(
				FIPSAuditEvent.Severity.CRITICAL, fipsAuditEvent.getSeverity());

			Map<String, Object> fields = fipsAuditEvent.getFields();

			Assert.assertEquals(
				provider.getName(), fields.get("provider-name"));
			Assert.assertEquals(
				"java.lang.AssertionError: Self test failed",
				fields.get("provider-error-message"));
		}
	}

	@Test
	public void testCreateCipherWhenTheModuleRefuses() throws Exception {
		Provider provider = _createProvider();

		_setApprovedModeProvider(provider);

		try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class);
			MockedStatic<FIPSModeValidator> fipsModeValidatorMockedStatic =
				Mockito.mockStatic(
					FIPSModeValidator.class, Mockito.CALLS_REAL_METHODS);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			fipsModeValidatorMockedStatic.when(
				() -> FIPSModeValidator.isHealthyProvider(provider)
			).thenReturn(
				true
			);

			Assert.assertThrows(
				NoSuchAlgorithmException.class,
				() -> CryptoUtil.createCipher(
					_key, Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION));

			Map<String, Object> fields = _assertAlgorithmRejected(
				true, fipsAuditUtilMockedStatic, "provider-refused",
				_GCM_TRANSFORMATION);

			Assert.assertEquals(
				provider.getName(), fields.get("provider-name"));
		}
	}

	public static class FailingCipherSpi extends CipherSpi {

		public FailingCipherSpi() {
			throw new AssertionError("Self test failed");
		}

		@Override
		protected byte[] engineDoFinal(
			byte[] inputBytes, int inputOffset, int inputLength) {

			return null;
		}

		@Override
		protected int engineDoFinal(
			byte[] inputBytes, int inputOffset, int inputLength,
			byte[] outputBytes, int outputOffset) {

			return 0;
		}

		@Override
		protected int engineGetBlockSize() {
			return 0;
		}

		@Override
		protected byte[] engineGetIV() {
			return null;
		}

		@Override
		protected int engineGetOutputSize(int inputLength) {
			return 0;
		}

		@Override
		protected AlgorithmParameters engineGetParameters() {
			return null;
		}

		@Override
		protected void engineInit(
			int mode, Key key, AlgorithmParameters algorithmParameters,
			SecureRandom secureRandom) {
		}

		@Override
		protected void engineInit(
			int mode, Key key, AlgorithmParameterSpec algorithmParameterSpec,
			SecureRandom secureRandom) {
		}

		@Override
		protected void engineInit(
			int mode, Key key, SecureRandom secureRandom) {
		}

		@Override
		protected void engineSetMode(String mode) {
		}

		@Override
		protected void engineSetPadding(String padding) {
		}

		@Override
		protected byte[] engineUpdate(
			byte[] inputBytes, int inputOffset, int inputLength) {

			return null;
		}

		@Override
		protected int engineUpdate(
			byte[] inputBytes, int inputOffset, int inputLength,
			byte[] outputBytes, int outputOffset) {

			return 0;
		}

	}

	private Map<String, Object> _assertAlgorithmRejected(
		boolean enforced, MockedStatic<FIPSAuditUtil> mockedStatic,
		String rejectionReason, String requestedAlgorithm) {

		ArgumentCaptor<FIPSAuditEvent> argumentCaptor = ArgumentCaptor.forClass(
			FIPSAuditEvent.class);

		mockedStatic.verify(
			() -> FIPSAuditUtil.write(argumentCaptor.capture()));

		mockedStatic.clearInvocations();

		FIPSAuditEvent fipsAuditEvent = argumentCaptor.getValue();

		Assert.assertEquals(
			"algorithm-rejected", fipsAuditEvent.getEventType());

		Map<String, Object> fields = fipsAuditEvent.getFields();

		Assert.assertEquals(enforced, fields.get("enforced"));
		Assert.assertEquals(
			Cipher.class.getName(), fields.get("factory-class-name"));
		Assert.assertEquals(rejectionReason, fields.get("rejection-reason"));
		Assert.assertEquals(
			requestedAlgorithm, fields.get("requested-algorithm"));

		return fields;
	}

	private GCMParameterSpec _createGCMParameterSpec() {
		String initializationVector = RandomTestUtil.randomString(12);

		return new GCMParameterSpec(128, initializationVector.getBytes());
	}

	private Provider _createProvider() {
		return new Provider(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString()) {
		};
	}

	private void _setApprovedModeProvider(Provider provider) {
		AtomicReference<Provider> atomicReference =
			ReflectionTestUtil.getFieldValue(
				FIPSModeValidator.class,
				"_approvedModeProviderAtomicReference");

		atomicReference.set(provider);
	}

	private static final String _GCM_TRANSFORMATION = "AES/GCM/NoPadding";

	private Key _key;

}