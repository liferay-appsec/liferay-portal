/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.util.List;

/**
 * @author Jorge García Jiménez
 */
public class FIPSModeValidationException extends SecurityException {

	@Override
	public String getMessage() {
		if (_validationErrors == null) {
			return super.getMessage();
		}

		StringBundler sb = new StringBundler(
			(_validationErrors.size() * 7) + 1);

		sb.append("The configuration is not allowed in FIPS mode:");

		for (int i = 0; i < _validationErrors.size(); i++) {
			ValidationError validationError = _validationErrors.get(i);

			sb.append(StringPool.NEW_LINE);
			sb.append(i + 1);
			sb.append(". ");
			sb.append(validationError.getErrorMessage());
			sb.append(". See \"");
			sb.append(validationError.getGuideSection());
			sb.append("\" in the FIPS migration guide.");
		}

		return sb.toString();
	}

	public List<ValidationError> getValidationErrors() {
		return _validationErrors;
	}

	public void setValidationErrors(List<ValidationError> validationErrors) {
		_validationErrors = validationErrors;

		for (ValidationError validationError : validationErrors) {
			addSuppressed(validationError.getSecurityException());
		}
	}

	public static class ValidationError {

		public ValidationError(
			String guideSection, SecurityException securityException) {

			_guideSection = guideSection;
			_securityException = securityException;
		}

		public String getErrorMessage() {
			return _securityException.getMessage();
		}

		public String getGuideSection() {
			return _guideSection;
		}

		public SecurityException getSecurityException() {
			return _securityException;
		}

		private final String _guideSection;
		private final SecurityException _securityException;

	}

	private List<ValidationError> _validationErrors;

}