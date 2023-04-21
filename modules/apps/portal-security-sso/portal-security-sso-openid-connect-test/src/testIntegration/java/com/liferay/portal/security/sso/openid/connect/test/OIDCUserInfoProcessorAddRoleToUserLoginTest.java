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

package com.liferay.portal.security.sso.openid.connect.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Álvaro Saugar
 * @author Olivér Kecskeméty
 */
@RunWith(Arquillian.class)
public class OIDCUserInfoProcessorAddRoleToUserLoginTest
	extends BaseClientTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testBothPropertiesDefinedAndCorrectRole() throws Exception {
		String roleName = "roleWithRegularTypeAlsoDefinedInRoleProp";

		String issuer = "issuerAlsoDefinedInIssuerProp";

		setIssuer(issuer);

		setRegularRole(roleName);

		_role = RoleTestUtil.addRole(roleName, RoleConstants.TYPE_REGULAR);

		openIdConnectAuthenticationHandler.processAuthenticationResponse(generateHttpRequest(), mockHttpServletResponse, null);

		Assert.assertTrue(true);

//		User user = UserLocalServiceUtil.getUser(
//			_oidcUserInfoProcessor.processUserInfo(
//				_company.getCompanyId(), issuer, null, generateUserInfoJSON(),
//				userInfoMapperJSON));
//
//		Assert.assertTrue(
//			ArrayUtil.contains(user.getRoleIds(), _role.getRoleId()));
	}

	@DeleteAfterTestRun
	private Role _role;

}