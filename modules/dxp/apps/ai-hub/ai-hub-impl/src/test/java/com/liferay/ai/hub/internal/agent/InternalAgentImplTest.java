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
		long companyId = RandomTestUtil.randomLong();
		String plainTextAccessToken = RandomTestUtil.randomString();
		String plainTextUserToken = RandomTestUtil.randomString();

		AgentContext agentContext = _mockAgentContext(
			companyId, plainTextAccessToken, plainTextUserToken);

		String workflowDefinitionName = RandomTestUtil.randomString();

		WorkflowDefinitionManager workflowDefinitionManager =
			_mockWorkflowDefinitionManager(companyId, workflowDefinitionName);

		WorkflowInstance workflowInstance = Mockito.mock(
			WorkflowInstance.class);

		WorkflowInstanceManager workflowInstanceManager =
			_mockWorkflowInstanceManager(workflowInstance);

		Company company = Mockito.mock(Company.class);

		CompanyLocalService companyLocalService = _mockCompanyLocalService(
			company, companyId);

		String encryptedAccessToken = RandomTestUtil.randomString();
		String encryptedUserToken = RandomTestUtil.randomString();

		Encryptor encryptor = _mockEncryptor(
			company, encryptedAccessToken, encryptedUserToken,
			plainTextAccessToken, plainTextUserToken);

		InternalAgentImpl internalAgentImpl = new InternalAgentImpl(
			agentContext, companyLocalService, encryptor,
			workflowDefinitionManager, workflowInstanceManager);

		internalAgentImpl.setAgentArguments(Collections.emptyList());
		internalAgentImpl.setWorkflowDefinitionName(workflowDefinitionName);

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

		ArgumentCaptor<Map<String, Serializable>> argumentCaptor =
			ArgumentCaptor.forClass(Map.class);

		Mockito.verify(
			workflowInstanceManager
		).startWorkflowInstance(
			Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(),
			Mockito.any(), Mockito.any(), Mockito.any(),
			argumentCaptor.capture()
		);

		Map<String, Serializable> workflowContext = argumentCaptor.getValue();

		Assert.assertEquals(
			encryptedAccessToken, workflowContext.get("accessToken"));
		Assert.assertEquals(
			encryptedUserToken, workflowContext.get("userToken"));
	}

	private AgentContext _mockAgentContext(
		long companyId, String plainTextAccessToken,
		String plainTextUserToken) {

		AgentContext agentContext = Mockito.mock(AgentContext.class);

		Mockito.when(
			agentContext.getAccessToken()
		).thenReturn(
			plainTextAccessToken
		);

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

		Mockito.when(
			agentContext.getUserToken()
		).thenReturn(
			plainTextUserToken
		);

		return agentContext;
	}

	private CompanyLocalService _mockCompanyLocalService(
			Company company, long companyId)
		throws Exception {

		CompanyLocalService companyLocalService = Mockito.mock(
			CompanyLocalService.class);

		Mockito.when(
			companyLocalService.getCompany(companyId)
		).thenReturn(
			company
		);

		return companyLocalService;
	}

	private Encryptor _mockEncryptor(
			Company company, String encryptedAccessToken,
			String encryptedUserToken, String plainTextAccessToken,
			String plainTextUserToken)
		throws Exception {

		Encryptor encryptor = Mockito.mock(Encryptor.class);

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

		return encryptor;
	}

	private WorkflowDefinitionManager _mockWorkflowDefinitionManager(
			long companyId, String workflowDefinitionName)
		throws Exception {

		WorkflowDefinitionManager workflowDefinitionManager = Mockito.mock(
			WorkflowDefinitionManager.class);

		WorkflowDefinition workflowDefinition = Mockito.mock(
			WorkflowDefinition.class);

		Mockito.when(
			workflowDefinition.getVersion()
		).thenReturn(
			1
		);

		Mockito.when(
			workflowDefinitionManager.liberalGetLatestWorkflowDefinition(
				companyId, workflowDefinitionName)
		).thenReturn(
			workflowDefinition
		);

		return workflowDefinitionManager;
	}

	private WorkflowInstanceManager _mockWorkflowInstanceManager(
			WorkflowInstance workflowInstance)
		throws Exception {

		WorkflowInstanceManager workflowInstanceManager = Mockito.mock(
			WorkflowInstanceManager.class);

		Mockito.when(
			workflowInstanceManager.startWorkflowInstance(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
		).thenReturn(
			workflowInstance
		);

		return workflowInstanceManager;
	}

}