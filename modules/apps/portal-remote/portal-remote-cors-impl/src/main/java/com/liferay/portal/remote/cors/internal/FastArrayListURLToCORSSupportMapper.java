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

package com.liferay.portal.remote.cors.internal;

import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
public class FastArrayListURLToCORSSupportMapper
	extends BaseURLToCORSSupportMapper {

	public FastArrayListURLToCORSSupportMapper(
		Map<String, CORSSupport> corsSupports) {

		_trieNodeArrayList = new TrieNodeArrayList();

		_extensionTrieNode = _trieNodeArrayList.nextAvailableNode();
		_wildCardTrieNode = _trieNodeArrayList.nextAvailableNode();

		for (Map.Entry<String, CORSSupport> entry : corsSupports.entrySet()) {
			put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public CORSSupport get(String urlPath) {
		try {
			CORSSupport corsSupport = _getWildcardCORSSupport(urlPath);

			if (corsSupport != null) {
				return corsSupport;
			}

			return _getExtensionCORSSupport(urlPath);
		}
		catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			throw new IllegalArgumentException(
				"urlPath contains invalid characters",
				indexOutOfBoundsException);
		}
	}

	@Override
	protected void put(CORSSupport corsSupport, String urlPattern)
		throws IllegalArgumentException {

		if (corsSupport == null) {
			throw new IllegalArgumentException("CORS support is null");
		}

		if (Validator.isBlank(urlPattern)) {
			throw new IllegalArgumentException("urlPattern is empty");
		}

		try {
			if (isWildcardURLPattern(urlPattern)) {
				_put(corsSupport, urlPattern, true);

				return;
			}

			if (isExtensionURLPattern(urlPattern)) {
				_put(corsSupport, urlPattern, false);

				return;
			}

			_put(corsSupport, urlPattern, true);
		}
		catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			throw new IllegalArgumentException(
				"urlPattern contains invalid characters",
				indexOutOfBoundsException);
		}
	}

	private CORSSupport _getExtensionCORSSupport(String urlPath) {
		TrieNode currentTrieNode = null;
		TrieNode previousTrieNode = _extensionTrieNode;

		for (int i = 0; i < urlPath.length(); ++i) {
			int index = urlPath.length() - 1 - i;

			char character = urlPath.charAt(index);

			if (character == '/') {
				break;
			}

			currentTrieNode = previousTrieNode.next(character);

			if (currentTrieNode == null) {
				break;
			}

			if (urlPath.charAt(index) == '.') {
				TrieNode nextTrieNode = currentTrieNode.next('*');

				if ((nextTrieNode != null) && nextTrieNode.isEnd()) {
					return nextTrieNode.get();
				}
			}

			previousTrieNode = currentTrieNode;
		}

		return null;
	}

	private CORSSupport _getWildcardCORSSupport(String urlPath) {
		boolean onlyExact = false;
		boolean onlyWildcard = false;

		if (urlPath.charAt(0) != '/') {
			onlyExact = true;
		}
		else if ((urlPath.length() > 1) &&
				 (urlPath.charAt(urlPath.length() - 2) == '/') &&
				 (urlPath.charAt(urlPath.length() - 1) == '*')) {

			onlyWildcard = true;
		}

		CORSSupport corsSupport = null;

		TrieNode currentTrieNode = null;
		TrieNode previousTrieNode = _wildCardTrieNode;

		for (int i = 0; i < urlPath.length(); ++i) {
			currentTrieNode = previousTrieNode.next(urlPath.charAt(i));

			if (currentTrieNode == null) {
				break;
			}

			if (!onlyExact && (urlPath.charAt(i) == '/')) {
				TrieNode nextTrieNode = currentTrieNode.next('*');

				if ((nextTrieNode != null) && nextTrieNode.isEnd()) {
					corsSupport = nextTrieNode.get();
				}
			}

			previousTrieNode = currentTrieNode;
		}

		if (currentTrieNode != null) {
			if (onlyExact) {
				if (!currentTrieNode.isEnd()) {
					return null;
				}

				return currentTrieNode.get();
			}

			if (!onlyWildcard && currentTrieNode.isEnd()) {
				return currentTrieNode.get();
			}

			currentTrieNode = currentTrieNode.next('/');

			if (currentTrieNode != null) {
				currentTrieNode = currentTrieNode.next('*');

				if ((currentTrieNode != null) && currentTrieNode.isEnd()) {
					corsSupport = currentTrieNode.get();
				}
			}
		}

		return corsSupport;
	}

	private void _put(
		CORSSupport corsSupport, String urlPattern, boolean wildcard) {

		TrieNode previousTrieNode = null;

		if (wildcard) {
			previousTrieNode = _wildCardTrieNode;
		}
		else {
			previousTrieNode = _extensionTrieNode;
		}

		TrieNode currentTrieNode = null;

		for (int i = 0; i < urlPattern.length(); ++i) {
			int index = i;

			if (!wildcard) {
				index = urlPattern.length() - 1 - i;
			}

			currentTrieNode = previousTrieNode.next(urlPattern.charAt(index));

			if (currentTrieNode == null) {
				TrieNode nextNode = _trieNodeArrayList.nextAvailableNode();

				currentTrieNode = previousTrieNode.setNext(
					urlPattern.charAt(index), nextNode);
			}

			previousTrieNode = currentTrieNode;
		}

		if (currentTrieNode != null) {
			currentTrieNode.set(corsSupport);
		}
	}

	private static final byte _ASCII_CHARACTER_RANGE = 96;

	private static final byte _ASCII_PRINTABLE_OFFSET = 32;

	private final TrieNode _extensionTrieNode;
	private final TrieNodeArrayList _trieNodeArrayList;
	private final TrieNode _wildCardTrieNode;

	private static class TrieNode {

		public TrieNode() {
			_trieNodes = new ArrayList<>(_ASCII_CHARACTER_RANGE);

			for (int i = 0; i < _ASCII_CHARACTER_RANGE; ++i) {
				_trieNodes.add(null);
			}
		}

		public CORSSupport get() {
			return _corsSupport;
		}

		public boolean isEnd() {
			if (_corsSupport != null) {
				return true;
			}

			return false;
		}

		public TrieNode next(char character) {
			return _trieNodes.get(character - _ASCII_PRINTABLE_OFFSET);
		}

		public void set(CORSSupport corsSupport) {
			_corsSupport = corsSupport;
		}

		public TrieNode setNext(char character, TrieNode nextNode) {
			_trieNodes.set(character - _ASCII_PRINTABLE_OFFSET, nextNode);

			return nextNode;
		}

		private CORSSupport _corsSupport;
		private final List<TrieNode> _trieNodes;

	}

	private static class TrieNodeArrayList {

		public TrieNodeArrayList() {
			_trieNodes = new ArrayList<>(_INIT_SIZE);

			for (int i = 0; i < _INIT_SIZE; ++i) {
				_trieNodes.add(new TrieNode());
			}
		}

		public TrieNode nextAvailableNode() {
			if (_nextAvailableNodeIndex >= _trieNodes.size()) {
				_trieNodes.ensureCapacity(_trieNodes.size() + _INIT_SIZE);

				for (int i = 0; i < _INIT_SIZE; ++i) {
					_trieNodes.add(new TrieNode());
				}
			}

			return _trieNodes.get(_nextAvailableNodeIndex++);
		}

		private static final int _INIT_SIZE = 1024;

		private int _nextAvailableNodeIndex;
		private ArrayList<TrieNode> _trieNodes;

	}

}