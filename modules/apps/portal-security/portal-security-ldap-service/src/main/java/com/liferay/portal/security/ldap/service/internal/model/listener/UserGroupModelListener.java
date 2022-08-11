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

package com.liferay.portal.security.ldap.service.internal.model.listener;

import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.security.exportimport.UserGroupImportTransactionThreadLocal;
import com.liferay.portal.security.ldap.service.LDAPServerAttributeRelLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael Bowerman
 */
@Component(immediate = true, service = ModelListener.class)
public class UserGroupModelListener extends BaseModelListener<UserGroup> {

	@Override
	public void onAfterCreate(UserGroup userGroup)
		throws ModelListenerException {

		long ldapServerId =
			UserGroupImportTransactionThreadLocal.getLDAPServerId();

		if (ldapServerId !=
				UserGroupImportTransactionThreadLocal.DEFAULT_LDAP_SERVER_ID) {

			try {
				_ldapServerAttributeRelLocalService.addLDAPServerAttributeRel(
					ldapServerId, UserGroup.class.getName(),
					userGroup.getUserGroupId());
			}
			catch (Exception exception) {
				throw new ModelListenerException(exception);
			}
		}
	}

	@Reference
	private LDAPServerAttributeRelLocalService
		_ldapServerAttributeRelLocalService;

}