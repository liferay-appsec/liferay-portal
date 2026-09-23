/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.users.admin.internal.workflow;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.audit.AuditRequestThreadLocal;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.workflow.BaseWorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	property = "model.class.name=com.liferay.portal.kernel.model.User",
	service = WorkflowHandler.class
)
public class UserWorkflowHandler extends BaseWorkflowHandler<User> {

	@Override
	public void contributeWorkflowContext(
		Map<String, Serializable> workflowContext) {

		ServiceContext serviceContext = (ServiceContext)workflowContext.get(
			WorkflowConstants.CONTEXT_SERVICE_CONTEXT);

		HttpServletRequest httpServletRequest = serviceContext.getRequest();

		if (httpServletRequest == null) {
			return;
		}

		if (FeatureFlagManagerUtil.isEnabled(
				serviceContext.getCompanyId(), "LPD-6417") &&
			_workflowDefinitionLinkLocalService.hasWorkflowDefinitionLink(
				GetterUtil.getLong(
					(String)workflowContext.get(
						WorkflowConstants.CONTEXT_COMPANY_ID)),
				GetterUtil.getLong(
					(String)workflowContext.get(
						WorkflowConstants.CONTEXT_GROUP_ID)),
				getClassName(), 0, 0)) {

			AuditRequestThreadLocal auditRequestThreadLocal =
				AuditRequestThreadLocal.getAuditThreadLocal();

			String correlationId = auditRequestThreadLocal.getCorrelationId();

			if (correlationId == null) {
				correlationId = PortalUUIDUtil.generate();

				auditRequestThreadLocal.setCorrelationId(correlationId);
			}

			serviceContext.setAttribute("auditCorrelationId", correlationId);
		}

		HttpSession httpSession = httpServletRequest.getSession();

		String auditSessionId = (String)httpSession.getAttribute(
			WebKeys.AUDIT_SESSION_ID);

		if (auditSessionId != null) {
			serviceContext.setAttribute("auditSessionId", auditSessionId);
		}

		serviceContext.setAttribute(
			"serverName", httpServletRequest.getServerName());
		serviceContext.setAttribute(
			"serverPort", httpServletRequest.getServerPort());

		serviceContext.setRequest(httpServletRequest);
	}

	@Override
	public String getClassName() {
		return User.class.getName();
	}

	@Override
	public String getType(Locale locale) {
		return ResourceActionsUtil.getModelResource(locale, getClassName());
	}

	@Override
	public boolean isScopeable() {
		return false;
	}

	@Override
	public User updateStatus(
			int status, Map<String, Serializable> workflowContext)
		throws PortalException {

		long userId = GetterUtil.getLong(
			(String)workflowContext.get(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK));

		User user = _userLocalService.getUser(userId);

		ServiceContext serviceContext = (ServiceContext)workflowContext.get(
			WorkflowConstants.CONTEXT_SERVICE_CONTEXT);

		if (((user.getStatus() == WorkflowConstants.STATUS_DRAFT) ||
			 (user.getStatus() == WorkflowConstants.STATUS_PENDING)) &&
			(status == WorkflowConstants.STATUS_APPROVED)) {

			_userLocalService.completeUserRegistration(user, serviceContext);

			user = _userLocalService.getUser(userId);

			AuditRequestThreadLocal auditRequestThreadLocal =
				AuditRequestThreadLocal.getAuditThreadLocal();

			String requestId = auditRequestThreadLocal.getRequestId();
			boolean requestIdGenerated =
				auditRequestThreadLocal.isRequestIdGenerated();

			try (SafeCloseable safeCloseable =
					AuditRequestThreadLocal.
						setNewAuditThreadLocalWithSafeCloseable()) {

				_updateAuditRequestThreadLocal(
					requestId, requestIdGenerated, workflowContext);

				return _userLocalService.updateStatus(
					user, status, serviceContext);
			}
		}

		return _userLocalService.updateStatus(user, status, serviceContext);
	}

	private void _updateAuditRequestThreadLocal(
		String requestId, boolean requestIdGenerated,
		Map<String, Serializable> workflowContext) {

		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		auditRequestThreadLocal.setRequestId(requestId);
		auditRequestThreadLocal.setRequestIdGenerated(requestIdGenerated);

		ServiceContext serviceContext = (ServiceContext)workflowContext.get(
			WorkflowConstants.CONTEXT_SERVICE_CONTEXT);

		auditRequestThreadLocal.setClientHost(serviceContext.getRemoteHost());
		auditRequestThreadLocal.setClientIP(serviceContext.getRemoteAddr());

		long userId = GetterUtil.getLong(
			(String)workflowContext.get(WorkflowConstants.CONTEXT_USER_ID));

		if (userId != 0) {
			auditRequestThreadLocal.setRealUserId(userId);
		}

		Serializable auditCorrelationId = serviceContext.getAttribute(
			"auditCorrelationId");

		if (auditCorrelationId != null) {
			auditRequestThreadLocal.setCorrelationId(
				(String)auditCorrelationId);
		}

		Serializable auditSessionId = serviceContext.getAttribute(
			"auditSessionId");

		if (auditSessionId != null) {
			auditRequestThreadLocal.setSessionID((String)auditSessionId);
		}

		Serializable serverName = serviceContext.getAttribute("serverName");

		if (serverName == null) {
			return;
		}

		auditRequestThreadLocal.setServerName((String)serverName);
		auditRequestThreadLocal.setServerPort(
			(int)serviceContext.getAttribute("serverPort"));
	}

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

}