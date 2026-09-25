/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.text.embeddings;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.net.HttpURLConnection;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class HuggingFaceInferenceAPITextEmbeddingProviderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetEmbedding() throws Exception {
		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));

		EmbeddingProviderConfiguration embeddingProviderConfiguration =
			new EmbeddingProviderConfiguration();

		embeddingProviderConfiguration.setAttributes(
			HashMapBuilder.<String, Object>put(
				"accessToken", keyReferenceString
			).put(
				"model", RandomTestUtil.randomString()
			).build());

		Http http = Mockito.mock(Http.class);

		Mockito.when(
			http.URLtoString(Mockito.any(Http.Options.class))
		).thenAnswer(
			invocation -> {
				Http.Options options = invocation.getArgument(
					0, Http.Options.class);

				Http.Response response = new Http.Response();

				response.setResponseCode(HttpURLConnection.HTTP_OK);

				options.setResponse(response);

				return "[1.0]";
			}
		);

		HuggingFaceInferenceAPITextEmbeddingProvider
			huggingFaceInferenceAPITextEmbeddingProvider =
				new HuggingFaceInferenceAPITextEmbeddingProvider();

		ReflectionTestUtil.setFieldValue(
			huggingFaceInferenceAPITextEmbeddingProvider, "_http", http);
		ReflectionTestUtil.setFieldValue(
			huggingFaceInferenceAPITextEmbeddingProvider, "_jsonFactory",
			new JSONFactoryImpl());

		long companyId = RandomTestUtil.randomLong();
		String accessToken = RandomTestUtil.randomString();

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		Mockito.when(
			secretResolver.resolve(companyId, keyReferenceString)
		).thenReturn(
			accessToken
		);

		ReflectionTestUtil.setFieldValue(
			huggingFaceInferenceAPITextEmbeddingProvider, "_secretResolver",
			secretResolver);

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			huggingFaceInferenceAPITextEmbeddingProvider.getEmbedding(
				embeddingProviderConfiguration, RandomTestUtil.randomString());
		}

		ArgumentCaptor<Http.Options> argumentCaptor = ArgumentCaptor.forClass(
			Http.Options.class);

		Mockito.verify(
			http
		).URLtoString(
			argumentCaptor.capture()
		);

		Http.Options options = argumentCaptor.getValue();

		Map<String, String> headers = options.getHeaders();

		Assert.assertEquals(
			"Bearer " + accessToken, headers.get(HttpHeaders.AUTHORIZATION));
	}

}