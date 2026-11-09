/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.message.boards.internal.util;

import com.liferay.message.boards.model.MBMailingList;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolverUtil;

/**
 * @author Caio Farias
 */
public class MBMailingListPasswordUtil {

	public static String getPassword(
		MBMailingList mailingList, String name, String password) {

		return SecretResolverUtil.resolve(
			mailingList.getCompanyId(),
			SecretContext.createEntitySecretContext(
				MBMailingList.class, mailingList.getCompanyId(), name,
				mailingList.getPrimaryKey()),
			password);
	}

}