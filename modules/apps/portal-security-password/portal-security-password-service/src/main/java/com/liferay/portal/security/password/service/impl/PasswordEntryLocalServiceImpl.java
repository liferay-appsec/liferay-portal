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
import com.liferay.portal.crypto.hash.flavor.HashFlavor;
import com.liferay.portal.crypto.hash.generation.context.HashGenerationContext;
import com.liferay.portal.crypto.hash.generation.response.HashGenerationResponse;
import com.liferay.portal.crypto.hash.processor.HashProcessor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PasswordPolicy;
import com.liferay.portal.kernel.service.PasswordPolicyLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.security.password.model.PasswordEntry;
import com.liferay.portal.security.password.model.PasswordHashGenerator;
import com.liferay.portal.security.password.service.PasswordHashGeneratorLocalService;
import com.liferay.portal.security.password.service.PasswordMetaLocalService;
import com.liferay.portal.security.password.service.base.PasswordEntryLocalServiceBaseImpl;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The implementation of the password entry local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the <code>com.liferay.portal.security.password.service.PasswordEntryLocalService</code> interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS passwords because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordEntryLocalServiceBaseImpl
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.password.model.PasswordEntry",
	service = AopService.class
)
public class PasswordEntryLocalServiceImpl
	extends PasswordEntryLocalServiceBaseImpl {

	/**
	 * If current hash generator is no longer considered secured, allow to
	 * apply a new and secured hash generator to the persisted password hash
	 * so that the persisted password will remain secured.
	 *
	 * @param  UserId the userId
	 * @param  PasswordHashGenerator the passwordHashGenerator
	 * @return Same passowrdEntry with different hash
	 * @throws PortalException
	 */
	@Override
	public PasswordEntry addHashGeneratorToCurrentPasswordEntryByUserId(
			long userId)
		throws PortalException {

		PasswordEntry passwordEntry =
			passwordEntryLocalService.getLastPasswordEntryByUserId(userId);

		return _addHashGeneratorToPasswordEntry(passwordEntry);
	}

	/**
	 * Add a passwordEntry for user of userId
	 *
	 * @param  UserId ID of user
	 * @param  Password plain text password
	 * @return Generated PassowrdEntry
	 * @throws PortalException
	 */
	@Override
	public PasswordEntry addPasswordEntry(long userId, String password)
		throws PortalException {

		List<PasswordEntry> passwordEntries = getPasswordEntriesByUserId(
			userId);

		if (!passwordEntries.isEmpty()) {
			throw new PortalException(
				"user " + userId + " already has password.");
		}

		return _addPasswordEntry(userId, password);
	}

	/**
	 * Remove all password entries for given user, including current password and history passwords.
	 *
	 * @param  UserId ID of user
	 */
	@Override
	public void deletePasswordEntriesByUserId(long userId) {
		List<PasswordEntry> passwordEntries =
			passwordEntryLocalService.getPasswordEntriesByUserId(userId);

		for (PasswordEntry passwordEntry : passwordEntries) {
			passwordEntryLocalService.deletePasswordEntry(passwordEntry);
		}
	}

	@Override
	public PasswordEntry deletePasswordEntry(long passwordEntryId)
		throws PortalException {

		_passwordMetaLocalService.deletePasswordMetasByPasswordEntryId(
			passwordEntryId);

		return passwordEntryPersistence.remove(passwordEntryId);
	}

	@Override
	public PasswordEntry deletePasswordEntry(PasswordEntry passwordEntry) {
		_passwordMetaLocalService.deletePasswordMetasByPasswordEntry(
			passwordEntry);

		return passwordEntryPersistence.remove(passwordEntry);
	}

	/**
	 * Return current password for given user.
	 *
	 * @param  UserId ID of user
	 * @return PasswordEntry
	 * @throws PortalException
	 */
	@Override
	public PasswordEntry getLastPasswordEntryByUserId(long userId)
		throws PortalException {

		// OrderByComparator can be null, because nature order is
		// set to ascending by create date in service.xml

		return passwordEntryPersistence.findByUserId_Last(userId, null);
	}

	/**
	 * Return all password entries for given user, including current password and history passwords. Ordered by modified date.
	 *
	 * @param  UserId ID of user
	 * @return A list of password entries
	 */
	@Override
	public List<PasswordEntry> getPasswordEntriesByUserId(long userId) {
		return passwordEntryPersistence.findByUserId(userId);
	}

	/**
	 * When updating passwordEntries for a user, two things can happen:
	 * 1. when passwordPolicy is set to have password history,
	 *    remove extra history passwords that are over history count limit,
	 *    and return a newly added password entry for the user.
	 * 2. otherwise,
	 *    remove all passwords and return a newly added password entry.
	 *
	 * @param  UserId ID of user
	 * @param  Password plain text password
	 * @return Updated PassowrdEntry
	 * @throws PortalException
	 */
	@Override
	public PasswordEntry updatePasswordEntriesByUserId(
			long userId, String password,
			PasswordHashGenerator passwordHashGenerator)
		throws PortalException {

		PasswordPolicy passwordPolicy =
			_passwordPolicyLocalService.getPasswordPolicyByUserId(userId);

		List<PasswordEntry> passwordEntries = getPasswordEntriesByUserId(
			userId);

		if ((passwordPolicy == null) || !passwordPolicy.isHistory()) {
			for (PasswordEntry passwordEntry : passwordEntries) {
				deletePasswordEntry(passwordEntry);
			}
		}
		else {
			int historyCount = passwordPolicy.getHistoryCount();

			for (int i = passwordEntries.size() - 1; i > (historyCount - 1);
				 --i) {

				deletePasswordEntry(passwordEntries.get(i));
			}
		}

		return _addPasswordEntry(userId, password);
	}

	/**
	 * If current hash generator is no longer considered secured, allow to
	 * apply a new and secured hash generator to the persisted password hash
	 * so that the persisted password will remain secured.
	 *
	 * @param  PasswordEntry the passwordEntry
	 * @return A new passowrdEntry with different hash
	 * @throws PortalException
	 */
	private PasswordEntry _addHashGeneratorToPasswordEntry(
			PasswordEntry passwordEntry)
		throws PortalException {

		HashGenerationResponse hashGenerationResponse;

		try {
			hashGenerationResponse = _hashProcessor.generate(
				Base64.decode(passwordEntry.getHash()), _hashGenerationContext);
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}

		HashFlavor hashFlavor = hashGenerationResponse.getHashFlavor();

		Optional<byte[]> optionalSalt = hashFlavor.getSalt();

		Optional<String> optionalPepperId = hashFlavor.getPepperId();

		_passwordMetaLocalService.addPasswordMeta(
			passwordEntry.getPasswordEntryId(),
			optionalSalt.orElse(new byte[0]), optionalPepperId.orElse(""));

		passwordEntry.setHash(Base64.encode(hashGenerationResponse.getHash()));

		return passwordEntryPersistence.update(passwordEntry);
	}

	private PasswordEntry _addPasswordEntry(long userId, String password)
		throws PortalException {

		long passwordEntryId = counterLocalService.increment(
			PasswordEntry.class.getName());

		PasswordEntry passwordEntry = passwordEntryPersistence.create(
			passwordEntryId);

		HashGenerationResponse hashGenerationResponse;

		try {
			hashGenerationResponse = _hashProcessor.generate(
				password.getBytes(), _hashGenerationContext);
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}

		HashFlavor hashFlavor = hashGenerationResponse.getHashFlavor();

		Optional<byte[]> optionalSalt = hashFlavor.getSalt();

		Optional<String> optionalPepperId = hashFlavor.getPepperId();

		_passwordMetaLocalService.addPasswordMeta(
			passwordEntryId, optionalSalt.orElse(new byte[0]),
			optionalPepperId.orElse(""));

		passwordEntry.setUserId(userId);

		passwordEntry.setHash(Base64.encode(hashGenerationResponse.getHash()));

		return passwordEntryPersistence.update(passwordEntry);
	}

	@Reference
	private HashGenerationContext _hashGenerationContext;

	@Reference
	private HashProcessor _hashProcessor;

	@Reference
	private PasswordHashGeneratorLocalService
		_passwordHashGeneratorLocalService;

	@Reference
	private PasswordMetaLocalService _passwordMetaLocalService;

	@Reference
	private PasswordPolicyLocalService _passwordPolicyLocalService;

}