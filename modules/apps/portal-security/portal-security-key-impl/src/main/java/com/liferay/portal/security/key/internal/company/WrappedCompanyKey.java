/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.company;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.encryptor.CompanyKeyUtil;
import com.liferay.portal.kernel.exception.CompanyKeyResolutionException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Validator;

import java.util.Arrays;

/**
 * @author Christopher Kian
 */
public class WrappedCompanyKey {

	public static WrappedCompanyKey parse(
		long companyId, String serializedKey) {

		if (!CompanyKeyUtil.isWrappedKey(serializedKey) ||
			!serializedKey.endsWith(StringPool.CLOSE_CURLY_BRACE)) {

			throw new CompanyKeyResolutionException(
				"Wrapped key is malformed for company " + companyId);
		}

		String body = serializedKey.substring(
			CompanyKeyUtil.WRAPPED_KEY_PREFIX.length(),
			serializedKey.length() - 1);

		int colonIndex = body.indexOf(CharPool.COLON);
		int pipeIndex = body.indexOf(CharPool.PIPE);

		if ((colonIndex <= 0) || (pipeIndex <= (colonIndex + 1)) ||
			(pipeIndex >= (body.length() - 1))) {

			throw new CompanyKeyResolutionException(
				"Wrapped key is malformed for company " + companyId);
		}

		byte[] ciphertext = Base64.decode(body.substring(pipeIndex + 1));

		if (ArrayUtil.isEmpty(ciphertext)) {
			throw new CompanyKeyResolutionException(
				"Wrapped key is malformed for company " + companyId);
		}

		return new WrappedCompanyKey(
			ciphertext, body.substring(colonIndex + 1, pipeIndex),
			body.substring(0, colonIndex));
	}

	public WrappedCompanyKey(
		byte[] ciphertext, String identifier, String providerId) {

		if (ciphertext == null) {
			throw new IllegalArgumentException("Ciphertext is null");
		}

		if (Validator.isNull(identifier)) {
			throw new IllegalArgumentException("Identifier is null");
		}

		if (Validator.isNull(providerId)) {
			throw new IllegalArgumentException("Provider ID is null");
		}

		_ciphertext = Arrays.copyOf(ciphertext, ciphertext.length);
		_identifier = identifier;
		_providerId = providerId;
	}

	public byte[] getCiphertext() {
		return Arrays.copyOf(_ciphertext, _ciphertext.length);
	}

	public String getIdentifier() {
		return _identifier;
	}

	public String getProviderId() {
		return _providerId;
	}

	public String serialize() {
		return StringBundler.concat(
			CompanyKeyUtil.WRAPPED_KEY_PREFIX, _providerId, StringPool.COLON,
			_identifier, StringPool.PIPE, Base64.encode(_ciphertext),
			StringPool.CLOSE_CURLY_BRACE);
	}

	private final byte[] _ciphertext;
	private final String _identifier;
	private final String _providerId;

}