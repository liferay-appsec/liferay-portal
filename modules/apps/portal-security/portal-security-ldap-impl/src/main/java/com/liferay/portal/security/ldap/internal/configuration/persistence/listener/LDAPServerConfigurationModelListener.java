/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.ldap.internal.configuration.persistence.listener;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;
import com.liferay.portal.security.ldap.persistence.service.LDAPServerAttributeRelLocalService;

import java.util.Dictionary;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael Bowerman
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.portal.security.ldap.configuration.LDAPServerConfiguration",
	service = ConfigurationModelListener.class
)
public class LDAPServerConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeDelete(String pid)
		throws ConfigurationModelListenerException {

		try {
			Configuration configuration = _configurationAdmin.getConfiguration(
				pid, StringPool.QUESTION);

			Dictionary<String, Object> properties =
				configuration.getProperties();

			long ldapServerId = (Long)properties.get("ldapServerId");

			ActionableDynamicQuery actionableDynamicQuery =
				_ldapServerAttributeRelLocalService.getActionableDynamicQuery();

			actionableDynamicQuery.setAddCriteriaMethod(
				dynamicQuery -> dynamicQuery.add(
					RestrictionsFactoryUtil.eq("ldapServerId", ldapServerId)));
			actionableDynamicQuery.setPerformActionMethod(
				ldapServerAttributeRel ->
					_ldapServerAttributeRelLocalService.
						deleteLDAPServerAttributeRel(
							(LDAPServerAttributeRel)ldapServerAttributeRel));

			actionableDynamicQuery.performActions();
		}
		catch (Exception exception) {
			throw new ConfigurationModelListenerException(
				exception.getMessage(), LDAPServerConfiguration.class,
				getClass(), null);
		}
	}

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private LDAPServerAttributeRelLocalService
		_ldapServerAttributeRelLocalService;

}