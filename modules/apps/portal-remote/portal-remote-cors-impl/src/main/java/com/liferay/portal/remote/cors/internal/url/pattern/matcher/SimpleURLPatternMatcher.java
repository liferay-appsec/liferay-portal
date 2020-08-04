/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.remote.cors.internal.url.pattern.matcher;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arthur Chan
 */
public class SimpleURLPatternMatcher<T> implements URLPatternMatcher<T> {

	@Override
	public T getValue(String urlPath) {
		T value = _exactURLPatternValues.get(urlPath);

		if (value != null) {
			return value;
		}

		int index = 0;

		for (int i = urlPath.length(); i > 0; --i) {
			value = _wildcardURLPatternValues.get(
				urlPath.substring(0, i) + "*");

			if (value != null) {
				return value;
			}

			if ((index < 1) && (urlPath.charAt(i - 1) == '.')) {
				index = i - 1;
			}
		}

		return _extensionURLPatternValues.get("*" + urlPath.substring(index));
	}

	@Override
	public void putValue(String urlPattern, T value)
		throws IllegalArgumentException {

		if (URLPatternMatcherUtil.isWildcardURLPattern(urlPattern)) {
			if (!_wildcardURLPatternValues.containsKey(urlPattern)) {
				_wildcardURLPatternValues.put(urlPattern, value);
			}

			return;
		}

		if (URLPatternMatcherUtil.isExtensionURLPattern(urlPattern)) {
			if (!_extensionURLPatternValues.containsKey(urlPattern)) {
				_extensionURLPatternValues.put(urlPattern, value);
			}

			return;
		}

		if (!_exactURLPatternValues.containsKey(urlPattern)) {
			_exactURLPatternValues.put(urlPattern, value);
		}
	}

	private final Map<String, T> _exactURLPatternValues = new HashMap<>();
	private final Map<String, T> _extensionURLPatternValues = new HashMap<>();
	private final Map<String, T> _wildcardURLPatternValues = new HashMap<>();

}