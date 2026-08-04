/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.security.fips.rest.client.resource.v1_0.FIPSHealthVerificationResource;

import java.net.HttpURLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lucas Miranda
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class FIPSHealthVerificationResourceTest
	extends BaseFIPSHealthVerificationResourceTestCase {

	@Override
	@Test
	public void testPostFIPSHealthVerification() throws Exception {
		FIPSHealthVerificationResource fipsHealthVerificationResource =
			FIPSHealthVerificationResource.builder(
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		assertHttpResponseStatusCode(
			HttpURLConnection.HTTP_FORBIDDEN,
			fipsHealthVerificationResource.
				postFIPSHealthVerificationHttpResponse());
	}

}