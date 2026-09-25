/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.web.cache;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.rest.text.embeddings.configuration.TextEmbeddingProvider;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.SecretResolverUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

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
public class TextEmbeddingProviderWebCacheItemTest {

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
	public void testConvert() {
		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), StringPool.STAR,
				KeyReference.Type.SECRET));
		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(Mockito.anyLong(), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> invocationOnMock.getArgument(1)
		);

		Mockito.when(
			_secretResolver.resolve(companyId, keyReferenceString)
		).thenReturn(
			value
		);

		_assertConvert(
			"accessToken", companyId, value, "hugging-face-inference-api",
			keyReferenceString);
		_assertConvert(
			"accessToken", companyId, keyReferenceString,
			"hugging-face-inference-endpoint", keyReferenceString);
		_assertConvert(
			"apiKey", companyId, value, "openai", keyReferenceString);
		_assertConvert("apiKey", companyId, value, "openai", value);
		_assertConvert(
			"basicAuthPassword", companyId, keyReferenceString, "txtai",
			keyReferenceString);
	}

	private void _assertConvert(
		String attributeName, long companyId, String expectedValue,
		String providerName, String value) {

		EmbeddingProviderConfiguration embeddingProviderConfiguration =
			new EmbeddingProviderConfiguration();

		embeddingProviderConfiguration.setAttributes(
			HashMapBuilder.<String, Object>put(
				attributeName, value
			).build());
		embeddingProviderConfiguration.setProviderName(providerName);

		TextEmbeddingProvider textEmbeddingProvider = Mockito.mock(
			TextEmbeddingProvider.class);

		TextEmbeddingProviderWebCacheItem textEmbeddingProviderWebCacheItem =
			new TextEmbeddingProviderWebCacheItem(
				companyId, embeddingProviderConfiguration,
				Mockito.mock(SemanticSearchConfiguration.class),
				RandomTestUtil.randomString(), textEmbeddingProvider);

		textEmbeddingProviderWebCacheItem.convert(
			RandomTestUtil.randomString());

		Mockito.verify(
			textEmbeddingProvider
		).getEmbedding(
			Mockito.same(embeddingProviderConfiguration), Mockito.anyString()
		);

		Map<String, Object> attributes =
			(Map<String, Object>)embeddingProviderConfiguration.getAttributes();

		Assert.assertEquals(expectedValue, attributes.get(attributeName));
	}

	private final SecretResolver _secretResolver = Mockito.mock(
		SecretResolver.class);
	private Snapshot<SecretResolver> _secretResolverSnapshot;

}