/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.reports.engine.console.web.internal.admin.portlet.action;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.reports.engine.console.model.Source;
import com.liferay.portal.reports.engine.console.service.SourceService;
import com.liferay.portal.reports.engine.console.util.ReportsEngineConsoleUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import java.util.Objects;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 */
public class TestDataSourceMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDoProcessAction() throws Exception {
		TestDataSourceMVCActionCommand testDataSourceMVCActionCommand =
			new TestDataSourceMVCActionCommand();

		ActionRequest actionRequest = Mockito.mock(ActionRequest.class);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		long companyId = RandomTestUtil.randomLong();

		Mockito.when(
			themeDisplay.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			actionRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		long sourceId = RandomTestUtil.randomLong();

		Mockito.when(
			actionRequest.getParameter("sourceId")
		).thenReturn(
			String.valueOf(sourceId)
		);

		SourceService sourceService = Mockito.mock(SourceService.class);

		Source source = Mockito.mock(Source.class);

		Mockito.when(
			source.getCompanyId()
		).thenReturn(
			companyId
		);

		String driverPassword = RandomTestUtil.randomString();

		Mockito.when(
			source.getDriverPassword()
		).thenReturn(
			driverPassword
		);

		Mockito.when(
			source.getPrimaryKey()
		).thenReturn(
			sourceId
		);

		Mockito.when(
			sourceService.getSource(sourceId)
		).thenReturn(
			source
		);

		ReflectionTestUtil.setFieldValue(
			testDataSourceMVCActionCommand, "_sourceService", sourceService);

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		String resolvedDriverPassword = RandomTestUtil.randomString();

		Mockito.when(
			secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(
							secretContext.getClassName(),
							Source.class.getName()) &&
						(secretContext.getCompanyId() == companyId) &&
						Objects.equals(
							secretContext.getKey(), "driverPassword") &&
						(secretContext.getPrimaryKey() == sourceId)),
				Mockito.eq(driverPassword))
		).thenReturn(
			resolvedDriverPassword
		);

		ReflectionTestUtil.setFieldValue(
			testDataSourceMVCActionCommand, "_secretResolver", secretResolver);

		try (MockedStatic<ReportsEngineConsoleUtil>
				reportsEngineConsoleUtilMockedStatic = Mockito.mockStatic(
					ReportsEngineConsoleUtil.class)) {

			testDataSourceMVCActionCommand.doProcessAction(
				actionRequest, Mockito.mock(ActionResponse.class));

			reportsEngineConsoleUtilMockedStatic.verify(
				() -> ReportsEngineConsoleUtil.validateJDBCConnection(
					null, null, null, resolvedDriverPassword));
		}
	}

}