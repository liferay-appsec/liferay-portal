/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.security.fips.FIPSApplicationState;
import com.liferay.portal.kernel.security.fips.FIPSApplicationStateMachineUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.fips.rest.dto.v1_0.FIPSHealthVerification;
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
public class FIPSHealthVerificationResourceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_fipsEnabled = PropsValues.FIPS_ENABLED;

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "FIPS_ENABLED", true);

		Response response = Mockito.mock(Response.class);

		Mockito.when(
			response.getStatusInfo()
		).thenReturn(
			Response.Status.SERVICE_UNAVAILABLE
		);

		_responseBuilder = Mockito.mock(
			Response.ResponseBuilder.class, Mockito.RETURNS_SELF);

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
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "FIPS_ENABLED", _fipsEnabled);

		RuntimeDelegate.setInstance(null);
	}

	@Test
	public void testPostFIPSHealthVerification() throws Exception {
		FIPSHealthVerificationResourceImpl fipsHealthVerificationResourceImpl =
			new FIPSHealthVerificationResourceImpl();

		try (MockedStatic<FIPSApplicationStateMachineUtil>
				fipsApplicationStateMachineUtilMockedStatic =
					Mockito.mockStatic(FIPSApplicationStateMachineUtil.class)) {

			fipsApplicationStateMachineUtilMockedStatic.when(
				() -> FIPSApplicationStateMachineUtil.selfTest(Mockito.any())
			).thenThrow(
				new SecurityException()
			);

			fipsApplicationStateMachineUtilMockedStatic.when(
				FIPSApplicationStateMachineUtil::getFIPSApplicationState
			).thenReturn(
				FIPSApplicationState.ERROR
			);

			Assert.assertThrows(
				WebApplicationException.class,
				fipsHealthVerificationResourceImpl::postFIPSHealthVerification);

			Mockito.verify(
				_responseBuilder
			).status(
				(Response.StatusType)Response.Status.SERVICE_UNAVAILABLE
			);

			ArgumentCaptor<FIPSHealthVerification> errorArgumentCaptor =
				ArgumentCaptor.forClass(FIPSHealthVerification.class);

			Mockito.verify(
				_responseBuilder
			).entity(
				errorArgumentCaptor.capture()
			);

			FIPSHealthVerification errorFIPSHealthVerification =
				errorArgumentCaptor.getValue();

			Assert.assertEquals(
				FIPSHealthVerification.Status.ERROR,
				errorFIPSHealthVerification.getStatus());

			Mockito.clearInvocations(_responseBuilder);

			fipsApplicationStateMachineUtilMockedStatic.when(
				() -> FIPSApplicationStateMachineUtil.selfTest(Mockito.any())
			).thenThrow(
				new IllegalStateException(
					"Unable to transition the FIPS application state")
			);

			fipsApplicationStateMachineUtilMockedStatic.when(
				FIPSApplicationStateMachineUtil::getFIPSApplicationState
			).thenReturn(
				FIPSApplicationState.POWER_OFF
			);

			Assert.assertThrows(
				WebApplicationException.class,
				fipsHealthVerificationResourceImpl::postFIPSHealthVerification);

			Mockito.verify(
				_responseBuilder
			).status(
				(Response.StatusType)Response.Status.SERVICE_UNAVAILABLE
			);

			ArgumentCaptor<FIPSHealthVerification> powerOffArgumentCaptor =
				ArgumentCaptor.forClass(FIPSHealthVerification.class);

			Mockito.verify(
				_responseBuilder
			).entity(
				powerOffArgumentCaptor.capture()
			);

			FIPSHealthVerification powerOffFIPSHealthVerification =
				powerOffArgumentCaptor.getValue();

			Assert.assertEquals(
				FIPSHealthVerification.Status.POWER_OFF,
				powerOffFIPSHealthVerification.getStatus());

			fipsApplicationStateMachineUtilMockedStatic.when(
				() -> FIPSApplicationStateMachineUtil.selfTest(Mockito.any())
			).thenAnswer(
				invocation -> null
			);

			fipsApplicationStateMachineUtilMockedStatic.when(
				FIPSApplicationStateMachineUtil::getFIPSApplicationState
			).thenReturn(
				FIPSApplicationState.OPERATIONAL
			);

			FIPSHealthVerification operationalFIPSHealthVerification =
				fipsHealthVerificationResourceImpl.postFIPSHealthVerification();

			Assert.assertEquals(
				FIPSHealthVerification.Status.OPERATIONAL,
				operationalFIPSHealthVerification.getStatus());
		}
	}

	private boolean _fipsEnabled;
	private Response.ResponseBuilder _responseBuilder;

}