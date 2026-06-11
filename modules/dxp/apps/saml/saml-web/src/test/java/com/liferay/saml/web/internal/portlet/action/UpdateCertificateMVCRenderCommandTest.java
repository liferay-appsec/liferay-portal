/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.web.internal.portlet.action;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.saml.constants.SamlWebKeys;
import com.liferay.saml.runtime.certificate.CertificateTool;

import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Rafael Praxedes
 */
public class UpdateCertificateMVCRenderCommandTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@After
	public void tearDown() throws Exception {
		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}
	}

	@Test
	public void testRender() throws Exception {
		UpdateCertificateMVCRenderCommand renderCommand =
			new UpdateCertificateMVCRenderCommand();

		ReflectionTestUtil.setFieldValue(
			renderCommand, "_certificateTool",
			Mockito.mock(CertificateTool.class));

		RenderRequest renderRequest = Mockito.mock(RenderRequest.class);
		RenderResponse renderResponse = Mockito.mock(RenderResponse.class);

		renderCommand.render(renderRequest, renderResponse);

		_assertRequestAttribute(
			new String[] {"RSA", "DSA"},
			SamlWebKeys.SAML_CERTIFICATE_KEY_ALGORITHMS, renderRequest);
		_assertRequestAttribute(
			new String[] {"4096", "2048", "1024", "512"},
			SamlWebKeys.SAML_CERTIFICATE_KEY_SIZES, renderRequest);

		_autoCloseable = ReflectionTestUtil.setFieldValueWithAutoCloseable(
			PropsValues.class, "FIPS_ENABLED", true);

		renderRequest = Mockito.mock(RenderRequest.class);

		renderCommand.render(renderRequest, renderResponse);

		_assertRequestAttribute(
			new String[] {"RSA"}, SamlWebKeys.SAML_CERTIFICATE_KEY_ALGORITHMS,
			renderRequest);
		_assertRequestAttribute(
			new String[] {"4096", "3072", "2048"},
			SamlWebKeys.SAML_CERTIFICATE_KEY_SIZES, renderRequest);
	}

	private void _assertRequestAttribute(
		String[] expectedValue, String key, RenderRequest renderRequest) {

		ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(
			String[].class);

		Mockito.verify(
			renderRequest
		).setAttribute(
			Mockito.eq(key), argumentCaptor.capture()
		);

		Assert.assertArrayEquals(expectedValue, argumentCaptor.getValue());
	}

	private AutoCloseable _autoCloseable;

}