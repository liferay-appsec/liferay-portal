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

package com.liferay.saas.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Marta Medio
 */
@ExtendedObjectClassDefinition(
	generateUI = false, scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(id = "com.liferay.saas.configuration.SaasConfiguration")
public interface SaasConfiguration {

	@Meta.AD(
		id = "saas.production.environment", name = "saas-production-environment"
	)
	public boolean isProductionEnvironment();

	@Meta.AD(id = "saas.preshared.key", name = "saas-pre-shared-key")
	public String preSharedKey();

	@Meta.AD(
		id = "saas.virtual.host.url.export",
		name = "saas-virtual-host-url-export", required = false
	)
	public String virtualHostURLExport();

}