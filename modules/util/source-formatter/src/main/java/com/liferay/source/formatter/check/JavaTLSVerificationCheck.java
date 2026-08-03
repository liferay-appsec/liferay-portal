/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.tools.ToolsUtil;
import com.liferay.source.formatter.parser.JavaClass;
import com.liferay.source.formatter.parser.JavaTerm;

import java.util.Arrays;
import java.util.List;

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

		if (absolutePath.contains("/test/") ||
			absolutePath.contains("/testIntegration/") ||
			absolutePath.contains("/third-party/") || _hasGuard(fileContent)) {

			return content;
		}

		if (javaTerm.isJavaConstructor() && _isTrustManager(javaTerm)) {
			addMessage(
				fileName,
				StringBundler.concat(
					"Call \"", _GUARD_PREFIX,
					"*\" in the same file as a trust manager, see LPD-93649"),
				javaTerm.getLineNumber());
		}

		for (String bypassString : _bypassStrings) {
			int x = -1;

			while (true) {
				x = content.indexOf(bypassString, x + 1);

				if (x == -1) {
					break;
				}

				if (ToolsUtil.isInsideQuotes(content, x)) {
					continue;
				}

				_addMessage(fileName, bypassString, javaTerm, x);
			}
		}

		return content;
	}

	@Override
	protected String[] getCheckableJavaTermNames() {
		return new String[] {JAVA_CONSTRUCTOR, JAVA_METHOD};
	}

	private void _addMessage(
		String fileName, String bypassString, JavaTerm javaTerm, int pos) {

		addMessage(
			fileName,
			StringBundler.concat(
				"Call \"", _GUARD_PREFIX, "*\" in the same file as \"",
				bypassString, "\", see LPD-93649"),
			javaTerm.getLineNumber(pos));
	}

	private boolean _hasGuard(String fileContent) {
		int x = -1;

		while (true) {
			x = fileContent.indexOf(_GUARD_PREFIX, x + 1);

			if (x == -1) {
				return false;
			}

			if (!ToolsUtil.isInsideQuotes(fileContent, x)) {
				return true;
			}
		}
	}

	private boolean _isTrustManager(JavaTerm javaTerm) {
		JavaClass javaClass = javaTerm.getParentJavaClass();

		if (javaClass == null) {
			return false;
		}

		for (String extendedClassName : javaClass.getExtendedClassNames()) {
			if (_trustManagerClassNames.contains(extendedClassName)) {
				return true;
			}
		}

		for (String implementedClassName :
				javaClass.getImplementedClassNames()) {

			if (_trustManagerClassNames.contains(implementedClassName)) {
				return true;
			}
		}

		return false;
	}

	private static final String _GUARD_PREFIX =
		"FIPSModeValidator.validateServer";

	private static final List<String> _bypassStrings = Arrays.asList(
		"ALLOW_ALL_HOSTNAME_VERIFIER", "AllowAllHostnameVerifier",
		"NoopHostnameVerifier", "TrustAllStrategy", "TrustSelfSignedStrategy",
		"new HostnameVerifier(", "new X509ExtendedTrustManager(",
		"new X509TrustManager(");
	private static final List<String> _trustManagerClassNames = Arrays.asList(
		"X509ExtendedTrustManager", "X509TrustManager");

}