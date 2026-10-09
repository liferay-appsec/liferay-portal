/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.StringUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * @author Rafael Praxedes
 */
public class CryptoSecretKeySpecCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.LITERAL_NEW};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		String absolutePath = getAbsolutePath();

		if (absolutePath.contains("/modules/apps/archived/") ||
			absolutePath.contains("/modules/third-party/") ||
			absolutePath.contains("/src/test/") ||
			absolutePath.contains("/src/testIntegration/") ||
			absolutePath.contains("/test/integration/") ||
			absolutePath.contains("/test/unit/") ||
			absolutePath.contains("/workspaces/") ||
			absolutePath.endsWith("/CryptoUtil.java") ||
			isExcludedPath(_CRYPTO_SECRET_KEY_SPEC_EXCLUDES) ||
			isExcludedPath(RUN_OUTSIDE_PORTAL_EXCLUDES)) {

			return;
		}

		DetailAST firstChildDetailAST = detailAST.getFirstChild();

		if ((firstChildDetailAST == null) ||
			(firstChildDetailAST.getType() != TokenTypes.IDENT)) {

			return;
		}

		if (StringUtil.equals(
				getFullyQualifiedTypeName(
					firstChildDetailAST.getText(), detailAST, false),
				"javax.crypto.spec.SecretKeySpec")) {

			log(detailAST, _MSG_USE_CRYPTO_UTIL);
		}
	}

	private static final String _CRYPTO_SECRET_KEY_SPEC_EXCLUDES =
		"crypto.secret.key.spec.excludes";

	private static final String _MSG_USE_CRYPTO_UTIL = "crypto.util.use";

}