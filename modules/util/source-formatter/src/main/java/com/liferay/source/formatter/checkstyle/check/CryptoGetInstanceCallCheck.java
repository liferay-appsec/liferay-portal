/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

/**
 * @author Rafael Praxedes
 */
public class CryptoGetInstanceCallCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.METHOD_CALL};
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
			isExcludedPath(_CRYPTO_GET_INSTANCE_EXCLUDES) ||
			isExcludedPath(RUN_OUTSIDE_PORTAL_EXCLUDES)) {

			return;
		}

		DetailAST dotDetailAST = detailAST.findFirstToken(TokenTypes.DOT);

		if (dotDetailAST == null) {
			return;
		}

		List<String> names = getNames(dotDetailAST, false);

		if ((names.size() != 2) ||
			!StringUtil.equals(names.get(1), "getInstance")) {

			return;
		}

		String className = names.get(0);

		String fullyQualifiedTypeName = getFullyQualifiedTypeName(
			className, detailAST, false);

		if (ArrayUtil.contains(
				_CRYPTO_FACTORY_UTIL_CLASS_NAMES, fullyQualifiedTypeName)) {

			log(detailAST, _MSG_USE_CRYPTO_UTIL, className);
		}
		else if (ArrayUtil.contains(
					_JCA_FACTORY_CLASS_NAMES, fullyQualifiedTypeName)) {

			log(detailAST, _MSG_AVOID_GET_INSTANCE, className);
		}
	}

	private static final String[] _CRYPTO_FACTORY_UTIL_CLASS_NAMES = {
		"java.security.KeyFactory", "java.security.KeyPairGenerator",
		"java.security.KeyStore", "java.security.MessageDigest",
		"java.security.Signature", "javax.crypto.Cipher",
		"javax.crypto.KeyGenerator", "javax.crypto.Mac",
		"javax.net.ssl.KeyManagerFactory", "javax.net.ssl.SSLContext",
		"javax.net.ssl.TrustManagerFactory"
	};

	private static final String _CRYPTO_GET_INSTANCE_EXCLUDES =
		"crypto.get.instance.excludes";

	private static final String[] _JCA_FACTORY_CLASS_NAMES = {
		"java.security.AlgorithmParameterGenerator",
		"java.security.AlgorithmParameters",
		"java.security.cert.CertPathBuilder",
		"java.security.cert.CertPathValidator", "java.security.cert.CertStore",
		"java.security.cert.CertificateFactory",
		"javax.crypto.ExemptionMechanism", "javax.crypto.KeyAgreement",
		"javax.crypto.SecretKeyFactory"
	};

	private static final String _MSG_AVOID_GET_INSTANCE = "get.instance.avoid";

	private static final String _MSG_USE_CRYPTO_UTIL = "crypto.util.use";

}