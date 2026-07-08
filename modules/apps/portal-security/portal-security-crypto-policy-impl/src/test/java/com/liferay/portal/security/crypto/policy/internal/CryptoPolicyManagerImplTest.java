/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.internal;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.security.crypto.policy.constants.CryptoServiceTypes;
import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Manuele Castro
 */
public class CryptoPolicyManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_cryptoPolicyManagerImpl = new CryptoPolicyManagerImpl();

		_safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_ENABLED", true);
	}

	@After
	public void tearDown() {
		_safeCloseable.close();
	}

	@Test
	public void testEmptyKeySizeSetAccepts() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"HmacSHA256", CryptoServiceTypes.KEY_GENERATOR, 256);
	}

	@Test
	public void testFIPSApprovedAlgorithmDoesNotThrow() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"SHA-256", CryptoServiceTypes.MESSAGE_DIGEST);
	}

	@Test(expected = CryptoPolicyException.class)
	public void testFIPSNonapprovedAlgorithmThrows() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"MD5", CryptoServiceTypes.MESSAGE_DIGEST);
	}

	@Test
	public void testGetApprovedAlgorithmFallsBackWhenAlgorithmNotApproved() {
		Assert.assertEquals(
			"SHA-256",
			_cryptoPolicyManagerImpl.getApprovedAlgorithm(
				"MD5", CryptoServiceTypes.MESSAGE_DIGEST, "SHA-256"));
	}

	@Test
	public void testGetApprovedAlgorithmNonfipsReturnsAlgorithmUnchanged() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(false);

		Assert.assertEquals(
			"MD5",
			cryptoPolicyManagerImpl.getApprovedAlgorithm(
				"MD5", CryptoServiceTypes.MESSAGE_DIGEST, "SHA-256"));
	}

	@Test
	public void testGetApprovedAlgorithmReturnsApprovedAlgorithmUnchanged() {
		Assert.assertEquals(
			"SHA-256",
			_cryptoPolicyManagerImpl.getApprovedAlgorithm(
				"SHA-256", CryptoServiceTypes.MESSAGE_DIGEST, "SHA-512"));
	}

	@Test
	public void testGetApprovedAlgorithmsExcludesLegacyPreservingOrder() {
		Assert.assertEquals(
			List.of("SHA-256", "SHA-512"),
			_cryptoPolicyManagerImpl.getApprovedAlgorithms(
				List.of("MD5", "SHA-256", "SHA-1", "SHA-512"),
				CryptoServiceTypes.MESSAGE_DIGEST));
	}

	@Test
	public void testGetApprovedAlgorithmsReturnsApprovedSubsetOfCandidates() {
		List<String> allowedAlgorithms =
			_cryptoPolicyManagerImpl.getApprovedAlgorithms(
				List.of("RSA", "DSA"), CryptoServiceTypes.KEY_PAIR_GENERATOR);

		Assert.assertEquals(List.of("RSA"), allowedAlgorithms);
		Assert.assertFalse(allowedAlgorithms.contains("EC"));
		Assert.assertFalse(allowedAlgorithms.contains("DSA"));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testGetApprovedAlgorithmThrowsWhenFallbackAlgorithmNotApproved() {
		_cryptoPolicyManagerImpl.getApprovedAlgorithm(
			"MD5", CryptoServiceTypes.MESSAGE_DIGEST, "MD2");
	}

	@Test
	public void testGetApprovedKeySizesFiltersToApproved() {
		Assert.assertEquals(
			List.of(2048, 3072, 4096),
			_cryptoPolicyManagerImpl.getApprovedKeySizes(
				"RSA", List.of(1024, 2048, 3072, 4096)));
	}

	@Test
	public void testGetApprovedKeySizesUnconstrainedReturnsAllCandidates() {
		List<Integer> candidateKeySizes = List.of(128, 256);

		Assert.assertEquals(
			candidateKeySizes,
			_cryptoPolicyManagerImpl.getApprovedKeySizes(
				"SHA-256", candidateKeySizes));
	}

	@Test
	public void testGetApprovedNonfipsReturnsCandidatesUnchanged() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(false);

		List<String> candidateAlgorithms = List.of("MD5", "SHA-256");

		Assert.assertSame(
			candidateAlgorithms,
			cryptoPolicyManagerImpl.getApprovedAlgorithms(
				candidateAlgorithms, CryptoServiceTypes.MESSAGE_DIGEST));

		List<Integer> candidateKeySizes = List.of(56, 128);

		Assert.assertSame(
			candidateKeySizes,
			cryptoPolicyManagerImpl.getApprovedKeySizes(
				"AES", candidateKeySizes));
	}

	@Test
	public void testKeySizeConstraintAcceptsApprovedSize() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"AES", CryptoServiceTypes.CIPHER, 192);
	}

	@Test(expected = CryptoPolicyException.class)
	public void testKeySizeConstraintRejectsBadSize() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"AES", CryptoServiceTypes.CIPHER, 200);
	}

	@Test
	public void testNonfipsAcceptsAnyAlgorithm() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(false);

		cryptoPolicyManagerImpl.checkAlgorithm(
			"MD5", CryptoServiceTypes.MESSAGE_DIGEST);
		cryptoPolicyManagerImpl.checkAlgorithm(
			"DES", CryptoServiceTypes.CIPHER, 56);
	}

	@Test
	public void testNormalizationReducesCipherTransformation() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"AES/GCM/NoPadding", CryptoServiceTypes.CIPHER, 256);
	}

	@Test(expected = CryptoPolicyException.class)
	public void testNormalizationStillRejectsNonapprovedBase() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"DES/CBC/PKCS5Padding", CryptoServiceTypes.CIPHER);
	}

	@Test
	public void testSHA1AllowedInHmacAndPBKDF2() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"HmacSHA1", CryptoServiceTypes.MAC);
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"PBKDF2WithHmacSHA1", CryptoServiceTypes.SECRET_KEY_FACTORY);
	}

	@Test(expected = CryptoPolicyException.class)
	public void testSHA1DisallowedForSignature() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"SHA1withRSA", CryptoServiceTypes.SIGNATURE);
	}

	@Test
	public void testSHA512SlashDigestIsApproved() {
		_cryptoPolicyManagerImpl.checkAlgorithm(
			"SHA-512/256", CryptoServiceTypes.MESSAGE_DIGEST);
	}

	private CryptoPolicyManagerImpl _create(boolean fipsEnabled) {
		_safeCloseable.close();

		_safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_ENABLED", fipsEnabled);

		return new CryptoPolicyManagerImpl();
	}

	private CryptoPolicyManagerImpl _cryptoPolicyManagerImpl;
	private SafeCloseable _safeCloseable;

}