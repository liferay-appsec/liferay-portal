/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.users.admin.internal.workflow.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.audit.AuditRequestThreadLocal;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.HttpSession;

import java.io.Serializable;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Álvaro Saugar
 */
@RunWith(Arquillian.class)
public class UserWorkflowHandlerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() throws Exception {
		AuditRequestThreadLocal.removeAuditThreadLocal();

		_workflowDefinitionLinkLocalService.deleteWorkflowDefinitionLink(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			User.class.getName(), 0, 0);
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testContributeWorkflowContext() throws Exception {
		_addWorkflowDefinitionLink();

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		HttpSession httpSession = mockHttpServletRequest.getSession();

		String auditSessionId = RandomTestUtil.randomString();

		httpSession.setAttribute(WebKeys.AUDIT_SESSION_ID, auditSessionId);

		ServiceContext serviceContext = _contributeWorkflowContext(
			mockHttpServletRequest);

		Assert.assertEquals(
			auditSessionId, serviceContext.getAttribute("auditSessionId"));

		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		String correlationId = auditRequestThreadLocal.getCorrelationId();

		Assert.assertNotNull(correlationId);
		Assert.assertEquals(
			correlationId, serviceContext.getAttribute("auditCorrelationId"));
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testContributeWorkflowContextWhenAuditSessionIdIsAbsent()
		throws Exception {

		_addWorkflowDefinitionLink();

		ServiceContext serviceContext = _contributeWorkflowContext(
			new MockHttpServletRequest());

		Assert.assertNull(serviceContext.getAttribute("auditSessionId"));
		Assert.assertNotNull(serviceContext.getAttribute("serverName"));
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testContributeWorkflowContextWhenCorrelationIdIsSet()
		throws Exception {

		_addWorkflowDefinitionLink();

		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		String correlationId = RandomTestUtil.randomString();

		auditRequestThreadLocal.setCorrelationId(correlationId);

		ServiceContext serviceContext = _contributeWorkflowContext(
			new MockHttpServletRequest());

		Assert.assertEquals(
			correlationId, serviceContext.getAttribute("auditCorrelationId"));

		Assert.assertEquals(
			correlationId, auditRequestThreadLocal.getCorrelationId());
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testContributeWorkflowContextWhenFeatureFlagIsDisabled()
		throws Exception {

		_addWorkflowDefinitionLink();

		ServiceContext serviceContext = _contributeWorkflowContext(
			new MockHttpServletRequest());

		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(auditRequestThreadLocal.getCorrelationId());

		Assert.assertNull(serviceContext.getAttribute("auditCorrelationId"));
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testContributeWorkflowContextWhenWorkflowIsAbsent()
		throws Exception {

		ServiceContext serviceContext = _contributeWorkflowContext(
			new MockHttpServletRequest());

		AuditRequestThreadLocal auditRequestThreadLocal =
			AuditRequestThreadLocal.getAuditThreadLocal();

		Assert.assertNull(auditRequestThreadLocal.getCorrelationId());

		Assert.assertNull(serviceContext.getAttribute("auditCorrelationId"));
	}

	private void _addWorkflowDefinitionLink() throws Exception {
		_workflowDefinitionLinkLocalService.addWorkflowDefinitionLink(
			null, TestPropsValues.getUserId(), TestPropsValues.getCompanyId(),
			TestPropsValues.getGroupId(), User.class.getName(), 0, 0,
			"Single Approver", 1);
	}

	private ServiceContext _contributeWorkflowContext(
			MockHttpServletRequest mockHttpServletRequest)
		throws Exception {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(TestPropsValues.getCompanyId());
		serviceContext.setRequest(mockHttpServletRequest);

		_workflowHandler.contributeWorkflowContext(
			HashMapBuilder.<String, Serializable>put(
				WorkflowConstants.CONTEXT_COMPANY_ID,
				String.valueOf(TestPropsValues.getCompanyId())
			).put(
				WorkflowConstants.CONTEXT_GROUP_ID,
				String.valueOf(TestPropsValues.getGroupId())
			).put(
				WorkflowConstants.CONTEXT_SERVICE_CONTEXT, serviceContext
			).build());

		return serviceContext;
	}

	@Inject
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

	@Inject(filter = "model.class.name=com.liferay.portal.kernel.model.User")
	private WorkflowHandler<User> _workflowHandler;

}