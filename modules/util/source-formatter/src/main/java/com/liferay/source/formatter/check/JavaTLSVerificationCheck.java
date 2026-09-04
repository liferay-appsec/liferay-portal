/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.tools.ToolsUtil;
import com.liferay.source.formatter.parser.JavaClass;
import com.liferay.source.formatter.parser.JavaTerm;

import java.util.List;
import java.util.Set;

/**
 * @author Caio Farias
 */
public class JavaTLSVerificationCheck extends BaseJavaTermCheck {

	@Override
	public boolean isLiferaySourceCheck() {
		return true;
	}

	@Override
	protected String doProcess(
		String fileName, String absolutePath, JavaTerm javaTerm,
		String fileContent) {

		String content = javaTerm.getContent();

		if (absolutePath.contains("/modules/apps/archived/") ||
			absolutePath.contains("/modules/third-party/") ||
			absolutePath.contains("/src/test/") ||
			absolutePath.contains("/src/testIntegration/") ||
			absolutePath.contains("/test/unit/") ||
			_hasGuard(javaTerm, content, fileContent)) {

			return content;
		}

		if (javaTerm.isJavaClass()) {
			JavaClass javaClass = (JavaClass)javaTerm;

			List<String> classNames = ListUtil.concat(
				javaClass.getExtendedClassNames(),
				javaClass.getImplementedClassNames());

			if (ListUtil.exists(
					classNames, _trustManagerClassNames::contains)) {

				addMessage(
					fileName,
					StringBundler.concat(
						"Call \"", _GUARD_METHOD_NAME,
						"\" in the same class as a trust manager, see ",
						"LPD-93649"),
					javaTerm.getLineNumber());
			}

			return content;
		}

		for (String bypassString : _bypassStrings) {
			int x = _indexOfCode(content, bypassString, 0);

			while (x != -1) {
				addMessage(
					fileName,
					StringBundler.concat(
						"Call \"", _GUARD_METHOD_NAME,
						"\" in the same method as \"", bypassString,
						"\", see LPD-93649"),
					javaTerm.getLineNumber(x));

				x = _indexOfCode(content, bypassString, x + 1);
			}
		}

		return content;
	}

	@Override
	protected String[] getCheckableJavaTermNames() {
		return new String[] {
			JAVA_CLASS, JAVA_CONSTRUCTOR, JAVA_METHOD, JAVA_STATIC_BLOCK,
			JAVA_VARIABLE
		};
	}

	private boolean _hasGuard(
		JavaTerm javaTerm, String content, String fileContent) {

		String guardContent = content;

		if (javaTerm.isJavaVariable()) {
			guardContent = fileContent;
		}

		if (_indexOfCode(guardContent, _GUARD_METHOD_NAME, 0) != -1) {
			return true;
		}

		return false;
	}

	private int _indexOfCode(String content, String s, int fromIndex) {
		int x = _indexOfOutsideQuotes(content, s, fromIndex);

		while (x != -1) {
			if (!_isInsideComment(content, x)) {
				return x;
			}

			x = _indexOfOutsideQuotes(content, s, x + 1);
		}

		return -1;
	}

	private int _indexOfOutsideQuotes(String content, String s, int fromIndex) {
		int x = fromIndex - 1;

		while (true) {
			x = content.indexOf(s, x + 1);

			if ((x == -1) || !ToolsUtil.isInsideQuotes(content, x)) {
				return x;
			}
		}
	}

	private boolean _isInsideComment(String content, int pos) {
		int x = content.lastIndexOf(CharPool.NEW_LINE, pos);

		int y = _indexOfOutsideQuotes(content, "//", x + 1);

		if ((y != -1) && (y < pos)) {
			return true;
		}

		x = _indexOfOutsideQuotes(content, "*/", pos);

		if (x == -1) {
			return false;
		}

		y = _indexOfOutsideQuotes(content, "/*", pos);

		if ((y != -1) && (y < x)) {
			return false;
		}

		return true;
	}

	private static final String _GUARD_METHOD_NAME =
		"FIPSModeValidator.validateTLSVerification";

	private static final List<String> _bypassStrings = List.of(
		"ALLOW_ALL_HOSTNAME_VERIFIER", "AllowAllHostnameVerifier",
		"NoopHostnameVerifier", "TrustAllStrategy", "TrustSelfSignedStrategy",
		"new HostnameVerifier(", "new X509ExtendedTrustManager(",
		"new X509TrustManager(", "setEndpointIdentificationAlgorithm(\"\")",
		"setEndpointIdentificationAlgorithm(null)");
	private static final Set<String> _trustManagerClassNames = Set.of(
		"X509ExtendedTrustManager", "X509TrustManager");

}