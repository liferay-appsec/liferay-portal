/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.AgentContext;
import com.liferay.ai.hub.internal.agent.util.AgentUtil;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;

import java.io.Serializable;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class InternalAgentImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testInvokeEncryptsTokensInWorkflowContext() throws Throwable {
		AgentContext agentContext = Mockito.mock(AgentContext.class);
		String plainTextAccessToken = RandomTestUtil.randomString();

		Mockito.when(
			agentContext.getAccessToken()
		).thenReturn(
			plainTextAccessToken
		);

		long companyId = RandomTestUtil.randomLong();

		Mockito.when(
			agentContext.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			agentContext.getServiceContext()
		).thenReturn(
			new ServiceContext()
		);

		Mockito.when(
			agentContext.getSseEventSinkKey()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		String plainTextUserToken = RandomTestUtil.randomString();

		Mockito.when(
			agentContext.getUserToken()
		).thenReturn(
			plainTextUserToken
		);

		WorkflowDefinitionManager workflowDefinitionManager = Mockito.mock(
			WorkflowDefinitionManager.class);

		WorkflowDefinition workflowDefinition = Mockito.mock(
			WorkflowDefinition.class);

		Mockito.when(
			workflowDefinition.getVersion()
		).thenReturn(
			1
		);

		String workflowDefinitionName = RandomTestUtil.randomString();

		Mockito.when(
			workflowDefinitionManager.liberalGetLatestWorkflowDefinition(
				companyId, workflowDefinitionName)
		).thenReturn(
			workflowDefinition
		);

		WorkflowInstance workflowInstance = Mockito.mock(
			WorkflowInstance.class);
		WorkflowInstanceManager workflowInstanceManager = Mockito.mock(
			WorkflowInstanceManager.class);

		Mockito.when(
			workflowInstanceManager.startWorkflowInstance(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
		).thenReturn(
			workflowInstance
		);

		CompanyLocalService companyLocalService = Mockito.mock(
			CompanyLocalService.class);
		Encryptor encryptor = Mockito.mock(Encryptor.class);

		InternalAgentImpl internalAgentImpl = new InternalAgentImpl(
			agentContext, companyLocalService, encryptor,
			workflowDefinitionManager, workflowInstanceManager);

		internalAgentImpl.setAgentArguments(Collections.emptyList());
		internalAgentImpl.setWorkflowDefinitionName(workflowDefinitionName);

		String encryptedAccessToken = RandomTestUtil.randomString();
		String encryptedUserToken = RandomTestUtil.randomString();

		Company company = Mockito.mock(Company.class);

		Mockito.when(
			companyLocalService.getCompany(companyId)
		).thenReturn(
			company
		);

		Mockito.when(
			encryptor.encrypt(company.getKeyObj(), plainTextAccessToken)
		).thenReturn(
			encryptedAccessToken
		);

		Mockito.when(
			encryptor.encrypt(company.getKeyObj(), plainTextUserToken)
		).thenReturn(
			encryptedUserToken
		);

		try (MockedStatic<AgentUtil> agentUtilMockedStatic = Mockito.mockStatic(
				AgentUtil.class)) {

			agentUtilMockedStatic.when(
				() -> AgentUtil.getOutput(workflowInstance)
			).thenReturn(
				"output"
			);

			internalAgentImpl.invoke(
				null, Object.class.getMethod("toString"), null);
		}

		ArgumentCaptor<Map<String, Serializable>> contextCaptor =
			ArgumentCaptor.forClass(Map.class);

		Mockito.verify(
			workflowInstanceManager
		).startWorkflowInstance(
			Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(),
			Mockito.any(), Mockito.any(), Mockito.any(), contextCaptor.capture()
		);

		Map<String, Serializable> context = contextCaptor.getValue();

		Assert.assertEquals(encryptedAccessToken, context.get("accessToken"));
		Assert.assertEquals(encryptedUserToken, context.get("userToken"));
	}

}