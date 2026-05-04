/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.internal.security;

import com.liferay.ai.hub.cell.configuration.AIHubCellConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class JWTTokenProviderImplTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_jwtTokenProviderImpl = _newJWTTokenProviderImpl();
	}

	@Test
	public void testGenerateToken() throws Exception {
		String token = _generateToken(
			TimeUnit.MINUTES.toMillis(1), _ISSUER, _USER_ID);

		Assert.assertFalse(token.isEmpty());

		SignedJWT signedJWT = SignedJWT.parse(token);

		JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

		Assert.assertEquals(_ISSUER, jwtClaimsSet.getIssuer());
		Assert.assertEquals(
			String.valueOf(_USER_ID), jwtClaimsSet.getSubject());
	}

	@Test
	public void testGetUserId() throws Exception {
		String token = _generateToken(
			TimeUnit.MINUTES.toMillis(1), _ISSUER, _USER_ID);

		Assert.assertEquals(_USER_ID, _jwtTokenProviderImpl.getUserId(token));

		_testGetUserId(
			"Invalid JWT signature",
			token.substring(0, token.length() - 5) + "abcde");

		_testGetUserId(
			"The JWT token is expired", _generateToken(0, _ISSUER, _USER_ID));
		_testGetUserId(
			"Unable to parse and verify the JWT token",
			RandomTestUtil.randomString());

		_jwtTokenProviderImpl = _newJWTTokenProviderImpl();

		_testGetUserId("Invalid JWT signature", token);
	}

	private String _generateToken(
		long expirationTime, String issuer, long userId) {

		return ReflectionTestUtil.invoke(
			_jwtTokenProviderImpl, "_generateToken",
			new Class<?>[] {long.class, String.class, long.class},
			expirationTime, issuer, userId);
	}

	private AIHubCellConfiguration _mockAIHubCellConfiguration() {
		AIHubCellConfiguration aiHubCellConfiguration = Mockito.mock(
			AIHubCellConfiguration.class);

		int sha256BlockSize = 64;

		byte[] secretBytes = new byte[sha256BlockSize];

		for (int i = 0; i < secretBytes.length; i++) {
			secretBytes[i] = SecureRandomUtil.nextByte();
		}

		Mockito.when(
			aiHubCellConfiguration.secret()
		).thenReturn(
			Base64.encode(secretBytes)
		);

		return aiHubCellConfiguration;
	}

	private JWTTokenProviderImpl _newJWTTokenProviderImpl() {
		ConfigurationProvider configurationProvider = Mockito.mock(
			ConfigurationProvider.class);

		AIHubCellConfiguration aiHubCellConfiguration =
			_mockAIHubCellConfiguration();

		try {
			Mockito.when(
				configurationProvider.getCompanyConfiguration(
					Mockito.eq(AIHubCellConfiguration.class), Mockito.anyLong())
			).thenReturn(
				aiHubCellConfiguration
			);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}

		JWTTokenProviderImpl jwtTokenProviderImpl = new JWTTokenProviderImpl();

		ReflectionTestUtil.setFieldValue(
			jwtTokenProviderImpl, "_configurationProvider",
			configurationProvider);

		return jwtTokenProviderImpl;
	}

	private void _testGetUserId(String expectedLogMessage, String token) {
		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.ai.hub.cell.internal.security." +
					"JWTTokenProviderImpl",
				LoggerTestUtil.DEBUG)) {

			_jwtTokenProviderImpl.getUserId(token);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(LoggerTestUtil.DEBUG, logEntry.getPriority());

			Assert.assertEquals(expectedLogMessage, logEntry.getMessage());
		}
	}

	private static final String _ISSUER = RandomTestUtil.randomString();

	private static final long _USER_ID = RandomTestUtil.randomLong();

	private JWTTokenProviderImpl _jwtTokenProviderImpl;

}