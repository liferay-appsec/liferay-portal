/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.ldap.FIPSModeUtil;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.authenticator.configuration.LDAPAuthConfiguration;
import com.liferay.portal.security.ldap.constants.LDAPConstants;

import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.ldap.authenticator.configuration.LDAPAuthConfiguration",
	service = ConfigurationModelListener.class
)
public class FIPSLDAPAuthConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (!PropsValues.FIPS_ENABLED ||
			!LDAPConstants.AUTH_METHOD_PASSWORD_COMPARE.equals(
				GetterUtil.getString(
					properties.get(LDAPConstants.AUTH_METHOD)))) {

			return;
		}

		String passwordEncryptionAlgorithm = GetterUtil.getString(
			properties.get("passwordEncryptionAlgorithm"));

		if (FIPSModeUtil.isNotAllowedPasswordAlgorithm(
				passwordEncryptionAlgorithm)) {

			throw new LDAPConfigurationModelListenerException(
				StringBundler.concat(
					"The algorithm \"", passwordEncryptionAlgorithm,
					"\" is not allowed in FIPS mode."),
				"the-algorithm-x-is-not-allowed-in-fips-mode",
				new Object[] {passwordEncryptionAlgorithm},
				LDAPAuthConfiguration.class, getClass(), properties);
		}
	}

}