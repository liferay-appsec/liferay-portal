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

package com.liferay.portal.security.ldap.persistence.service.impl;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;
import com.liferay.portal.security.ldap.persistence.service.base.LDAPServerAttributeRelLocalServiceBaseImpl;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel",
	service = AopService.class
)
public class LDAPServerAttributeRelLocalServiceImpl
	extends LDAPServerAttributeRelLocalServiceBaseImpl {

	@Override
	public LDAPServerAttributeRel addLDAPServerAttributeRel(
			long ldapServerId, String className, long classPK)
		throws PortalException {

		long classNameId = _classNameLocalService.getClassNameId(className);

		LDAPServerAttributeRel ldapServerAttributeRel =
			ldapServerAttributeRelPersistence.fetchByL_C_C(
				ldapServerId, classNameId, classPK);

		if (ldapServerAttributeRel != null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"LDAP server attribute relationship already exists ",
						"for LDAP server id ", ldapServerId, ", class name id ",
						classNameId, ", and class primary key ", classPK));
			}

			return ldapServerAttributeRel;
		}

		ldapServerAttributeRel = createLDAPServerAttributeRel(
			counterLocalService.increment());

		ldapServerAttributeRel.setLdapServerId(ldapServerId);
		ldapServerAttributeRel.setClassNameId(classNameId);
		ldapServerAttributeRel.setClassPK(classPK);

		return ldapServerAttributeRelPersistence.update(ldapServerAttributeRel);
	}

	@Override
	public void deleteLDAPServerAttributeRel(
			long ldapServerId, String className, long classPK)
		throws PortalException {

		ldapServerAttributeRelPersistence.removeByL_C_C(
			ldapServerId, _classNameLocalService.getClassNameId(className),
			classPK);
	}

	@Override
	public List<LDAPServerAttributeRel> getLDAPServerAttributeRels(
		long ldapServerId, String className) {

		return ldapServerAttributeRelPersistence.findByL_C(
			ldapServerId, _classNameLocalService.getClassNameId(className));
	}

	@Override
	public boolean hasLDAPServerAttributeRel(
		long ldapServerId, String className, long classPK) {

		LDAPServerAttributeRel ldapServerAttributeRel =
			ldapServerAttributeRelPersistence.fetchByL_C_C(
				ldapServerId, _classNameLocalService.getClassNameId(className),
				classPK);

		if (ldapServerAttributeRel != null) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LDAPServerAttributeRelLocalServiceImpl.class);

	@Reference
	private ClassNameLocalService _classNameLocalService;

}