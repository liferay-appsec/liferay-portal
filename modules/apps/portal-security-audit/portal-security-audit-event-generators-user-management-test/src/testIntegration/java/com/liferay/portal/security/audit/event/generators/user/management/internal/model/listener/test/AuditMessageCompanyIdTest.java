/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.user.management.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.ContactLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.OrganizationTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserGroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class AuditMessageCompanyIdTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_auditMessages = new ArrayList<>();

		Bundle bundle = FrameworkUtil.getBundle(
			AuditMessageCompanyIdTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("eventTypes", "*");

		_serviceRegistration = bundleContext.registerService(
			AuditMessageProcessor.class,
			auditMessage -> _auditMessages.add(auditMessage), properties);
	}

	@After
	public void tearDown() throws Exception {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	@Test
	public void testUpdateAddressAuditMessageCompanyId() throws Exception {
		_user = UserTestUtil.addUser();

		_company = CompanyTestUtil.addCompany();

		_address = _addressLocalService.addAddress(
			null, _user.getUserId(), User.class.getName(), _user.getUserId(), 0,
			0, 0, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			false, RandomTestUtil.randomString(), false,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), null, RandomTestUtil.randomString(),
			null, ServiceContextTestUtil.getServiceContext());

		_auditMessages.clear();

		_address.setStreet1(RandomTestUtil.randomString());

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_addressLocalService.updateAddress(_address);
		}

		AuditMessage auditMessage = _fetchAuditMessage(
			User.class.getName(), EventTypes.UPDATE);

		Assert.assertNotNull(auditMessage);
		Assert.assertEquals(_user.getCompanyId(), auditMessage.getCompanyId());
	}

	@Test
	public void testUpdateContactAuditMessageCompanyId() throws Exception {
		_user = UserTestUtil.addUser();

		_company = CompanyTestUtil.addCompany();

		Contact contact = _user.getContact();

		_auditMessages.clear();

		contact.setFirstName(RandomTestUtil.randomString());

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_contactLocalService.updateContact(contact);
		}

		AuditMessage auditMessage = _fetchAuditMessage(
			User.class.getName(), EventTypes.UPDATE);

		Assert.assertNotNull(auditMessage);
		Assert.assertEquals(_user.getCompanyId(), auditMessage.getCompanyId());
	}

	@Test
	public void testUpdateOrganizationAuditMessageCompanyId() throws Exception {
		_organization = OrganizationTestUtil.addOrganization();

		_company = CompanyTestUtil.addCompany();

		_auditMessages.clear();

		_organization.setName(RandomTestUtil.randomString());

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_organization = _organizationLocalService.updateOrganization(
				_organization);
		}

		AuditMessage auditMessage = _fetchAuditMessage(
			Organization.class.getName(), EventTypes.UPDATE);

		Assert.assertNotNull(auditMessage);
		Assert.assertEquals(
			_organization.getCompanyId(), auditMessage.getCompanyId());
	}

	@Test
	public void testUpdateUserAuditMessageCompanyId() throws Exception {
		_user = UserTestUtil.addUser();

		_company = CompanyTestUtil.addCompany();

		_auditMessages.clear();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_userLocalService.updateAgreedToTermsOfUse(_user.getUserId(), true);
		}

		AuditMessage auditMessage = _fetchAuditMessage(
			User.class.getName(), EventTypes.AGREED_TO_TERMS_OF_USE);

		Assert.assertNotNull(auditMessage);
		Assert.assertEquals(_user.getCompanyId(), auditMessage.getCompanyId());
	}

	@Test
	public void testUpdateUserGroupAuditMessageCompanyId() throws Exception {
		_userGroup = UserGroupTestUtil.addUserGroup();

		_company = CompanyTestUtil.addCompany();

		_auditMessages.clear();

		_userGroup.setName(RandomTestUtil.randomString());

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					_company.getCompanyId())) {

			_userGroup = _userGroupLocalService.updateUserGroup(_userGroup);
		}

		AuditMessage auditMessage = _fetchAuditMessage(
			UserGroup.class.getName(), EventTypes.UPDATE);

		Assert.assertNotNull(auditMessage);
		Assert.assertEquals(
			_userGroup.getCompanyId(), auditMessage.getCompanyId());
	}

	private AuditMessage _fetchAuditMessage(
		String className, String eventType) {

		for (AuditMessage auditMessage : _auditMessages) {
			if (eventType.equals(auditMessage.getEventType()) &&
				className.equals(auditMessage.getClassName())) {

				return auditMessage;
			}
		}

		return null;
	}

	@DeleteAfterTestRun
	private Address _address;

	@Inject
	private AddressLocalService _addressLocalService;

	private List<AuditMessage> _auditMessages;

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private ContactLocalService _contactLocalService;

	@DeleteAfterTestRun
	private Organization _organization;

	@Inject
	private OrganizationLocalService _organizationLocalService;

	private ServiceRegistration<AuditMessageProcessor> _serviceRegistration;

	@DeleteAfterTestRun
	private User _user;

	@DeleteAfterTestRun
	private UserGroup _userGroup;

	@Inject
	private UserGroupLocalService _userGroupLocalService;

	@Inject
	private UserLocalService _userLocalService;

}