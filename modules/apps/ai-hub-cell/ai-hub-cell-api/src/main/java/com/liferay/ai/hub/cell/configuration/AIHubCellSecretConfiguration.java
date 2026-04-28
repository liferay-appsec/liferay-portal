/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Pedro Victor Silvestre
 */
@ExtendedObjectClassDefinition(
	category = "ai-hub", featureFlagKey = "LPD-62272", generateUI = false,
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.ai.hub.cell.configuration.AIHubCellSecretConfiguration"
)
public interface AIHubCellSecretConfiguration {

	@Meta.AD(
		deflt = "", name = "secret", required = false, type = Meta.Type.Password
	)
	public String secret();

}