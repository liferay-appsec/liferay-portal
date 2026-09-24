/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.persistence.impl;

import com.liferay.portal.kernel.dao.orm.ArgumentsResolver;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.security.audit.storage.model.AuditPseudonymTable;
import com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymImpl;
import com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymModelImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

/**
 * The arguments resolver class for retrieving value from AuditPseudonym.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(
	property = {
		"class.name=com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymImpl",
		"table.name=Audit_AuditPseudonym"
	},
	service = ArgumentsResolver.class
)
public class AuditPseudonymModelArgumentsResolver implements ArgumentsResolver {

	@Override
	public Object[] getArguments(
		FinderPath finderPath, BaseModel<?> baseModel, boolean checkColumn,
		boolean original) {

		String[] columnNames = finderPath.getColumnNames();

		if ((columnNames == null) || (columnNames.length == 0)) {
			if (baseModel.isNew()) {
				return new Object[0];
			}

			return null;
		}

		AuditPseudonymModelImpl auditPseudonymModelImpl =
			(AuditPseudonymModelImpl)baseModel;

		long columnBitmask = auditPseudonymModelImpl.getColumnBitmask();

		if (!checkColumn || (columnBitmask == 0)) {
			return _getValue(auditPseudonymModelImpl, finderPath, original);
		}

		Long finderPathColumnBitmask = _finderPathColumnBitmasksCache.get(
			finderPath);

		if (finderPathColumnBitmask == null) {
			finderPathColumnBitmask = 0L;

			for (String columnName : columnNames) {
				finderPathColumnBitmask |=
					auditPseudonymModelImpl.getColumnBitmask(columnName);
			}

			_finderPathColumnBitmasksCache.put(
				finderPath, finderPathColumnBitmask);
		}

		if ((columnBitmask & finderPathColumnBitmask) != 0) {
			return _getValue(auditPseudonymModelImpl, finderPath, original);
		}

		return null;
	}

	@Override
	public String getClassName() {
		return AuditPseudonymImpl.class.getName();
	}

	@Override
	public String getTableName() {
		return AuditPseudonymTable.INSTANCE.getTableName();
	}

	private static Object[] _getValue(
		AuditPseudonymModelImpl auditPseudonymModelImpl, FinderPath finderPath,
		boolean original) {

		String[] columnNames = finderPath.getColumnNames();

		Object[] arguments = new Object[columnNames.length];

		for (int i = 0; i < arguments.length; i++) {
			String columnName = columnNames[i];

			Object value;

			if (original) {
				value = auditPseudonymModelImpl.getColumnOriginalValue(
					columnName);
			}
			else {
				value = auditPseudonymModelImpl.getColumnValue(columnName);
			}

			arguments[i] = finderPath.normalizeArgument(i, value);
		}

		return arguments;
	}

	private static final Map<FinderPath, Long> _finderPathColumnBitmasksCache =
		new ConcurrentHashMap<>();

}
// LIFERAY-SERVICE-BUILDER-HASH:839441918