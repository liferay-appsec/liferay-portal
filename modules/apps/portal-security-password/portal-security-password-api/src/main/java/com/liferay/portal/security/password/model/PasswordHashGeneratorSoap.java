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
 * This class is used by SOAP remote services, specifically {@link com.liferay.portal.security.password.service.http.PasswordHashGeneratorServiceSoap}.
 *
 * @author Arthur Chan
 * @generated
 */
public class PasswordHashGeneratorSoap implements Serializable {

	public static PasswordHashGeneratorSoap toSoapModel(
		PasswordHashGenerator model) {

		PasswordHashGeneratorSoap soapModel = new PasswordHashGeneratorSoap();

		soapModel.setMvccVersion(model.getMvccVersion());
		soapModel.setUuid(model.getUuid());
		soapModel.setPasswordHashGeneratorId(
			model.getPasswordHashGeneratorId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setCreateDate(model.getCreateDate());
		soapModel.setHashGeneratorName(model.getHashGeneratorName());
		soapModel.setHashGeneratorMeta(model.getHashGeneratorMeta());

		return soapModel;
	}

	public static PasswordHashGeneratorSoap[] toSoapModels(
		PasswordHashGenerator[] models) {

		PasswordHashGeneratorSoap[] soapModels =
			new PasswordHashGeneratorSoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static PasswordHashGeneratorSoap[][] toSoapModels(
		PasswordHashGenerator[][] models) {

		PasswordHashGeneratorSoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels =
				new PasswordHashGeneratorSoap[models.length][models[0].length];
		}
		else {
			soapModels = new PasswordHashGeneratorSoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static PasswordHashGeneratorSoap[] toSoapModels(
		List<PasswordHashGenerator> models) {

		List<PasswordHashGeneratorSoap> soapModels =
			new ArrayList<PasswordHashGeneratorSoap>(models.size());

		for (PasswordHashGenerator model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(
			new PasswordHashGeneratorSoap[soapModels.size()]);
	}

	public PasswordHashGeneratorSoap() {
	}

	public long getPrimaryKey() {
		return _passwordHashGeneratorId;
	}

	public void setPrimaryKey(long pk) {
		setPasswordHashGeneratorId(pk);
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

	public long getPasswordHashGeneratorId() {
		return _passwordHashGeneratorId;
	}

	public void setPasswordHashGeneratorId(long passwordHashGeneratorId) {
		_passwordHashGeneratorId = passwordHashGeneratorId;
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

	public String getHashGeneratorName() {
		return _hashGeneratorName;
	}

	public void setHashGeneratorName(String hashGeneratorName) {
		_hashGeneratorName = hashGeneratorName;
	}

	public String getHashGeneratorMeta() {
		return _hashGeneratorMeta;
	}

	public void setHashGeneratorMeta(String hashGeneratorMeta) {
		_hashGeneratorMeta = hashGeneratorMeta;
	}

	private long _mvccVersion;
	private String _uuid;
	private long _passwordHashGeneratorId;
	private long _companyId;
	private Date _createDate;
	private String _hashGeneratorName;
	private String _hashGeneratorMeta;

}