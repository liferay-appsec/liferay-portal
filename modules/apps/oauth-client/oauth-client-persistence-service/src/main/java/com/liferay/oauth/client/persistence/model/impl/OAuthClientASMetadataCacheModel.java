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

package com.liferay.oauth.client.persistence.model.impl;

import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing OAuthClientASMetadata in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class OAuthClientASMetadataCacheModel
	implements CacheModel<OAuthClientASMetadata>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof OAuthClientASMetadataCacheModel)) {
			return false;
		}

		OAuthClientASMetadataCacheModel oAuthClientASMetadataCacheModel =
			(OAuthClientASMetadataCacheModel)object;

		if ((oAuthClientASMetadataId ==
				oAuthClientASMetadataCacheModel.oAuthClientASMetadataId) &&
			(mvccVersion == oAuthClientASMetadataCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, oAuthClientASMetadataId);

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
		StringBundler sb = new StringBundler(19);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", oAuthClientASMetadataId=");
		sb.append(oAuthClientASMetadataId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", modifiedDate=");
		sb.append(modifiedDate);
		sb.append(", discoveryEndpoint=");
		sb.append(discoveryEndpoint);
		sb.append(", issuer=");
		sb.append(issuer);
		sb.append(", metadataJSON=");
		sb.append(metadataJSON);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public OAuthClientASMetadata toEntityModel() {
		OAuthClientASMetadataImpl oAuthClientASMetadataImpl =
			new OAuthClientASMetadataImpl();

		oAuthClientASMetadataImpl.setMvccVersion(mvccVersion);
		oAuthClientASMetadataImpl.setOAuthClientASMetadataId(
			oAuthClientASMetadataId);
		oAuthClientASMetadataImpl.setCompanyId(companyId);
		oAuthClientASMetadataImpl.setUserId(userId);

		if (createDate == Long.MIN_VALUE) {
			oAuthClientASMetadataImpl.setCreateDate(null);
		}
		else {
			oAuthClientASMetadataImpl.setCreateDate(new Date(createDate));
		}

		if (modifiedDate == Long.MIN_VALUE) {
			oAuthClientASMetadataImpl.setModifiedDate(null);
		}
		else {
			oAuthClientASMetadataImpl.setModifiedDate(new Date(modifiedDate));
		}

		if (discoveryEndpoint == null) {
			oAuthClientASMetadataImpl.setDiscoveryEndpoint("");
		}
		else {
			oAuthClientASMetadataImpl.setDiscoveryEndpoint(discoveryEndpoint);
		}

		if (issuer == null) {
			oAuthClientASMetadataImpl.setIssuer("");
		}
		else {
			oAuthClientASMetadataImpl.setIssuer(issuer);
		}

		if (metadataJSON == null) {
			oAuthClientASMetadataImpl.setMetadataJSON("");
		}
		else {
			oAuthClientASMetadataImpl.setMetadataJSON(metadataJSON);
		}

		oAuthClientASMetadataImpl.resetOriginalValues();

		return oAuthClientASMetadataImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput)
		throws ClassNotFoundException, IOException {

		mvccVersion = objectInput.readLong();

		oAuthClientASMetadataId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();
		createDate = objectInput.readLong();
		modifiedDate = objectInput.readLong();
		discoveryEndpoint = objectInput.readUTF();
		issuer = objectInput.readUTF();
		metadataJSON = (String)objectInput.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(oAuthClientASMetadataId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(userId);
		objectOutput.writeLong(createDate);
		objectOutput.writeLong(modifiedDate);

		if (discoveryEndpoint == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(discoveryEndpoint);
		}

		if (issuer == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(issuer);
		}

		if (metadataJSON == null) {
			objectOutput.writeObject("");
		}
		else {
			objectOutput.writeObject(metadataJSON);
		}
	}

	public long mvccVersion;
	public long oAuthClientASMetadataId;
	public long companyId;
	public long userId;
	public long createDate;
	public long modifiedDate;
	public String discoveryEndpoint;
	public String issuer;
	public String metadataJSON;

}