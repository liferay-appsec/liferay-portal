/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the AuditPseudonym service. Represents a row in the &quot;Audit_AuditPseudonym&quot; database table, with each column mapped to a property of this class.
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonymModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymImpl"
)
@ProviderType
public interface AuditPseudonym extends AuditPseudonymModel, PersistedModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<AuditPseudonym, Long>
		AUDIT_PSEUDONYM_ID_ACCESSOR = new Accessor<AuditPseudonym, Long>() {

			@Override
			public Long get(AuditPseudonym auditPseudonym) {
				return auditPseudonym.getAuditPseudonymId();
			}

			@Override
			public Class<Long> getAttributeClass() {
				return Long.class;
			}

			@Override
			public Class<AuditPseudonym> getTypeClass() {
				return AuditPseudonym.class;
			}

		};

}
// LIFERAY-SERVICE-BUILDER-HASH:-459936044