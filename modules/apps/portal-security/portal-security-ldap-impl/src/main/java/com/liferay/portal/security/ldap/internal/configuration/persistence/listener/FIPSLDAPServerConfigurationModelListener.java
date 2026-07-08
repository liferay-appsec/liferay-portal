/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.ldap.FIPSModeUtil;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;

import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration",
	service = ConfigurationModelListener.class
)
public class FIPSLDAPServerConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		String baseProviderURL = GetterUtil.getString(
			properties.get("baseProviderURL"));

		if (Validator.isNull(baseProviderURL)) {
			return;
		}

		if (FIPSModeUtil.isNotAllowedProtocol(baseProviderURL)) {
			throw new LDAPConfigurationModelListenerException(
				"The LDAP base provider URL \"" + baseProviderURL +
					"\" must use the \"ldaps://\" scheme in FIPS mode",
				"fips-mode-requires-the-ldaps-scheme-for-the-base-provider-" +
					"url-x",
				new Object[] {baseProviderURL}, LDAPServerConfiguration.class,
				getClass(), properties);
		}
	}

}