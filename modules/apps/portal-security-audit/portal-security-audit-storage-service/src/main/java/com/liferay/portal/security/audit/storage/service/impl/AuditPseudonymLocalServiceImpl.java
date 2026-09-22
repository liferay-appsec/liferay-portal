/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.impl;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.base.AuditPseudonymLocalServiceBaseImpl;

import java.util.Date;

import org.osgi.service.component.annotations.Component;

/**
 * @author Christian Moura
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.audit.storage.model.AuditPseudonym",
	service = AopService.class
)
public class AuditPseudonymLocalServiceImpl
	extends AuditPseudonymLocalServiceBaseImpl {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AuditPseudonym addAuditPseudonym(
		long companyId, String contextName, String fieldCategory,
		String value) {

		long auditPseudonymId = counterLocalService.increment();

		AuditPseudonym auditPseudonym = auditPseudonymPersistence.create(
			auditPseudonymId);

		auditPseudonym.setCompanyId(companyId);
		auditPseudonym.setCreateDate(new Date());
		auditPseudonym.setContextName(contextName);
		auditPseudonym.setFieldCategory(fieldCategory);
		auditPseudonym.setValue(value);

		return auditPseudonymPersistence.update(auditPseudonym);
	}

	public AuditPseudonym getOrAddAuditPseudonym(
		long companyId, String contextName, String fieldCategory,
		String value) {

		contextName = GetterUtil.getString(contextName);

		AuditPseudonym auditPseudonym =
			auditPseudonymPersistence.fetchByC_C_FC_V(
				companyId, contextName, fieldCategory, value);

		if (auditPseudonym != null) {
			return auditPseudonym;
		}

		try {
			return auditPseudonymLocalService.addAuditPseudonym(
				companyId, contextName, fieldCategory, value);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			auditPseudonym = auditPseudonymPersistence.fetchByC_C_FC_V(
				companyId, contextName, fieldCategory, value, false);

			if (auditPseudonym == null) {
				return ReflectionUtil.throwException(exception);
			}

			return auditPseudonym;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AuditPseudonymLocalServiceImpl.class);

}