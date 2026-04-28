/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.internal.security;

import com.liferay.ai.hub.cell.configuration.AIHubCellSecretConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 * @author Pedro Victor Silvestre
 */
public class JWTTokenImplTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_jwtTokenImpl = new JWTTokenImpl();

		Map<Long, Map<String, String>> configStates = new HashMap<>();

		ConfigurationProvider configurationProvider = Mockito.mock(
			ConfigurationProvider.class);

		Mockito.when(
			configurationProvider.getCompanyConfiguration(
				Mockito.eq(AIHubCellSecretConfiguration.class),
				Mockito.anyLong())
		).thenAnswer(
			invocation -> {
				long companyId = invocation.getArgument(1);

				Map<String, String> state = configStates.computeIfAbsent(
					companyId, k -> new HashMap<>());

				AIHubCellSecretConfiguration aiHubCellSecretConfiguration =
					Mockito.mock(AIHubCellSecretConfiguration.class);

				Mockito.when(
					aiHubCellSecretConfiguration.secret()
				).thenReturn(
					state.getOrDefault("secret", "")
				);

				return aiHubCellSecretConfiguration;
			}
		);

		Mockito.doAnswer(
			invocation -> {
				long companyId = invocation.getArgument(1);

				Dictionary<String, Object> props = invocation.getArgument(2);

				configStates.computeIfAbsent(
					companyId, k -> new HashMap<>()
				).put(
					"secret", (String)props.get("secret")
				);

				return null;
			}
		).when(
			configurationProvider
		).saveCompanyConfiguration(
			Mockito.eq(AIHubCellSecretConfiguration.class), Mockito.anyLong(),
			Mockito.any()
		);

		ReflectionTestUtil.setFieldValue(
			_jwtTokenImpl, "_configurationProvider", configurationProvider);
	}

	@Test
	public void testGenerateToken() throws Exception {
		String token = _jwtTokenImpl.generateToken(
			_COMPANY_ID, TimeUnit.MINUTES.toMillis(1), _ISSUER, _USER_ID);

		Assert.assertNotNull(token);
		Assert.assertFalse(token.isEmpty());

		SignedJWT signedJWT = SignedJWT.parse(token);

		JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

		Assert.assertEquals(_ISSUER, jwtClaimsSet.getIssuer());
		Assert.assertEquals(
			String.valueOf(_USER_ID), jwtClaimsSet.getSubject());
	}

	@Test
	public void testGetUserId() throws Exception {
		String token = _jwtTokenImpl.generateToken(
			_COMPANY_ID, TimeUnit.MINUTES.toMillis(1), _ISSUER, _USER_ID);

		Assert.assertEquals(
			_USER_ID, _jwtTokenImpl.getUserId(_COMPANY_ID, token));

		_testGetUserId("Invalid JWT signature", _COMPANY_ID_OTHER, token);

		_testGetUserId(
			"Invalid JWT signature", _COMPANY_ID,
			token.substring(0, token.length() - 5) + "abcde");

		_testGetUserId(
			"The JWT token is expired", _COMPANY_ID,
			_jwtTokenImpl.generateToken(_COMPANY_ID, 0, _ISSUER, _USER_ID));
		_testGetUserId(
			"Unable to parse and verify the JWT token", _COMPANY_ID,
			RandomTestUtil.randomString());
	}

	private void _testGetUserId(
		String expectedLogMessage, long companyId, String token) {

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.ai.hub.cell.internal.security.JWTTokenImpl",
				LoggerTestUtil.DEBUG)) {

			_jwtTokenImpl.getUserId(companyId, token);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(LoggerTestUtil.DEBUG, logEntry.getPriority());

			Assert.assertEquals(expectedLogMessage, logEntry.getMessage());
		}
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _COMPANY_ID_OTHER = RandomTestUtil.randomLong();

	private static final String _ISSUER = RandomTestUtil.randomString();

	private static final long _USER_ID = RandomTestUtil.randomLong();

	private JWTTokenImpl _jwtTokenImpl;

}