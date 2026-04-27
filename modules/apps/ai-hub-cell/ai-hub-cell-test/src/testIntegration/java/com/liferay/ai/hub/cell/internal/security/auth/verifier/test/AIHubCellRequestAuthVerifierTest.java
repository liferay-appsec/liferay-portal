/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.internal.security.auth.verifier.test;

import com.liferay.ai.hub.cell.security.JWTTokenUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.security.service.access.policy.ServiceAccessPolicy;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Manuele Castro
 */
@FeatureFlag("LPD-62272")
@RunWith(Arquillian.class)
public class AIHubCellRequestAuthVerifierTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testVerify() throws Exception {
		AuthVerifierResult authVerifierResult = _verify(
			RandomTestUtil.randomString());

		Assert.assertEquals(
			AuthVerifierResult.State.INVALID_CREDENTIALS,
			authVerifierResult.getState());

		authVerifierResult = _verify(null);

		Assert.assertEquals(
			AuthVerifierResult.State.NOT_APPLICABLE,
			authVerifierResult.getState());

		Company company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());

		String token = JWTTokenUtil.generateToken(
			TimeUnit.MINUTES.toMillis(1), company.getVirtualHostname(),
			TestPropsValues.getUserId());

		authVerifierResult = _verify(token);

		Assert.assertEquals(
			AuthVerifierResult.State.SUCCESS, authVerifierResult.getState());
		Assert.assertEquals(
			TestPropsValues.getUserId(), authVerifierResult.getUserId());

		Map<String, Object> settings = authVerifierResult.getSettings();

		@SuppressWarnings("unchecked")
		List<String> serviceAccessPolicyNames = (List<String>)settings.get(
			ServiceAccessPolicy.SERVICE_ACCESS_POLICY_NAMES);

		Assert.assertNotNull(serviceAccessPolicyNames);
		Assert.assertTrue(
			serviceAccessPolicyNames.contains("AI_HUB_CELL_TOKEN"));
	}

	private AuthVerifierResult _verify(String token) throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		if (Validator.isNotNull(token)) {
			mockHttpServletRequest.addHeader(
				"Liferay-AI-Hub-Cell-On-Behalf-Of", token);
		}

		AccessControlContext accessControlContext = new AccessControlContext();

		accessControlContext.setRequest(mockHttpServletRequest);

		return _aiHubCellRequestAuthVerifier.verify(
			accessControlContext, new Properties());
	}

	@Inject(
		filter = "component.name=com.liferay.ai.hub.cell.internal.security.auth.verifier.AIHubCellRequestAuthVerifier"
	)
	private AuthVerifier _aiHubCellRequestAuthVerifier;

	@Inject
	private CompanyLocalService _companyLocalService;

}