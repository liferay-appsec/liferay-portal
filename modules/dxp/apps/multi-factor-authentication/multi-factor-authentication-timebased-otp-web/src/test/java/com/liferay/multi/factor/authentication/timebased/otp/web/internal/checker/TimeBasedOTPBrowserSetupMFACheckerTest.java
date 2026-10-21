/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.multi.factor.authentication.timebased.otp.web.internal.checker;

import com.liferay.multi.factor.authentication.timebased.otp.model.MFATimeBasedOTPEntry;
import com.liferay.multi.factor.authentication.timebased.otp.service.MFATimeBasedOTPEntryLocalService;
import com.liferay.multi.factor.authentication.timebased.otp.web.internal.configuration.MFATimeBasedOTPConfiguration;
import com.liferay.multi.factor.authentication.timebased.otp.web.internal.constants.MFATimeBasedOTPEventTypes;
import com.liferay.multi.factor.authentication.timebased.otp.web.internal.util.MFATimeBasedOTPUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRequestContext;
import com.liferay.portal.kernel.audit.AuditRequestContextThreadLocal;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Regisson Aguiar
 */
public class TimeBasedOTPBrowserSetupMFACheckerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		AuditRequestContext auditRequestContext =
			AuditRequestContextThreadLocal.getAuditRequestContext();

		auditRequestContext.setRealUserId(_REAL_USER_ID);

		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		Mockito.when(
			_portal.getOriginalServletRequest(Mockito.any())
		).thenAnswer(
			invocation -> invocation.getArgument(0)
		);

		Mockito.when(
			_portal.getUserName(_REAL_USER_ID, StringPool.BLANK)
		).thenReturn(
			_REAL_USER_NAME
		);

		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(_portal);

		_serviceRegistration = _bundleContext.registerService(
			AuditRouter.class, _auditRouter, null);

		ReflectionTestUtil.setFieldValue(
			_timeBasedOTPBrowserSetupMFAChecker,
			"_mfaTimeBasedOTPConfiguration",
			Mockito.mock(MFATimeBasedOTPConfiguration.class));
		ReflectionTestUtil.setFieldValue(
			_timeBasedOTPBrowserSetupMFAChecker,
			"_mfaTimeBasedOTPEntryLocalService",
			_mfaTimeBasedOTPEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_timeBasedOTPBrowserSetupMFAChecker, "_portal", _portal);
	}

	@After
	public void tearDown() {
		AuditRequestContextThreadLocal.removeAuditRequestContext();

		PrincipalThreadLocal.setName(null);

		_serviceRegistration.unregister();
	}

	@Test
	public void testRemoveExistingSetup() throws Exception {
		PrincipalThreadLocal.setName(_REAL_USER_ID);

		long selectedUserId = RandomTestUtil.randomLong();

		_timeBasedOTPBrowserSetupMFAChecker.removeExistingSetup(selectedUserId);

		Mockito.verify(
			_auditRouter, Mockito.never()
		).route(
			Mockito.any()
		);

		Mockito.when(
			_mfaTimeBasedOTPEntryLocalService.fetchMFATimeBasedOTPEntryByUserId(
				selectedUserId)
		).thenReturn(
			Mockito.mock(MFATimeBasedOTPEntry.class)
		);

		_timeBasedOTPBrowserSetupMFAChecker.removeExistingSetup(selectedUserId);

		_assertAuditMessage(
			null, MFATimeBasedOTPEventTypes.MFA_TIMEBASED_OTP_SETUP_REMOVED,
			selectedUserId);
	}

	@Test
	public void testSetUp() throws Exception {
		long selectedUserId = RandomTestUtil.randomLong();

		PrincipalThreadLocal.setName(selectedUserId);

		try (MockedStatic<MFATimeBasedOTPUtil> mfaTimeBasedOTPUtilMockedStatic =
				Mockito.mockStatic(MFATimeBasedOTPUtil.class)) {

			Assert.assertFalse(
				_timeBasedOTPBrowserSetupMFAChecker.setUp(
					new MockHttpServletRequest(), selectedUserId));

			Mockito.verify(
				_auditRouter, Mockito.never()
			).route(
				Mockito.any()
			);

			mfaTimeBasedOTPUtilMockedStatic.when(
				() -> MFATimeBasedOTPUtil.verifyTimeBasedOTP(
					Mockito.anyLong(), Mockito.any(), Mockito.any())
			).thenReturn(
				true
			);

			Mockito.when(
				_mfaTimeBasedOTPEntryLocalService.addTimeBasedOTPEntry(
					Mockito.eq(selectedUserId), Mockito.any())
			).thenReturn(
				Mockito.mock(MFATimeBasedOTPEntry.class)
			);

			Assert.assertTrue(
				_timeBasedOTPBrowserSetupMFAChecker.setUp(
					new MockHttpServletRequest(), selectedUserId));
		}

		_assertAuditMessage(
			String.valueOf(selectedUserId),
			MFATimeBasedOTPEventTypes.MFA_TIMEBASED_OTP_SETUP_ADDED,
			selectedUserId);
	}

	private void _assertAuditMessage(
			String doAsUserId, String eventType, long selectedUserId)
		throws Exception {

		ArgumentCaptor<AuditMessage> argumentCaptor = ArgumentCaptor.forClass(
			AuditMessage.class);

		Mockito.verify(
			_auditRouter
		).route(
			argumentCaptor.capture()
		);

		AuditMessage auditMessage = argumentCaptor.getValue();

		JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			doAsUserId, additionalInfoJSONObject.getString("doAsUserId", null));

		Assert.assertEquals(User.class.getName(), auditMessage.getClassName());
		Assert.assertEquals(
			String.valueOf(selectedUserId), auditMessage.getClassPK());
		Assert.assertEquals(eventType, auditMessage.getEventType());
		Assert.assertEquals(_REAL_USER_ID, auditMessage.getUserId());
		Assert.assertEquals(_REAL_USER_NAME, auditMessage.getUserName());
	}

	private static final long _REAL_USER_ID = RandomTestUtil.randomLong();

	private static final String _REAL_USER_NAME = RandomTestUtil.randomString();

	private static final BundleContext _bundleContext =
		SystemBundleUtil.getBundleContext();

	private final AuditRouter _auditRouter = Mockito.mock(AuditRouter.class);
	private final MFATimeBasedOTPEntryLocalService
		_mfaTimeBasedOTPEntryLocalService = Mockito.mock(
			MFATimeBasedOTPEntryLocalService.class);
	private final Portal _portal = Mockito.mock(Portal.class);
	private ServiceRegistration<AuditRouter> _serviceRegistration;
	private final TimeBasedOTPBrowserSetupMFAChecker
		_timeBasedOTPBrowserSetupMFAChecker =
			new TimeBasedOTPBrowserSetupMFAChecker();

}