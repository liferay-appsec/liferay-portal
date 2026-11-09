/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.avalara.connector.web.internal.portlet.action;

import com.liferay.commerce.avalara.connector.configuration.CommerceAvalaraConnectorConfiguration;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
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
public class EditCommerceAvalaraConnectorMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDoProcessAction() throws Exception {
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

		String licenseKey = RandomTestUtil.randomString();

		Mockito.when(
			actionRequest.getParameter("licenseKey")
		).thenReturn(
			licenseKey
		);

		Settings settings = Mockito.mock(Settings.class);

		ModifiableSettings modifiableSettings = Mockito.mock(
			ModifiableSettings.class);

		Mockito.when(
			settings.getModifiableSettings()
		).thenReturn(
			modifiableSettings
		);

		EditCommerceAvalaraConnectorMVCActionCommand
			editCommerceAvalaraConnectorMVCActionCommand =
				new EditCommerceAvalaraConnectorMVCActionCommand();

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));

		Mockito.when(
			secretResolver.store(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(secretContext.getKey(), "licenseKey") &&
						(secretContext.getSettingsLocator() instanceof
							CompanyServiceSettingsLocator
								companyServiceSettingsLocator) &&
						(companyServiceSettingsLocator.getCompanyId() ==
							companyId) &&
						Objects.equals(
							companyServiceSettingsLocator.getSettingsId(),
							CommerceAvalaraConnectorConfiguration.class.
								getName())),
				Mockito.eq(licenseKey))
		).thenReturn(
			keyReferenceString
		);

		ReflectionTestUtil.setFieldValue(
			editCommerceAvalaraConnectorMVCActionCommand, "_secretResolver",
			secretResolver);

		try (MockedStatic<FallbackKeysSettingsUtil>
				fallbackKeysSettingsUtilMockedStatic = Mockito.mockStatic(
					FallbackKeysSettingsUtil.class)) {

			fallbackKeysSettingsUtilMockedStatic.when(
				() -> FallbackKeysSettingsUtil.getSettings(
					Mockito.any(SettingsLocator.class))
			).thenReturn(
				settings
			);

			editCommerceAvalaraConnectorMVCActionCommand.doProcessAction(
				actionRequest, Mockito.mock(ActionResponse.class));
		}

		Mockito.verify(
			modifiableSettings
		).setValue(
			"licenseKey", keyReferenceString
		);
	}

}