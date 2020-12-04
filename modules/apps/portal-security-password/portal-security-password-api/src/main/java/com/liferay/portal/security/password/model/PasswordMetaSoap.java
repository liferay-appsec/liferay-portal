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
 * This class is used by SOAP remote services, specifically {@link com.liferay.portal.security.password.service.http.PasswordMetaServiceSoap}.
 *
 * @author Arthur Chan
 * @generated
 */
public class PasswordMetaSoap implements Serializable {

	public static PasswordMetaSoap toSoapModel(PasswordMeta model) {
		PasswordMetaSoap soapModel = new PasswordMetaSoap();

		soapModel.setMvccVersion(model.getMvccVersion());
		soapModel.setUuid(model.getUuid());
		soapModel.setPasswordMetaId(model.getPasswordMetaId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setCreateDate(model.getCreateDate());
		soapModel.setPasswordEntryId(model.getPasswordEntryId());
		soapModel.setSalt(model.getSalt());
		soapModel.setPepperId(model.getPepperId());

		return soapModel;
	}

	public static PasswordMetaSoap[] toSoapModels(PasswordMeta[] models) {
		PasswordMetaSoap[] soapModels = new PasswordMetaSoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static PasswordMetaSoap[][] toSoapModels(PasswordMeta[][] models) {
		PasswordMetaSoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels = new PasswordMetaSoap[models.length][models[0].length];
		}
		else {
			soapModels = new PasswordMetaSoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static PasswordMetaSoap[] toSoapModels(List<PasswordMeta> models) {
		List<PasswordMetaSoap> soapModels = new ArrayList<PasswordMetaSoap>(
			models.size());

		for (PasswordMeta model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(new PasswordMetaSoap[soapModels.size()]);
	}

	public PasswordMetaSoap() {
	}

	public long getPrimaryKey() {
		return _passwordMetaId;
	}

	public void setPrimaryKey(long pk) {
		setPasswordMetaId(pk);
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

	public long getPasswordMetaId() {
		return _passwordMetaId;
	}

	public void setPasswordMetaId(long passwordMetaId) {
		_passwordMetaId = passwordMetaId;
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

	public long getPasswordEntryId() {
		return _passwordEntryId;
	}

	public void setPasswordEntryId(long passwordEntryId) {
		_passwordEntryId = passwordEntryId;
	}

	public String getSalt() {
		return _salt;
	}

	public void setSalt(String salt) {
		_salt = salt;
	}

	public String getPepperId() {
		return _pepperId;
	}

	public void setPepperId(String pepperId) {
		_pepperId = pepperId;
	}

	private long _mvccVersion;
	private String _uuid;
	private long _passwordMetaId;
	private long _companyId;
	private Date _createDate;
	private long _passwordEntryId;
	private String _salt;
	private String _pepperId;

}