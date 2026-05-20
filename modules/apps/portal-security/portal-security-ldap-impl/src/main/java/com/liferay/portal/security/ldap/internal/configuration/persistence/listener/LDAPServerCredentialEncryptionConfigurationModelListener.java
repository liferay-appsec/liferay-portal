/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;

import java.util.Dictionary;

import javax.crypto.spec.GCMParameterSpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration",
	service = ConfigurationModelListener.class
)
public class LDAPServerCredentialEncryptionConfigurationModelListener
	implements ConfigurationModelListener {

	public static final String ENCRYPTED_VALUE_PREFIX = "{ENC}";

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		String securityCredential = GetterUtil.getString(
			properties.get("securityCredential"));

		if (Validator.isNull(securityCredential) ||
			securityCredential.startsWith(ENCRYPTED_VALUE_PREFIX)) {

			return;
		}

		long companyId = GetterUtil.getLong(properties.get("companyId"));

		if (companyId <= 0) {
			companyId = _portal.getDefaultCompanyId();
		}

		try {
			Company company = _companyLocalService.getCompany(companyId);

			byte[] iv = new byte[_GCM_IV_LENGTH];

			for (int i = 0; i < iv.length; i++) {
				iv[i] = SecureRandomUtil.nextByte();
			}

			String encrypted = EncryptorUtil.encrypt(
				company.getKeyObj(), securityCredential, "AES/GCM/NoPadding",
				new GCMParameterSpec(_GCM_TAG_LENGTH_BITS, iv));

			properties.put(
				"securityCredential",
				StringBundler.concat(
					ENCRYPTED_VALUE_PREFIX, Base64.encode(iv), ".", encrypted));
		}
		catch (EncryptorException | PortalException exception) {
			_log.error(
				"Unable to encrypt LDAP security credential for company " +
					companyId,
				exception);

			throw new ConfigurationModelListenerException(
				exception, LDAPServerConfiguration.class, getClass(),
				properties);
		}
	}

	private static final int _GCM_IV_LENGTH = 12;

	private static final int _GCM_TAG_LENGTH_BITS = 128;

	private static final Log _log = LogFactoryUtil.getLog(
		LDAPServerCredentialEncryptionConfigurationModelListener.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private Portal _portal;

}