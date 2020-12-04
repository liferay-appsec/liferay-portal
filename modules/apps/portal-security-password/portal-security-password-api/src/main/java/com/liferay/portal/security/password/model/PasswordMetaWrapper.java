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
 * This class is a wrapper for {@link PasswordMeta}.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordMeta
 * @generated
 */
public class PasswordMetaWrapper
	extends BaseModelWrapper<PasswordMeta>
	implements ModelWrapper<PasswordMeta>, PasswordMeta {

	public PasswordMetaWrapper(PasswordMeta passwordMeta) {
		super(passwordMeta);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("uuid", getUuid());
		attributes.put("passwordMetaId", getPasswordMetaId());
		attributes.put("companyId", getCompanyId());
		attributes.put("createDate", getCreateDate());
		attributes.put("passwordEntryId", getPasswordEntryId());
		attributes.put("salt", getSalt());
		attributes.put("pepperId", getPepperId());

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

		Long passwordMetaId = (Long)attributes.get("passwordMetaId");

		if (passwordMetaId != null) {
			setPasswordMetaId(passwordMetaId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		Long passwordEntryId = (Long)attributes.get("passwordEntryId");

		if (passwordEntryId != null) {
			setPasswordEntryId(passwordEntryId);
		}

		String salt = (String)attributes.get("salt");

		if (salt != null) {
			setSalt(salt);
		}

		String pepperId = (String)attributes.get("pepperId");

		if (pepperId != null) {
			setPepperId(pepperId);
		}
	}

	/**
	 * Returns the company ID of this password meta.
	 *
	 * @return the company ID of this password meta
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this password meta.
	 *
	 * @return the create date of this password meta
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the mvcc version of this password meta.
	 *
	 * @return the mvcc version of this password meta
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the password entry ID of this password meta.
	 *
	 * @return the password entry ID of this password meta
	 */
	@Override
	public long getPasswordEntryId() {
		return model.getPasswordEntryId();
	}

	/**
	 * Returns the password meta ID of this password meta.
	 *
	 * @return the password meta ID of this password meta
	 */
	@Override
	public long getPasswordMetaId() {
		return model.getPasswordMetaId();
	}

	/**
	 * Returns the pepper ID of this password meta.
	 *
	 * @return the pepper ID of this password meta
	 */
	@Override
	public String getPepperId() {
		return model.getPepperId();
	}

	/**
	 * Returns the primary key of this password meta.
	 *
	 * @return the primary key of this password meta
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the salt of this password meta.
	 *
	 * @return the salt of this password meta
	 */
	@Override
	public String getSalt() {
		return model.getSalt();
	}

	/**
	 * Returns the uuid of this password meta.
	 *
	 * @return the uuid of this password meta
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
	 * Sets the company ID of this password meta.
	 *
	 * @param companyId the company ID of this password meta
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this password meta.
	 *
	 * @param createDate the create date of this password meta
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the mvcc version of this password meta.
	 *
	 * @param mvccVersion the mvcc version of this password meta
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the password entry ID of this password meta.
	 *
	 * @param passwordEntryId the password entry ID of this password meta
	 */
	@Override
	public void setPasswordEntryId(long passwordEntryId) {
		model.setPasswordEntryId(passwordEntryId);
	}

	/**
	 * Sets the password meta ID of this password meta.
	 *
	 * @param passwordMetaId the password meta ID of this password meta
	 */
	@Override
	public void setPasswordMetaId(long passwordMetaId) {
		model.setPasswordMetaId(passwordMetaId);
	}

	/**
	 * Sets the pepper ID of this password meta.
	 *
	 * @param pepperId the pepper ID of this password meta
	 */
	@Override
	public void setPepperId(String pepperId) {
		model.setPepperId(pepperId);
	}

	/**
	 * Sets the primary key of this password meta.
	 *
	 * @param primaryKey the primary key of this password meta
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the salt of this password meta.
	 *
	 * @param salt the salt of this password meta
	 */
	@Override
	public void setSalt(String salt) {
		model.setSalt(salt);
	}

	/**
	 * Sets the uuid of this password meta.
	 *
	 * @param uuid the uuid of this password meta
	 */
	@Override
	public void setUuid(String uuid) {
		model.setUuid(uuid);
	}

	@Override
	protected PasswordMetaWrapper wrap(PasswordMeta passwordMeta) {
		return new PasswordMetaWrapper(passwordMeta);
	}

}