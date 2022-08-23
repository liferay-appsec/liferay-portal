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

package com.liferay.portal.security.ldap.persistence.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link LDAPServerAttributeRel}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRel
 * @generated
 */
public class LDAPServerAttributeRelWrapper
	extends BaseModelWrapper<LDAPServerAttributeRel>
	implements LDAPServerAttributeRel, ModelWrapper<LDAPServerAttributeRel> {

	public LDAPServerAttributeRelWrapper(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		super(ldapServerAttributeRel);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put(
			"ldapServerAttributeRelId", getLdapServerAttributeRelId());
		attributes.put("companyId", getCompanyId());
		attributes.put("ldapServerId", getLdapServerId());
		attributes.put("classNameId", getClassNameId());
		attributes.put("classPK", getClassPK());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long ldapServerAttributeRelId = (Long)attributes.get(
			"ldapServerAttributeRelId");

		if (ldapServerAttributeRelId != null) {
			setLdapServerAttributeRelId(ldapServerAttributeRelId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long ldapServerId = (Long)attributes.get("ldapServerId");

		if (ldapServerId != null) {
			setLdapServerId(ldapServerId);
		}

		Long classNameId = (Long)attributes.get("classNameId");

		if (classNameId != null) {
			setClassNameId(classNameId);
		}

		Long classPK = (Long)attributes.get("classPK");

		if (classPK != null) {
			setClassPK(classPK);
		}
	}

	@Override
	public LDAPServerAttributeRel cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the fully qualified class name of this ldap server attribute rel.
	 *
	 * @return the fully qualified class name of this ldap server attribute rel
	 */
	@Override
	public String getClassName() {
		return model.getClassName();
	}

	/**
	 * Returns the class name ID of this ldap server attribute rel.
	 *
	 * @return the class name ID of this ldap server attribute rel
	 */
	@Override
	public long getClassNameId() {
		return model.getClassNameId();
	}

	/**
	 * Returns the class pk of this ldap server attribute rel.
	 *
	 * @return the class pk of this ldap server attribute rel
	 */
	@Override
	public long getClassPK() {
		return model.getClassPK();
	}

	/**
	 * Returns the company ID of this ldap server attribute rel.
	 *
	 * @return the company ID of this ldap server attribute rel
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the ldap server attribute rel ID of this ldap server attribute rel.
	 *
	 * @return the ldap server attribute rel ID of this ldap server attribute rel
	 */
	@Override
	public long getLdapServerAttributeRelId() {
		return model.getLdapServerAttributeRelId();
	}

	/**
	 * Returns the ldap server ID of this ldap server attribute rel.
	 *
	 * @return the ldap server ID of this ldap server attribute rel
	 */
	@Override
	public long getLdapServerId() {
		return model.getLdapServerId();
	}

	/**
	 * Returns the mvcc version of this ldap server attribute rel.
	 *
	 * @return the mvcc version of this ldap server attribute rel
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the primary key of this ldap server attribute rel.
	 *
	 * @return the primary key of this ldap server attribute rel
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	@Override
	public void persist() {
		model.persist();
	}

	@Override
	public void setClassName(String className) {
		model.setClassName(className);
	}

	/**
	 * Sets the class name ID of this ldap server attribute rel.
	 *
	 * @param classNameId the class name ID of this ldap server attribute rel
	 */
	@Override
	public void setClassNameId(long classNameId) {
		model.setClassNameId(classNameId);
	}

	/**
	 * Sets the class pk of this ldap server attribute rel.
	 *
	 * @param classPK the class pk of this ldap server attribute rel
	 */
	@Override
	public void setClassPK(long classPK) {
		model.setClassPK(classPK);
	}

	/**
	 * Sets the company ID of this ldap server attribute rel.
	 *
	 * @param companyId the company ID of this ldap server attribute rel
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the ldap server attribute rel ID of this ldap server attribute rel.
	 *
	 * @param ldapServerAttributeRelId the ldap server attribute rel ID of this ldap server attribute rel
	 */
	@Override
	public void setLdapServerAttributeRelId(long ldapServerAttributeRelId) {
		model.setLdapServerAttributeRelId(ldapServerAttributeRelId);
	}

	/**
	 * Sets the ldap server ID of this ldap server attribute rel.
	 *
	 * @param ldapServerId the ldap server ID of this ldap server attribute rel
	 */
	@Override
	public void setLdapServerId(long ldapServerId) {
		model.setLdapServerId(ldapServerId);
	}

	/**
	 * Sets the mvcc version of this ldap server attribute rel.
	 *
	 * @param mvccVersion the mvcc version of this ldap server attribute rel
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the primary key of this ldap server attribute rel.
	 *
	 * @param primaryKey the primary key of this ldap server attribute rel
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	@Override
	protected LDAPServerAttributeRelWrapper wrap(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return new LDAPServerAttributeRelWrapper(ldapServerAttributeRel);
	}

}