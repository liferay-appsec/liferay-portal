/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.fips.FIPSApplicationStateMachineUtil;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.fips.rest.dto.v1_0.HealthVerification;
import com.liferay.portal.security.fips.rest.internal.audit.FIPSHealthCheckAuditor;
import com.liferay.portal.security.fips.rest.resource.v1_0.HealthVerificationResource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Lucas Miranda
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/health-verification.properties",
	scope = ServiceScope.PROTOTYPE, service = HealthVerificationResource.class
)
public class HealthVerificationResourceImpl
	extends BaseHealthVerificationResourceImpl {

	@Override
	public HealthVerification postHealthVerification() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isOmniadmin() &&
			!permissionChecker.isCompanyAdmin()) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, "TRIGGER_HEALTH_VERIFICATION");
		}

		HealthVerification healthVerification = new HealthVerification();

		healthVerification.setDate(() -> new Date());

		if (!PropsValues.FIPS_ENABLED) {
			healthVerification.setStatus(
				() -> HealthVerification.Status.NOT_APPLICABLE);

			throw new WebApplicationException(
				Response.status(
					Response.Status.CONFLICT
				).entity(
					healthVerification
				).build());
		}

		try {
			FIPSApplicationStateMachineUtil.selfTest(
				FIPSModeValidator::validate);
		}
		catch (Exception exception) {

			// A rejected state transition means the FIPS application was
			// already non-operational, not that a self-test failed, so it
			// emits no audit event.

			if (!(exception instanceof IllegalStateException)) {
				_fipsHealthCheckAuditor.audit(exception);
			}

			healthVerification.setProviderMessage(exception::getMessage);
			healthVerification.setStatus(
				() -> HealthVerification.Status.FAILED);

			throw new WebApplicationException(
				Response.status(
					Response.Status.SERVICE_UNAVAILABLE
				).entity(
					healthVerification
				).build());
		}

		healthVerification.setStatus(() -> HealthVerification.Status.HEALTHY);

		return healthVerification;
	}

	@Reference
	private FIPSHealthCheckAuditor _fipsHealthCheckAuditor;

}