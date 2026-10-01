/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Caio Farias
 */
public class FIPSAlgorithmCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.METHOD_CALL};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		String absolutePath = getAbsolutePath();

		if (absolutePath.contains("/modules/apps/archived/") ||
			absolutePath.contains("/modules/sdk/") ||
			absolutePath.contains("/modules/test/") ||
			absolutePath.contains("/modules/third-party/") ||
			absolutePath.contains("/modules/util/") ||
			absolutePath.contains("/src/test/") ||
			absolutePath.contains("/src/testIntegration/") ||
			absolutePath.contains("/test/integration/") ||
			absolutePath.contains("/test/unit/") ||
			absolutePath.contains("/workspaces/")) {

			return;
		}

		String className = _getCryptoClassName(detailAST);

		if (className == null) {
			return;
		}

		String methodName = getMethodName(detailAST);

		String implicitAlgorithm = _getImplicitAlgorithm(className, methodName);

		if (implicitAlgorithm != null) {
			if (!_isGuarded(detailAST)) {
				log(detailAST, _MSG_ALGORITHM_NOT_ALLOWED, implicitAlgorithm);
			}

			return;
		}

		List<DetailAST> parameterExprDetailASTs = getParameterExprDetailASTs(
			detailAST);

		String[] allowedAlgorithms = _getAllowedAlgorithms(
			className, methodName, parameterExprDetailASTs.size());

		if ((allowedAlgorithms == null) || _isGuarded(detailAST)) {
			return;
		}

		for (String algorithm :
				_getAlgorithms(0, parameterExprDetailASTs.get(0))) {

			if (!_isAllowedAlgorithm(algorithm, allowedAlgorithms)) {
				log(detailAST, _MSG_ALGORITHM_NOT_ALLOWED, algorithm);
			}
		}
	}

	private List<String> _getAlgorithms(int depth, DetailAST detailAST) {
		if ((detailAST == null) || (depth > 5)) {
			return Collections.emptyList();
		}

		int type = detailAST.getType();

		if (type == TokenTypes.DOT) {
			FullIdent fullIdent = FullIdent.createFullIdent(detailAST);

			String algorithm = _digesterUtilAlgorithmsMap.get(
				fullIdent.getText());

			if (algorithm == null) {
				return Collections.emptyList();
			}

			return Collections.singletonList(algorithm);
		}

		if (type == TokenTypes.EXPR) {
			return _getAlgorithms(depth, detailAST.getFirstChild());
		}

		if (type == TokenTypes.IDENT) {
			return _getVariableAlgorithms(depth, detailAST);
		}

		if (type == TokenTypes.QUESTION) {
			return _getTernaryAlgorithms(depth, detailAST);
		}

		if (type == TokenTypes.STRING_LITERAL) {
			String text = detailAST.getText();

			return Collections.singletonList(
				text.substring(1, text.length() - 1));
		}

		return Collections.emptyList();
	}

	private String[] _getAllowedAlgorithms(
		String className, String methodName, int parametersCount) {

		if (className.equals("com.liferay.portal.kernel.util.DigesterUtil")) {
			if (methodName.startsWith("digest") && (parametersCount > 1)) {
				return _ALGORITHMS_MESSAGE_DIGEST;
			}

			return null;
		}

		if (className.equals("org.apache.commons.codec.digest.DigestUtils")) {
			if (methodName.equals("getDigest") && (parametersCount > 0)) {
				return _ALGORITHMS_MESSAGE_DIGEST;
			}

			return null;
		}

		if (methodName.equals("getInstance") && (parametersCount > 0)) {
			return _allowedAlgorithmsMap.get(className);
		}

		return null;
	}

	private String _getCryptoClassName(DetailAST methodCallDetailAST) {
		String className = getClassOrVariableName(methodCallDetailAST);

		if (className == null) {
			return null;
		}

		int x = className.lastIndexOf(CharPool.PERIOD);

		if (!ArrayUtil.contains(
				_CLASS_SIMPLE_NAMES, className.substring(x + 1))) {

			return null;
		}

		if (x != -1) {
			return className;
		}

		for (String importName : getImportNames(methodCallDetailAST)) {
			if (importName.endsWith(StringPool.PERIOD + className)) {
				return importName;
			}
		}

		return getPackageName(methodCallDetailAST) + StringPool.PERIOD +
			className;
	}

	private String _getImplicitAlgorithm(String className, String methodName) {
		if (!ArrayUtil.contains(_CLASS_NAMES_IMPLICIT_ALGORITHM, className)) {
			return null;
		}

		Matcher matcher = _implicitAlgorithmPattern.matcher(methodName);

		if (!matcher.matches()) {
			return null;
		}

		return _implicitAlgorithmsMap.get(
			StringUtil.toLowerCase(matcher.group(1)));
	}

	private List<DetailAST> _getOperandDetailASTs(DetailAST detailAST) {
		List<DetailAST> operandDetailASTs = new ArrayList<>();

		DetailAST childDetailAST = detailAST.getFirstChild();

		while (childDetailAST != null) {
			int type = childDetailAST.getType();

			if ((type != TokenTypes.COLON) && (type != TokenTypes.LPAREN) &&
				(type != TokenTypes.RPAREN)) {

				operandDetailASTs.add(childDetailAST);
			}

			childDetailAST = childDetailAST.getNextSibling();
		}

		return operandDetailASTs;
	}

	private List<String> _getTernaryAlgorithms(
		int depth, DetailAST questionDetailAST) {

		List<DetailAST> operandDetailASTs = _getOperandDetailASTs(
			questionDetailAST);

		if (operandDetailASTs.size() != 3) {
			return Collections.emptyList();
		}

		DetailAST conditionDetailAST = operandDetailASTs.get(0);

		if (_isFIPSEnabledCondition(conditionDetailAST)) {
			return _getAlgorithms(depth + 1, operandDetailASTs.get(1));
		}

		if (_isFIPSDisabledCondition(conditionDetailAST)) {
			return _getAlgorithms(depth + 1, operandDetailASTs.get(2));
		}

		List<String> algorithms = new ArrayList<>(
			_getAlgorithms(depth + 1, operandDetailASTs.get(1)));

		algorithms.addAll(_getAlgorithms(depth + 1, operandDetailASTs.get(2)));

		return algorithms;
	}

	private List<String> _getVariableAlgorithms(
		int depth, DetailAST identDetailAST) {

		String variableName = identDetailAST.getText();

		DetailAST variableDefinitionDetailAST = getVariableDefinitionDetailAST(
			identDetailAST, variableName);

		if ((variableDefinitionDetailAST == null) ||
			(variableDefinitionDetailAST.getType() !=
				TokenTypes.VARIABLE_DEF) ||
			_isReassigned(variableDefinitionDetailAST, variableName)) {

			return Collections.emptyList();
		}

		DetailAST assignDetailAST = variableDefinitionDetailAST.findFirstToken(
			TokenTypes.ASSIGN);

		if (assignDetailAST == null) {
			return Collections.emptyList();
		}

		return _getAlgorithms(depth + 1, assignDetailAST.getFirstChild());
	}

	private boolean _isAllowedAlgorithm(
		String algorithm, String[] allowedAlgorithms) {

		for (String allowedAlgorithm : allowedAlgorithms) {
			if (StringUtil.equalsIgnoreCase(algorithm, allowedAlgorithm) ||
				StringUtil.startsWith(
					algorithm, allowedAlgorithm + StringPool.SLASH)) {

				return true;
			}
		}

		return false;
	}

	private boolean _isFIPSDisabledCondition(DetailAST detailAST) {
		if ((detailAST == null) || (detailAST.getType() != TokenTypes.LNOT)) {
			return false;
		}

		List<DetailAST> operandDetailASTs = _getOperandDetailASTs(detailAST);

		if (operandDetailASTs.size() != 1) {
			return false;
		}

		return _isFIPSEnabledCondition(operandDetailASTs.get(0));
	}

	private boolean _isFIPSEnabledCondition(DetailAST detailAST) {
		if ((detailAST == null) || (detailAST.getType() != TokenTypes.DOT)) {
			return false;
		}

		FullIdent fullIdent = FullIdent.createFullIdent(detailAST);

		return StringUtil.equals(
			fullIdent.getText(), "PropsValues.FIPS_ENABLED");
	}

	private boolean _isGuarded(DetailAST methodCallDetailAST) {
		DetailAST childDetailAST = methodCallDetailAST;
		DetailAST parentDetailAST = methodCallDetailAST.getParent();

		while (parentDetailAST != null) {
			if ((parentDetailAST.getType() == TokenTypes.CTOR_DEF) ||
				(parentDetailAST.getType() == TokenTypes.METHOD_DEF)) {

				List<DetailAST> methodCallDetailASTs = getMethodCalls(
					parentDetailAST, "FIPSModeValidator", "validateAlgorithm");

				return !methodCallDetailASTs.isEmpty();
			}

			if (parentDetailAST.getType() == TokenTypes.LITERAL_IF) {
				DetailAST exprDetailAST = parentDetailAST.findFirstToken(
					TokenTypes.EXPR);

				DetailAST conditionDetailAST = exprDetailAST.getFirstChild();

				if (childDetailAST.getType() == TokenTypes.LITERAL_ELSE) {
					if (_isFIPSEnabledCondition(conditionDetailAST)) {
						return true;
					}
				}
				else if ((childDetailAST != exprDetailAST) &&
						 _isFIPSDisabledCondition(conditionDetailAST)) {

					return true;
				}
			}

			childDetailAST = parentDetailAST;
			parentDetailAST = parentDetailAST.getParent();
		}

		return false;
	}

	private boolean _isReassigned(
		DetailAST variableDefinitionDetailAST, String variableName) {

		DetailAST rootDetailAST = variableDefinitionDetailAST;

		while (rootDetailAST.getParent() != null) {
			rootDetailAST = rootDetailAST.getParent();
		}

		for (DetailAST assignDetailAST :
				getAllChildTokens(rootDetailAST, true, TokenTypes.ASSIGN)) {

			DetailAST firstChildDetailAST = assignDetailAST.getFirstChild();
			DetailAST parentDetailAST = assignDetailAST.getParent();

			if ((firstChildDetailAST != null) &&
				(firstChildDetailAST.getType() == TokenTypes.IDENT) &&
				(parentDetailAST.getType() != TokenTypes.VARIABLE_DEF) &&
				variableName.equals(firstChildDetailAST.getText())) {

				return true;
			}
		}

		return false;
	}

	private static final String[] _ALGORITHMS_KEY = {
		"DH", "DiffieHellman", "EC", "Ed25519", "Ed448", "EdDSA", "RSA",
		"RSASSA-PSS"
	};

	private static final String[] _ALGORITHMS_MESSAGE_DIGEST = {
		"SHA-256", "SHA-384", "SHA-512"
	};

	private static final String[] _CLASS_NAMES_IMPLICIT_ALGORITHM = {
		"com.google.common.hash.Hashing",
		"org.apache.commons.codec.digest.DigestUtils",
		"org.springframework.util.DigestUtils"
	};

	private static final String[] _CLASS_SIMPLE_NAMES = {
		"Cipher", "DigestUtils", "DigesterUtil", "Hashing", "KeyAgreement",
		"KeyFactory", "KeyGenerator", "KeyPairGenerator", "Mac",
		"MessageDigest", "SecretKeyFactory", "SecureRandom", "Signature"
	};

	private static final String _MSG_ALGORITHM_NOT_ALLOWED =
		"algorithm.not.allowed";

	private static final Map<String, String[]> _allowedAlgorithmsMap =
		HashMapBuilder.put(
			"java.security.KeyFactory", _ALGORITHMS_KEY
		).put(
			"java.security.KeyPairGenerator", _ALGORITHMS_KEY
		).put(
			"java.security.MessageDigest", _ALGORITHMS_MESSAGE_DIGEST
		).put(
			"java.security.SecureRandom", new String[] {"DEFAULT", "DRBG"}
		).put(
			"java.security.Signature",
			new String[] {
				"Ed25519", "Ed448", "EdDSA", "RSASSA-PSS", "SHA256withECDSA",
				"SHA256withRSA", "SHA384withECDSA", "SHA384withRSA",
				"SHA512withECDSA", "SHA512withRSA"
			}
		).put(
			"javax.crypto.Cipher",
			new String[] {
				"AES", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding",
				"RSA/ECB/OAEPWithSHA-384AndMGF1Padding",
				"RSA/ECB/OAEPWithSHA-512AndMGF1Padding"
			}
		).put(
			"javax.crypto.KeyAgreement",
			new String[] {"DH", "DiffieHellman", "ECDH"}
		).put(
			"javax.crypto.KeyGenerator",
			new String[] {"AES", "HmacSHA256", "HmacSHA384", "HmacSHA512"}
		).put(
			"javax.crypto.Mac",
			new String[] {"HmacSHA256", "HmacSHA384", "HmacSHA512"}
		).put(
			"javax.crypto.SecretKeyFactory",
			new String[] {
				"AES", "PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA384",
				"PBKDF2WithHmacSHA512"
			}
		).build();
	private static final Map<String, String> _digesterUtilAlgorithmsMap =
		HashMapBuilder.put(
			"DigesterUtil.MD5", "MD5"
		).put(
			"DigesterUtil.SHA", "SHA"
		).put(
			"DigesterUtil.SHA_1", "SHA-1"
		).put(
			"DigesterUtil.SHA_256", "SHA-256"
		).build();
	private static final Pattern _implicitAlgorithmPattern = Pattern.compile(
		"(?:append|get)?(hmacMd5|hmacSha1|md2|md5|sha1?)(?:Digest)?" +
			"(?:AsHex|Hex)?",
		Pattern.CASE_INSENSITIVE);
	private static final Map<String, String> _implicitAlgorithmsMap =
		HashMapBuilder.put(
			"hmacmd5", "HmacMD5"
		).put(
			"hmacsha1", "HmacSHA1"
		).put(
			"md2", "MD2"
		).put(
			"md5", "MD5"
		).put(
			"sha", "SHA-1"
		).put(
			"sha1", "SHA-1"
		).build();

}