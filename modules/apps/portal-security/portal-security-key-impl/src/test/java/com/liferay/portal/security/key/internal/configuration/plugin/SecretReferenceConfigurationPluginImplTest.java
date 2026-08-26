/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.configuration.plugin;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Dictionary;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Pedro Victor Silvestre
 */
public class SecretReferenceConfigurationPluginImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		ReflectionTestUtil.setFieldValue(
			SecretReferenceConfigurationPluginImpl.class,
			"_secretResolverSnapshot", _snapshot);

		Mockito.when(
			_snapshot.get()
		).thenReturn(
			_secretResolver
		);
	}

	@Test
	public void testModifyConfiguration() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		_assertResolvedPassword(
			companyId,
			HashMapDictionaryBuilder.<String, Object>put(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey(),
				companyId
			).put(
				"password", _SECRET_REFERENCE
			).build());

		_assertResolvedPassword(
			CompanyConstants.SYSTEM,
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _SECRET_REFERENCE
			).build());
	}

	@Test
	public void testModifyConfigurationWhenOneReferenceFails()
		throws Exception {

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"otherPassword", _SECRET_REFERENCE_OTHER
			).put(
				"password", _SECRET_REFERENCE
			).build();

		Mockito.when(
			_secretResolver.resolve(
				CompanyConstants.SYSTEM, _SECRET_REFERENCE_OTHER)
		).thenThrow(
			new SecretException()
		);

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(CompanyConstants.SYSTEM, _SECRET_REFERENCE)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(
			_SECRET_REFERENCE_OTHER, properties.get("otherPassword"));
		Assert.assertEquals(value, properties.get("password"));
	}

	@Test
	public void testModifyConfigurationWhenReferenceIsCrypto()
		throws Exception {

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _CRYPTO_REFERENCE
			).build();

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(_CRYPTO_REFERENCE, properties.get("password"));

		Mockito.verifyNoInteractions(_secretResolver);
	}

	@Test
	public void testModifyConfigurationWhenSecretResolverIsUnavailable()
		throws Exception {

		Mockito.when(
			_snapshot.get()
		).thenReturn(
			null
		);

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _SECRET_REFERENCE
			).build();

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(_SECRET_REFERENCE, properties.get("password"));
	}

	@Test
	public void testModifyConfigurationWhenThereAreNoReferences()
		throws Exception {

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null,
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", Boolean.TRUE
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"size", 42
			).build());

		Mockito.verifyNoInteractions(_secretResolver);
	}

	@Test
	public void testModifyConfigurationWhenValuesHaveDifferentTypes()
		throws Exception {

		String literal = RandomTestUtil.randomString();

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", Boolean.TRUE
			).put(
				"password", _SECRET_REFERENCE
			).put(
				"passwords", new String[] {literal, _SECRET_REFERENCE}
			).put(
				"size", 42
			).build();

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(CompanyConstants.SYSTEM, _SECRET_REFERENCE)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(Boolean.TRUE, properties.get("enabled"));
		Assert.assertEquals(value, properties.get("password"));
		Assert.assertArrayEquals(
			new String[] {literal, value},
			(String[])properties.get("passwords"));
		Assert.assertEquals(42, properties.get("size"));
	}

	private void _assertResolvedPassword(
			long companyId, Dictionary<String, Object> properties)
		throws Exception {

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(companyId, _SECRET_REFERENCE)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(value, properties.get("password"));
	}

	private static final String _CRYPTO_REFERENCE =
		"${keyRef:provider:identifier}";

	private static final String _SECRET_REFERENCE =
		"${secretRef:provider:identifier}";

	private static final String _SECRET_REFERENCE_OTHER =
		"${secretRef:provider:other}";

	private final SecretReferenceConfigurationPluginImpl
		_secretReferenceConfigurationPluginImpl =
			new SecretReferenceConfigurationPluginImpl();

	@Mock
	private SecretResolver _secretResolver;

	@Mock
	private Snapshot<SecretResolver> _snapshot;

}