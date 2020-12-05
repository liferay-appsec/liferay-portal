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

package com.liferay.portal.security.password.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.security.password.model.PasswordEntry;
import com.liferay.portal.security.password.model.PasswordHashGenerator;
import com.liferay.portal.security.password.model.PasswordMeta;
import com.liferay.portal.security.password.service.base.PasswordMetaLocalServiceBaseImpl;
import com.liferay.portal.security.password.service.persistence.PasswordHashGeneratorPersistence;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The implementation of the password meta local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the <code>PasswordMetaLocalService</code> interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordMetaLocalServiceBaseImpl
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.password.model.PasswordMeta",
	service = AopService.class
)
public class PasswordMetaLocalServiceImpl
	extends PasswordMetaLocalServiceBaseImpl {

	/**
	 * Add a passwordMeta for the given passwordEntry during passwordEntry generation.
	 *
	 * @param  PasswordEntryId the passwordEntryId
	 * @param  salt the password salt
	 * @return Generated meta for passwordEntry
	 * @throws PortalException
	 */
	@Override
	public PasswordMeta addPasswordMeta(
			long passwordEntryId, byte[] salt, String pepperId)
		throws PortalException {

		long metaId = counterLocalService.increment(
			PasswordMeta.class.getName());

		PasswordMeta meta = passwordMetaPersistence.create(metaId);

		meta.setPasswordEntryId(passwordEntryId);
		meta.setSalt(Base64.encode(salt));
		meta.setPepperId(pepperId);

		return passwordMetaPersistence.update(meta);
	}

	/**
	 * Delete the given passwordMeta from database, if the given passwordMeta
	 * is the oldest entry, then also delete all PasswordHashGenerators that
	 * are older than its create date.
	 *
	 * @param  PasswordMetaId the passwordMetaId
	 * @return The deleted PasswordEntry
	 */
	@Override
	public PasswordMeta deletePasswordMeta(long passwordMetaId)
		throws PortalException {

		return passwordMetaLocalService.deletePasswordMeta(
			getPasswordMeta(passwordMetaId));
	}

	/**
	 * Delete the given passwordMeta from database, if the given passwordMeta
	 * is the oldest entry, then also delete all PasswordHashGenerators that
	 * are older than its create date.
	 *
	 * @param  PasswordMeta the passwordMeta
	 * @return The deleted PasswordEntry
	 */
	@Override
	public PasswordMeta deletePasswordMeta(PasswordMeta passwordMeta) {
		Date createDate = passwordMeta.getCreateDate();

		PasswordHashGenerator passwordHashGenerator =
			_passwordHashGeneratorPersistence.fetchByLtCreateDate_Last(
				createDate, null);

		// Potentially need to remove meta's corresponding hahsGenerator
		// if it is not null.

		if (passwordHashGenerator != null) {
			PasswordMeta prevPasswordMeta =
				passwordMetaPersistence.fetchByLtCreateDate_Last(
					createDate, null);

			// Meaning this meta is the oldest meta in the table and
			// thus the only one using this hashGenerator

			if (prevPasswordMeta == null) {
				_passwordHashGeneratorPersistence.remove(passwordHashGenerator);
			}
			else {
				Date passwordHashGeneratorCreateDate =
					passwordHashGenerator.getCreateDate();

				Date prevPasswordMetaCreateDate =
					prevPasswordMeta.getCreateDate();

				// Meaning this meta is the only one using this hashGenerator

				if (passwordHashGeneratorCreateDate.after(
						prevPasswordMetaCreateDate)) {

					_passwordHashGeneratorPersistence.remove(
						passwordHashGenerator);
				}
			}
		}

		return passwordMetaPersistence.remove(passwordMeta);
	}

	/**
	 * Delete all password metas associated with the given passwordEntry
	 *
	 * @param  PasswordEntryId the passwordEntryId
	 */
	@Override
	public void deletePasswordMetasByPasswordEntry(
		PasswordEntry passwordEntry) {

		passwordMetaLocalService.deletePasswordMetasByPasswordEntryId(
			passwordEntry.getPasswordEntryId());
	}

	/**
	 * Delete all password metas associated with the given passwordEntry Id
	 *
	 * @param  PasswordEntryId the passwordEntryId
	 */
	@Override
	public void deletePasswordMetasByPasswordEntryId(long passwordEntryId) {
		List<PasswordMeta> metas =
			passwordMetaPersistence.findByPasswordEntryId(passwordEntryId);

		for (PasswordMeta meta : metas) {
			passwordMetaLocalService.deletePasswordMeta(meta);
		}
	}

	/**
	 * Return a list of metas of PasswordEntry, asc ordered by create date
	 *
	 * @param  PasswordEntry the passwordEntry
	 * @return a list of metas of PasswordEntry
	 * @throws PortalException
	 */
	@Override
	public List<PasswordMeta> getPasswordMetasByPasswordEntry(
			PasswordEntry passwordEntry)
		throws PortalException {

		return passwordMetaLocalService.getPasswordMetasByPasswordEntryId(
			passwordEntry.getPasswordEntryId());
	}

	/**
	 * Return a list of metas of PasswordEntry, asc ordered by create date
	 *
	 * @param  PasswordEntryId the ID of passwordEntry
	 * @return a list of metas of PasswordEntry
	 * @throws PortalException
	 */
	@Override
	public List<PasswordMeta> getPasswordMetasByPasswordEntryId(
			long passwordEntryId)
		throws PortalException {

		return passwordMetaPersistence.findByPasswordEntryId(passwordEntryId);
	}

	@Override
	public int getPasswordMetasCountByPasswordEntry(PasswordEntry passwordEntry)
		throws PortalException {

		return passwordMetaLocalService.getPasswordMetasCountByPasswordEntryId(
			passwordEntry.getPasswordEntryId());
	}

	@Override
	public int getPasswordMetasCountByPasswordEntryId(long passwordEntryId)
		throws PortalException {

		return passwordMetaPersistence.countByPasswordEntryId(passwordEntryId);
	}

	@Reference
	private PasswordHashGeneratorPersistence _passwordHashGeneratorPersistence;

}