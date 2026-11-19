/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.service.impl;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.PasswordPolicy;
import com.liferay.portal.kernel.model.PasswordTracker;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.service.PasswordPolicyLocalService;
import com.liferay.portal.kernel.service.persistence.PasswordTrackerPersistence;
import com.liferay.portal.kernel.service.persistence.UserPersistence;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Pedro Victor Silvestre
 */
public class PasswordTrackerLocalServiceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_passwordTrackerLocalServiceImpl =
			new PasswordTrackerLocalServiceImpl();

		_passwordPolicyLocalService = Mockito.mock(
			PasswordPolicyLocalService.class);
		_passwordTrackerPersistence = Mockito.mock(
			PasswordTrackerPersistence.class);
		_userPersistence = Mockito.mock(UserPersistence.class);

		ReflectionTestUtil.setFieldValue(
			_passwordTrackerLocalServiceImpl, "_passwordPolicyLocalService",
			_passwordPolicyLocalService);
		ReflectionTestUtil.setFieldValue(
			_passwordTrackerLocalServiceImpl, "passwordTrackerPersistence",
			_passwordTrackerPersistence);
		ReflectionTestUtil.setFieldValue(
			_passwordTrackerLocalServiceImpl, "_userPersistence",
			_userPersistence);
	}

	@Test
	public void testIsSameAsCurrentPassword() throws Exception {
		_setUpCurrentPassword(_PBKDF2_HASH);

		try (MockedStatic<PasswordEncryptorUtil> mockedStatic =
				Mockito.mockStatic(PasswordEncryptorUtil.class)) {

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(_NEW_PASSWORD, _PBKDF2_HASH)
			).thenReturn(
				_PBKDF2_HASH
			);

			Assert.assertTrue(
				_passwordTrackerLocalServiceImpl.isSameAsCurrentPassword(
					_USER_ID, _NEW_PASSWORD));

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(_NEW_PASSWORD, _PBKDF2_HASH)
			).thenReturn(
				"{PBKDF2WithHmacSHA256}other"
			);

			Assert.assertFalse(
				_passwordTrackerLocalServiceImpl.isSameAsCurrentPassword(
					_USER_ID, _NEW_PASSWORD));
		}
	}

	@Test
	public void testIsSameAsCurrentPasswordWithLegacyHashInFIPSMode()
		throws Exception {

		_setUpCurrentPassword(_LEGACY_HASH);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertFalse(
				_passwordTrackerLocalServiceImpl.isSameAsCurrentPassword(
					_USER_ID, _NEW_PASSWORD));
		}
	}

	@Test
	public void testIsValidPassword() throws Exception {
		_setUpPasswordHistory(_PBKDF2_HASH);

		try (MockedStatic<PasswordEncryptorUtil> mockedStatic =
				Mockito.mockStatic(PasswordEncryptorUtil.class)) {

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(_NEW_PASSWORD, _PBKDF2_HASH)
			).thenReturn(
				"{PBKDF2WithHmacSHA256}other"
			);

			Assert.assertTrue(
				_passwordTrackerLocalServiceImpl.isValidPassword(
					_USER_ID, _NEW_PASSWORD));

			mockedStatic.when(
				() -> PasswordEncryptorUtil.encrypt(_NEW_PASSWORD, _PBKDF2_HASH)
			).thenReturn(
				_PBKDF2_HASH
			);

			Assert.assertFalse(
				_passwordTrackerLocalServiceImpl.isValidPassword(
					_USER_ID, _NEW_PASSWORD));
		}
	}

	@Test
	public void testIsValidPasswordWithLegacyHashInFIPSMode() throws Exception {
		_setUpPasswordHistory(_LEGACY_HASH);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			Assert.assertTrue(
				_passwordTrackerLocalServiceImpl.isValidPassword(
					_USER_ID, _NEW_PASSWORD));
		}
	}

	private void _setUpCurrentPassword(String encryptedPassword)
		throws Exception {

		User user = Mockito.mock(User.class);

		Mockito.when(
			user.isPasswordEncrypted()
		).thenReturn(
			true
		);

		Mockito.when(
			user.getPassword()
		).thenReturn(
			encryptedPassword
		);

		Mockito.when(
			_userPersistence.findByPrimaryKey(_USER_ID)
		).thenReturn(
			user
		);
	}

	private void _setUpPasswordHistory(String encryptedPassword)
		throws Exception {

		PasswordPolicy passwordPolicy = Mockito.mock(PasswordPolicy.class);

		Mockito.when(
			passwordPolicy.isHistory()
		).thenReturn(
			true
		);

		Mockito.when(
			passwordPolicy.getHistoryCount()
		).thenReturn(
			6
		);

		Mockito.when(
			_passwordPolicyLocalService.fetchPasswordPolicyByUserId(_USER_ID)
		).thenReturn(
			passwordPolicy
		);

		PasswordTracker passwordTracker = Mockito.mock(PasswordTracker.class);

		Mockito.when(
			passwordTracker.getPassword()
		).thenReturn(
			encryptedPassword
		);

		Mockito.when(
			_passwordTrackerPersistence.findByUserId(_USER_ID)
		).thenReturn(
			Collections.singletonList(passwordTracker)
		);
	}

	private static final String _LEGACY_HASH = "{MD5}legacy";

	private static final String _NEW_PASSWORD = RandomTestUtil.randomString();

	private static final String _PBKDF2_HASH = "{PBKDF2WithHmacSHA256}stored";

	private static final long _USER_ID = 1L;

	private PasswordPolicyLocalService _passwordPolicyLocalService;
	private PasswordTrackerLocalServiceImpl _passwordTrackerLocalServiceImpl;
	private PasswordTrackerPersistence _passwordTrackerPersistence;
	private UserPersistence _userPersistence;

}