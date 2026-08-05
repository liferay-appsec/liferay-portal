/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.security.fips.FIPSApplicationStateMachineUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.fips.rest.dto.v1_0.HealthVerification;
import com.liferay.portal.security.fips.rest.internal.audit.FIPSHealthCheckAuditor;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class HealthVerificationResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_fipsEnabled = PropsValues.FIPS_ENABLED;

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "FIPS_ENABLED", true);

		_permissionChecker = Mockito.mock(PermissionChecker.class);

		Mockito.when(
			_permissionChecker.isOmniadmin()
		).thenReturn(
			true
		);

		_responseBuilder = Mockito.mock(
			Response.ResponseBuilder.class, Mockito.RETURNS_SELF);

		Response response = Mockito.mock(Response.class);

		Mockito.when(
			response.getStatusInfo()
		).thenReturn(
			Response.Status.SERVICE_UNAVAILABLE
		);

		Mockito.when(
			_responseBuilder.build()
		).thenReturn(
			response
		);

		RuntimeDelegate runtimeDelegate = Mockito.mock(RuntimeDelegate.class);

		Mockito.when(
			runtimeDelegate.createResponseBuilder()
		).thenReturn(
			_responseBuilder
		);

		RuntimeDelegate.setInstance(runtimeDelegate);
	}

	@After
	public void tearDown() {
		RuntimeDelegate.setInstance(null);

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "FIPS_ENABLED", _fipsEnabled);
	}

	@Test
	public void testPostHealthVerification() throws Exception {
		HealthVerificationResourceImpl healthVerificationResourceImpl =
			new HealthVerificationResourceImpl();

		FIPSHealthCheckAuditor fipsHealthCheckAuditor = Mockito.mock(
			FIPSHealthCheckAuditor.class);

		ReflectionTestUtil.setFieldValue(
			healthVerificationResourceImpl, "_fipsHealthCheckAuditor",
			fipsHealthCheckAuditor);

		try (MockedStatic<PermissionThreadLocal>
				permissionThreadLocalMockedStatic = Mockito.mockStatic(
					PermissionThreadLocal.class);
			MockedStatic<FIPSApplicationStateMachineUtil>
				fipsApplicationStateMachineUtilMockedStatic =
					Mockito.mockStatic(FIPSApplicationStateMachineUtil.class)) {

			permissionThreadLocalMockedStatic.when(
				PermissionThreadLocal::getPermissionChecker
			).thenReturn(
				_permissionChecker
			);

			HealthVerification healthVerification =
				healthVerificationResourceImpl.postHealthVerification();

			Assert.assertEquals(
				HealthVerification.Status.HEALTHY,
				healthVerification.getStatus());
		}

		try (MockedStatic<PermissionThreadLocal>
				permissionThreadLocalMockedStatic = Mockito.mockStatic(
					PermissionThreadLocal.class);
			MockedStatic<FIPSApplicationStateMachineUtil>
				fipsApplicationStateMachineUtilMockedStatic =
					Mockito.mockStatic(FIPSApplicationStateMachineUtil.class)) {

			permissionThreadLocalMockedStatic.when(
				PermissionThreadLocal::getPermissionChecker
			).thenReturn(
				_permissionChecker
			);

			fipsApplicationStateMachineUtilMockedStatic.when(
				() -> FIPSApplicationStateMachineUtil.selfTest(Mockito.any())
			).thenThrow(
				new SecurityException("boom")
			);

			Assert.assertThrows(
				WebApplicationException.class,
				healthVerificationResourceImpl::postHealthVerification);

			Mockito.verify(
				_responseBuilder
			).status(
				(Response.StatusType)Response.Status.SERVICE_UNAVAILABLE
			);

			ArgumentCaptor<HealthVerification> argumentCaptor =
				ArgumentCaptor.forClass(HealthVerification.class);

			Mockito.verify(
				_responseBuilder
			).entity(
				argumentCaptor.capture()
			);

			HealthVerification healthVerification = argumentCaptor.getValue();

			Assert.assertEquals(
				HealthVerification.Status.FAILED,
				healthVerification.getStatus());

			Mockito.verify(
				fipsHealthCheckAuditor
			).audit(
				Mockito.any(Exception.class)
			);
		}

		Mockito.reset(fipsHealthCheckAuditor);

		try (MockedStatic<PermissionThreadLocal>
				permissionThreadLocalMockedStatic = Mockito.mockStatic(
					PermissionThreadLocal.class);
			MockedStatic<FIPSApplicationStateMachineUtil>
				fipsApplicationStateMachineUtilMockedStatic =
					Mockito.mockStatic(FIPSApplicationStateMachineUtil.class)) {

			permissionThreadLocalMockedStatic.when(
				PermissionThreadLocal::getPermissionChecker
			).thenReturn(
				_permissionChecker
			);

			fipsApplicationStateMachineUtilMockedStatic.when(
				() -> FIPSApplicationStateMachineUtil.selfTest(Mockito.any())
			).thenThrow(
				new IllegalStateException(
					"Unable to transition the FIPS application state")
			);

			Assert.assertThrows(
				WebApplicationException.class,
				healthVerificationResourceImpl::postHealthVerification);

			Mockito.verifyNoInteractions(fipsHealthCheckAuditor);
		}
	}

	private boolean _fipsEnabled;
	private PermissionChecker _permissionChecker;
	private Response.ResponseBuilder _responseBuilder;

}