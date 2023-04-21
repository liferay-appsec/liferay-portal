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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.security.sso.openid.connect.internal.OIDCUserInfoProcessor;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Álvaro Saugar
 * @author Olivér Kecskeméty
 */
public class OIDCUserInfoProcessorAddRoleToUserLoginTest2 {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		_companyId = company.getCompanyId();

		_defaultIssuer = TestPropsUtil.get(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY);

		_defaultRegularRole = TestPropsUtil.get(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY);

		_userInfoMapperJSON = _generateUserInfoMapperJSON();
	}

	@AfterClass
	public static void tearDownClass() throws PortalException {
		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY,
			_defaultIssuer);

		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY,
			_defaultRegularRole);
	}

	@Test
	public void testBothPropertiesDefinedAndCorrectRole() throws Exception {
		String roleName = "roleWithRegularTypeAlsoDefinedInRoleProp";

		String issuer = "issuerAlsoDefinedInIssuerProp";

		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY, issuer);

		TestPropsUtil.set(
			_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY,
			roleName);

		Role role = _createRole(roleName, RoleConstants.TYPE_REGULAR);

		User user = UserLocalServiceUtil.getUser(
			_oidcUserInfoProcessor.processUserInfo(
				_companyId, issuer, null, _generateUserInfoJSON(),
				_userInfoMapperJSON));

		Assert.assertTrue(
			ArrayUtil.contains(user.getRoleIds(), role.getRoleId()));
	}

	private static String _generateUserInfoMapperJSON() {
		return JSONUtil.put(
			"address", JSONUtil.put("street", "street")
		).put(
			"contact", JSONUtil.put("birthdate", "birthdate")
		).put(
			"user",
			JSONUtil.put(
				"emailAddress", "email"
			).put(
				"firstName", "given_name"
			).put(
				"jobTitle", "jobTitle"
			).put(
				"lastName", "family_name"
			).put(
				"middleName", "middle_name"
			).put(
				"screenName", "screen_name"
			)
		).toString();
	}

	private Role _createRole(String roleName, int roleType) throws Exception {
		Role role = RoleTestUtil.addRole(roleName, roleType);
		role.setCompanyId(_companyId);

		return role;
	}

	private String _generateUserInfoJSON() {
		return JSONUtil.put(
			"email",
			StringBundler.concat(
				RandomTestUtil.randomString(), RandomTestUtil.nextLong(), "@",
				RandomTestUtil.randomString(), ".com")
		).put(
			"family_name", RandomTestUtil.randomString()
		).put(
			"given_name", RandomTestUtil.randomString()
		).put(
			"jobTitle", RandomTestUtil.randomString()
		).put(
			"middle_name", RandomTestUtil.randomString()
		).put(
			"screen_name", RandomTestUtil.randomString()
		).toString();
	}

	private static final String
		_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_ISSUER_PROP_KEY =
		"open.id.connect.user.info.processor.impl.issuer";

	private static final String
		_OPEN_ID_CONNECT_USER_INFO_PROCESSOR_IMPL_REGULAR_ROLE_PROP_KEY =
		"open.id.connect.user.info.processor.impl.regular.role";

	private static long _companyId;

	private static String _defaultIssuer;
	private static String _defaultRegularRole;

	@Inject
	private static OIDCUserInfoProcessor _oidcUserInfoProcessor;

	private static String _userInfoMapperJSON;

}