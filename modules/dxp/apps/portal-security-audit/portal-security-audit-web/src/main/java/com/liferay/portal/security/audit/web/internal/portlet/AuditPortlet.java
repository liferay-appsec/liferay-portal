/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.web.internal.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.security.audit.AuditEvent;
import com.liferay.portal.security.audit.storage.model.AuditEventAccountEntryRel;
import com.liferay.portal.security.audit.storage.service.AuditEventLocalService;
import com.liferay.portal.security.audit.web.internal.AuditAccountEntryUtil;
import com.liferay.portal.security.audit.web.internal.AuditEventManagerUtil;
import com.liferay.portal.security.audit.web.internal.constants.AuditPortletKeys;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;
import jakarta.portlet.Portlet;
import jakarta.portlet.PortletException;
import jakarta.portlet.PortletRequest;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;
import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Greenwald
 */
@Component(
	property = {
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.icon=/icons/audit.png",
		"com.liferay.portlet.instanceable=false",
		"jakarta.portlet.display-name=Audit",
		"jakarta.portlet.expiration-cache=0",
		"jakarta.portlet.init-param.view-template=/view.jsp",
		"jakarta.portlet.name=" + AuditPortletKeys.AUDIT,
		"jakarta.portlet.portlet-info.short-title=Audit",
		"jakarta.portlet.portlet-info.title=Audit",
		"jakarta.portlet.portlet-mode=text/html;view",
		"jakarta.portlet.resource-bundle=content.Language",
		"jakarta.portlet.security-role-ref=administrator",
		"jakarta.portlet.version=4.0"
	},
	service = Portlet.class
)
public class AuditPortlet extends MVCPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		_checkPermission(actionRequest);

		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		_checkPermission(renderRequest);

		super.render(renderRequest, renderResponse);
	}

	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {

		_checkPermission(resourceRequest);

		super.serveResource(resourceRequest, resourceResponse);
	}

	private void _checkPermission(PortletRequest portletRequest)
		throws PortletException {

		try {
			PermissionChecker permissionChecker =
				PermissionThreadLocal.getPermissionChecker();

			long[] accountEntryIds = null;

			if (!permissionChecker.isCompanyAdmin()) {
				accountEntryIds =
					AuditAccountEntryUtil.getAdministeredAccountEntryIds(
						permissionChecker);

				if (ArrayUtil.isEmpty(accountEntryIds)) {
					throw new PrincipalException.MustBeCompanyAdmin(
						permissionChecker.getUserId());
				}
			}

			long auditEventId = ParamUtil.getLong(
				portletRequest, "auditEventId");

			if (auditEventId <= 0) {
				return;
			}

			AuditEvent auditEvent = AuditEventManagerUtil.fetchAuditEvent(
				auditEventId);

			if ((auditEvent != null) &&
				((permissionChecker.getCompanyId() !=
					auditEvent.getCompanyId()) ||
				 ((accountEntryIds != null) &&
				  !_hasAccountEntryId(accountEntryIds, auditEvent)))) {

				throw new PrincipalException.MustBeCompanyAdmin(
					permissionChecker.getUserId());
			}
		}
		catch (PortalException portalException) {
			throw new PortletException(portalException);
		}
	}

	private boolean _hasAccountEntryId(
		long[] accountEntryIds, AuditEvent auditEvent) {

		if (ArrayUtil.contains(
				accountEntryIds, auditEvent.getAccountEntryId())) {

			return true;
		}

		for (AuditEventAccountEntryRel auditEventAccountEntryRel :
				_auditEventLocalService.getAuditEventAccountEntryRels(
					auditEvent.getAuditEventId())) {

			if (ArrayUtil.contains(
					accountEntryIds,
					auditEventAccountEntryRel.getAccountEntryId())) {

				return true;
			}
		}

		return false;
	}

	@Reference
	private AuditEventLocalService _auditEventLocalService;

}