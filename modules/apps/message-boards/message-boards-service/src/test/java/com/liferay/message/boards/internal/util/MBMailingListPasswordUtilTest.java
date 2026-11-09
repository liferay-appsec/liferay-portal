/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.message.boards.internal.util;

import com.liferay.message.boards.model.MBMailingList;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class MBMailingListPasswordUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
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
		ReflectionTestUtil.setFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			_secretResolverSnapshot);
	}

	@Test
	public void testGetPassword() {
		MBMailingList mailingList = Mockito.mock(MBMailingList.class);

		long companyId = RandomTestUtil.randomLong();

		Mockito.when(
			mailingList.getCompanyId()
		).thenReturn(
			companyId
		);

		long primaryKey = RandomTestUtil.randomLong();

		Mockito.when(
			mailingList.getPrimaryKey()
		).thenReturn(
			primaryKey
		);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));

		String name = RandomTestUtil.randomString();
		String resolvedPassword = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(
							secretContext.getClassName(),
							MBMailingList.class.getName()) &&
						(secretContext.getCompanyId() == companyId) &&
						Objects.equals(secretContext.getKey(), name) &&
						(secretContext.getPrimaryKey() == primaryKey)),
				Mockito.eq(keyReferenceString))
		).thenReturn(
			resolvedPassword
		);

		Assert.assertEquals(
			resolvedPassword,
			MBMailingListPasswordUtil.getPassword(
				mailingList, name, keyReferenceString));
	}

	private final SecretResolver _secretResolver = Mockito.mock(
		SecretResolver.class);
	private Snapshot<SecretResolver> _secretResolverSnapshot;

}