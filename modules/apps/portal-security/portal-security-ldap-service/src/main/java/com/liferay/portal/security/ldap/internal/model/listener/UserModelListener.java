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

package com.liferay.portal.security.ldap.internal.model.listener;

import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.security.ldap.service.LDAPServerAttributeRelLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael Bowerman
 */
@Component(immediate = true, service = ModelListener.class)
public class UserModelListener extends BaseModelListener<User> {

	@Override
	public void onAfterCreate(User user) throws ModelListenerException {
		long ldapServerId = user.getLdapServerId();

		if (ldapServerId > 0) {
			try {
				_ldapServerAttributeRelLocalService.addLDAPServerAttributeRel(
					ldapServerId, User.class.getName(), user.getUserId());
			}
			catch (Exception exception) {
				throw new ModelListenerException(exception);
			}
		}
	}

	@Override
	public void onAfterUpdate(User originalUser, User user)
		throws ModelListenerException {

		long ldapServerId = user.getLdapServerId();

		if ((ldapServerId > 0) &&
			!_ldapServerAttributeRelLocalService.hasLDAPServerAttributeRel(
				ldapServerId, User.class.getName(), user.getUserId())) {

			try {
				_ldapServerAttributeRelLocalService.addLDAPServerAttributeRel(
					ldapServerId, User.class.getName(), user.getUserId());
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