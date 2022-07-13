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

package com.liferay.portal.kernel.security.exportimport;

import com.liferay.petra.lang.CentralizedThreadLocal;

/**
 * @author Brian Wing Shun Chan
 */
public class UserGroupImportTransactionThreadLocal {

	public static final long DEFAULT_LDAP_SERVER_ID = -1L;

	public static long getLDAPServerId() {
		return _ldapServerId.get();
	}

	public static void setLDAPServerId(long ldapServerId) {
		_ldapServerId.set(ldapServerId);
	}

	private static final ThreadLocal<Long> _ldapServerId =
		new CentralizedThreadLocal<>(
			UserGroupImportTransactionThreadLocal.class + "._ldapServerId",
			() -> -1L, false);

}