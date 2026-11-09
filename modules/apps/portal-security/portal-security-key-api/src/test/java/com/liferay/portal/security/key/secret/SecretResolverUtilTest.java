/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.secret;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletPreferences;

import java.util.Collections;

import org.junit.After;
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
public class SecretResolverUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_secretResolverSnapshot = ReflectionTestUtil.getFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot");
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			_secretResolverSnapshot);
	}

	@Test
	public void testCopy() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		SecretContext sourceSecretContext =
			SecretContext.createCompanySecretContext(
				companyId, RandomTestUtil.randomString());
		SecretContext targetSecretContext =
			SecretContext.createCompanySecretContext(
				companyId, RandomTestUtil.randomString());

		String keyReferenceString = _randomKeyReferenceString();

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		String copiedValue = _randomKeyReferenceString();

		Mockito.when(
			secretResolver.copy(
				companyId, sourceSecretContext, targetSecretContext,
				keyReferenceString)
		).thenReturn(
			copiedValue
		);

		_setUpSecretResolverSnapshot(secretResolver);

		PortletPreferences portletPreferences = Mockito.mock(
			PortletPreferences.class);

		String key = RandomTestUtil.randomString();
		String plainValue = RandomTestUtil.randomString();

		Mockito.when(
			portletPreferences.getMap()
		).thenReturn(
			HashMapBuilder.put(
				key, new String[] {keyReferenceString, plainValue}
			).put(
				RandomTestUtil.randomString(), new String[] {plainValue}
			).build()
		);

		SecretResolverUtil.copy(
			companyId, portletPreferences, currentKey -> sourceSecretContext,
			currentKey -> targetSecretContext);

		Mockito.verify(
			portletPreferences
		).getMap();

		Mockito.verify(
			portletPreferences
		).setValues(
			key, copiedValue, plainValue
		);

		Mockito.verifyNoMoreInteractions(portletPreferences);

		Mockito.verify(
			secretResolver
		).copy(
			companyId, sourceSecretContext, targetSecretContext,
			keyReferenceString
		);

		Mockito.verifyNoMoreInteractions(secretResolver);

		PortletPreferences plainPortletPreferences = Mockito.mock(
			PortletPreferences.class);

		Mockito.when(
			plainPortletPreferences.getMap()
		).thenReturn(
			HashMapBuilder.put(
				RandomTestUtil.randomString(), new String[] {plainValue}
			).build()
		);

		SecretResolverUtil.copy(
			companyId, plainPortletPreferences,
			currentKey -> sourceSecretContext,
			currentKey -> targetSecretContext);

		Mockito.verify(
			plainPortletPreferences
		).getMap();

		Mockito.verifyNoMoreInteractions(plainPortletPreferences);

		Mockito.verifyNoMoreInteractions(secretResolver);
	}

	@Test
	public void testResolve() {
		long companyId = RandomTestUtil.randomLong();
		String resolvedValue = RandomTestUtil.randomString();
		String value = _randomKeyReferenceString();

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		Mockito.when(
			secretResolver.resolve(companyId, value)
		).thenReturn(
			resolvedValue
		);

		_setUpSecretResolverSnapshot(secretResolver);

		Assert.assertSame(
			resolvedValue, SecretResolverUtil.resolve(companyId, value));

		String plainValue = RandomTestUtil.randomString();

		Assert.assertSame(
			plainValue, SecretResolverUtil.resolve(companyId, plainValue));

		Mockito.verify(
			secretResolver, Mockito.never()
		).resolve(
			companyId, plainValue
		);
	}

	@Test
	public void testResolveProperty() {
		String key = RandomTestUtil.randomString();
		String resolvedValue = RandomTestUtil.randomString();

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		Mockito.when(
			secretResolver.resolveProperty(key)
		).thenReturn(
			resolvedValue
		);

		_setUpSecretResolverSnapshot(secretResolver);

		try (MockedStatic<PropsUtil> propsUtilMockedStatic = Mockito.mockStatic(
				PropsUtil.class)) {

			propsUtilMockedStatic.when(
				() -> PropsUtil.get(key)
			).thenReturn(
				_randomKeyReferenceString()
			);

			Assert.assertSame(
				resolvedValue, SecretResolverUtil.resolveProperty(key));

			String plainValue = RandomTestUtil.randomString();

			propsUtilMockedStatic.when(
				() -> PropsUtil.get(key)
			).thenReturn(
				plainValue
			);

			Assert.assertSame(
				plainValue, SecretResolverUtil.resolveProperty(key));
		}

		Mockito.verify(
			secretResolver
		).resolveProperty(
			key
		);
	}

	@Test
	public void testResolveWhenSecretResolverIsUnavailable() {
		_setUpSecretResolverSnapshot(null);

		Assert.assertThrows(
			IllegalStateException.class,
			() -> SecretResolverUtil.resolve(
				RandomTestUtil.randomLong(), _randomKeyReferenceString()));

		String plainValue = RandomTestUtil.randomString();

		Assert.assertSame(
			plainValue,
			SecretResolverUtil.resolve(
				RandomTestUtil.randomLong(), plainValue));

		Assert.assertNull(
			SecretResolverUtil.resolve(RandomTestUtil.randomLong(), null));
	}

	@Test
	public void testResolveWithSecretContext() {
		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = _randomKeyReferenceString();
		String resolvedValue = RandomTestUtil.randomString();

		SecretContext secretContext =
			SecretContext.createConfigurationSecretContext(
				RandomTestUtil.randomString(), Collections.emptyMap());

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		Mockito.when(
			secretResolver.resolve(companyId, secretContext, keyReferenceString)
		).thenReturn(
			resolvedValue
		);

		_setUpSecretResolverSnapshot(secretResolver);

		Assert.assertSame(
			resolvedValue,
			SecretResolverUtil.resolve(
				companyId, secretContext, keyReferenceString));

		String plainValue = RandomTestUtil.randomString();

		Assert.assertSame(
			plainValue,
			SecretResolverUtil.resolve(companyId, secretContext, plainValue));

		Mockito.verify(
			secretResolver, Mockito.never()
		).resolve(
			companyId, secretContext, plainValue
		);
	}

	private String _randomKeyReferenceString() {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	private void _setUpSecretResolverSnapshot(SecretResolver secretResolver) {
		ReflectionTestUtil.setFieldValue(
			SecretResolverUtil.class, "_secretResolverSnapshot",
			new Snapshot<SecretResolver>(
				SecretResolverUtil.class, SecretResolver.class) {

				@Override
				public SecretResolver get() {
					return secretResolver;
				}

			});
	}

	private Snapshot<SecretResolver> _secretResolverSnapshot;

}