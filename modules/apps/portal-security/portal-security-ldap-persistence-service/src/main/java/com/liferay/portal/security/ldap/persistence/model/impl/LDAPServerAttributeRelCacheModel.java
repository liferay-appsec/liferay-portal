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

package com.liferay.portal.security.ldap.persistence.model.impl;

import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The cache model class for representing LDAPServerAttributeRel in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class LDAPServerAttributeRelCacheModel
	implements CacheModel<LDAPServerAttributeRel>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof LDAPServerAttributeRelCacheModel)) {
			return false;
		}

		LDAPServerAttributeRelCacheModel ldapServerAttributeRelCacheModel =
			(LDAPServerAttributeRelCacheModel)object;

		if ((ldapServerAttributeRelId ==
				ldapServerAttributeRelCacheModel.ldapServerAttributeRelId) &&
			(mvccVersion == ldapServerAttributeRelCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, ldapServerAttributeRelId);

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
		StringBundler sb = new StringBundler(13);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", ldapServerAttributeRelId=");
		sb.append(ldapServerAttributeRelId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", ldapServerId=");
		sb.append(ldapServerId);
		sb.append(", classNameId=");
		sb.append(classNameId);
		sb.append(", classPK=");
		sb.append(classPK);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public LDAPServerAttributeRel toEntityModel() {
		LDAPServerAttributeRelImpl ldapServerAttributeRelImpl =
			new LDAPServerAttributeRelImpl();

		ldapServerAttributeRelImpl.setMvccVersion(mvccVersion);
		ldapServerAttributeRelImpl.setLdapServerAttributeRelId(
			ldapServerAttributeRelId);
		ldapServerAttributeRelImpl.setCompanyId(companyId);
		ldapServerAttributeRelImpl.setLdapServerId(ldapServerId);
		ldapServerAttributeRelImpl.setClassNameId(classNameId);
		ldapServerAttributeRelImpl.setClassPK(classPK);

		ldapServerAttributeRelImpl.resetOriginalValues();

		return ldapServerAttributeRelImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		ldapServerAttributeRelId = objectInput.readLong();

		companyId = objectInput.readLong();

		ldapServerId = objectInput.readLong();

		classNameId = objectInput.readLong();

		classPK = objectInput.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(ldapServerAttributeRelId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(ldapServerId);

		objectOutput.writeLong(classNameId);

		objectOutput.writeLong(classPK);
	}

	public long mvccVersion;
	public long ldapServerAttributeRelId;
	public long companyId;
	public long ldapServerId;
	public long classNameId;
	public long classPK;

}