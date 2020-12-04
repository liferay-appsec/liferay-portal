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

package com.liferay.portal.security.password.model.impl;

import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;
import com.liferay.portal.security.password.model.PasswordHashGenerator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing PasswordHashGenerator in entity cache.
 *
 * @author Arthur Chan
 * @generated
 */
public class PasswordHashGeneratorCacheModel
	implements CacheModel<PasswordHashGenerator>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PasswordHashGeneratorCacheModel)) {
			return false;
		}

		PasswordHashGeneratorCacheModel passwordHashGeneratorCacheModel =
			(PasswordHashGeneratorCacheModel)object;

		if ((passwordHashGeneratorId ==
				passwordHashGeneratorCacheModel.passwordHashGeneratorId) &&
			(mvccVersion == passwordHashGeneratorCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, passwordHashGeneratorId);

		return HashUtil.hash(hashCode, mvccVersion);
	}

	@Override
	public long getMvccVersion() {
		return mvccVersion;
	}

	@Override
	public void setMvccVersion(long mvccVersion) {
		this.mvccVersion = mvccVersion;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(15);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", uuid=");
		sb.append(uuid);
		sb.append(", passwordHashGeneratorId=");
		sb.append(passwordHashGeneratorId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", hashGeneratorName=");
		sb.append(hashGeneratorName);
		sb.append(", hashGeneratorMeta=");
		sb.append(hashGeneratorMeta);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public PasswordHashGenerator toEntityModel() {
		PasswordHashGeneratorImpl passwordHashGeneratorImpl =
			new PasswordHashGeneratorImpl();

		passwordHashGeneratorImpl.setMvccVersion(mvccVersion);

		if (uuid == null) {
			passwordHashGeneratorImpl.setUuid("");
		}
		else {
			passwordHashGeneratorImpl.setUuid(uuid);
		}

		passwordHashGeneratorImpl.setPasswordHashGeneratorId(
			passwordHashGeneratorId);
		passwordHashGeneratorImpl.setCompanyId(companyId);

		if (createDate == Long.MIN_VALUE) {
			passwordHashGeneratorImpl.setCreateDate(null);
		}
		else {
			passwordHashGeneratorImpl.setCreateDate(new Date(createDate));
		}

		if (hashGeneratorName == null) {
			passwordHashGeneratorImpl.setHashGeneratorName("");
		}
		else {
			passwordHashGeneratorImpl.setHashGeneratorName(hashGeneratorName);
		}

		if (hashGeneratorMeta == null) {
			passwordHashGeneratorImpl.setHashGeneratorMeta("");
		}
		else {
			passwordHashGeneratorImpl.setHashGeneratorMeta(hashGeneratorMeta);
		}

		passwordHashGeneratorImpl.resetOriginalValues();

		return passwordHashGeneratorImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();
		uuid = objectInput.readUTF();

		passwordHashGeneratorId = objectInput.readLong();

		companyId = objectInput.readLong();
		createDate = objectInput.readLong();
		hashGeneratorName = objectInput.readUTF();
		hashGeneratorMeta = objectInput.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		if (uuid == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(uuid);
		}

		objectOutput.writeLong(passwordHashGeneratorId);

		objectOutput.writeLong(companyId);
		objectOutput.writeLong(createDate);

		if (hashGeneratorName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(hashGeneratorName);
		}

		if (hashGeneratorMeta == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(hashGeneratorMeta);
		}
	}

	public long mvccVersion;
	public String uuid;
	public long passwordHashGeneratorId;
	public long companyId;
	public long createDate;
	public String hashGeneratorName;
	public String hashGeneratorMeta;

}