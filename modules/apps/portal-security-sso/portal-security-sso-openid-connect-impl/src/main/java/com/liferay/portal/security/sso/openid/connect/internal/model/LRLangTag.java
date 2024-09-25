/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.sso.openid.connect.internal.model;

import com.nimbusds.langtag.LangTag;
import com.nimbusds.langtag.LangTagException;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jorge García Jiménez
 */
public class LRLangTag extends LangTag {

	public static LRLangTag parse(String s, boolean lowerCase)
		throws LangTagException {

		if ((s != null) &&
			!s.trim(
			).isEmpty()) {

			String[] subtags = s.split("-");
			int pos = 0;
			String primaryLang = null;
			List<String> extLangSubtags = new LinkedList<>();

			if (_isPrimaryLanguage(subtags[0])) {
				primaryLang = subtags[pos++];
			}

			while ((pos < subtags.length) &&
				   _isExtendedLanguageSubtag(subtags[pos])) {

				extLangSubtags.add(subtags[pos++]);
			}

			LRLangTag langTag = new LRLangTag(
				primaryLang, lowerCase,
				(String[])extLangSubtags.toArray(new String[0]));

			if ((pos < subtags.length) && _isScript(subtags[pos])) {
				langTag.setScript(subtags[pos++]);
			}

			if ((pos < subtags.length) && _isRegion(subtags[pos])) {
				langTag.setRegion(subtags[pos++]);
			}

			LinkedList<String> variantSubtags = new LinkedList<>();

			while ((pos < subtags.length) && _isVariant(subtags[pos])) {
				variantSubtags.add(subtags[pos++]);
			}

			if (!variantSubtags.isEmpty()) {
				langTag.setVariants(
					(String[])variantSubtags.toArray(new String[0]));
			}

			LinkedList<String> extSubtags = new LinkedList<>();

			while ((pos < subtags.length) &&
				   _isExtensionSingleton(subtags[pos])) {

				String singleton = subtags[pos++];

				if (pos == subtags.length) {
					throw new LangTagException("Invalid extension subtag");
				}

				extSubtags.add(singleton + "-" + subtags[pos++]);
			}

			if (!extSubtags.isEmpty()) {
				langTag.setExtensions(
					(String[])extSubtags.toArray(new String[0]));
			}

			if ((pos < subtags.length) && subtags[pos].equals("x")) {
				++pos;

				if (pos == subtags.length) {
					throw new LangTagException("Invalid private use subtag");
				}

				langTag.setPrivateUse("x-" + subtags[pos++]);
			}

			if (pos < subtags.length) {
				throw new LangTagException(
					"Invalid language tag: Unexpected subtag");
			}

			return langTag;
		}

		return null;
	}

	public LRLangTag(
			String primaryLanguage, boolean lowerCase,
			String... languageSubtags)
		throws LangTagException {

		super(primaryLanguage, languageSubtags);

		_lowercase = lowerCase;
	}

	public String toString() {
		if (_lowercase) {
			return super.toString(
			).toLowerCase();
		}

		return super.toString();
	}

	private static boolean _isExtendedLanguageSubtag(String s) {
		return s.matches("[a-zA-Z]{3}");
	}

	private static boolean _isExtensionSingleton(String s) {
		return s.matches("[0-9a-wA-Wy-zY-Z]");
	}

	private static boolean _isPrimaryLanguage(String s) {
		return s.matches("[a-zA-Z]{2,3}");
	}

	private static boolean _isRegion(String s) {
		return s.matches("[a-zA-Z]{2}|\\d{3}");
	}

	private static boolean _isScript(String s) {
		return s.matches("[a-zA-Z]{4}");
	}

	private static boolean _isVariant(String s) {
		return s.matches("[a-zA-Z][a-zA-Z0-9]{4,}|[0-9][a-zA-Z0-9]{3,}");
	}

	private final boolean _lowercase;

}