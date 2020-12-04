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

package com.liferay.portal.security.password.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link PasswordEntry}.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordEntry
 * @generated
 */
public class PasswordEntryWrapper
	extends BaseModelWrapper<PasswordEntry>
	implements ModelWrapper<PasswordEntry>, PasswordEntry {

	public PasswordEntryWrapper(PasswordEntry passwordEntry) {
		super(passwordEntry);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("uuid", getUuid());
		attributes.put("passwordEntryId", getPasswordEntryId());
		attributes.put("userId", getUserId());
		attributes.put("companyId", getCompanyId());
		attributes.put("createDate", getCreateDate());
		attributes.put("hash", getHash());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		String uuid = (String)attributes.get("uuid");

		if (uuid != null) {
			setUuid(uuid);
		}

		Long passwordEntryId = (Long)attributes.get("passwordEntryId");

		if (passwordEntryId != null) {
			setPasswordEntryId(passwordEntryId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		String hash = (String)attributes.get("hash");

		if (hash != null) {
			setHash(hash);
		}
	}

	/**
	 * Returns the company ID of this password entry.
	 *
	 * @return the company ID of this password entry
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this password entry.
	 *
	 * @return the create date of this password entry
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the hash of this password entry.
	 *
	 * @return the hash of this password entry
	 */
	@Override
	public String getHash() {
		return model.getHash();
	}

	/**
	 * Returns the mvcc version of this password entry.
	 *
	 * @return the mvcc version of this password entry
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the password entry ID of this password entry.
	 *
	 * @return the password entry ID of this password entry
	 */
	@Override
	public long getPasswordEntryId() {
		return model.getPasswordEntryId();
	}

	/**
	 * Returns the primary key of this password entry.
	 *
	 * @return the primary key of this password entry
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the user ID of this password entry.
	 *
	 * @return the user ID of this password entry
	 */
	@Override
	public long getUserId() {
		return model.getUserId();
	}

	/**
	 * Returns the user uuid of this password entry.
	 *
	 * @return the user uuid of this password entry
	 */
	@Override
	public String getUserUuid() {
		return model.getUserUuid();
	}

	/**
	 * Returns the uuid of this password entry.
	 *
	 * @return the uuid of this password entry
	 */
	@Override
	public String getUuid() {
		return model.getUuid();
	}

	@Override
	public void persist() {
		model.persist();
	}

	/**
	 * Sets the company ID of this password entry.
	 *
	 * @param companyId the company ID of this password entry
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this password entry.
	 *
	 * @param createDate the create date of this password entry
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the hash of this password entry.
	 *
	 * @param hash the hash of this password entry
	 */
	@Override
	public void setHash(String hash) {
		model.setHash(hash);
	}

	/**
	 * Sets the mvcc version of this password entry.
	 *
	 * @param mvccVersion the mvcc version of this password entry
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the password entry ID of this password entry.
	 *
	 * @param passwordEntryId the password entry ID of this password entry
	 */
	@Override
	public void setPasswordEntryId(long passwordEntryId) {
		model.setPasswordEntryId(passwordEntryId);
	}

	/**
	 * Sets the primary key of this password entry.
	 *
	 * @param primaryKey the primary key of this password entry
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the user ID of this password entry.
	 *
	 * @param userId the user ID of this password entry
	 */
	@Override
	public void setUserId(long userId) {
		model.setUserId(userId);
	}

	/**
	 * Sets the user uuid of this password entry.
	 *
	 * @param userUuid the user uuid of this password entry
	 */
	@Override
	public void setUserUuid(String userUuid) {
		model.setUserUuid(userUuid);
	}

	/**
	 * Sets the uuid of this password entry.
	 *
	 * @param uuid the uuid of this password entry
	 */
	@Override
	public void setUuid(String uuid) {
		model.setUuid(uuid);
	}

	@Override
	protected PasswordEntryWrapper wrap(PasswordEntry passwordEntry) {
		return new PasswordEntryWrapper(passwordEntry);
	}

}