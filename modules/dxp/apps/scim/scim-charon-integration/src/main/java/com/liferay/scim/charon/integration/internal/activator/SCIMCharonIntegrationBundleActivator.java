/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.charon.integration.internal.activator;

import com.liferay.portal.kernel.util.FileUtil;

import java.io.File;

import java.util.Collections;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.wso2.charon3.core.config.SCIMUserSchemaExtensionBuilder;
import org.wso2.charon3.core.protocol.endpoints.AbstractResourceManager;
import org.wso2.charon3.core.schema.SCIMConstants;

/**
 * @author Rafael Praxedes
 */
public class SCIMCharonIntegrationBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		_registerLiferayUserSchemaExtension();

		AbstractResourceManager.setEndpointURLMap(
			Collections.singletonMap(
				SCIMConstants.USER_ENDPOINT, "/o/scim/Users"));
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

	private void _registerLiferayUserSchemaExtension() throws Exception {
		File file = FileUtil.createTempFile(
			SCIMCharonIntegrationBundleActivator.class.getResourceAsStream(
				"dependencies/liferay-user-schema-extension.json"));

		SCIMUserSchemaExtensionBuilder scimUserSchemaExtensionBuilder =
			SCIMUserSchemaExtensionBuilder.getInstance();

		scimUserSchemaExtensionBuilder.buildUserSchemaExtension(file.getPath());
	}

}