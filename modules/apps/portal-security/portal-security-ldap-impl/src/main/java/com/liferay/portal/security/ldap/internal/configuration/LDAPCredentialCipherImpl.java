/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration;

import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.ldap.FIPSModeUtil;
import com.liferay.portal.security.ldap.LDAPCredentialCipher;
import com.liferay.portal.security.ldap.internal.configuration.persistence.listener.LDAPServerCredentialEncryptionConfigurationModelListener;

import javax.crypto.spec.GCMParameterSpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jorge García Jiménez
 */
@Component(service = LDAPCredentialCipher.class)
public class LDAPCredentialCipherImpl implements LDAPCredentialCipher {

	@Override
	public String resolve(long companyId, String value) {
		if (!StringUtil.startsWith(
				value,
				LDAPServerCredentialEncryptionConfigurationModelListener.
					ENCRYPTED_VALUE_PREFIX)) {

			return value;
		}

		if (companyId <= 0) {
			companyId = _portal.getDefaultCompanyId();
		}

		try {
			Company company = _companyLocalService.getCompany(companyId);

			String stripped = value.substring(
				LDAPServerCredentialEncryptionConfigurationModelListener.
					ENCRYPTED_VALUE_PREFIX.length());

			if (FIPSModeUtil.isEnabled()) {
				int delimiter = stripped.indexOf('.');

				if (delimiter <= 0) {
					_log.error(
						"Malformed encrypted LDAP credential for company " +
							companyId);

					return value;
				}

				byte[] iv = Base64.decode(stripped.substring(0, delimiter));

				return EncryptorUtil.decrypt(
					company.getKeyObj(), stripped.substring(delimiter + 1),
					"AES/GCM/NoPadding",
					new GCMParameterSpec(_GCM_TAG_LENGTH_BITS, iv));
			}

			return EncryptorUtil.decrypt(company.getKeyObj(), stripped);
		}
		catch (EncryptorException | PortalException exception) {
			_log.error(
				"Unable to decrypt LDAP security credential for company " +
					companyId,
				exception);

			return value;
		}
	}

	private static final int _GCM_TAG_LENGTH_BITS = 128;

	private static final Log _log = LogFactoryUtil.getLog(
		LDAPCredentialCipherImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private Portal _portal;

}