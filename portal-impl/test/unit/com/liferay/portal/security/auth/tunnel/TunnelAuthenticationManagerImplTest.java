/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.auth.tunnel;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.auth.AuthException;
import com.liferay.portal.kernel.security.auth.RemoteAuthException;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Lucas Miranda
 */
public class TunnelAuthenticationManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetSharedSecretKey() throws Exception {
		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", false);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "Blowfish");
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET", _SHARED_SECRET);
			SafeCloseable safeCloseable4 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET_HEX", false)) {

			Key key = _getSharedSecretKey();

			Assert.assertEquals("Blowfish", key.getAlgorithm());
		}

		try (SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable("FIPS_ENABLED", true);
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET", _SHARED_SECRET);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET_HEX", false)) {

			try (SafeCloseable safeCloseable4 =
					PropsValuesTestUtil.swapWithSafeCloseable(
						"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "AES")) {

				Key key = _getSharedSecretKey();

				Assert.assertEquals("AES", key.getAlgorithm());
			}
		}
	}

	@Test
	public void testGetUserId() throws Exception {
		long userId = RandomTestUtil.randomLong();

		String login = String.valueOf(userId);

		String password = RandomTestUtil.randomString();

		Encryptor encryptor = Mockito.mock(Encryptor.class);

		Mockito.when(
			encryptor.decrypt(Mockito.any(Key.class), Mockito.eq(password))
		).thenReturn(
			login
		);

		User user = Mockito.mock(User.class);

		Mockito.when(
			user.getUserId()
		).thenReturn(
			userId
		);

		UserLocalService userLocalService = Mockito.mock(
			UserLocalService.class);

		Mockito.when(
			userLocalService.fetchUser(userId)
		).thenReturn(
			user
		);

		try (AutoCloseable autoCloseable1 = _setEncryptor(encryptor);
			AutoCloseable autoCloseable2 =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					UserLocalServiceUtil.class, "_service", userLocalService);
			SafeCloseable safeCloseable1 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_ENCRYPTION_ALGORITHM", "AES");
			SafeCloseable safeCloseable2 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET", _SHARED_SECRET);
			SafeCloseable safeCloseable3 =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"TUNNELING_SERVLET_SHARED_SECRET_HEX", false)) {

			TunnelAuthenticationManagerImpl tunnelAuthenticationManagerImpl =
				new TunnelAuthenticationManagerImpl();

			Assert.assertEquals(
				userId,
				tunnelAuthenticationManagerImpl.getUserId(
					_getHttpServletRequest(login, password)));

			_assertWrongSharedSecret(
				_getHttpServletRequest(login, null),
				tunnelAuthenticationManagerImpl);
			_assertWrongSharedSecret(
				_getHttpServletRequest(RandomTestUtil.randomString(), password),
				tunnelAuthenticationManagerImpl);

			Mockito.when(
				encryptor.decrypt(Mockito.any(Key.class), Mockito.eq(password))
			).thenThrow(
				new EncryptorException(RandomTestUtil.randomString())
			);

			_assertWrongSharedSecret(
				_getHttpServletRequest(login, password),
				tunnelAuthenticationManagerImpl);
		}
	}

	private void _assertWrongSharedSecret(
		HttpServletRequest httpServletRequest,
		TunnelAuthenticationManagerImpl tunnelAuthenticationManagerImpl) {

		AuthException authException = Assert.assertThrows(
			RemoteAuthException.class,
			() -> tunnelAuthenticationManagerImpl.getUserId(
				httpServletRequest));

		Assert.assertEquals(
			RemoteAuthException.WRONG_SHARED_SECRET, authException.getType());
	}

	private HttpServletRequest _getHttpServletRequest(
		String login, String password) {

		String credentials = login;

		if (password != null) {
			credentials = login + ":" + password;
		}

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader(
			HttpHeaders.AUTHORIZATION,
			"Basic " +
				Base64.encode(credentials.getBytes(StandardCharsets.UTF_8)));

		return mockHttpServletRequest;
	}

	private Key _getSharedSecretKey() {
		return ReflectionTestUtil.invoke(
			new TunnelAuthenticationManagerImpl(), "getSharedSecretKey",
			new Class<?>[0]);
	}

	private AutoCloseable _setEncryptor(Encryptor encryptor) {
		Snapshot<Encryptor> snapshot = Mockito.mock(Snapshot.class);

		Mockito.when(
			snapshot.get()
		).thenReturn(
			encryptor
		);

		return ReflectionTestUtil.setFieldValueWithAutoCloseable(
			EncryptorUtil.class, "_encryptorSnapshot", snapshot);
	}

	private static final String _SHARED_SECRET = RandomTestUtil.randomString(
		16);

}