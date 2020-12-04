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
 * This class is a wrapper for {@link PasswordHashGenerator}.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordHashGenerator
 * @generated
 */
public class PasswordHashGeneratorWrapper
	extends BaseModelWrapper<PasswordHashGenerator>
	implements ModelWrapper<PasswordHashGenerator>, PasswordHashGenerator {

	public PasswordHashGeneratorWrapper(
		PasswordHashGenerator passwordHashGenerator) {

		super(passwordHashGenerator);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("uuid", getUuid());
		attributes.put("passwordHashGeneratorId", getPasswordHashGeneratorId());
		attributes.put("companyId", getCompanyId());
		attributes.put("createDate", getCreateDate());
		attributes.put("hashGeneratorName", getHashGeneratorName());
		attributes.put("hashGeneratorMeta", getHashGeneratorMeta());

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

		Long passwordHashGeneratorId = (Long)attributes.get(
			"passwordHashGeneratorId");

		if (passwordHashGeneratorId != null) {
			setPasswordHashGeneratorId(passwordHashGeneratorId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		String hashGeneratorName = (String)attributes.get("hashGeneratorName");

		if (hashGeneratorName != null) {
			setHashGeneratorName(hashGeneratorName);
		}

		String hashGeneratorMeta = (String)attributes.get("hashGeneratorMeta");

		if (hashGeneratorMeta != null) {
			setHashGeneratorMeta(hashGeneratorMeta);
		}
	}

	/**
	 * Returns the company ID of this password hash generator.
	 *
	 * @return the company ID of this password hash generator
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this password hash generator.
	 *
	 * @return the create date of this password hash generator
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the hash generator meta of this password hash generator.
	 *
	 * @return the hash generator meta of this password hash generator
	 */
	@Override
	public String getHashGeneratorMeta() {
		return model.getHashGeneratorMeta();
	}

	/**
	 * Returns the hash generator name of this password hash generator.
	 *
	 * @return the hash generator name of this password hash generator
	 */
	@Override
	public String getHashGeneratorName() {
		return model.getHashGeneratorName();
	}

	/**
	 * Returns the mvcc version of this password hash generator.
	 *
	 * @return the mvcc version of this password hash generator
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the password hash generator ID of this password hash generator.
	 *
	 * @return the password hash generator ID of this password hash generator
	 */
	@Override
	public long getPasswordHashGeneratorId() {
		return model.getPasswordHashGeneratorId();
	}

	/**
	 * Returns the primary key of this password hash generator.
	 *
	 * @return the primary key of this password hash generator
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the uuid of this password hash generator.
	 *
	 * @return the uuid of this password hash generator
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
	 * Sets the company ID of this password hash generator.
	 *
	 * @param companyId the company ID of this password hash generator
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this password hash generator.
	 *
	 * @param createDate the create date of this password hash generator
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the hash generator meta of this password hash generator.
	 *
	 * @param hashGeneratorMeta the hash generator meta of this password hash generator
	 */
	@Override
	public void setHashGeneratorMeta(String hashGeneratorMeta) {
		model.setHashGeneratorMeta(hashGeneratorMeta);
	}

	/**
	 * Sets the hash generator name of this password hash generator.
	 *
	 * @param hashGeneratorName the hash generator name of this password hash generator
	 */
	@Override
	public void setHashGeneratorName(String hashGeneratorName) {
		model.setHashGeneratorName(hashGeneratorName);
	}

	/**
	 * Sets the mvcc version of this password hash generator.
	 *
	 * @param mvccVersion the mvcc version of this password hash generator
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the password hash generator ID of this password hash generator.
	 *
	 * @param passwordHashGeneratorId the password hash generator ID of this password hash generator
	 */
	@Override
	public void setPasswordHashGeneratorId(long passwordHashGeneratorId) {
		model.setPasswordHashGeneratorId(passwordHashGeneratorId);
	}

	/**
	 * Sets the primary key of this password hash generator.
	 *
	 * @param primaryKey the primary key of this password hash generator
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the uuid of this password hash generator.
	 *
	 * @param uuid the uuid of this password hash generator
	 */
	@Override
	public void setUuid(String uuid) {
		model.setUuid(uuid);
	}

	@Override
	protected PasswordHashGeneratorWrapper wrap(
		PasswordHashGenerator passwordHashGenerator) {

		return new PasswordHashGeneratorWrapper(passwordHashGenerator);
	}

}