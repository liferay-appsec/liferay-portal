/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.Authenticator;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.security.fips.test.util.FIPSTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.SynchronousMailTestRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jorge García Jiménez
 */
@RunWith(Arquillian.class)
public class UserLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), SynchronousMailTestRule.INSTANCE);

	@Test
	public void testAuthenticateByEmailAddress() throws Exception {
		String password = RandomTestUtil.randomString(14);

		User legacyUser = _addUser(password, "MD5");
		User outdatedUser = _addUser(
			password, "PBKDF2WITHHMACSHA1/160/2600000");

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setPathMain(_portal.getPathMain());
		serviceContext.setPortalURL(RandomTestUtil.randomString());

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try (SafeCloseable safeCloseable =
				FIPSTestUtil.enableFIPSModeWithSafeCloseable()) {

			Assert.assertEquals(
				Authenticator.FAILURE,
				_userLocalService.authenticateByEmailAddress(
					legacyUser.getCompanyId(),
					legacyUser.getDisplayEmailAddress(), password, null, null,
					null));
			Assert.assertEquals(
				Authenticator.SUCCESS,
				_userLocalService.authenticateByEmailAddress(
					outdatedUser.getCompanyId(),
					outdatedUser.getDisplayEmailAddress(), password, null, null,
					null));
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}

		List<JSONObject> jsonObjects = _getAuditLogJSONObjects(
			"password-reset-forced", legacyUser);

		Assert.assertEquals(jsonObjects.toString(), 1, jsonObjects.size());

		jsonObjects = _getAuditLogJSONObjects("password-rehash", outdatedUser);

		Assert.assertEquals(jsonObjects.toString(), 1, jsonObjects.size());

		JSONObject jsonObject = jsonObjects.get(0);

		JSONObject fieldsJSONObject = jsonObject.getJSONObject("fields");

		Assert.assertEquals(
			"PBKDF2WITHHMACSHA1/160/2600000",
			fieldsJSONObject.getString("from-algorithm"));
		Assert.assertEquals(
			PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM),
			fieldsJSONObject.getString("to-algorithm"));
	}

	@Test
	public void testAuthenticateForBasic() throws Exception {
		String password = RandomTestUtil.randomString(14);

		User legacyUser = _addUser(password, "MD5");
		User validUser = _addUser(
			password, PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM));

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setPathMain(_portal.getPathMain());
		serviceContext.setPortalURL(RandomTestUtil.randomString());

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try (SafeCloseable safeCloseable =
				FIPSTestUtil.enableFIPSModeWithSafeCloseable()) {

			Assert.assertEquals(
				0,
				_userLocalService.authenticateForBasic(
					legacyUser.getCompanyId(), CompanyConstants.AUTH_TYPE_EA,
					legacyUser.getDisplayEmailAddress(), password));
			Assert.assertEquals(
				validUser.getUserId(),
				_userLocalService.authenticateForBasic(
					validUser.getCompanyId(), CompanyConstants.AUTH_TYPE_EA,
					validUser.getDisplayEmailAddress(), password));
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}

		List<JSONObject> jsonObjects = _getAuditLogJSONObjects(
			"password-reset-forced", legacyUser);

		Assert.assertEquals(jsonObjects.toString(), 1, jsonObjects.size());
	}

	private User _addUser(String password, String passwordsEncryptionAlgorithm)
		throws Exception {

		User user = UserTestUtil.addUser();

		_users.add(user);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PasswordEncryptorUtil.class,
					"_PASSWORDS_ENCRYPTION_ALGORITHM",
					passwordsEncryptionAlgorithm)) {

			return _userLocalService.updatePassword(
				user.getUserId(), password, password, false, true);
		}
	}

	private List<JSONObject> _getAuditLogJSONObjects(
			String eventType, User user)
		throws Exception {

		String userId = String.valueOf(user.getUserId());

		return ListUtil.filter(
			FIPSTestUtil.getAuditLogJSONObjects(),
			jsonObject -> {
				if (!Objects.equals(
						jsonObject.getString("event-type"), eventType)) {

					return false;
				}

				JSONObject fieldsJSONObject = jsonObject.getJSONObject(
					"fields");

				return Objects.equals(
					fieldsJSONObject.getString("user-id"), userId);
			});
	}

	@Inject
	private Portal _portal;

	@Inject
	private UserLocalService _userLocalService;

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>();

}