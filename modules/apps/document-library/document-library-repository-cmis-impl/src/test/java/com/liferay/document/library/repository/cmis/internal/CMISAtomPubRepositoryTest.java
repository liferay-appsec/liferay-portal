/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.repository.cmis.internal;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.SessionParameter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class CMISAtomPubRepositoryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_dlRepositoryGuestPassword = GetterUtil.getString(
			PropsUtil.get(PropsKeys.DL_REPOSITORY_GUEST_PASSWORD));
		_secretResolverSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			new Snapshot<SecretResolver>(
				SecretResolverUtil.class, SecretResolver.class) {

				@Override
				public SecretResolver get() {
					return _secretResolver;
				}

			});
	}

	@After
	public void tearDown() {
		PropsUtil.set(
			PropsKeys.DL_REPOSITORY_GUEST_PASSWORD, _dlRepositoryGuestPassword);
		ReflectionTestUtil.setFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			_secretResolverSnapshot);
	}

	@Test
	public void testGetSession() throws Exception {
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
		String password = RandomTestUtil.randomString();

		_secretResolver = Mockito.mock(SecretResolver.class);

		Mockito.when(
			_secretResolver.resolveProperty(
				PropsKeys.DL_REPOSITORY_GUEST_PASSWORD)
		).thenReturn(
			password
		);

		_testGetSession(password, keyReferenceString);
		_testGetSession(password, password);
	}

	private void _testGetSession(String expectedPassword, String password)
		throws Exception {

		PropsUtil.set(PropsKeys.DL_REPOSITORY_GUEST_PASSWORD, password);

		try (MockedStatic<CMISRepositoryUtil> cmisRepositoryUtilMockedStatic =
				Mockito.mockStatic(CMISRepositoryUtil.class)) {

			CMISAtomPubRepository cmisAtomPubRepository =
				new CMISAtomPubRepository();

			cmisAtomPubRepository.getSession();

			ArgumentCaptor<Map<String, String>> argumentCaptor =
				ArgumentCaptor.forClass(Map.class);

			cmisRepositoryUtilMockedStatic.verify(
				() -> CMISRepositoryUtil.createSession(
					argumentCaptor.capture()));

			Map<String, String> parameters = argumentCaptor.getValue();

			Assert.assertEquals(
				expectedPassword, parameters.get(SessionParameter.PASSWORD));
		}
	}

	private String _dlRepositoryGuestPassword;
	private SecretResolver _secretResolver;
	private Snapshot<SecretResolver> _secretResolverSnapshot;

}