/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Caio Farias
 */
public class FIPSModeHelperUtilTest {

	@Test
	public void testGetElementsMap() {
		Document document1 = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_toDocument",
			new Class<?>[] {String.class},
			StringBundler.concat(
				"<config>", _XML_ASYM_ENCRYPT, _XML_AUTH, "</config>"));

		Map<String, Element> elementsMap = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_getElementsMap",
			new Class<?>[] {Document.class}, document1);

		Assert.assertEquals(
			List.of("ASYM_ENCRYPT", "AUTH"), List.copyOf(elementsMap.keySet()));

		Element asymEncryptElement = elementsMap.get("ASYM_ENCRYPT");

		Assert.assertEquals(
			"2048", asymEncryptElement.getAttribute("asym_keylength"));
		Assert.assertEquals(
			_TRANSFORMATION_SYM,
			asymEncryptElement.getAttribute("sym_algorithm"));

		Element authElement = elementsMap.get("AUTH");

		Assert.assertEquals(
			_AUTH_CLASS, authElement.getAttribute("auth_class"));

		Document document2 = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_toDocument",
			new Class<?>[] {String.class}, "<config><UDP /></config>");

		Assert.assertEquals(
			Collections.emptyMap(),
			ReflectionTestUtil.invoke(
				FIPSModeHelperUtil.class, "_getElementsMap",
				new Class<?>[] {Document.class}, document2));

		Document document3 = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_toDocument",
			new Class<?>[] {String.class},
			StringBundler.concat(
				"<config><!--", _XML_SYM_ENCRYPT, "-->", _XML_ASYM_ENCRYPT,
				"</config>"));

		elementsMap = ReflectionTestUtil.invoke(
			FIPSModeHelperUtil.class, "_getElementsMap",
			new Class<?>[] {Document.class}, document3);

		Assert.assertEquals(
			List.of("ASYM_ENCRYPT"), List.copyOf(elementsMap.keySet()));
	}

	@Test
	public void testGetJGroupsProfileElements() throws Exception {
		Path path = Files.createTempFile(null, ".xml");

		try {
			String channelPropertiesXML = StringBundler.concat(
				"<config>", _XML_AUTH, _XML_SYM_ENCRYPT, "</config>");

			Files.write(path, channelPropertiesXML.getBytes());

			Map<String, Element> elementsMap =
				FIPSModeHelperUtil.getJGroupsProfileElements(
					String.valueOf(path));

			Assert.assertEquals(
				List.of("AUTH", "SYM_ENCRYPT"),
				List.copyOf(elementsMap.keySet()));

			Element authElement = elementsMap.get("AUTH");

			Assert.assertEquals(
				_AUTH_CLASS, authElement.getAttribute("auth_class"));

			Files.write(path, "<config><AUTH".getBytes());

			_assertSecurityException(
				"Unable to parse the JGroups channel properties",
				() -> FIPSModeHelperUtil.getJGroupsProfileElements(
					String.valueOf(path)));
		}
		finally {
			Files.delete(path);
		}

		_assertSecurityException(
			"Unable to read the JGroups channel properties",
			() -> FIPSModeHelperUtil.getJGroupsProfileElements(
				RandomTestUtil.randomString()));
	}

	private void _assertSecurityException(
		String expectedMessage, ThrowingRunnable throwingRunnable) {

		SecurityException securityException = Assert.assertThrows(
			SecurityException.class, throwingRunnable);

		String message = securityException.getMessage();

		Assert.assertTrue(message, message.contains(expectedMessage));
	}

	private static final String _AUTH_CLASS = "org.jgroups.auth.X509Token";

	private static final String _TRANSFORMATION_SYM = "AES/CBC/PKCS5Padding";

	private static final String _XML_ASYM_ENCRYPT = StringBundler.concat(
		"<ASYM_ENCRYPT ",
		"asym_algorithm=\"RSA/ECB/OAEPWithSHA-256AndMGF1Padding\" ",
		"asym_keylength=\"2048\" sym_algorithm=\"", _TRANSFORMATION_SYM,
		"\" sym_iv_length=\"16\" sym_keylength=\"128\" />");

	private static final String _XML_AUTH = StringBundler.concat(
		"<AUTH auth_class=\"", _AUTH_CLASS, "\" />");

	private static final String _XML_SYM_ENCRYPT = StringBundler.concat(
		"<SYM_ENCRYPT sym_algorithm=\"", _TRANSFORMATION_SYM,
		"\" sym_iv_length=\"16\" sym_keylength=\"128\" />");

}