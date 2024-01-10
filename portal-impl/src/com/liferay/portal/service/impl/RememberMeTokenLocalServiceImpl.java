/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.service.impl;

import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.exception.NoSuchRememberMeTokenException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.RememberMeToken;
import com.liferay.portal.kernel.service.persistence.RememberMeTokenUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.service.base.RememberMeTokenLocalServiceBaseImpl;

import java.util.Date;

/**
 * @author Brian Wing Shun Chan
 * @author Manuele Castro
 * @author Pedro Silvestre
 */
public class RememberMeTokenLocalServiceImpl
	extends RememberMeTokenLocalServiceBaseImpl {

	@Override
	public RememberMeToken addRememberMeToken(
		long companyId, long userId, Date expirationDate) {

		long rememberMeTokenId = counterLocalService.increment();

		RememberMeToken rememberMeToken = rememberMeTokenPersistence.create(
			rememberMeTokenId);

		rememberMeToken.setCompanyId(companyId);
		rememberMeToken.setUserId(userId);
		rememberMeToken.setCreateDate(new Date());
		rememberMeToken.setAccessToken(PortalUUIDUtil.generate());
		rememberMeToken.setExpirationDate(expirationDate);

		return rememberMeTokenPersistence.update(rememberMeToken);
	}

	@Override
	public RememberMeToken getRememberMeToken(String accessToken)
		throws NoSuchRememberMeTokenException {

		return RememberMeTokenUtil.findByAccessToken(accessToken);
	}

	@Override
	public void removeRememberMeToken(
			Company company, String rememberMeAccessToken)
		throws NoSuchRememberMeTokenException {

		try {
			rememberMeAccessToken = EncryptorUtil.decrypt(
				company.getKeyObj(), rememberMeAccessToken);
		}
		catch (EncryptorException encryptorException) {
			throw new SystemException(encryptorException);
		}

		RememberMeToken rememberMeToken = getRememberMeToken(
			rememberMeAccessToken);

		deleteRememberMeToken(rememberMeToken);
	}

}