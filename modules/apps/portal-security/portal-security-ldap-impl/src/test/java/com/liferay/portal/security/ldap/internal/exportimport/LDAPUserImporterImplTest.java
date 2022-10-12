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

package com.liferay.portal.security.ldap.internal.exportimport;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.security.ldap.LDAPSettings;
import com.liferay.portal.security.ldap.SafePortalLDAP;
import com.liferay.portal.security.ldap.exportimport.LDAPUserImporter;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import javax.naming.InvalidNameException;
import javax.naming.directory.Attribute;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * @author Jorge Díaz
 */
public class LDAPUserImporterImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testBindingInNamespaceEscape() throws InvalidNameException {
		Assert.assertEquals(
			"cn=User\\\\,with\\\\,commas,ou=users,dc=example,dc=com",
			escapeLDAPName(
				"cn=User\\,with\\,commas,ou=users,dc=example,dc=com"));
		Assert.assertEquals(
			"cn=User\\\\2cwith\\\\2ccommas,ou=users,dc=example,dc=com",
			escapeLDAPName(
				"cn=User\\2cwith\\2ccommas,ou=users,dc=example,dc=com"));
	}

	@Test
	public void testPreserveUserGroupMembership() throws Exception {

		LDAPUserImporterImpl ldapUserImporter = new LDAPUserImporterImpl();

		Attribute usersLdapAttribute = Mockito.mock(Attribute.class);

		long ldapServerId = 0;
		long companyId = 0;
		long userGroupId = 0;

		LDAPImportContext ldapImportContext =
			Mockito.mock(LDAPImportContext.class);

		_setField(
			LDAPUserImporterImpl.class, ldapUserImporter, "_ldapSettings",
			ldapImportContext);

		SafePortalLDAP safePortalLDAP = Mockito.mock(SafePortalLDAP.class);

		_setField(
			LDAPUserImporterImpl.class, ldapUserImporter, "_safePortalLDAP",
			safePortalLDAP);

		ldapUserImporter.importUsers(ldapServerId, companyId);
	}

	private <T> void _setField(Class<T> clazz, T object, String name, Object value)
			throws Exception {

		Field field = ReflectionUtil.getDeclaredField(clazz, name);

		field.set(object, value);
	}

	protected String escapeLDAPName(String query) {
		return _ldapUserImporterImpl.escapeLDAPName(query);
	}

	private static final LDAPUserImporterImpl _ldapUserImporterImpl =
		new LDAPUserImporterImpl();

}