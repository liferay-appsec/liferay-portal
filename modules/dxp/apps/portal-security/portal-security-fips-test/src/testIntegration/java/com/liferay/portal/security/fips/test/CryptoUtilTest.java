/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.fips.test.util.FIPSTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class CryptoUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Assume.assumeTrue(PropsValues.FIPS_ENABLED);

		_approvedModeProvider = FIPSModeValidator.fetchApprovedModeProvider();

		Assert.assertNotNull(_approvedModeProvider);
	}

	@Test
	public void testCreateCipher() throws Exception {
		List<String> messages = new ArrayList<>();

		Logger logger = Logger.getLogger(
			"org.bouncycastle.jcajce.provider.BaseCipher");

		Handler handler = new Handler() {

			@Override
			public void close() {
			}

			@Override
			public void flush() {
			}

			@Override
			public void publish(LogRecord logRecord) {
				messages.add(logRecord.getMessage());
			}

		};

		Level level = logger.getLevel();

		logger.addHandler(handler);
		logger.setLevel(Level.FINE);

		try {
			Key key = EncryptorUtil.generateKey();

			Cipher cipher = CryptoUtil.createCipher(
				key, Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION);

			Assert.assertSame(_approvedModeProvider, cipher.getProvider());

			byte[] initializationVector = cipher.getIV();

			Assert.assertEquals(
				Arrays.toString(initializationVector), 12,
				initializationVector.length);

			byte[] bytes = RandomTestUtil.randomBytes();

			byte[] encryptedBytes = cipher.doFinal(bytes);

			cipher = CryptoUtil.createCipher(
				new GCMParameterSpec(128, initializationVector), key,
				Cipher.DECRYPT_MODE, _GCM_TRANSFORMATION);

			Assert.assertSame(_approvedModeProvider, cipher.getProvider());
			Assert.assertArrayEquals(bytes, cipher.doFinal(encryptedBytes));

			String plaintext = RandomTestUtil.randomString();

			Assert.assertEquals(
				plaintext,
				EncryptorUtil.decryptAuthenticated(
					key, EncryptorUtil.encryptAuthenticated(key, plaintext)));

			Assert.assertThrows(
				InvalidAlgorithmParameterException.class,
				() -> CryptoUtil.createCipher(
					new GCMParameterSpec(128, initializationVector), key,
					Cipher.ENCRYPT_MODE, _GCM_TRANSFORMATION));
		}
		finally {
			logger.removeHandler(handler);
			logger.setLevel(level);
		}

		Assert.assertFalse(
			messages.toString(),
			messages.contains("Passed in GCM nonce detected"));

		boolean callerSuppliedParameters = false;

		for (JSONObject jsonObject : FIPSTestUtil.getAuditLogJSONObjects()) {
			if (!Objects.equals(
					jsonObject.getString("event-type"), "algorithm-rejected")) {

				continue;
			}

			JSONObject fieldsJSONObject = jsonObject.getJSONObject("fields");

			if (!Objects.equals(
					fieldsJSONObject.getString("requested-algorithm"),
					_GCM_TRANSFORMATION)) {

				continue;
			}

			String rejectionReason = fieldsJSONObject.getString(
				"rejection-reason");

			Assert.assertNotEquals("served-outside-module", rejectionReason);

			if (Objects.equals(rejectionReason, "caller-supplied-parameters")) {
				callerSuppliedParameters = true;
			}
		}

		Assert.assertTrue(callerSuppliedParameters);
	}

	@Test
	public void testCreateKeyGenerator() throws Exception {
		KeyGenerator keyGenerator = CryptoUtil.createKeyGenerator("AES", 128);

		Assert.assertSame(_approvedModeProvider, keyGenerator.getProvider());

		Key key = keyGenerator.generateKey();

		byte[] encodedBytes = key.getEncoded();

		Assert.assertEquals(
			Arrays.toString(encodedBytes), 32, encodedBytes.length);

		key = EncryptorUtil.generateKey();

		encodedBytes = key.getEncoded();

		Assert.assertEquals(
			Arrays.toString(encodedBytes), 32, encodedBytes.length);
	}

	@Test
	public void testCreateMessageDigest() throws Exception {
		for (String algorithm : new String[] {"MD5", "SHA-1"}) {
			MessageDigest messageDigest = CryptoUtil.createMessageDigest(
				algorithm, CryptoFunctionApprovalType.ALLOWED);

			Provider provider = messageDigest.getProvider();

			Assert.assertTrue(
				provider.getName(),
				_hasAuditLogJSONObject(
					algorithm, CryptoFunctionApprovalType.ALLOWED,
					"algorithm-served", provider.getName()));
		}

		MessageDigest messageDigest = CryptoUtil.createMessageDigest("SHA-256");

		Assert.assertSame(_approvedModeProvider, messageDigest.getProvider());

		Assert.assertThrows(
			NoSuchAlgorithmException.class,
			() -> CryptoUtil.createMessageDigest("MD5"));
	}

	private boolean _hasAuditLogJSONObject(
			String algorithm, CryptoFunctionApprovalType approvalType,
			String eventType, String providerName)
		throws Exception {

		for (JSONObject jsonObject : FIPSTestUtil.getAuditLogJSONObjects()) {
			JSONObject fieldsJSONObject = jsonObject.getJSONObject("fields");

			if (Objects.equals(jsonObject.getString("event-type"), eventType) &&
				Objects.equals(
					fieldsJSONObject.getString("requested-algorithm"),
					algorithm) &&
				Objects.equals(
					fieldsJSONObject.getString("crypto-function-approval-type"),
					approvalType.name()) &&
				Objects.equals(
					fieldsJSONObject.getString("provider-name"),
					providerName)) {

				return true;
			}
		}

		return false;
	}

	private static final String _GCM_TRANSFORMATION = "AES/GCM/NoPadding";

	private Provider _approvedModeProvider;

}