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
import com.liferay.portal.security.password.model.PasswordMeta;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing PasswordMeta in entity cache.
 *
 * @author Arthur Chan
 * @generated
 */
public class PasswordMetaCacheModel
	implements CacheModel<PasswordMeta>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PasswordMetaCacheModel)) {
			return false;
		}

		PasswordMetaCacheModel passwordMetaCacheModel =
			(PasswordMetaCacheModel)object;

		if ((passwordMetaId == passwordMetaCacheModel.passwordMetaId) &&
			(mvccVersion == passwordMetaCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, passwordMetaId);

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
		StringBundler sb = new StringBundler(17);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", uuid=");
		sb.append(uuid);
		sb.append(", passwordMetaId=");
		sb.append(passwordMetaId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", passwordEntryId=");
		sb.append(passwordEntryId);
		sb.append(", salt=");
		sb.append(salt);
		sb.append(", pepperId=");
		sb.append(pepperId);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public PasswordMeta toEntityModel() {
		PasswordMetaImpl passwordMetaImpl = new PasswordMetaImpl();

		passwordMetaImpl.setMvccVersion(mvccVersion);

		if (uuid == null) {
			passwordMetaImpl.setUuid("");
		}
		else {
			passwordMetaImpl.setUuid(uuid);
		}

		passwordMetaImpl.setPasswordMetaId(passwordMetaId);
		passwordMetaImpl.setCompanyId(companyId);

		if (createDate == Long.MIN_VALUE) {
			passwordMetaImpl.setCreateDate(null);
		}
		else {
			passwordMetaImpl.setCreateDate(new Date(createDate));
		}

		passwordMetaImpl.setPasswordEntryId(passwordEntryId);

		if (salt == null) {
			passwordMetaImpl.setSalt("");
		}
		else {
			passwordMetaImpl.setSalt(salt);
		}

		if (pepperId == null) {
			passwordMetaImpl.setPepperId("");
		}
		else {
			passwordMetaImpl.setPepperId(pepperId);
		}

		passwordMetaImpl.resetOriginalValues();

		return passwordMetaImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();
		uuid = objectInput.readUTF();

		passwordMetaId = objectInput.readLong();

		companyId = objectInput.readLong();
		createDate = objectInput.readLong();

		passwordEntryId = objectInput.readLong();
		salt = objectInput.readUTF();
		pepperId = objectInput.readUTF();
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

		objectOutput.writeLong(passwordMetaId);

		objectOutput.writeLong(companyId);
		objectOutput.writeLong(createDate);

		objectOutput.writeLong(passwordEntryId);

		if (salt == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(salt);
		}

		if (pepperId == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(pepperId);
		}
	}

	public long mvccVersion;
	public String uuid;
	public long passwordMetaId;
	public long companyId;
	public long createDate;
	public long passwordEntryId;
	public String salt;
	public String pepperId;

}