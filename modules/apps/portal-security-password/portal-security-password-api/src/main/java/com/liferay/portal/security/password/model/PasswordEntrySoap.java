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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used by SOAP remote services, specifically {@link com.liferay.portal.security.password.service.http.PasswordEntryServiceSoap}.
 *
 * @author Arthur Chan
 * @generated
 */
public class PasswordEntrySoap implements Serializable {

	public static PasswordEntrySoap toSoapModel(PasswordEntry model) {
		PasswordEntrySoap soapModel = new PasswordEntrySoap();

		soapModel.setMvccVersion(model.getMvccVersion());
		soapModel.setUuid(model.getUuid());
		soapModel.setPasswordEntryId(model.getPasswordEntryId());
		soapModel.setUserId(model.getUserId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setCreateDate(model.getCreateDate());
		soapModel.setHash(model.getHash());

		return soapModel;
	}

	public static PasswordEntrySoap[] toSoapModels(PasswordEntry[] models) {
		PasswordEntrySoap[] soapModels = new PasswordEntrySoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static PasswordEntrySoap[][] toSoapModels(PasswordEntry[][] models) {
		PasswordEntrySoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels = new PasswordEntrySoap[models.length][models[0].length];
		}
		else {
			soapModels = new PasswordEntrySoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static PasswordEntrySoap[] toSoapModels(List<PasswordEntry> models) {
		List<PasswordEntrySoap> soapModels = new ArrayList<PasswordEntrySoap>(
			models.size());

		for (PasswordEntry model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(new PasswordEntrySoap[soapModels.size()]);
	}

	public PasswordEntrySoap() {
	}

	public long getPrimaryKey() {
		return _passwordEntryId;
	}

	public void setPrimaryKey(long pk) {
		setPasswordEntryId(pk);
	}

	public long getMvccVersion() {
		return _mvccVersion;
	}

	public void setMvccVersion(long mvccVersion) {
		_mvccVersion = mvccVersion;
	}

	public String getUuid() {
		return _uuid;
	}

	public void setUuid(String uuid) {
		_uuid = uuid;
	}

	public long getPasswordEntryId() {
		return _passwordEntryId;
	}

	public void setPasswordEntryId(long passwordEntryId) {
		_passwordEntryId = passwordEntryId;
	}

	public long getUserId() {
		return _userId;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public void setCreateDate(Date createDate) {
		_createDate = createDate;
	}

	public String getHash() {
		return _hash;
	}

	public void setHash(String hash) {
		_hash = hash;
	}

	private long _mvccVersion;
	private String _uuid;
	private long _passwordEntryId;
	private long _userId;
	private long _companyId;
	private Date _createDate;
	private String _hash;

}