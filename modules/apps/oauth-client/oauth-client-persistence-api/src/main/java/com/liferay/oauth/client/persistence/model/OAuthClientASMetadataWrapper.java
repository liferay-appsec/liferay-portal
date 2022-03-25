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

package com.liferay.oauth.client.persistence.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link OAuthClientASMetadata}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadata
 * @generated
 */
public class OAuthClientASMetadataWrapper
	extends BaseModelWrapper<OAuthClientASMetadata>
	implements ModelWrapper<OAuthClientASMetadata>, OAuthClientASMetadata {

	public OAuthClientASMetadataWrapper(
		OAuthClientASMetadata oAuthClientASMetadata) {

		super(oAuthClientASMetadata);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("oAuthClientASMetadataId", getOAuthClientASMetadataId());
		attributes.put("companyId", getCompanyId());
		attributes.put("userId", getUserId());
		attributes.put("createDate", getCreateDate());
		attributes.put("modifiedDate", getModifiedDate());
		attributes.put("discoveryEndpoint", getDiscoveryEndpoint());
		attributes.put("issuer", getIssuer());
		attributes.put("metadataJSON", getMetadataJSON());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long oAuthClientASMetadataId = (Long)attributes.get(
			"oAuthClientASMetadataId");

		if (oAuthClientASMetadataId != null) {
			setOAuthClientASMetadataId(oAuthClientASMetadataId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		Date modifiedDate = (Date)attributes.get("modifiedDate");

		if (modifiedDate != null) {
			setModifiedDate(modifiedDate);
		}

		String discoveryEndpoint = (String)attributes.get("discoveryEndpoint");

		if (discoveryEndpoint != null) {
			setDiscoveryEndpoint(discoveryEndpoint);
		}

		String issuer = (String)attributes.get("issuer");

		if (issuer != null) {
			setIssuer(issuer);
		}

		String metadataJSON = (String)attributes.get("metadataJSON");

		if (metadataJSON != null) {
			setMetadataJSON(metadataJSON);
		}
	}

	@Override
	public OAuthClientASMetadata cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the company ID of this o auth client as metadata.
	 *
	 * @return the company ID of this o auth client as metadata
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this o auth client as metadata.
	 *
	 * @return the create date of this o auth client as metadata
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the discovery endpoint of this o auth client as metadata.
	 *
	 * @return the discovery endpoint of this o auth client as metadata
	 */
	@Override
	public String getDiscoveryEndpoint() {
		return model.getDiscoveryEndpoint();
	}

	/**
	 * Returns the issuer of this o auth client as metadata.
	 *
	 * @return the issuer of this o auth client as metadata
	 */
	@Override
	public String getIssuer() {
		return model.getIssuer();
	}

	/**
	 * Returns the metadata json of this o auth client as metadata.
	 *
	 * @return the metadata json of this o auth client as metadata
	 */
	@Override
	public String getMetadataJSON() {
		return model.getMetadataJSON();
	}

	/**
	 * Returns the modified date of this o auth client as metadata.
	 *
	 * @return the modified date of this o auth client as metadata
	 */
	@Override
	public Date getModifiedDate() {
		return model.getModifiedDate();
	}

	/**
	 * Returns the mvcc version of this o auth client as metadata.
	 *
	 * @return the mvcc version of this o auth client as metadata
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the o auth client as metadata ID of this o auth client as metadata.
	 *
	 * @return the o auth client as metadata ID of this o auth client as metadata
	 */
	@Override
	public long getOAuthClientASMetadataId() {
		return model.getOAuthClientASMetadataId();
	}

	/**
	 * Returns the primary key of this o auth client as metadata.
	 *
	 * @return the primary key of this o auth client as metadata
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the user ID of this o auth client as metadata.
	 *
	 * @return the user ID of this o auth client as metadata
	 */
	@Override
	public long getUserId() {
		return model.getUserId();
	}

	/**
	 * Returns the user uuid of this o auth client as metadata.
	 *
	 * @return the user uuid of this o auth client as metadata
	 */
	@Override
	public String getUserUuid() {
		return model.getUserUuid();
	}

	@Override
	public void persist() {
		model.persist();
	}

	/**
	 * Sets the company ID of this o auth client as metadata.
	 *
	 * @param companyId the company ID of this o auth client as metadata
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this o auth client as metadata.
	 *
	 * @param createDate the create date of this o auth client as metadata
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the discovery endpoint of this o auth client as metadata.
	 *
	 * @param discoveryEndpoint the discovery endpoint of this o auth client as metadata
	 */
	@Override
	public void setDiscoveryEndpoint(String discoveryEndpoint) {
		model.setDiscoveryEndpoint(discoveryEndpoint);
	}

	/**
	 * Sets the issuer of this o auth client as metadata.
	 *
	 * @param issuer the issuer of this o auth client as metadata
	 */
	@Override
	public void setIssuer(String issuer) {
		model.setIssuer(issuer);
	}

	/**
	 * Sets the metadata json of this o auth client as metadata.
	 *
	 * @param metadataJSON the metadata json of this o auth client as metadata
	 */
	@Override
	public void setMetadataJSON(String metadataJSON) {
		model.setMetadataJSON(metadataJSON);
	}

	/**
	 * Sets the modified date of this o auth client as metadata.
	 *
	 * @param modifiedDate the modified date of this o auth client as metadata
	 */
	@Override
	public void setModifiedDate(Date modifiedDate) {
		model.setModifiedDate(modifiedDate);
	}

	/**
	 * Sets the mvcc version of this o auth client as metadata.
	 *
	 * @param mvccVersion the mvcc version of this o auth client as metadata
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the o auth client as metadata ID of this o auth client as metadata.
	 *
	 * @param oAuthClientASMetadataId the o auth client as metadata ID of this o auth client as metadata
	 */
	@Override
	public void setOAuthClientASMetadataId(long oAuthClientASMetadataId) {
		model.setOAuthClientASMetadataId(oAuthClientASMetadataId);
	}

	/**
	 * Sets the primary key of this o auth client as metadata.
	 *
	 * @param primaryKey the primary key of this o auth client as metadata
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the user ID of this o auth client as metadata.
	 *
	 * @param userId the user ID of this o auth client as metadata
	 */
	@Override
	public void setUserId(long userId) {
		model.setUserId(userId);
	}

	/**
	 * Sets the user uuid of this o auth client as metadata.
	 *
	 * @param userUuid the user uuid of this o auth client as metadata
	 */
	@Override
	public void setUserUuid(String userUuid) {
		model.setUserUuid(userUuid);
	}

	@Override
	protected OAuthClientASMetadataWrapper wrap(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return new OAuthClientASMetadataWrapper(oAuthClientASMetadata);
	}

}