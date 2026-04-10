/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
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
import org.junit.Before;
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

	@Before
	public void setUp() {
		_renderCommand = new UpdateCertificateMVCRenderCommand();

		ReflectionTestUtil.setFieldValue(
			_renderCommand, "_certificateTool",
			Mockito.mock(CertificateTool.class));

		_renderRequest = Mockito.mock(RenderRequest.class);
		_renderResponse = Mockito.mock(RenderResponse.class);
	}

	@After
	public void tearDown() throws Exception {
		if (_autoCloseable != null) {
			_autoCloseable.close();

			_autoCloseable = null;
		}
	}

	@Test
	public void testRenderFIPSModeAlgorithmsAndKeySizes() throws Exception {
		_enableFIPSMode();

		_renderCommand.render(_renderRequest, _renderResponse);

		ArgumentCaptor<String[]> algorithmsCaptor = ArgumentCaptor.forClass(
			String[].class);

		Mockito.verify(
			_renderRequest
		).setAttribute(
			Mockito.eq(SamlWebKeys.SAML_CERTIFICATE_KEY_ALGORITHMS),
			algorithmsCaptor.capture()
		);

		String[] algorithms = algorithmsCaptor.getValue();

		Assert.assertArrayEquals(new String[] {"RSA"}, algorithms);

		ArgumentCaptor<String[]> sizesCaptor = ArgumentCaptor.forClass(
			String[].class);

		Mockito.verify(
			_renderRequest
		).setAttribute(
			Mockito.eq(SamlWebKeys.SAML_CERTIFICATE_KEY_SIZES),
			sizesCaptor.capture()
		);

		String[] sizes = sizesCaptor.getValue();

		Assert.assertArrayEquals(
			new String[] {"4096", "3072", "2048"}, sizes);
	}

	@Test
	public void testRenderNonFIPSModeAlgorithmsAndKeySizes() throws Exception {
		_renderCommand.render(_renderRequest, _renderResponse);

		ArgumentCaptor<String[]> algorithmsCaptor = ArgumentCaptor.forClass(
			String[].class);

		Mockito.verify(
			_renderRequest
		).setAttribute(
			Mockito.eq(SamlWebKeys.SAML_CERTIFICATE_KEY_ALGORITHMS),
			algorithmsCaptor.capture()
		);

		String[] algorithms = algorithmsCaptor.getValue();

		Assert.assertArrayEquals(new String[] {"RSA", "DSA"}, algorithms);

		ArgumentCaptor<String[]> sizesCaptor = ArgumentCaptor.forClass(
			String[].class);

		Mockito.verify(
			_renderRequest
		).setAttribute(
			Mockito.eq(SamlWebKeys.SAML_CERTIFICATE_KEY_SIZES),
			sizesCaptor.capture()
		);

		String[] sizes = sizesCaptor.getValue();

		Assert.assertArrayEquals(
			new String[] {"4096", "2048", "1024", "512"}, sizes);
	}

	@Test
	public void testRenderReturnPath() throws Exception {
		String path = _renderCommand.render(_renderRequest, _renderResponse);

		Assert.assertEquals("/admin/update_certificate.jsp", path);
	}

	private void _enableFIPSMode() {
		_autoCloseable =
			ReflectionTestUtil.setFieldValueWithAutoCloseable(
				PropsValues.class, "PORTAL_SECURITY_FIPS_MODE_ENABLED", true);
	}

	private AutoCloseable _autoCloseable;
	private UpdateCertificateMVCRenderCommand _renderCommand;
	private RenderRequest _renderRequest;
	private RenderResponse _renderResponse;

}
