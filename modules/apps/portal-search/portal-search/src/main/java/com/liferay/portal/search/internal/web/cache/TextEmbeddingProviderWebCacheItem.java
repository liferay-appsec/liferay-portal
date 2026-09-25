/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.web.cache;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.webcache.WebCacheItem;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.rest.dto.v1_0.EmbeddingProviderConfiguration;
import com.liferay.portal.search.rest.text.embeddings.configuration.TextEmbeddingProvider;
import com.liferay.portal.security.key.secret.SecretResolverUtil;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class TextEmbeddingProviderWebCacheItem implements WebCacheItem {

	public static Double[] get(
		long companyId,
		EmbeddingProviderConfiguration embeddingProviderConfiguration,
		SemanticSearchConfiguration semanticSearchConfiguration, String text,
		TextEmbeddingProvider textEmbeddingProvider) {

		try {
			return (Double[])WebCachePoolUtil.get(
				StringBundler.concat(
					TextEmbeddingProviderWebCacheItem.class.getName(),
					StringPool.POUND,
					embeddingProviderConfiguration.getProviderName(),
					StringPool.POUND, text),
				new TextEmbeddingProviderWebCacheItem(
					companyId, embeddingProviderConfiguration,
					semanticSearchConfiguration, text, textEmbeddingProvider));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return new Double[0];
		}
	}

	public TextEmbeddingProviderWebCacheItem(
		long companyId,
		EmbeddingProviderConfiguration embeddingProviderConfiguration,
		SemanticSearchConfiguration semanticSearchConfiguration, String text,
		TextEmbeddingProvider textEmbeddingProvider) {

		_companyId = companyId;
		_embeddingProviderConfiguration = embeddingProviderConfiguration;
		_semanticSearchConfiguration = semanticSearchConfiguration;
		_text = text;
		_textEmbeddingProvider = textEmbeddingProvider;
	}

	@Override
	public Double[] convert(String key) {
		try {
			Map<String, Object> attributes =
				(Map<String, Object>)
					_embeddingProviderConfiguration.getAttributes();
			String credentialAttributeName = _credentialAttributeNames.get(
				_embeddingProviderConfiguration.getProviderName());

			if ((attributes != null) && (credentialAttributeName != null)) {
				attributes.computeIfPresent(
					credentialAttributeName,
					(name, value) -> SecretResolverUtil.resolve(
						_companyId, GetterUtil.getString(value)));
			}

			return _textEmbeddingProvider.getEmbedding(
				_embeddingProviderConfiguration, _text);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public long getRefreshTime() {
		return _semanticSearchConfiguration.textEmbeddingCacheTimeout();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingProviderWebCacheItem.class);

	private static final Map<String, String> _credentialAttributeNames =
		HashMapBuilder.put(
			"hugging-face-inference-api", "accessToken"
		).put(
			"openai", "apiKey"
		).build();

	private final long _companyId;
	private final EmbeddingProviderConfiguration
		_embeddingProviderConfiguration;
	private final SemanticSearchConfiguration _semanticSearchConfiguration;
	private final String _text;
	private final TextEmbeddingProvider _textEmbeddingProvider;

}