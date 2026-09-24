/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link AuditPseudonym}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonym
 * @generated
 */
public class AuditPseudonymWrapper
	extends BaseModelWrapper<AuditPseudonym>
	implements AuditPseudonym, ModelWrapper<AuditPseudonym> {

	public AuditPseudonymWrapper(AuditPseudonym auditPseudonym) {
		super(auditPseudonym);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("auditPseudonymId", getAuditPseudonymId());
		attributes.put("companyId", getCompanyId());
		attributes.put("createDate", getCreateDate());
		attributes.put("contextName", getContextName());
		attributes.put("fieldCategory", getFieldCategory());
		attributes.put("value", getValue());
		attributes.put("valueHash", getValueHash());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long auditPseudonymId = (Long)attributes.get("auditPseudonymId");

		if (auditPseudonymId != null) {
			setAuditPseudonymId(auditPseudonymId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		String contextName = (String)attributes.get("contextName");

		if (contextName != null) {
			setContextName(contextName);
		}

		String fieldCategory = (String)attributes.get("fieldCategory");

		if (fieldCategory != null) {
			setFieldCategory(fieldCategory);
		}

		String value = (String)attributes.get("value");

		if (value != null) {
			setValue(value);
		}

		String valueHash = (String)attributes.get("valueHash");

		if (valueHash != null) {
			setValueHash(valueHash);
		}
	}

	@Override
	public AuditPseudonym cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the audit pseudonym ID of this audit pseudonym.
	 *
	 * @return the audit pseudonym ID of this audit pseudonym
	 */
	@Override
	public long getAuditPseudonymId() {
		return model.getAuditPseudonymId();
	}

	/**
	 * Returns the company ID of this audit pseudonym.
	 *
	 * @return the company ID of this audit pseudonym
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the context name of this audit pseudonym.
	 *
	 * @return the context name of this audit pseudonym
	 */
	@Override
	public String getContextName() {
		return model.getContextName();
	}

	/**
	 * Returns the create date of this audit pseudonym.
	 *
	 * @return the create date of this audit pseudonym
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the field category of this audit pseudonym.
	 *
	 * @return the field category of this audit pseudonym
	 */
	@Override
	public String getFieldCategory() {
		return model.getFieldCategory();
	}

	/**
	 * Returns the primary key of this audit pseudonym.
	 *
	 * @return the primary key of this audit pseudonym
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the value of this audit pseudonym.
	 *
	 * @return the value of this audit pseudonym
	 */
	@Override
	public String getValue() {
		return model.getValue();
	}

	/**
	 * Returns the value hash of this audit pseudonym.
	 *
	 * @return the value hash of this audit pseudonym
	 */
	@Override
	public String getValueHash() {
		return model.getValueHash();
	}

	@Override
	public void persist() {
		model.persist();
	}

	/**
	 * Sets the audit pseudonym ID of this audit pseudonym.
	 *
	 * @param auditPseudonymId the audit pseudonym ID of this audit pseudonym
	 */
	@Override
	public void setAuditPseudonymId(long auditPseudonymId) {
		model.setAuditPseudonymId(auditPseudonymId);
	}

	/**
	 * Sets the company ID of this audit pseudonym.
	 *
	 * @param companyId the company ID of this audit pseudonym
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the context name of this audit pseudonym.
	 *
	 * @param contextName the context name of this audit pseudonym
	 */
	@Override
	public void setContextName(String contextName) {
		model.setContextName(contextName);
	}

	/**
	 * Sets the create date of this audit pseudonym.
	 *
	 * @param createDate the create date of this audit pseudonym
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the field category of this audit pseudonym.
	 *
	 * @param fieldCategory the field category of this audit pseudonym
	 */
	@Override
	public void setFieldCategory(String fieldCategory) {
		model.setFieldCategory(fieldCategory);
	}

	/**
	 * Sets the primary key of this audit pseudonym.
	 *
	 * @param primaryKey the primary key of this audit pseudonym
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the value of this audit pseudonym.
	 *
	 * @param value the value of this audit pseudonym
	 */
	@Override
	public void setValue(String value) {
		model.setValue(value);
	}

	/**
	 * Sets the value hash of this audit pseudonym.
	 *
	 * @param valueHash the value hash of this audit pseudonym
	 */
	@Override
	public void setValueHash(String valueHash) {
		model.setValueHash(valueHash);
	}

	@Override
	public String toXmlString() {
		return model.toXmlString();
	}

	@Override
	protected AuditPseudonymWrapper wrap(AuditPseudonym auditPseudonym) {
		return new AuditPseudonymWrapper(auditPseudonym);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:423282585