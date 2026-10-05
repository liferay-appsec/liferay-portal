/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.model.impl;

import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing AuditPseudonym in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class AuditPseudonymCacheModel
	implements CacheModel<AuditPseudonym>, Externalizable {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof AuditPseudonymCacheModel)) {
			return false;
		}

		AuditPseudonymCacheModel auditPseudonymCacheModel =
			(AuditPseudonymCacheModel)object;

		if (auditPseudonymId == auditPseudonymCacheModel.auditPseudonymId) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return HashUtil.hash(0, auditPseudonymId);
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(15);

		sb.append("{auditPseudonymId=");
		sb.append(auditPseudonymId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", contextName=");
		sb.append(contextName);
		sb.append(", fieldCategory=");
		sb.append(fieldCategory);
		sb.append(", value=");
		sb.append(value);
		sb.append(", valueHash=");
		sb.append(valueHash);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public AuditPseudonym toEntityModel() {
		AuditPseudonymImpl auditPseudonymImpl = new AuditPseudonymImpl();

		auditPseudonymImpl.setAuditPseudonymId(auditPseudonymId);
		auditPseudonymImpl.setCompanyId(companyId);

		if (createDate == Long.MIN_VALUE) {
			auditPseudonymImpl.setCreateDate(null);
		}
		else {
			auditPseudonymImpl.setCreateDate(new Date(createDate));
		}

		if (contextName == null) {
			auditPseudonymImpl.setContextName("");
		}
		else {
			auditPseudonymImpl.setContextName(contextName);
		}

		if (fieldCategory == null) {
			auditPseudonymImpl.setFieldCategory("");
		}
		else {
			auditPseudonymImpl.setFieldCategory(fieldCategory);
		}

		if (value == null) {
			auditPseudonymImpl.setValue("");
		}
		else {
			auditPseudonymImpl.setValue(value);
		}

		if (valueHash == null) {
			auditPseudonymImpl.setValueHash("");
		}
		else {
			auditPseudonymImpl.setValueHash(valueHash);
		}

		auditPseudonymImpl.resetOriginalValues();

		return auditPseudonymImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		auditPseudonymId = objectInput.readLong();

		companyId = objectInput.readLong();
		createDate = objectInput.readLong();
		contextName = objectInput.readUTF();
		fieldCategory = objectInput.readUTF();
		value = objectInput.readUTF();
		valueHash = objectInput.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(auditPseudonymId);

		objectOutput.writeLong(companyId);
		objectOutput.writeLong(createDate);

		if (contextName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(contextName);
		}

		if (fieldCategory == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(fieldCategory);
		}

		if (value == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(value);
		}

		if (valueHash == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(valueHash);
		}
	}

	public long auditPseudonymId;
	public long companyId;
	public long createDate;
	public String contextName;
	public String fieldCategory;
	public String value;
	public String valueHash;

}
// LIFERAY-SERVICE-BUILDER-HASH:1615478028