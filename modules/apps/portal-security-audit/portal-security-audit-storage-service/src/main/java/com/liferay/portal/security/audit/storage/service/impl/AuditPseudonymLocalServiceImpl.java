/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.storage.exception.AuditPseudonymValueException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.base.AuditPseudonymLocalServiceBaseImpl;

import java.util.Date;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.audit.storage.model.AuditPseudonym",
	service = AopService.class
)
public class AuditPseudonymLocalServiceImpl
	extends AuditPseudonymLocalServiceBaseImpl {

	@Override
	public AuditPseudonym addAuditPseudonym(
			long companyId, String contextName, String fieldCategory,
			String value)
		throws PortalException {

		if (Validator.isBlank(value)) {
			throw new AuditPseudonymValueException("Value is blank");
		}

		int maxLength = ModelHintsUtil.getMaxLength(
			AuditPseudonym.class.getName(), "value");

		if (value.length() > maxLength) {
			throw new AuditPseudonymValueException(
				"Maximum length of value exceeded");
		}

		if (Validator.isBlank(contextName)) {
			contextName = "INSTANCE";
		}

		String valueHash = DigesterUtil.digestHex(DigesterUtil.SHA_256, value);

		AuditPseudonym auditPseudonym =
			auditPseudonymPersistence.fetchByC_CN_FC_VH(
				companyId, contextName, fieldCategory, valueHash);

		if (auditPseudonym != null) {
			return auditPseudonym;
		}

		long auditPseudonymId = counterLocalService.increment();

		auditPseudonym = auditPseudonymPersistence.create(auditPseudonymId);

		auditPseudonym.setCompanyId(companyId);
		auditPseudonym.setCreateDate(new Date());
		auditPseudonym.setContextName(contextName);
		auditPseudonym.setFieldCategory(fieldCategory);
		auditPseudonym.setValue(value);
		auditPseudonym.setValueHash(valueHash);

		return auditPseudonymPersistence.update(auditPseudonym);
	}

}