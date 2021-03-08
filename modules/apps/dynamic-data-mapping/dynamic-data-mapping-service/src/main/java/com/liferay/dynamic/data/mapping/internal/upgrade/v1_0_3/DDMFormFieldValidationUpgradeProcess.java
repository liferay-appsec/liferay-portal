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

package com.liferay.dynamic.data.mapping.internal.upgrade.v1_0_3;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.PortalUtil;

/**
 * @author Lino Alves
 */
public class DDMFormFieldValidationUpgradeProcess
	extends com.liferay.dynamic.data.mapping.internal.upgrade.v2_0_5.
				DDMFormFieldValidationUpgradeProcess {

	public DDMFormFieldValidationUpgradeProcess(JSONFactory jsonFactory) {
		super(jsonFactory);
	}

	@Override
	protected long getClassNameId() {
		return PortalUtil.getClassNameId(
			"com.liferay.dynamic.data.lists.model.DDLRecordSet");
	}

}