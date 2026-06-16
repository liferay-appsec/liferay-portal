/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.ldap.FIPSModeUtil;
import com.liferay.portal.security.ldap.LDAPConfigurationModelListenerException;
import com.liferay.portal.security.ldap.exportimport.configuration.LDAPImportConfiguration;

import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.ldap.exportimport.configuration.LDAPImportConfiguration",
	service = ConfigurationModelListener.class
)
public class FIPSLDAPImportConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		if (!PropsValues.FIPS_ENABLED ||
			!GetterUtil.getBoolean(
				properties.get("importUserPasswordEnabled"))) {

			return;
		}

		String portalAlgorithm = PropsUtil.get(
			PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM);

		if (FIPSModeUtil.isNotAllowedPasswordAlgorithm(portalAlgorithm)) {
			throw new LDAPConfigurationModelListenerException(
				StringBundler.concat(
					"The algorithm \"", portalAlgorithm,
					"\" is not allowed for password import in FIPS mode."),
				"the-algorithm-x-is-not-allowed-for-password-import-in-fips-" +
					"mode",
				new Object[] {portalAlgorithm}, LDAPImportConfiguration.class,
				getClass(), properties);
		}
	}

}